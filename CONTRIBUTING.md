<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
# Contributing to Geb

Contributions are always welcome to Geb!
If you'd like to contribute (and we hope you do) please take a look at the following guidelines and instructions.

## Contributor License Agreement (CLA)

To become a committer, you need to sign the [Apache Individual Contributor Agreement (ICLA)](https://www.apache.org/licenses/icla.pdf). If you work for a company, your company may need to sign also a [Corporate Contributor License Agreement]([https://www.apache.org/licenses/ccla.pdf](https://www.apache.org/licenses/cla-corporate.pdf). For small ad-hoc PRs no contributor license agreement is needed.

## Build Environment

Geb builds with [Gradle](http://www.gradle.org/). 
You do not need to have Gradle installed to work with the Geb build as Gradle
provides an executable wrapper that you use to drive the build.

On UNIX type environments this is `gradlew` and is `gradlew.bat` on Windows.
Most examples here are for UNIX type systems.
You can leave off the leading `./` when using the wrapper on Windows.

To see all available tasks you can run:

    ./gradlew tasks

But there are a few very common tasks worth knowing.
For example, to run all the automated tests and quality
checks for the entire project you would run:

    ./gradlew check

To publish Geb artifacts to your local Maven cache you would run:

    ./gradlew publishToMavenLocal

## Bootstrapping the build environment from a source distribution

If you have cloned the source from the GitHub repo, you can skip this section. If instead
you are using an ASF source release Zip, read on.

ASF source releases don't contain binary files such as those needed by the Gradle wrapper.
If using a source Zip release, there is an additional boostrap step you need to do
to properly set up the build environment.

The bootstrap step requires you to have a version of Gradle installed on your system.
If you know the desired Gradle version, you can run Gradle's `wrapper` task.
Alternatively, you can use the bootstrap project which knows about the correct Gradle version:

    gradle -P bootstrap

Now the wrapper will be installed, and you can follow the instructions elsewhere which use the wrapper.

## IDE import

The project is set up to work with IntelliJ IDEA, simply import it using the native IntelliJ IDEA import for Gradle projects.

If you use a different IDE or editor you're on your own.    

## Contributing Documentation

Geb documentation comes in two forms: the manual and the API (i.e. the Groovydoc amongst the source).

### The Manual

The manual project can be found at `doc/manual` within the project tree. 
The [AsciiDoc](http://asciidoc.org/) source files, CSS and images that make up the manual can be found at `doc/manual/src` (the manual is compiled using a tool called [AsciiDoctor](http://asciidoctor.org/)).
Most documentation contributions are simply modifications to these files.

To compile the manual in or to see any changes made, simply run (from the root of the geb project)…

    ./gradlew :doc:manual:asciidoctor

You will then find the compiled HTML in the `doc/manual/build/asciidoc` directory.

Note that all snippets in the manual are imported from the tests of the `:doc:manual-snippets` and `:doc:manual-snippets:real-browser` subprojects.
This is to make them executable and be able to check that they are working with current Geb code in an automated manner.

### The API reference

The API reference is made up of the Groovydoc (like Javadoc) that annotates the Groovy files for the different modules in `module/`. 
To make a change to the reference API documentation, find the corresponding file in `module/«module»/src/main/groovy` and make the change.

You can then generate the API reference HTML by running…

    ./gradlew :doc:manual:apiDoc

You will then find the compiled HTML in the directory `doc/manual/build/apiDoc`

> Note that you can build the manual chapters and reference API in one go with `./gradlew doc:manual:packageManual`
 
### The Geb website

You can generate the Geb Website by running…

```
    ./gradlew :doc:site:build
```

It is a static website. You can find it in the folder `doc/site/build/dist`.

You can serve it [jwebserver](https://blogs.oracle.com/javamagazine/post/java-18-simple-web-server) with 

```
cd doc/site/build/dist
jwebserver -d $(pwd)
```

## Contributing features/patches

The source code for all the modules is contained in the `module/` directory.

To run the tests and quality checks after making your change to a module, you can run…

    ./gradlew :module:«module-name»:check

There are lots of example tests in the `:module:geb-core` module that use the classes from the `:internal:test-support` module for running against an in memory HTTP server.

To run the entire test suite and quality checks, run…

    ./gradlew check

Please remember to include relevant updates to the manual with your changes.

## Coding style guidelines

The following are some general guidelines to observe when contributing code:

1. All source files must have the appropriate ASLv2 license header
1. All source files use an indent of 4 spaces
1. Everything needs to be tested
1. Documentation needs to be updated appropriately to the change

The build processes checks that most of the above conditions have been met.

## Code changes

Code can be submitted via GitHub pull requests.
When a pull request is send it triggers a CI build to verify the test and quality checks still pass.

## Proposing new features

If you would like to implement a new feature, please [raise an issue](https://github.com/apache/groovy-geb/issues) before sending a pull request so the feature can be discussed.
This is to avoid you spending your valuable time working on a feature that the project developers are not willing to accept into the codebase.

## Fixing bugs

If you would like to fix a bug, please [raise an issue](https://github.com/apache/groovy-geb/issues) before sending a pull request so it can be discussed.
If the fix is trivial or non-controversial then this is not usually necessary.

## Development Mailing List

If you want to do some work on Geb and want some help,
you can join the `geb-dev@groovy.apache.org` mailing list:
[Browse](https://lists.apache.org/list.html?geb-dev@groovy.apache.org)
[Subscribe](mailto:geb-dev-subscribe@groovy.apache.org)
[Unsubscribe](mailto:geb-dev-unsubscribe@groovy.apache.org)

## Licensing and attribution

Geb is licensed under [ASLv2](https://www.apache.org/licenses/LICENSE-2.0). All source code falls under this license.

The source code will not contain attribution information (e.g. Javadoc) for contributed code.
Contributions will be recognised elsewhere in the project documentation.
