package nl.tudelft.medtechchain.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import nl.tudelft.medtechchain.jwt.JwtProvider;
import nl.tudelft.medtechchain.models.Researcher;
import nl.tudelft.medtechchain.models.UserData;
import nl.tudelft.medtechchain.models.UserRole;
import nl.tudelft.medtechchain.repositories.UserDataRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/data.sql")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Taken from data.sql. Make sure to update these fields in case of changes in data.sql
    private static final String ADMIN_USERNAME = "admintest";
    private static final String ADMIN_PASSWORD = "admin.test";
    private static final UUID ADMIN_USER_ID =
            UUID.fromString("87f8304e-4740-45e6-9934-1bce37ac3d1b");

    private final UUID randomUserId = UUID.fromString("2d38ca54-2bc4-4508-ab60-24d84b9441df");
    private final UserData testResearcher1 = new UserData("jdoe", "password1",
            "J.Doe@tudelft.nl", "John", "Doe", "TU Delft", UserRole.RESEARCHER);

    private final UserData testResearcher2 = new UserData("jdoe-1", "password2",
            "J.Doe-1@tudelft.nl", "Jane", "Doe", "TU Delft", UserRole.RESEARCHER);


    /**
     * Creates a JSON string from a list of key-value pairs.
     *
     * @param kvPairs       the key-value pairs which should be converted into a JSON string
     * @return              a JSON string, constructed based on the provided key-value pairs
     */
    private String createJson(String... kvPairs) {
        ObjectNode node = new ObjectMapper().createObjectNode();

        for (int i = 0; i < kvPairs.length; i += 2) {
            node.put(kvPairs[i], kvPairs[i + 1]);
        }
        return node.toString();
    }

    /**
     * Creates a JWT for the admin user in order to authenticate when sending a request.
     *
     * @return              the JWT for the admin user
     */
    private String createAdminJwt() {
        return this.jwtProvider.generateJwtToken(ADMIN_USER_ID, UserRole.ADMIN, new Date());
    }

    /**
     * Removes all researchers from the repository before each test (to avoid flakiness).
     */
    @BeforeEach
    public void setup() {
        List<UUID> researcherUserIds = this.userDataRepository
                .findAllResearchers().stream().map(Researcher::getUserId).toList();
        this.userDataRepository.deleteAllById(researcherUserIds);
    }

    @Test
    public void testLogInBadRequest() throws Exception {
        String jsonRequest = createJson("username", ADMIN_USERNAME, "not_password", "no_password");

        this.mockMvc
                .perform(post(ApiEndpoints.LOGIN_API).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogInAdminWrongUsername() throws Exception {
        String jsonRequest = createJson("username", "the_best_admin", "password", ADMIN_PASSWORD);

        this.mockMvc
                .perform(post(ApiEndpoints.LOGIN_API).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogInAdminWrongPassword() throws Exception {
        String jsonRequest = createJson("username", ADMIN_USERNAME, "password", "no_password");

        this.mockMvc
                .perform(post(ApiEndpoints.LOGIN_API).contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogInAdminSuccessful() throws Exception {
        String jsonRequest = createJson("username", ADMIN_USERNAME, "password", ADMIN_PASSWORD);

        MockHttpServletResponse response = this.mockMvc
                .perform(post(ApiEndpoints.LOGIN_API).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON.toString());

        String jsonResult = response.getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResult);
        Assertions.assertThat(jsonNode.has("jwt")).isTrue();
        Assertions.assertThat(jsonNode.has("token_type")).isTrue();
        Assertions.assertThat(jsonNode.has("expires_in")).isTrue();

        String jwt = jsonNode.get("jwt").asText();
        Jws<Claims> claims = this.jwtProvider.parseClaims(jwt);

        Assertions.assertThat(this.jwtProvider.getUserId(claims)).isEqualTo(ADMIN_USER_ID);
        Assertions.assertThat(this.jwtProvider.getRole(claims)).isEqualTo(UserRole.ADMIN);
        Assertions.assertThat(this.jwtProvider.isExpired(claims)).isFalse();
    }

    @Test
    public void testLogInResearcherSuccessful() throws Exception {
        String hashedPassword = this.passwordEncoder.encode("password1");
        this.testResearcher1.setPassword(hashedPassword);

        UserData user = this.userDataRepository.save(this.testResearcher1);
        String jsonRequest = createJson("username", user.getUsername(), "password", "password1");

        MockHttpServletResponse response = this.mockMvc
                .perform(post(ApiEndpoints.LOGIN_API).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON.toString());

        String jsonResult = response.getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResult);
        Assertions.assertThat(jsonNode.has("jwt")).isTrue();
        Assertions.assertThat(jsonNode.has("token_type")).isTrue();
        Assertions.assertThat(jsonNode.has("expires_in")).isTrue();

        String jwt = jsonNode.get("jwt").asText();
        Jws<Claims> claims = this.jwtProvider.parseClaims(jwt);

        Assertions.assertThat(this.jwtProvider.getUserId(claims)).isEqualTo(user.getUserId());
        Assertions.assertThat(this.jwtProvider.getRole(claims)).isEqualTo(UserRole.RESEARCHER);
        Assertions.assertThat(this.jwtProvider.isExpired(claims)).isFalse();

        // Revert the changed password
        this.testResearcher1.setPassword("password1");
    }

    @Test
    public void testRegisterNewUserNoJwt() throws Exception {
        String jsonRequest = createJson("first_name", "John", "last_name", "Doe",
                "email", "J.Doe@tudelft.nl", "affiliation", "TU Delft");

        this.mockMvc
                .perform(post(ApiEndpoints.REGISTER_API).contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterNewUserNotAllowed() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher2).getUserId();

        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());
        String jsonRequest = createJson("first_name", "Jane", "last_name", "Doe",
                "email", "J.Doe-1@tudelft.nl", "affiliation", "TU Delft");

        this.mockMvc
                .perform(post(ApiEndpoints.REGISTER_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testRegisterNewUserNoBearerPrefix() throws Exception {
        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "Jane", "last_name", "Doe",
                "email", "J.Doe-1@tudelft.nl", "affiliation", "TU Delft");

        this.mockMvc
                .perform(post(ApiEndpoints.REGISTER_API)
                        .header(HttpHeaders.AUTHORIZATION, jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterNewUserBadRequest() throws Exception {
        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "Jane", "very_last_name", "Doe",
                "email", "J.Doe-1@tudelft.nl", "affiliation", "TU Delft");

        this.mockMvc
                .perform(post(ApiEndpoints.REGISTER_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterNewUserInvalidEmail() throws Exception {
        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "John", "last_name", "Doe",
                "email", "J.Doe[at]tudelft.nl", "affiliation", "TU Delft");

        this.mockMvc
                .perform(post(ApiEndpoints.REGISTER_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterNewUserEmptyName() throws Exception {
        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "", "last_name", "",
                "email", "J.Doe@tudelft.nl", "affiliation", "TU Delft");

        this.mockMvc
                .perform(post(ApiEndpoints.REGISTER_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterNewUserEmailAlreadyExists() throws Exception {
        this.userDataRepository.save(this.testResearcher1);

        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "John", "last_name", "Doe",
                "email", "J.Doe@tudelft.nl", "affiliation", "TU Delft");

        this.mockMvc
                .perform(post(ApiEndpoints.REGISTER_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRegisterNewUserSuccessful() throws Exception {
        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "John", "last_name", "Doe",
                "email", "J.Doe@tudelft.nl", "affiliation", "TU Delft");

        this.mockMvc
                .perform(post(ApiEndpoints.REGISTER_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isCreated());

        Assertions.assertThat(this.userDataRepository.findByUsername("jdoe")).isPresent();
    }

    @Test
    public void testGetAllResearchersNotAllowed() throws Exception {
        UserData user1 = this.userDataRepository.save(this.testResearcher1);

        String jwt = this.jwtProvider
                .generateJwtToken(user1.getUserId(), UserRole.RESEARCHER, new Date());

        this.mockMvc
                .perform(get(ApiEndpoints.GET_RESEARCHERS_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllResearchersNoResearchers() throws Exception {
        String jwt = this.createAdminJwt();

        MockHttpServletResponse response = this.mockMvc
                .perform(get(ApiEndpoints.GET_RESEARCHERS_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON.toString());

        String jsonResult = response.getContentAsString();
        List<Researcher> actual = new ObjectMapper().readValue(jsonResult, new TypeReference<>(){});
        Assertions.assertThat(actual).containsExactlyElementsOf(List.of());
    }

    @Test
    public void testGetAllResearchersSuccessful() throws Exception {
        UserData user1 = this.userDataRepository.save(this.testResearcher1);
        UserData user2 = this.userDataRepository.save(this.testResearcher2);
        List<Researcher> expected = List.of(
                new Researcher(user1.getUserId(), user1.getFirstName(), user2.getLastName(),
                        user1.getEmail(), user1.getAffiliation()),
                new Researcher(user2.getUserId(), user2.getFirstName(), user2.getLastName(),
                        user2.getEmail(), user2.getAffiliation())
        );

        String jwt = this.createAdminJwt();

        MockHttpServletResponse response = this.mockMvc
                .perform(get(ApiEndpoints.GET_RESEARCHERS_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON.toString());

        String jsonResult = response.getContentAsString();
        List<Researcher> actual = new ObjectMapper().readValue(jsonResult, new TypeReference<>(){});
        Assertions.assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    public void testUpdatePersonalDetailsNotAllowed() throws Exception {
        UserData user1 = this.userDataRepository.save(this.testResearcher1);

        String jwt = this.jwtProvider
                .generateJwtToken(user1.getUserId(), UserRole.RESEARCHER, new Date());
        String jsonRequest = createJson("first_name", "John", "last_name",
                "Roe", "affiliation", "TU Delft");

        this.mockMvc
                .perform(put(ApiEndpoints.UPDATE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", user1.getUserId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdatePersonalDetailsBadUserId() throws Exception {
        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "John", "last_name",
                "Roe", "affiliation", "TU Delft");

        this.mockMvc
                .perform(put(ApiEndpoints.UPDATE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", "placeholder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdatePersonalDetailsBadRequest() throws Exception {
        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "John", "very_last_name",
                "Roe", "affiliation", "TU Delft");

        this.mockMvc
                .perform(put(ApiEndpoints.UPDATE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", this.randomUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdatePersonalDetailsNoUser() throws Exception {
        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", "John", "last_name",
                "Roe", "affiliation", "TU Delft");

        this.mockMvc
                .perform(put(ApiEndpoints.UPDATE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", this.randomUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdatePersonalDetailsSuccessful() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher1).getUserId();
        String newFirstName = "Johnny";
        String newLastName = "Roe";
        String newAffiliation = "TU Twente";

        String jwt = this.createAdminJwt();
        String jsonRequest = createJson("first_name", newFirstName, "last_name",
                newLastName, "affiliation", newAffiliation);

        this.mockMvc
                .perform(put(ApiEndpoints.UPDATE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        assert this.userDataRepository.findByUserId(userId).isPresent();
        UserData updatedUser = this.userDataRepository.findByUserId(userId).get();
        Assertions.assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
        Assertions.assertThat(updatedUser.getLastName()).isEqualTo(newLastName);
        Assertions.assertThat(updatedUser.getAffiliation()).isEqualTo(newAffiliation);
    }

    @Test
    public void testDeleteUserNotAllowed() throws Exception {
        UserData user1 = this.userDataRepository.save(this.testResearcher1);

        String jwt = this.jwtProvider
                .generateJwtToken(user1.getUserId(), UserRole.RESEARCHER, new Date());

        this.mockMvc
                .perform(delete(ApiEndpoints.DELETE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", user1.getUserId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteUserBadRequest() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher1).getUserId();

        String jwt = this.createAdminJwt();

        this.mockMvc
                .perform(delete(ApiEndpoints.DELETE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("userId", userId.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteUserBadUserId() throws Exception {
        String jwt = this.createAdminJwt();

        this.mockMvc
                .perform(delete(ApiEndpoints.DELETE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", "placeholder"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteUserNoUser() throws Exception {
        String jwt = this.createAdminJwt();

        this.mockMvc
                .perform(delete(ApiEndpoints.DELETE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", this.randomUserId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteUserSuccessful() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher1).getUserId();

        String jwt = this.createAdminJwt();

        this.mockMvc
                .perform(delete(ApiEndpoints.DELETE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", userId.toString()))
                .andExpect(status().isOk());

        Assertions.assertThat(this.userDataRepository.findByUserId(userId)).isEmpty();
    }

    @Test
    public void testChangePasswordUserDoesNotExist() throws Exception {
        String jsonRequest = createJson("username", "jdoe", "old_password",
                "password1", "new_password", "password2");

        this.mockMvc
                .perform(put(ApiEndpoints.CHANGE_PASSWORD_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testChangePasswordBadRequest() throws Exception {
        String jsonRequest = createJson("username", "jdoe", "oldPassword",
                "password1", "newPassword", "password2");

        this.mockMvc
                .perform(put(ApiEndpoints.CHANGE_PASSWORD_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testChangePasswordBadCredentials() throws Exception {
        String hashedPassword = this.passwordEncoder.encode("password1");
        this.testResearcher1.setPassword(hashedPassword);

        UserData user = this.userDataRepository.save(this.testResearcher1);
        String jsonRequest = createJson("username", user.getUsername(), "old_password",
                "notpassword1", "new_password", "password2");

        this.mockMvc
                .perform(put(ApiEndpoints.CHANGE_PASSWORD_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized());

        // Revert the changed password
        this.testResearcher1.setPassword("password1");
    }

    @Test
    public void testChangePasswordSuccessful() throws Exception {
        String hashedPassword = this.passwordEncoder.encode("password1");
        this.testResearcher1.setPassword(hashedPassword);

        UserData user = this.userDataRepository.save(this.testResearcher1);
        String jsonRequest = createJson("username", user.getUsername(), "old_password",
                "password1", "new_password", "password2");

        this.mockMvc
                .perform(put(ApiEndpoints.CHANGE_PASSWORD_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest).characterEncoding("utf-8"))
                .andExpect(status().isOk());

        // Revert the changed password
        this.testResearcher1.setPassword("password1");
    }


    // SOME TESTS FOR JWT
    @Test
    public void testJwtExpired() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher1).getUserId();
        long jwtExpirationTime = TimeUnit.MINUTES.toMillis(this.jwtProvider.getJwtExpirationTime());
        long issueTime = System.currentTimeMillis() - jwtExpirationTime - 60000; // 1 minute offset
        Date issueDate = new Date(issueTime);

        String jwt = this.jwtProvider.generateJwtToken(ADMIN_USER_ID, UserRole.ADMIN, issueDate);

        String responseString = this.mockMvc
                .perform(delete(ApiEndpoints.DELETE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", userId.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(responseString).isEqualTo("JWT has expired");
    }

    @Test
    public void testJwtUserDoesNotExist() throws Exception {
        String jwt = this.jwtProvider.generateJwtToken(randomUserId, UserRole.ADMIN, new Date());

        String responseString = this.mockMvc
                .perform(delete(ApiEndpoints.DELETE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("user_id", randomUserId.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(responseString).isEqualTo("JWT is invalid");
    }

    @Test
    public void testJwtInvalidJwt() throws Exception {
        String responseString = this.mockMvc
                .perform(delete(ApiEndpoints.DELETE_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer jwt")
                        .queryParam("user_id", randomUserId.toString()))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(responseString).isEqualTo("JWT is invalid");
    }
}
