/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package geb.testcontainers

import com.github.dockerjava.api.model.ContainerNetwork
import geb.Browser
import geb.Configuration
import geb.ConfigurationLoader
import geb.download.DownloadSupport
import geb.spock.SpockGebTestManagerBuilder
import geb.test.GebTestManager
import geb.testcontainers.support.LocalhostDownloadSupport
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.openqa.selenium.SessionNotCreatedException
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.ContainerFetchException
import org.testcontainers.containers.PortForwardingContainer
import org.testcontainers.containers.SeleniumUtils
import org.testcontainers.containers.VncRecordingContainer
import org.testcontainers.images.PullPolicy
import org.testcontainers.utility.DockerImageName

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.function.Supplier

/**
 * Responsible for initializing a {@link BrowserWebDriverContainer}
 * per the Spec's {@link ContainerGebConfiguration}. This class will try to
 * reuse the same container if the configuration matches the current container.
 *
 * @author James Daugherty
 * @since 4.1
 */
@Slf4j
@CompileStatic
class WebDriverContainerHolder {

    private static final String DEFAULT_HOSTNAME_FROM_HOST = 'localhost'
    private static final String REMOTE_ADDRESS_PROPERTY = 'webdriver.remote.server'
    private static final String HOST_PORT_PROPERTY = 'hostPort'
    private static final String BASE_URL_PROPERTY = 'baseUrl'
    private static final String DEFAULT_BROWSER = 'firefox'
    private static final String CI_PROPERTY = 'CI'
    private static final String LOCALHOST_IP = '127.0.0.1'

    GebContainerSettings settings
    GebTestManager testManager
    Browser browser
    BrowserWebDriverContainer container
    DownloadSupport downloadSupport
    SpecContainerConfiguration containerConf

    WebDriverContainerHolder(GebContainerSettings settings) {
        this.settings = settings
    }

    boolean isInitialized() {
        container != null
    }

    void stop() {
        System.clearProperty(REMOTE_ADDRESS_PROPERTY)
        container?.stop()
        container = null
        browser = null
        testManager = null
        downloadSupport = null
        containerConf = null
    }

    boolean matchesCurrentContainerConfiguration(SpecContainerConfiguration specConf) {
        specConf == containerConf &&
            settings.recordingMode == BrowserWebDriverContainer.VncRecordingMode.SKIP
    }

    @PackageScope
    boolean reinitialize(IMethodInvocation methodInvocation) {
        def specConf = new SpecContainerConfiguration(methodInvocation.spec)

        if (matchesCurrentContainerConfiguration(specConf)) {
            return false
        }

        if (initialized) {
            stop()
        }

        def gebConf = new ConfigurationLoader().conf
        def gebConfigExists = gebConf.rawConfig.size() != 0
        def dockerImageName = createDockerImageName(DEFAULT_BROWSER)
        def customBrowser = gebConf.rawConfig.containerBrowser as String

        if (gebConfigExists) {
            log.info('A Geb configuration exists...')
            validateDriverConf(gebConf)

            if (customBrowser) {
                log.info(
                    'A \'containerBrowser\' property was found in GebConfig. ' +
                        "Using [$customBrowser] container image."
                )
                dockerImageName = createDockerImageName(customBrowser)
            } else {
                log.info(
                    'No \'containerBrowser\' property found in GebConfig. ' +
                        "Using default [$DEFAULT_BROWSER] container image."
                )
            }
        }

        containerConf = specConf
        container = new BrowserWebDriverContainer(dockerImageName).withRecordingMode(
            settings.recordingMode,
            settings.recordingDirectory,
            settings.recordingFormat
        )

        container.with {
            withEnv('SE_ENABLE_TRACING', settings.tracingEnabled.toString())
            if (!System.getenv(CI_PROPERTY)) {
                withAccessToHost(true)
            } else {
                withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES))
            }
            withImagePullPolicy(PullPolicy.ageBased(Duration.of(1, ChronoUnit.DAYS)))
        }

        startContainer(container, dockerImageName, customBrowser)

        if (hostnameChanged) {
            container.execInContainer(
                '/bin/sh', '-c',
                "echo '$hostIp\t$containerConf.hostName' | sudo tee -a /etc/hosts"
            )
        }

        // Ensure that the driver points to the re-initialized container with the correct host.
        gebConf.cacheDriver = false
        gebConf.quitDriverOnBrowserReset = false
        gebConf.baseUrl = container.seleniumAddress

        if (containerConf.reporting) {
            gebConf.reportsDir = settings.reportingDirectory
            gebConf.reporter = (methodInvocation.sharedInstance as ContainerGebSpec).createReporter()
        }

        // Set the selenium address as a system property so that RemoteWebDriver
        // constructors (without explicit URL) can find it. Tests run serially
        // (enforced by ExclusiveResource) so this is safe.
        System.setProperty(REMOTE_ADDRESS_PROPERTY, container.seleniumAddress.toString())

        gebConf.driverConf = gebConf.driverConf ?: { ->
            log.info('Using default Firefox RemoteWebDriver for {}', container.seleniumAddress)
            new RemoteWebDriver(container.seleniumAddress, new FirefoxOptions())
        }

        browser = createBrowser(gebConf)
        applyFileDetector(browser, containerConf)

        // There's a bit of a chicken and egg problem here: the container and browser are initialized
        // when the shared fields are initialized, which is before the server under test has started,
        // so the real url cannot be set yet. We set the url to localhost, which the selenium server
        // should respond to (albeit with an error that will be ignored).
        browser.baseUrl = 'http://localhost'

        downloadSupport = new LocalhostDownloadSupport(browser, hostNameFromHost)
        testManager = createTestManager()
        return true
    }

    void setupBrowserUrl(IMethodInvocation methodInvocation) {
        if (!browser) {
            return
        }

        def baseUrl = findBaseUrl(methodInvocation)
        if (baseUrl) {
            browser.baseUrl = baseUrl
        } else {
            int hostPort = findServerPort(methodInvocation)
            Testcontainers.exposeHostPorts(hostPort)
            browser.baseUrl = "$containerConf.protocol://$containerConf.hostName:$hostPort"
        }
    }

    /**
     * Returns the hostname that the server under test is available on from the host.
     * <p>This is useful when using any of the {@code download*()} methods as they will
     * connect from the host, and not from within the container.
     *
     * <p>Defaults to {@code localhost}. If the value returned by {@code webDriverContainer.getHost()}
     * is different from the default, this method will return the same value as
     * {@code webDriverContainer.getHost()}.
     */
    String getHostNameFromHost() {
        hostNameChanged ? container.host : DEFAULT_HOSTNAME_FROM_HOST
    }

    /**
     * Workaround for https://github.com/testcontainers/testcontainers-java/issues/3998
     * <p>
     * Restarts the VNC recording container to enable separate recording files for each
     * test method. Uses reflection to access the VNC recording container field in
     * BrowserWebDriverContainer. Should be called BEFORE each test starts.
     */
    void restartVncRecordingContainer() {
        if (!settings.recordingEnabled || !settings.restartRecordingContainerPerTest || !container) {
            return
        }

        try {
            def field = BrowserWebDriverContainer.getDeclaredField('vncRecordingContainer').tap {
                accessible = true
            }
            def vncContainer = field.get(container) as VncRecordingContainer
            if (vncContainer) {
                vncContainer.stop()
                def newVncContainer = new VncRecordingContainer(container)
                    .withVncPassword('secret')
                    .withVncPort(5900)
                    .withVideoFormat(settings.recordingFormat)
                field.set(container, newVncContainer)
                newVncContainer.start()
                log.debug('Successfully restarted VNC recording container')
            }
        } catch (Exception e) {
            log.warn("Failed to restart VNC recording container: $e.message", e)
        }
    }

    private static int findServerPort(IMethodInvocation methodInvocation) {
        // Use getMetaProperty to check for a real declared property, not propertyMissing
        // (which @DynamicallyDispatchesToBrowser would intercept)
        try {
            if (methodInvocation.instance.metaClass.getMetaProperty(HOST_PORT_PROPERTY)) {
                return (int) methodInvocation.instance[HOST_PORT_PROPERTY]
            }
        } catch (ignored) {
            log.info("no ${HOST_PORT_PROPERTY} from methodInvocation")
        }

        try {
            Configuration gebConfig = new ConfigurationLoader().conf
            return (int) gebConfig.rawConfig.getProperty(HOST_PORT_PROPERTY)
        } catch (ignored) {
            log.info("no ${HOST_PORT_PROPERTY} in config")
        }

        log.info("using default ${HOST_PORT_PROPERTY} 8080")
        return 8080
    }

    private static String findBaseUrl(IMethodInvocation methodInvocation) {
        // Use getMetaProperty to check for a real declared property, not propertyMissing
        // (which @DynamicallyDispatchesToBrowser would intercept)
        try {
            if (methodInvocation.instance.metaClass.getMetaProperty(BASE_URL_PROPERTY)) {
                String baseUrl = methodInvocation.instance[BASE_URL_PROPERTY]
                if (baseUrl) {
                    log.info("using ${BASE_URL_PROPERTY}: ${baseUrl} from method invocation.")
                    return baseUrl
                }
            }
        } catch (ignored) {
            log.info("no ${BASE_URL_PROPERTY} from methodInvocation")
        }

        try {
            Configuration gebConfig = new ConfigurationLoader().conf
            String baseUrl = gebConfig.getProperty(BASE_URL_PROPERTY) ?: ''
            if (baseUrl) {
                log.info("using ${BASE_URL_PROPERTY}: ${baseUrl} from configuration.")
                return baseUrl
            }
        } catch (ignored) {
            log.info("no ${BASE_URL_PROPERTY} in config.")
        }

        log.info("no configured ${BASE_URL_PROPERTY} found.")
        return ''
    }

    private static Browser createBrowser(Configuration gebConf) {
        def browser = new Browser(gebConf)
        try {
            browser.driver
        }
        catch (SessionNotCreatedException e) {
            throw new IllegalStateException(
                    'Failed to create a remote browser session. ' +
                            'Did you set a \'containerBrowser\' property ' +
                            'corresponding to the \'driver\' in GebConfig?',
                    e
            )
        }
        browser
    }

    private static void applyFileDetector(Browser browser, SpecContainerConfiguration conf) {
        ((RemoteWebDriver) browser.driver).fileDetector = conf.fileDetector.getDeclaredConstructor().newInstance()
    }

    private static void startContainer(BrowserWebDriverContainer container, DockerImageName dockerImageName, String customBrowser) {
        try {
            container.start()
        } catch (ContainerFetchException e) {
            if (customBrowser) {
                throw new IllegalStateException(
                        "Could not find the Docker image [$dockerImageName] " +
                                "with the browser name from the 'containerBrowser' [$customBrowser] " +
                                'property specified in GebConfig. ' +
                                'See https://hub.docker.com/u/selenium for a list of available images.',
                        e
                )
            }
            throw e
        }
    }

    private static String getHostIp() {
        if (System.getenv(CI_PROPERTY)) {
            try {
                def process = 'hostname -I'.execute()
                process.waitFor()
                def ip = process.text.trim().split(/\s+/)[0]
                if (ip && ip != LOCALHOST_IP) {
                    return ip
                }
            } catch (Exception e) {
                log.warn('Failed to get container IP via hostname -I', e)
            }
            return LOCALHOST_IP
        }

        try {
            PortForwardingContainer.getDeclaredMethod('getNetwork').with {
                accessible = true
                (invoke(PortForwardingContainer.INSTANCE) as Optional<ContainerNetwork>)
                        .get()
                        .ipAddress
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    'Could not access network from PortForwardingContainer',
                    e
            )
        }
    }

    private static DockerImageName createDockerImageName(Object browserName) {
        DockerImageName.parse(
                "selenium/standalone-$browserName:$seleniumVersion"
        )
    }

    private static void validateDriverConf(Configuration gebConf) {
        if (gebConf.driverConf && !(gebConf.driverConf instanceof Closure)) {
            throw new IllegalStateException(
                    'The \'driver\' property of GebConfig must be a ' +
                            'Closure that returns an instance of RemoteWebDriver.'
            )
        }
    }

    private static String getSeleniumVersion() {
        SeleniumUtils.determineClasspathSeleniumVersion()
    }

    private GebTestManager createTestManager() {
        new SpockGebTestManagerBuilder()
                .withReportingEnabled(containerConf.reporting)
                .withBrowserCreator(
                        new Supplier<Browser>() {
                            @Override
                            Browser get() {
                                browser
                            }
                        }
                )
                .build()
    }

    private boolean isHostnameChanged() {
        containerConf.hostName != SpecContainerConfiguration.DEFAULT_HOSTNAME
    }

    private boolean isHostNameChanged() {
        container.host != SpecContainerConfiguration.DEFAULT_HOSTNAME
    }

    @CompileStatic
    @EqualsAndHashCode
    @PackageScope
    static class SpecContainerConfiguration {

        static final String DEFAULT_PROTOCOL = 'http'
        static final String DEFAULT_HOSTNAME = GenericContainer.INTERNAL_HOST_HOSTNAME
        static final Class<? extends ContainerFileDetector> DEFAULT_FILE_DETECTOR = DefaultContainerFileDetector

        String protocol
        String hostName
        boolean reporting
        Class<? extends ContainerFileDetector> fileDetector

        SpecContainerConfiguration(SpecInfo spec) {
            // Check for @ContainerGebConfiguration annotation first (including inherited)
            def annotation = spec.reflection.getAnnotation(ContainerGebConfiguration)

            if (annotation) {
                // Annotation values take priority
                protocol = annotation.protocol()
                hostName = annotation.hostName()
                reporting = annotation.reporting()
                fileDetector = annotation.fileDetector()
            } else {
                // Fall back to instance methods on the spec (overridable defaults from ContainerGebSpec)
                def instance = spec.reflection.getConstructor().newInstance() as ContainerGebSpec
                protocol = instance.protocol()
                hostName = instance.hostName()
                reporting = instance.reporting()
                fileDetector = instance.fileDetector()
            }
        }
    }
}
