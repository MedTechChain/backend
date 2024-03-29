package nl.tudelft.medtechchain.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import nl.medtechchain.protos.devicemetadata.DeviceType;
import nl.medtechchain.protos.query.Hospital;
import nl.medtechchain.protos.query.QueryType;
import nl.tudelft.medtechchain.jwt.JwtProvider;
import nl.tudelft.medtechchain.models.UserData;
import nl.tudelft.medtechchain.models.UserRole;
import nl.tudelft.medtechchain.repositories.UserDataRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql("/data.sql")
public class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID ADMIN_USER_ID =
            UUID.fromString("87f8304e-4740-45e6-9934-1bce37ac3d1b");

    private final UserData testResearcher = new UserData("jdoe", "password1",
            "J.Doe@tudelft.nl", "John", "Doe", "TU Delft", UserRole.RESEARCHER);


    @Test
    public void testQueryChainFailsOnNonExistingFields() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        String json = this.objectMapper.createObjectNode()
                .put(QueryJsonBuilder.QUERY_TYPE_PARAM, QueryType.COUNT.toString())
                .put(QueryJsonBuilder.FIELD_PARAM, "is_vulnerable")
                .put("qwerty", "123456")
                .toString();

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testQueryChainFailsOnForbidden() throws Exception {
        String jwt = this.jwtProvider.generateJwtToken(ADMIN_USER_ID, UserRole.ADMIN, new Date());

        String json = QueryJsonBuilder.builder(this.objectMapper)
                .withQueryType(QueryType.COUNT_ALL)
                .withFilterList("kernel_version", "v6.2.1")
                .withField("is_vulnerable")
                .build();

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testQueryChainFailsOnMissingRequiredFieldsQueryType() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        String json = QueryJsonBuilder.builder(this.objectMapper)
                .withField("has_device_version")
                .build();

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testQueryChainFailsOnMissingRequiredFieldsField() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        String json = QueryJsonBuilder.builder(this.objectMapper)
                .withQueryType(QueryType.COUNT_ALL)
                .build();

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testQueryChainInvalidValue() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());


        String json = objectMapper.createObjectNode()
                .put(QueryJsonBuilder.QUERY_TYPE_PARAM, "value")
                .put(QueryJsonBuilder.FIELD_PARAM, "count_vulnerabilities")
                .toString();

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testQueryChainSuccessfulOnlyRequiredFields() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        String json = QueryJsonBuilder.builder(this.objectMapper)
                .withQueryType(QueryType.AVERAGE)
                .withField("%x%x%x")
                .build();

        MockHttpServletResponse response = this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        String jsonResult = response.getContentAsString();
        JsonNode jsonNode = this.objectMapper.readTree(jsonResult);
        Assertions.assertThat(jsonNode.has("result")).isTrue();

        int result = Integer.parseInt(jsonNode.get("result").asText());
        Assertions.assertThat(result).isEqualTo(5);
    }

    @Test
    public void testQueryChainSuccessfulAllFields() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        String json = QueryJsonBuilder.builder(this.objectMapper)
                .withQueryType(QueryType.AVERAGE)
                .withDeviceType(DeviceType.WEARABLE_DEVICE)
                .withHospitalList(Hospital.LIFECARE, Hospital.MEDIVALE, Hospital.HEALPOINT)
                .withStartTime(Instant.now())
                .withStopTime(Instant.now())
                .withFilterList("version", "v6.2.1", "operating_system", "Arch Linux")
                .withField("has_firmware_version")
                .withValue("v4.3.2")
                .build();
        System.out.println(json);

        MockHttpServletResponse response = this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        String jsonResult = response.getContentAsString();
        JsonNode jsonNode = this.objectMapper.readTree(jsonResult);
        Assertions.assertThat(jsonNode.has("result")).isTrue();

        int result = Integer.parseInt(jsonNode.get("result").asText());
        Assertions.assertThat(result).isEqualTo(5);
    }


    /**
     * A helper class to simplify the creation of a JSON body for a query request.
     */
    static class QueryJsonBuilder {
        public static final String QUERY_TYPE_PARAM = "query_type";
        public static final String DEVICE_TYPE_PARAM = "device_type";
        public static final String HOSPITAL_LIST_PARAM = "hospital_list";
        public static final String HOSPITALS_PARAM = "hospitals";
        private static final String START_TIME_PARAM = "start_time";
        private static final String STOP_TIME_PARAM = "stop_time";
        private static final String FILTER_LIST_PARAM = "filter_list";
        private static final String FILTERS_PARAM = "filters";
        public static final String FIELD_PARAM = "field";
        public static final String VALUE_PARAM = "value";
        private final ObjectNode objectNode;

        private final ObjectMapper objectMapper;

        private QueryJsonBuilder(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            this.objectNode = this.objectMapper.createObjectNode();
        }

        public static QueryJsonBuilder builder(ObjectMapper objectMapper) {
            return new QueryJsonBuilder(objectMapper);
        }

        public QueryJsonBuilder withQueryType(QueryType queryType) {
            this.objectNode.put(QUERY_TYPE_PARAM, queryType.toString());
            return this;
        }

        public QueryJsonBuilder withDeviceType(DeviceType deviceType) {
            this.objectNode.put(DEVICE_TYPE_PARAM, deviceType.toString());
            return this;
        }

        public QueryJsonBuilder withHospitalList(Hospital... hospitals) {
            ArrayNode hospitalNode = this.objectMapper.createArrayNode();

            for (Hospital hospital : hospitals) {
                hospitalNode.add(hospital.toString());
            }
            this.objectNode
                    .putObject(HOSPITAL_LIST_PARAM)
                    .putArray(HOSPITALS_PARAM)
                    .addAll(hospitalNode);
            return this;
        }

        public QueryJsonBuilder withStartTime(Instant instant) {
            this.objectNode.put(START_TIME_PARAM, instant.toString());
            return this;
        }

        public QueryJsonBuilder withStopTime(Instant instant) {
            this.objectNode.put(STOP_TIME_PARAM, instant.toString());
            return this;
        }

        public QueryJsonBuilder withFilterList(String... kvPairs) {
            ArrayNode filters = this.objectMapper.createArrayNode();

            if (kvPairs.length % 2 != 0) {
                throw new IllegalArgumentException("The number of key-value pairs must be even");
            }

            for (int i = 0; i < kvPairs.length; i += 2) {
                filters.addObject()
                        .put("field", kvPairs[i])
                        .put("value", kvPairs[i + 1]);
            }
            this.objectNode.putObject(FILTER_LIST_PARAM).putArray(FILTERS_PARAM).addAll(filters);
            return this;
        }

        public QueryJsonBuilder withField(String field) {
            this.objectNode.put(FIELD_PARAM, field);
            return this;
        }

        public QueryJsonBuilder withValue(String value) {
            this.objectNode.put(VALUE_PARAM, value);
            return this;
        }

        public String build() {
            return this.objectNode.toPrettyString();
        }
    }
}
