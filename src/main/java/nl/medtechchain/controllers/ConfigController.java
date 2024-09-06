package nl.medtechchain.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.medtechchain.dto.InterfaceConfigurationDTO;
import nl.medtechchain.proto.common.ChaincodeResponse;
import nl.medtechchain.proto.config.PlatformConfig;
import nl.medtechchain.proto.config.UpdateNetworkConfig;
import nl.medtechchain.proto.config.UpdatePlatformConfig;
import nl.medtechchain.proto.devicedata.DeviceCategory;
import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.devicedata.DeviceDataFieldType;
import nl.medtechchain.proto.devicedata.MedicalSpeciality;
import nl.medtechchain.proto.query.Filter;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.protoutils.DeviceDataFieldTypeMapper;
import nl.medtechchain.services.ChaincodeService;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.GatewayException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static nl.medtechchain.protoutils.Base64EncodingOps.decode64;
import static nl.medtechchain.protoutils.JsonEncodingOps.parseJson;
import static nl.medtechchain.protoutils.JsonEncodingOps.printJson;


@RestController
@RequestMapping(ApiEndpoints.CONFIGS_API_PREFIX)
@RequiredArgsConstructor
public class ConfigController {

    private static final Logger logger = Logger.getLogger(QueryController.class.getName());

    private final ObjectMapper objectMapper;

    private final ChaincodeService chaincodeService;

    @GetMapping(ApiEndpoints.INTERFACE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void queryInterfaceConfiguration(HttpServletResponse response) throws IOException {
        var platformConfig = chaincodeService.getPlatformConfig();

        var validCountTargetFields = platformConfig.get(PlatformConfig.Config.CONFIG_FEATURE_QUERY_INTERFACE_COUNT_FIELDS);
        var validGroupedCountTargetFields = platformConfig.get(PlatformConfig.Config.CONFIG_FEATURE_QUERY_INTERFACE_GROUPED_COUNT_FIELDS);
        var validAverageTargetFields = platformConfig.get(PlatformConfig.Config.CONFIG_FEATURE_QUERY_INTERFACE_AVERAGE_FIELDS);

        var result = new InterfaceConfigurationDTO(
                validCountTargetFields.map(s -> List.of(s.split(","))).orElse(List.of()),
                validGroupedCountTargetFields.map(s -> List.of(s.split(","))).orElse(List.of()),
                validAverageTargetFields.map(s -> List.of(s.split(","))).orElse(List.of()),
                DeviceDataAsset.DeviceData.getDescriptor().getFields().stream().map(fd -> new InterfaceConfigurationDTO.Field(DeviceDataFieldTypeMapper.fromFieldName(fd.getName()), fd.getName())).toList(),
                Map.of(
                        DeviceDataFieldType.STRING, Stream.of(Filter.StringFilter.StringOperator.values()).filter(s -> s != Filter.StringFilter.StringOperator.UNRECOGNIZED && s != Filter.StringFilter.StringOperator.STRING_OPERATOR_UNSPECIFIED).map(Enum::name).toList(),
                        DeviceDataFieldType.INTEGER, Stream.of(Filter.IntegerFilter.IntOperator.values()).filter(s -> s != Filter.IntegerFilter.IntOperator.UNRECOGNIZED && s != Filter.IntegerFilter.IntOperator.INT_OPERATOR_UNSPECIFIED).map(Enum::name).toList(),
                        DeviceDataFieldType.BOOL, Stream.of(Filter.BoolFilter.BoolOperator.values()).filter(s -> s != Filter.BoolFilter.BoolOperator.UNRECOGNIZED && s != Filter.BoolFilter.BoolOperator.BOOL_OPERATOR_UNSPECIFIED).map(Enum::name).toList(),
                        DeviceDataFieldType.TIMESTAMP, Stream.of(Filter.TimestampFilter.TimestampOperator.values()).filter(s -> s != Filter.TimestampFilter.TimestampOperator.UNRECOGNIZED && s != Filter.TimestampFilter.TimestampOperator.TIMESTAMP_OPERATOR_UNSPECIFIED).map(Enum::name).toList(),
                        DeviceDataFieldType.DEVICE_CATEGORY, Stream.of(DeviceCategory.values()).filter(s -> s != DeviceCategory.UNRECOGNIZED && s != DeviceCategory.DEVICE_CATEGORY_UNSPECIFIED).map(Enum::name).toList(),
                        DeviceDataFieldType.MEDICAL_SPECIALITY, Stream.of(MedicalSpeciality.values()).filter(s -> s != MedicalSpeciality.UNRECOGNIZED && s != MedicalSpeciality.MEDICAL_SPECIALITY_UNSPECIFIED).map(Enum::name).toList()
                )
        );

        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @GetMapping(ApiEndpoints.PLATFORM)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void platformConfiguration(HttpServletResponse response) throws IOException {
        var platformConfig = chaincodeService.getPlatformConfig();

        for (PlatformConfig.Config c : PlatformConfig.Config.values()) {
            if (c == PlatformConfig.Config.UNRECOGNIZED || c == PlatformConfig.Config.CONFIG_UNSPECIFIED)
                continue;

            if (platformConfig.get(c).isEmpty())
                platformConfig.override(c, "NOT_SET");
        }

        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(platformConfig.toPlatformConfig()));
    }

    @PostMapping(ApiEndpoints.PLATFORM)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updatePlatformConfig(@RequestBody String body, HttpServletResponse response) throws IOException, GatewayException, CommitException {
        String queryResult = this.runUpdatePlatformConfig(parseJson(body, UpdatePlatformConfig.newBuilder()));
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(queryResult);
    }

    private String runUpdatePlatformConfig(UpdatePlatformConfig update) throws GatewayException, InvalidProtocolBufferException, CommitException {
        logger.info(String.format("\n--> Run Update config transaction:%n%s%n", update.toString()));
        var result = chaincodeService.submitUpdatePlatformConfig(update);

        if (result.getChaincodeResponseCase() == ChaincodeResponse.ChaincodeResponseCase.SUCCESS) {
            logger.info("*** Result:\n" + result);
            return printJson(decode64(result.getSuccess().getMessage(), UpdatePlatformConfig::parseFrom));
        }
        return printJson(result);
    }

    @GetMapping(ApiEndpoints.NETWORK)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void networkConfiguration(HttpServletResponse response) throws IOException {
        var networkConfig = chaincodeService.getNetworkConfig();
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(networkConfig));
    }

    @PostMapping(ApiEndpoints.NETWORK)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateNetworkConfig(@RequestBody String body, HttpServletResponse response) throws IOException, GatewayException, CommitException {
        String queryResult = this.runUpdateNetworkConfig(parseJson(body, UpdateNetworkConfig.newBuilder()));
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(queryResult);
    }

    private String runUpdateNetworkConfig(UpdateNetworkConfig update) throws GatewayException, InvalidProtocolBufferException, CommitException {
        logger.info(String.format("\n--> Run Update config transaction:%n%s%n", update.toString()));
        var result = chaincodeService.submitUpdateNetworkConfig(update);

        if (result.getChaincodeResponseCase() == ChaincodeResponse.ChaincodeResponseCase.SUCCESS) {
            logger.info("*** Result:\n" + result);
            return printJson(decode64(result.getSuccess().getMessage(), UpdateNetworkConfig::parseFrom));
        }
        return printJson(result);
    }
}

