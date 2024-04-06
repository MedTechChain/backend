package nl.medtechchain.controllers;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import com.google.protobuf.util.JsonFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import nl.medtechchain.jwt.JwtProvider;
import nl.medtechchain.models.UserData;
import nl.medtechchain.models.UserRole;
import nl.medtechchain.protos.devicemetadata.DeviceType;
import nl.medtechchain.protos.query.AverageResult;
import nl.medtechchain.protos.query.CountAllResult;
import nl.medtechchain.protos.query.CountResult;
import nl.medtechchain.protos.query.Hospital;
import nl.medtechchain.protos.query.QueryType;
import nl.medtechchain.repositories.UserDataRepository;
import org.assertj.core.api.Assertions;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    Gateway gateway;

    @Value("${server.ssl.enabled}")
    private boolean https;

    @Value("${gateway.query-smart-contract-name}")
    private String queryContractName;

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

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API).secure(https)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testQueryChainFailsOnForbidden() throws Exception {
        String jwt = this.jwtProvider.generateJwtToken(ADMIN_USER_ID, UserRole.ADMIN, new Date());

        String json = QueryJsonBuilder.builder(this.objectMapper)
                .withQueryType(QueryType.COUNT_ALL)
                .withFilterList("kernel_version", "v6.2.1")
                .withField("is_vulnerable")
                .build();

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API).secure(https)
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

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API).secure(https)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testQueryChainInvalidValue() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());


        String json = objectMapper.createObjectNode()
                .put(QueryJsonBuilder.QUERY_TYPE_PARAM, "value")
                .put(QueryJsonBuilder.FIELD_PARAM, "count_vulnerabilities")
                .toString();

        this.mockMvc.perform(post(ApiEndpoints.QUERIES_API).secure(https)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testQueryChainSuccessfulOnlyRequiredFieldsCount() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        // Configure the Contract mock
        Contract contractMock = this.gateway.getNetwork("").getContract("", "");

        CountResult countResult = CountResult.newBuilder().setResult(7).build();
        String countMatcher = String.format(".*%s.*", QueryType.COUNT);
        String countResultJson = JsonFormat.printer().print(countResult);
        Mockito.when(contractMock.evaluateTransaction(eq(this.queryContractName),
                        Mockito.matches(countMatcher)))
                .thenReturn(countResultJson.getBytes());

        // Build the JSON query
        String json = QueryJsonBuilder.builder(this.objectMapper)
                .withQueryType(QueryType.AVERAGE)
                .withField("%x%x%x")
                .build();

        MockHttpServletResponse response = this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .secure(https)
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

        double result = Double.parseDouble(jsonNode.get("result").asText());
        Assertions.assertThat(result).isEqualTo(4.4);
    }

    @Test
    public void testQueryChainSuccessfulOnlyRequiredFieldsCountAll() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        // Configure the Contract mock
        Contract contractMock = this.gateway.getNetwork("").getContract("", "");

        CountAllResult countAllResult = CountAllResult.newBuilder()
                .putResult("r1", 9).build();
        String countAll = String.format(".*%s.*", QueryType.COUNT_ALL);
        String countAllResultJson = JsonFormat.printer().print(countAllResult);
        Mockito.when(contractMock.evaluateTransaction(eq(this.queryContractName),
                        Mockito.matches(countAll)))
                .thenReturn(countAllResultJson.getBytes());

        // Build the JSON query
        String json = QueryJsonBuilder.builder(this.objectMapper)
                .withQueryType(QueryType.COUNT_ALL)
                .withField("%s%s%s\0%s%s%s")
                .build();

        MockHttpServletResponse response = this.mockMvc.perform(post(ApiEndpoints.QUERIES_API)
                        .secure(https)
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

        JsonNode resultNode = jsonNode.get("result");
        Map<String, Integer> resultMap = objectMapper.convertValue(resultNode,
                new TypeReference<>() {});

        Assertions.assertThat(resultMap.containsKey("r1")).isTrue();
        Assertions.assertThat(resultMap.get("r1")).isEqualTo(9);
    }

    @Test
    public void testQueryChainSuccessfulAllFieldsAverage() throws Exception {
        UUID userId = this.userDataRepository.save(this.testResearcher).getUserId();
        String jwt = this.jwtProvider.generateJwtToken(userId, UserRole.RESEARCHER, new Date());

        // Configure the Contract mock
        Contract contractMock = this.gateway.getNetwork("").getContract("", "");

        AverageResult averageResult = AverageResult.newBuilder().setResult(4.4).build();
        String average = String.format(".*%s.*", QueryType.AVERAGE);
        String averageResultJson = JsonFormat.printer().print(averageResult);
        Mockito.when(contractMock.evaluateTransaction(eq(this.queryContractName),
                        Mockito.matches(average)))
                .thenReturn(averageResultJson.getBytes());

        // Build the JSON query
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
                        .secure(https)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Assertions.assertThat(response.getHeader(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        String jsonResult = response.getContentAsString();
        JsonNode jsonNode = this.objectMapper.readTree(jsonResult);
        System.out.println(jsonNode.toPrettyString());
        Assertions.assertThat(jsonNode.has("result")).isTrue();

        double result = Double.parseDouble(jsonNode.get("result").asText());
        Assertions.assertThat(result).isEqualTo(4.4);
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
