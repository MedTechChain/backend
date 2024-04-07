# How to contribute

In this document (future) developers and contributors can find more information about the backend server repository of the MedTech Chain project.

## Main classes

Below is an index for the Java classes together with their purpose. All these classes can be found in [src/main/java/nl/medtechchain/](src/main/java/nl/medtechchain/) directory.

[config](src/main/java/nl/medtechchain/config)
- [GatewayConfig.java](src/main/java/nl/medtechchain/config/GatewayConfig.java) : A configuration class for the Fabric Gateway. For testing, it has to be mocked (see variable `gateway.mock` in [application.properties](src/main/resources/application.properties))
- [JacksonConfig.java](src/main/java/nl/medtechchain/config/JacksonConfig.java): A configuration class for ObjectMapper to specify custom JSON (de)serializers.
- [PasswordConfig.java](src/main/java/nl/medtechchain/config/PasswordConfig.java): A configuration class for the (BCrypt) PasswordEncoder.
- [SecurityConfig.java](src/main/java/nl/medtechchain/config/SecurityConfig.java): A configuration class for some of the Spring Security components (such as SecurityFilterChain, AuthenticationProvider, AuthenticationManager, CorsConfigurationSource). In this class, authorization requirements are defined for the endpoints (e.g. some endpoints are only accessible for admin, others for both admin and researcher).

[controllers](src/main/java/nl/medtechchain/controllers)
- [error](src/main/java/nl/medtechchain/controllers/error)
  - [ErrorResponse](src/main/java/nl/medtechchain/controllers/error/ErrorResponse.java) : A custom class for error responses.
  - [GlobalExceptionHandler](src/main/java/nl/medtechchain/controllers/error/GlobalExceptionHandler.java): A class for global exception handling.
- [ApiEndpoints](src/main/java/nl/medtechchain/controllers/ApiEndpoints.java): A class that contains all supported API endpoints, so that they can be accessed from all places (for easy reference and modification).
- [UserController.java](src/main/java/nl/medtechchain/controllers/UserController.java): A controller class that provides API endpoints for managing users and interacts with the AuthenticationService class. For the full API documentation, see [docs/](docs/) directory. Possible operations are:
  - **POST** `/api/users/login` (accessible for all)
  - **POST** `/api/users/register` (accessible only for admin)
  - **GET** `/api/users/researchers` (accessible only for admin)
  - **PUT** `/api/users/update` (accessible only for admin)
  - **DELETE** `/api/users/delete` (accessible only for admin)
  - **PUT** `/api/users/change_password` (accessible for all)
- [QueryController.java](src/main/java/nl/medtechchain/controllers/QueryController.java): A controller class that gets queries from researchers, sends them to the blockchain and returns the result. For the full API documentation, see [docs/](docs/) directory. Possible operations are:
    - **POST** `/api/queries` (accessible only for researchers)

[jwt](src/main/java/nl/medtechchain/jwt)
- [JwtAuthenticationFilter.java](src/main/java/nl/medtechchain/jwt/JwtAuthenticationFilter.java): A class that represents a custom authentication filter based on JWT.
- [JwtProvider.java](src/main/java/nl/medtechchain/jwt/JwtProvider.java): A class that manages JWTs, i.e. generation, parsing and validation etc.
- [JwtSecretKey.java](src/main/java/nl/medtechchain/jwt/JwtSecretKey.java): A configuration class for creating the JWT key.

[models](src/main/java/nl/medtechchain/models)
- [email](src/main/java/nl/medtechchain/models/email)
  - [EmailData](src/main/java/nl/medtechchain/models/email/EmailData.java): An abstract class that stores the basic email data (recipient, subject and template) and is used to store the data common for all email types (i.e. child classes).
  - [CredentialsEmail](src/main/java/nl/medtechchain/models/email/CredentialsEmail.java): A class that stores the data necessary to send an email with the credentials when registering a new user.
- [Researcher.java](src/main/java/nl/medtechchain/models/Researcher.java): A DTO class for a researcher that will be sent when researchers have been requested.
- [UserData.java](src/main/java/nl/medtechchain/models/UserData.java): A class that is used to store the user data (userID, username, password, email, first name, last name, affiliation etc.).
- [UserRole.java](src/main/java/nl/medtechchain/models/UserRole.java): An enum class used to represent different user roles (currently "admin" and "researcher"), used for authorization checks when accessing endpoints. A new user is registered as researcher and this role cannot be changed. There is only one admin.

[protoutils](src/main/java/nl/medtechchain/protoutils)
- [JsonToProtobufDeserializer](src/main/java/nl/medtechchain/protoutils/JsonToProtobufDeserializer.java): A custom deserializer for the Query (protobuf) object, which is used when receiving a query request with JSON body which has to be forwarded to the blockchain.

[repositories](src/main/java/nl/medtechchain/repositories)
- [UserDataRepository.java](src/main/java/nl/medtechchain/repositories/UserDataRepository.java): A class for the database that stores the user data (see [UserData.java](src/main/java/nl/medtechchain/models/UserData.java) class).

[services](src/main/java/nl/medtechchain/services)
- [AuthenticationService.java](src/main/java/nl/medtechchain/services/AuthenticationService.java): A service class that communicates with the database with the user data (see [UserDataRepository.java](src/main/java/nl/medtechchain/repositories/UserDataRepository.java) and [UserData.java](src/main/java/nl/medtechchain/models/UserData.java) classes).
- [EmailService.java](src/main/java/nl/medtechchain/services/EmailService.java): A service class used to send emails (when registering a new user, the generated credentials are sent to the new user by email).

[Application.java](src/main/java/nl/medtechchain/Application.java): The main class for the backend server.

## Configuration

### Database

For *production/development* configurations, see [application.properties](src/main/resources/application.properties) file. Postgres database is used. The database is initialised with [the SQL script](src/main/resources/data.sql). In order to run the database, you have to run the build command in the [README.md](README.md) with `deps` option.

For *testing* configurations, see [application-test.properties](src/test/resources/application-test.properties) file. H2 database is used. The database is initialised with [the SQL script](src/test/resources/data.sql).

### Fabric Gateway

In order to run the Fabric Gateway, you need to have [chaincode](https://github.com/MedTechChain/chaincode) and [tools](https://github.com/MedTechChain/tools) repositories cloned in the parent directory of this repository (i.e. you will have `chaincode`, `tools` and `backend` in one directory). To run the infrastructure, run `./infra-start.sh` and then `./cc-deploy.sh` (also see [README.md](README.md)).

## Testing

Tests can be found in [src/test/java/nl/medtechchain/](src/test/java/nl/medtechchain/) directory. [TestConfig](src/test/java/nl/medtechchain/TestConfig.java) class configures some mocks used for testing. The actual tests can be found in `controllers`, `models` and `services` packages (directories). For controller tests, `MockMvc` is used.

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


## A couple of debugging tips...

Debugging the application (especially the one using Spring Security) might be very hard and intimidating.
While we cannot give you a full guide on debugging, we can share a couple of tips, which might help you in locating the errors.

### HTTP request does not reach its destination (e.g. 403 is returned or something else)

We have disabled the CSRF protection which Spring Security uses by default, which, however, also leads to not very informative 403 errors for many other exceptions.
In our case, we are using JWT tokens in the Authorization header, so CSRF tokens are not per se needed.

Furthermore, we allow thrown exceptions to actually reach the destinations with their status codes instead of constant 403 by adding the line `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()` to the [SecurityConfig.java](src/main/java/nl/medtechchain/config/SecurityConfig.java).

Nonetheless, if you still happen to get weird (403) exceptions, we can recommend some possible actions:
- Check whether you are requesting the correct endpoint and the correct method (e.g. you might have forgotten to update your HTTP request to use POST and you are still trying to access GET).
- To debug the Authorization errors, set a breakpoint to [JwtAuthenticationFilter.java](src/main/java/nl/medtechchain/jwt/JwtAuthenticationFilter.java). This is the first filter that you can interact with. From there, you can *step into* the `doFilter` method of the `filterChain` in order to get to the `FilterChainProxy` and iterate over the filters. This might be quite painful...
- Authentication errors (related to username-password authentications) can be debugged in the `DaoAuthenticationProvider` class.
- You can always try disabling some configurations in the [SecurityConfig.java](src/main/java/nl/medtechchain/config/SecurityConfig.java) to see if requests without authentication/authorization are successful.

### CORS

If you are also running frontend and think that you are facing some CORS issues,
you can change the configuration in [application.properties](src/main/resources/application.properties) file or in [SecurityConfig.java](src/main/java/nl/medtechchain/config/SecurityConfig.java) to make the policy extremely mild.
If the request succeeds, then this is indeed a CORS issue. Otherwise, there is some other problem...

### Query request to the chain fails

This can be for multiple reasons. In case the issue is in the JSON to Protobuf deserialization, the first place where you can set a breakpoint would be [JsonToProtobufDeserializer.java](src/main/java/nl/medtechchain/protoutils/JsonToProtobufDeserializer.java).
There you can see whether the JSON in the request body has been deserialized successfully.
If the deserialization has been successful, then most likely the issue is in the chaincode...

### Final thoughts on debugging

As a general tip, it is always nice to see what exactly does an API except.
E.g. in the [SecurityConfig.java](src/main/java/nl/medtechchain/config/SecurityConfig.java) you can see an overview of the endpoints and the permissions they require.
Example requests can also be found in the [docs](docs) folder, e.g. in the [Markdown API documentation](docs/MedTechChainAPI.md).

Furthermore, (JavaDoc) comments might provide some help regarding the expectations of the methods.
We have tried documenting subtle details as comments in the methods or in the JavaDoc, so you can try also reading those.