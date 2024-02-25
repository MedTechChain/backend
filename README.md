# HeartBlocks - Backend

## Description

The backend part of HealthBlocks project. In particular, this repository contains the User Management Server,
used to create, retrieve, update and delete researchers. In addition, authenticated researchers can perform queries on
the Hyperledger Fabric Blockchain.

The application uses Java Spring Boot (version 3.2.2) and Spring Security. The required Java version is 21.
Gradle is used as the build tool and for running other tasks (such as Checkstyle or tests).
See [build.gradle](build.gradle) file for all dependencies.

## Build

### Command Line

If you want to build the project in the command line, you can run:

```shell
gradle build
```

Make sure your Java version is set to the required one (i.e. 21). You can also use the Gradle Wrapper, for example:

```shell
# Linux/macOS
./gradlew build
```
```shell
# Windows
\.gradlew.bat build
```

### IntelliJ

If you want to build and run the application in IntelliJ IDEA, first open the project as a Gradle project
(NB! click on the `build.gradle` file and then press "Open", instead of clicking on the project root directory).

Change your Java version to the required one (i.e. 21). Go to `File>Project Structure>Project Settings>Project>SDK`
and select version 21 (maybe also do the same in `File>Settings>Build, Execution, Deployment>Compiler>Java Compiler`).
Set Gradle to the same version as Java JVM (`File>Settings>Build, Execution, Deployment>Build Tools>Gradle`,
select the project and set the JVM version next to `Gradle JVM`).

## Run the application

### Command Line

To run the application in the command line, type:

```shell
gradle bootRun
```
You can substitute `gradle` with`./gradlew`/`\.gradlew.bat`.

### IntelliJ

Navigate to [src/main/java/nl/tudelft/healthblocks/Application.java](src/main/java/nl/tudelft/healthblocks/Application.java),
right click on it and press `Run 'Application'`.

### Accessing endpoints

By default, the server is running on port 8088 (configured in [application.properties](src/main/resources/application.properties) file).
See [docs](docs) for the available API endpoints.

The admin details are also stored in [application.properties](src/main/resources/application.properties) file and can be configured.
You are strongly encouraged to update the admin password and also the JWT secret key.

## Running other tasks

You can also run other Gradle tasks, both from command line and IntelliJ.

### Command line

You can run Checkstyle (static code style analysis) as follows:

```shell
gradle checkstyleMain  # The regular codebase
gradle checkstyleTest  # The code in the test folders
```
You can substitute `gradle` with`./gradlew`/`\.gradlew.bat`.

Running tests can be done tests as follows:
```shell
gradle test
```
You are free to substitute `gradle` with`./gradlew`/`\.gradlew.bat`.

### IntelliJ

If you want to use the Gradle plugin, click on the Gradle icon (usually located on the right side of the IDE; you can reinstall the plugin if you cannot find it).
There you can find different tasks (e.g. `checkstyleMain` and `chackstyleTest` in the section `other`, and `test` in the `verification` section).