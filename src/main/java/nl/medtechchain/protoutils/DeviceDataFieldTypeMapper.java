package nl.medtechchain.protoutils;

import nl.medtechchain.proto.devicedata.DeviceDataAsset;
import nl.medtechchain.proto.devicedata.DeviceDataFieldType;

import java.util.Optional;

import static nl.medtechchain.proto.devicedata.DeviceDataFieldType.*;

public class DeviceDataFieldTypeMapper {

    public static DeviceDataFieldType fromFieldName(String name) {
        // all fields are wrapped in a protobuf message
        var descriptor = Optional.ofNullable(DeviceDataAsset.DeviceData.getDescriptor().findFieldByName(name));
        return descriptor.map(fieldDescriptor -> switch (fieldDescriptor.getMessageType().getFullName()) {
            case "devicedata.DeviceDataAsset.StringField" -> STRING;
            case "devicedata.DeviceDataAsset.TimestampField" -> TIMESTAMP;
            case "devicedata.DeviceDataAsset.IntegerField" -> INTEGER;
            case "devicedata.DeviceDataAsset.BoolField" -> BOOL;
            case "devicedata.DeviceDataAsset.MedicalSpecialityField" -> MEDICAL_SPECIALITY;
            case "devicedata.DeviceDataAsset.DeviceCategoryField" -> DEVICE_CATEGORY;
            default -> DEVICE_DATA_FIELD_TYPE_UNSPECIFIED;
        }).orElse(UNRECOGNIZED);

    }
}
