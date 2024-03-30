# MedTech Chain - Backend

[![Pipeline](https://github.com/MedTechChain/backend/actions/workflows/workflow.yml/badge.svg)](https://github.com/MedTechChain/backend/actions/workflows/workflow.yml)

## Description

The backend part of MedTech Chain project. In particular, this repository contains the User Management Server,
used to create, retrieve, update and delete researchers. In addition, authenticated researchers can perform queries on the Hyperledger Fabric Blockchain.

The application uses Java Spring Boot (version 3.2.2) and Spring Security. The required Java version is 21.
Gradle is used as the build tool and for running other tasks (such as Checkstyle or tests).
See [build.gradle](build.gradle) file for all dependencies.

## Build

### Load Crypto

Before running the service (IDE or Docker), make sure to copy the crypto
material required for the integration with Fabric infrastructure.

Use the `scripts/copy-crypto.sh` script to automatically copy the crypto material.

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

Navigate to [src/main/java/nl/tudelft/medtechchain/Application.java](src/main/java/nl/tudelft/medtechchain/Application.java),
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

## Docker

There are three profiles defined in the docker compose:
- `deps`: run the backend application on host and run the database and development tools (e.g., smtp4dev, pgadmin4) in Docker
- `dev`: run both backend application and its dependencies (database and development tools) in Docker
- `demo`: run both backend application and database only (no development tools) in Docker

### Command

When running the `demo` profile, make sure to provide the email app password.

```shell 
export SMTP_PASSWORD="" && docker-compose --profile <deps|dev|demo> -p medtechchain-ums-be up -d --build
```

### PGAdmin

If you want to inspect the database and perform queries by hand, access PGAdmin at
`localhost:10000`. Take the dummy credentials from the `docker-compose.yaml` for
authentication and setting up the database connection.

### smtp4dev

This is a testing SMTP server, having the interface accessible at `localhost:11000`.
Mind that the backend is configured by default to send emails here. If you want to send
real emails, please use the `demo` profile. Mind that when running `demo` profile, all volumes are deleted.

## Run the infrastructure

In order to run the blockchain infrastructure, go to `tools` repository which has to be present in the parent directory. Then run:

```shell
./infra-up.sh
./cc-deploy.sh
```

Check [tools](https://github.com/MedTechChain/tools) for more instructions regarding running the infrastructure.