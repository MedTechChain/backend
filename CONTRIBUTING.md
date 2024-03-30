# How to contribute

In this document (future) developers and contributors can find more information about the backend server repository of the Mectech Chain project.

## Main classes

Below is an index for the Java classes together with their purpose. All these classes can be found in [src/main/java/nl/tudelft/medtechchain/](src/main/java/nl/tudelft/medtechchain/) directory.

`config`
- `GatewayConfig.java`: A configuration class for the Fabric Gateway. For testing, it has to be mocked (see variable `gateway.mock` in `application.properties`)
- `JacksonConfig`: A configuration class for ObjectMapper to specify custom JSON (de)serializers.
- `PasswordConfig.java`: A configuration class for the (BCrypt) PasswordEncoder.
- `SecurityConfig.java`: A configuration class for some of the Spring Security components (such as SecurityFilterChain, AuthenticationProvider, AuthenticationManager, CorsConfigurationSource). In this class, authorization requirements are defined for the endpoints (e.g. some endpoints are only accessible for admin, others for both admin and researcher).

`controllers`
- `ApiEndpoints`: A class that contains all supported API endpoints, so that they can be accessed from all places (for easy reference and modification).
- `UserController.java`: A controller class that provides API endpoints for managing users and interacts with the AuthenticationService class. For the full API documentation, see [docs/](docs/) directory. Possible operations are:
  - **POST** `/api/users/login` (accessible for all)
  - **POST** `/api/users/register` (accessible only for admin)
  - **GET** `/api/users/researchers` (accessible only for admin)
  - **PUT** `/api/users/update` (accessible only for admin)
  - **DELETE** `/api/users/delete` (accessible only for admin)
- `QueryController.java`: A controller class that gets queries from researchers, sends them to the blockchain and returns the result. For the full API documentation, see [docs/](docs/) directory. Possible operations are:
    - **POST** `/api/queries` (accessible only for researchers)

`jwt`
- `JwtAuthenticationFilter.java`: A class that represents a custom authentication filter based on JWT.
- `JwtProvider.java`: A class that manages JWTs, i.e. generation, parsing and validation etc.
- `JwtSecretKey.java`: A configuration class for creating the JWT key.

`models`
- `email`
  - `EmailData`: An abstract class that stores the basic email data (recipient, subject and template) and is used to store the data common for all email types (i.e. child classes).
  - `CredentialsEmail`: A class that stores the data necessary to send an email with the credentials when registering a new user.
- `queries`
  - `DeviceType`: An enum class used to represent different types of devices that can be queried on the chain (currently "count" and "average").
  - `QueryType`: An enum class used to represent different types of queries that can be sent to the chain (currently "bedside_monitor", "wearable_device" and "both").
- `Researcher.java`: A DTO class for a researcher that will be sent when researchers have been requested.
- `UserData.java`: A class that is used to store the user data (userID, username, password, email, first name, last name, affiliation etc.).
- `UserRole.java`: An enum class used to represent different user roles (currently "admin" and "researcher"), used for authorization checks when accessing endpoints. A new user is registered as researcher and this role cannot be changed. There is only one admin.

`protoutils`
- `JsonToProtobufDeserializer`: A custom deserializer for the Query (protobuf) object, which is used when receiving a query request with JSON body which has to be forwarded to the blockchain.

`repositories`
- `UserDataRepository.java`: A class for the database that stores the user data (see `UserData.java` class).

`services`
- `AuthenticationService.java`: A service class that communicates with the database with the user data (see `UserDataRepository.java` and `UserData.java` classes).
- `EmailService.java`: A service class used to send emails (when registering a new user, the generated credentials are sent to the new user by email).

`Application.java`: The main class for the backend server.

## Configuration

### Database

For *production/development* configurations, see [src/main/resources/application.properties](src/main/resources/application.properties) file. Postgres database is used. The database is initialised with the SQL script [src/main/resources/data.sql](src/main/resources/data.sql). In order to run the database, you have to run `./run-deps.sh` script in `tools/` directory.

For *testing* configurations, see [src/test/resources/application.properties](src/test/resources/application.properties) file. H2 database is used. The database is initialised with the SQL script [src/test/resources/data.sql](src/test/resources/data.sql).

### Fabric Gateway

In order to run the Fabric Gateway, you need to have [chaincode](https://github.com/MedTechChain/chaincode) and [tools](https://github.com/MedTechChain/tools) repositories cloned in the parent directory of this repository (i.e. you will have `chaincode`, `tools` and `backend` in one directory). To run the infrastructure, run `./infra-up.sh` and then `./cc-deploy.sh`.

## Testing

Tests can be found in [src/test/java/nl/tudelft/medtechchain/](src/test/java/nl/tudelft/medtechchain/) directory. `TestConfig` class configures some mocks used for testing. The actual tests can be found in `controllers`, `models` and `services` packages (directories). For controller tests, `MockMvc` is used.

When writing tests, please try to write meaningful assertions and make sure that the tests pass before you push. 

## GitHub Actions

[The workflow file](.github/workflows/workflow.yml) is structured as follows:

1. Set up Java (version 21) and Gradle
2. Run the build
3. Run Checkstyle
4. Run tests
5. Run JaCoCo coverage
6. Create the coverage summary (shown on the workflow page in GitHub)

## Pull requests

Create a Pull Request from your branch into `main` and make sure that the pipeline (i.e. GitHub Actions) passes. After that, wait for approval from the project maintainers. Your time and efforts are highly appreciated! ❤️
