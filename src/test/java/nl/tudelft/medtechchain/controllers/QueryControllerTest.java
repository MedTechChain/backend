package nl.tudelft.medtechchain.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import nl.tudelft.medtechchain.jwt.JwtProvider;
import nl.tudelft.medtechchain.models.Researcher;
import nl.tudelft.medtechchain.models.UserData;
import nl.tudelft.medtechchain.models.UserRole;
import nl.tudelft.medtechchain.models.queries.DeviceType;
import nl.tudelft.medtechchain.models.queries.QueryType;
import nl.tudelft.medtechchain.repositories.UserDataRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/data.sql")
public class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDataRepository userDataRepository;

    public static final String QUERIES_API = "/api/queries";

    public static final String QUERY_TYPE_PARAM = "query_type";
    public static final String DEVICE_TYPE_PARAM = "device_type";
    public static final String QUERY_FIELD_PARAM = "query_field";
    public static final String QUERY_VALUE_PARAM = "query_value";

    private static final UUID ADMIN_USER_ID =
            UUID.fromString("87f8304e-4740-45e6-9934-1bce37ac3d1b");

    private final UserData testResearcher = new UserData("jdoe", "password1",
            "J.Doe@tudelft.nl", "John", "Doe", "TU Delft", UserRole.RESEARCHER);

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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testQueryChainNonExistentFields() throws Exception {
        // NB! The mocks are defined in GatewayConfig
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        this.mockMvc.perform(get(QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam("query", "query"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testQueryChainNotAllowed() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(ADMIN_USER_ID, UserRole.ADMIN, new Date());

        this.mockMvc.perform(get(QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam(QUERY_TYPE_PARAM, QueryType.COUNT.toString())
                        .queryParam(DEVICE_TYPE_PARAM, DeviceType.WEARABLE_DEVICE.toString())
                        .queryParam(QUERY_FIELD_PARAM, "has_value_equals")
                        .queryParam(QUERY_VALUE_PARAM, "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testQueryChainMissingFields() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        this.mockMvc.perform(get(QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam(QUERY_TYPE_PARAM, QueryType.AVERAGE.name())
                        .queryParam(DEVICE_TYPE_PARAM, DeviceType.BEDSIDE_MONITOR.name()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testQueryChainNoValue() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        MockHttpServletResponse response = this.mockMvc.perform(get(QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam(QUERY_TYPE_PARAM, QueryType.COUNT.name())
                        .queryParam(DEVICE_TYPE_PARAM, DeviceType.BEDSIDE_MONITOR.name())
                        .queryParam(QUERY_FIELD_PARAM, "has_value_equals"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        String jsonResult = response.getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResult);
        Assertions.assertThat(jsonNode.has("result")).isTrue();

        int result = Integer.parseInt(jsonNode.get("result").asText());
        Assertions.assertThat(result).isEqualTo(5);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testQueryChainSuccessful() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        MockHttpServletResponse response = this.mockMvc.perform(get(QUERIES_API)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .queryParam(QUERY_TYPE_PARAM, QueryType.HISTOGRAM.name())
                        .queryParam(DEVICE_TYPE_PARAM, DeviceType.BEDSIDE_MONITOR.name())
                        .queryParam(QUERY_FIELD_PARAM, "has_value_equals")
                        .queryParam(QUERY_VALUE_PARAM, "5"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        String jsonResult = response.getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(jsonResult);
        Assertions.assertThat(jsonNode.has("result")).isTrue();

        int result = Integer.parseInt(jsonNode.get("result").asText());
        Assertions.assertThat(result).isEqualTo(5);
    }
}
