/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package geb

import geb.buildadapter.BuildAdapterFactory
import geb.error.UnableToLoadException

/**
 * Manages the process of creating {@link geb.Configuration} objects, which control the runtime behaviour of Geb.
 * <p>
 * Typically usage of this class is hidden from the user as the {@link geb.Browser} uses this class internally
 * to load configuration based on its construction. If however custom configuration loading mechanics are necessary,
 * users can use this class or a subclass of to create a {@link geb.Configuration} object and construct the {@link geb.Browser}
 * with that.
 * <p>
 * Another avenue for custom configuration is usage of the {@link geb.BuildAdapter build adapter}. The build adapter that
 * will be used with any loaded configurations will be what is provided by {@link geb.ConfigurationLoader#createBuildAdapter(groovy.lang.GroovyClassLoader)}.
 *
 * @see geb.Configuration
 * @see geb.Browser
 */
class ConfigurationLoader {

    final String environment
    final Properties properties
    final GroovyClassLoader specialClassLoader

    /**
     * Configures the loader using the defaults.
     *
     * @see ConfigurationLoader ( ClassLoader , String , Properties )
     */
    ConfigurationLoader() {
        this(null, null, null)
    }

    /**
     * Configures the loader with the given environment for parsing config scripts, and defaults for everything else.
     *
     * @see ConfigurationLoader ( String , Properties , ClassLoader )
     */
    ConfigurationLoader(String environment) {
        this(environment, null, null)
    }

    /**
     * Sets the loader environment.
     * <p>
     * If any of the parameters are {@code null}, the appropriate {@code getDefault«something»()} method will be used to supply the value.
     *
     * @param classLoader The loader to use to find classpath resources and to
     * {@link #createBuildAdapter(groovy.lang.GroovyClassLoader) load the build adapter}
     * @param environment If loading a config script, the environment to load it with
     * @param properties The properties given to created {@link geb.Configuration} objects
     * @see #getDefaultEnvironment()
     * @see #getDefaultProperties()
     */
    ConfigurationLoader(String environment, Properties properties, GroovyClassLoader classLoader) {
        this.environment = environment ?: getDefaultEnvironment()
        this.properties = properties ?: getDefaultProperties()
        this.specialClassLoader = classLoader ?: getDefaultSpecialClassLoader()
    }

    /**
     * Creates a config using the default path for the config script and the default config class name. It loads the
     * configuration from class only if the configuration script was not found.
     * <p>
     * Uses {@link #getDefaultConfigScriptResourcePath()} for the path and {@link #getDefaultConfigClassName()} for the class name.
     *
     * @throws geb.error.UnableToLoadException if the config script or class exists but could not be read or parsed.
     * @see #getConf(String)
     * @see #getConfFromClass(String)
     */
    Configuration getConf() throws UnableToLoadException {
        doGetConf(null) ?: doGetConfFromClass(null) ?: getDefaultConf()
    }

    /**
     * <p>Creates a config backed by the classpath config script resource at the given path.</p>
     *
     * <p>The resource is first searched for using the special class loader (thread context loader by default), and then the class loader of this class if it wasn't found.
     * If no classpath resource can be found at the given path, an empty config object will be used with the class loader of this class.</p>
     *
     * <p>The class loader that is used is then propagated to the created configuration object. This means that if it is the special loader it <strong>must</strong> have
     * the same copy of the Geb classes as this class loader and any other classes Geb depends on.</p>
     *
     * @param configFileResourcePath the classpath relative path to the config script to use (if {@code null}, {@link #getDefaultConfigScriptResourcePath() default} will be used).
     * @throws geb.error.UnableToLoadException if the config script exists but could not be read or parsed.
     * @see #getConf(URL, GroovyClassLoader)
     * @see #getConfFromClass(String)
     */
    Configuration getConf(String configFileResourcePath) throws UnableToLoadException {
        if (configFileResourcePath == null) {
            getConf()
        } else {
            doGetConf(configFileResourcePath) ?: getDefaultConf()
        }
    }

    /**
     * Creates a config backed by the config script at the given URL.
     * <p>
     * If URL is {@code null} or doesn't exist, an exception will be thrown.
     *
     * @param configLocation The absolute URL to the config script to use for the config (cannot be {@code null})
     * @param classLoader The class loader to load the config script with (must be the same or a child of the class loader of this class)
     * @throws geb.error.UnableToLoadException if the config script exists but could not be read or parsed.
     */
    Configuration getConf(URL configLocation, GroovyClassLoader classLoader) throws UnableToLoadException {
        if (configLocation == null) {
            throw new IllegalArgumentException("configLocation cannot be null")
        }

        createConf(loadRawConfig(configLocation, classLoader), classLoader)
    }

    /**
     * <p>Creates a config backed by the config class with a given name.</p>
     *
     * <p>The class is first searched for using the special class loader (thread context loader by default), and then the class loader of this class if it wasn't found.
     * If no such class can be found, an empty config object will be used with the class loader of this class.</p>
     *
     * <p>The class loader that is used is then propagated to the created configuration object. This means that if it is the special loader it <strong>must</strong> have
     * the same copy of the Geb classes as this class loader and any other classes Geb depends on.</p>
     *
     * @param configFileResourcePath the classpath relative path to the config script to use (if {@code null}, {@link #getDefaultConfigScriptResourcePath() default} will be used).
     * @throws geb.error.UnableToLoadException if the config script exists but could not be read or parsed.
     * @see #getConf(Class, GroovyClassLoader)
     * @see #getConf(String)
     */
    Configuration getConfFromClass(String className) throws UnableToLoadException {
        doGetConfFromClass(className) ?: getDefaultConf()
    }

    /**
     * Creates a config backed by a given class.
     *
     * @param configClass Class that contains configuration
     * @param classLoader The class loader to load the config script with (must be the same or a child of the class loader of this class)
     * @throws geb.error.UnableToLoadException when config class cannot be read
     */
    Configuration getConf(Class configClass, GroovyClassLoader classLoader) throws UnableToLoadException {
        createConf(loadRawConfig(configClass), classLoader)
    }

    /**
     * This implementation returns {@code "GebConfig.groovy"}
     */
    String getDefaultConfigScriptResourcePath() {
        "GebConfig.groovy"
    }

    /**
     * This implementation returns {@code "GebConfig"}
     */
    String getDefaultConfigClassName() {
        'GebConfig'
    }

    /**
     * <p>Creates a config backed by the config class with a given name. This method is used by {@link #getConfFromClass(String)}.</p>
     *
     * <p>The class is first searched for using the special class loader (thread context loader by default), and then the class loader of this class if it wasn't found.
     * If no such class can be found, null is returned.</p>
     *
     * <p>The class loader that is used is then propagated to the created configuration object. This means that if it is the special loader it <strong>must</strong> have
     * the same copy of the Geb classes as this class loader and any other classes Geb depends on.</p>
     *
     * @param configFileResourcePath the classpath relative path to the config script to use (if {@code null}, {@link #getDefaultConfigScriptResourcePath() default} will be used).
     * @throws geb.error.UnableToLoadException if the config script exists but could not be read or parsed.
     * @see #getConfFromClass(String)
     * @see #getConf(Class, GroovyClassLoader)
     */
    protected Configuration doGetConfFromClass(String className) {
        def configurationClassName = className ?: getDefaultConfigClassName()
        def configClass = tryToLoadClass(configurationClassName, specialClassLoader)
        if (configClass) {
            getConf(configClass, specialClassLoader)
        } else {
            def thisLoader = new GroovyClassLoader(getClass().classLoader)
            configClass = tryToLoadClass(configurationClassName, thisLoader)
            configClass ? getConf(configClass, thisLoader) : null
        }
    }

    protected Class tryToLoadClass(String className, ClassLoader loader) {
        def loaded
        try {
            loaded = loader.loadClass(className)
        } catch (ClassNotFoundException cnfe) {
            //just return null if the class could not be found
        }
        loaded
    }

    /**
     * Result of this method is used as the default configuration when there is no configuration script or class.
     * This implementation returns a configuration as if the loaded configuration script/class was empty.
     */
    protected Configuration getDefaultConf() {
        createConf(new ConfigObject(), new GroovyClassLoader(getClass().classLoader))
    }

    /**
     * <p>Creates a config backed by the classpath config script resource at the given path. This method is used by {@link #getConf(String)}.</p>
     *
     * <p>The resource is first searched for using the special class loader (thread context loader by default), and then the class loader of this class if it wasn't found.
     * If no classpath resource can be found at the given path null is returned.</p>
     *
     * <p>The class loader that is used is then propagated to the created configuration object. This means that if it is the special loader it <strong>must</strong> have
     * the same copy of the Geb classes as this class loader and any other classes Geb depends on.</p>
     *
     * @param configFileResourcePath the classpath relative path to the config script to use (if {@code null}, {@link #getDefaultConfigScriptResourcePath() default} will be used).
     * @throws geb.error.UnableToLoadException if the config script exists but could not be read or parsed.
     * @see #getConf(URL, GroovyClassLoader)
     * @see #getConf(String)
     */
    protected Configuration doGetConf(String configFileResourcePath) throws UnableToLoadException {
        def resourcePath = configFileResourcePath ?: getDefaultConfigScriptResourcePath()

        def specialLoaderResource = specialClassLoader.getResource(resourcePath)
        if (specialLoaderResource) {
            getConf(specialLoaderResource, specialClassLoader)
        } else {
            def thisClassLoader = new GroovyClassLoader(getClass().classLoader)
            def thisLoaderResource = thisClassLoader.getResource(resourcePath)
            thisLoaderResource ? getConf(thisLoaderResource, thisClassLoader) : null
        }
    }

    /**
     * This implementation returns a new {@link groovy.lang.GroovyClassLoader} which uses the
     * {@code Thread.currentThread ( ) .contextClassLoader} as the parent.
     */
    protected GroovyClassLoader getDefaultSpecialClassLoader() {
        new GroovyClassLoader()
    }

    /**
     * This implementation returns {@code System.properties["geb.env"]}
     */
    protected String getDefaultEnvironment() {
        System.properties["geb.env"]
    }

    /**
     * This implementation returns {@code System.properties}
     */
    protected Properties getDefaultProperties() {
        System.properties
    }

    /**
     * Reads the config scripts at {@code configLocation} with the {@link #createSlurper()}
     *
     * @throws geb.error.UnableToLoadException if the config script could not be read.
     */
    protected ConfigObject loadRawConfig(URL configLocation, GroovyClassLoader classLoader) throws UnableToLoadException {
        loadRawConfig(createSlurper(classLoader), configLocation)
    }

    /**
     * Reads the config class with the {@link #createSlurper()}
     *
     * @throws geb.error.UnableToLoadException if the config class could not be read.
     */
    protected ConfigObject loadRawConfig(Class configClass) throws UnableToLoadException {
        def slurper = createSlurper()
        synchronized (configClass) {
            loadRawConfig(slurper, configClass)
        }
    }

    protected ConfigObject loadRawConfig(ConfigSlurper slurper, URL source) {
        def configClass
        try {
            configClass = slurper.classLoader.parseClass(source.text)
            loadRawConfig(slurper, configClass)
        } catch (Throwable e) {
            throw new UnableToLoadException(source, slurper.environment, e)
        } finally {
            if (configClass) {
                GroovySystem.metaClassRegistry.removeMetaClass(configClass)
            }
        }
    }

    protected ConfigObject loadRawConfig(ConfigSlurper slurper, Class source) {
        try {
            slurper.parse(source)
        } catch (Throwable e) {
            throw new UnableToLoadException(source, slurper.environment, e)
        }
    }

    /**
     * Creates a config slurper with environment we were constructed with (if any) and sets
     * the slurpers classloader to the classloader we were constructed with.
     */
    protected ConfigSlurper createSlurper(GroovyClassLoader classLoader) {
        def slurper = createSlurper()
        slurper.classLoader = classLoader
        slurper
    }

    /**
     * Creates a config slurper with environment we were constructed with (if any).
     */
    protected ConfigSlurper createSlurper() {
        environment ? new ConfigSlurper(environment) : new ConfigSlurper()
    }

    /**
     * Creates a new {@link geb.Configuration} backed by {@code rawConfig} with the {@code properties}
     * we were constructed with, the {@code classLoader} passed in and a {@link geb.BuildAdapter build adapter}.
     *
     * @see geb.Configuration#Configuration(groovy.util.ConfigObject, java.util.Properties, geb.BuildAdapter, java.lang.ClassLoader)
     */
    protected createConf(ConfigObject rawConfig, GroovyClassLoader classLoader) {
        new Configuration(rawConfig, properties, createBuildAdapter(classLoader), classLoader)
    }

    /**
     * Uses the {@link geb.buildadapter.BuildAdapterFactory#getBuildAdapter(java.lang.ClassLoader) build adapter factory} to load
     * a build adapter with the {@code classLoader} passed in.
     */
    protected BuildAdapter createBuildAdapter(GroovyClassLoader classLoader) {
        BuildAdapterFactory.getBuildAdapter(classLoader)
    }

}