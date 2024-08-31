package nl.medtechchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.medtechchain.proto.devicedata.DeviceDataFieldType;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class InterfaceConfigurationDTO {
    private final List<String> validCountTargetFields;
    private final List<String> validGroupedCountTargetFields;
    private final List<String> validAverageTargetFields;
    private final List<Field> fields;
    private final Map<DeviceDataFieldType, List<String>> operators;

    @Data
    @AllArgsConstructor
    public static class Field {
        private final DeviceDataFieldType type;
        private final String name;
    }
}

