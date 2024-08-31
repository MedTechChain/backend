package nl.medtechchain.protoutils;

import com.google.protobuf.Timestamp;
import lombok.Getter;
import nl.medtechchain.proto.config.PlatformConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class PlatformConfigWrapper {
    private final String id;
    private final Timestamp timestamp;
    private final Map<nl.medtechchain.proto.config.PlatformConfig.Config, String> map;

    public PlatformConfigWrapper(nl.medtechchain.proto.config.PlatformConfig configs) {
        this.id = configs.getId();
        this.timestamp = configs.getTimestamp();

        this.map = new HashMap<>();

        for (nl.medtechchain.proto.config.PlatformConfig.Entry e : configs.getMapList())
            map.put(e.getKey(), e.getValue());
    }

    public Optional<String> get(nl.medtechchain.proto.config.PlatformConfig.Config property) {
        return Optional.ofNullable(map.get(property));
    }

    public void override(nl.medtechchain.proto.config.PlatformConfig.Config property, String value) {
        map.put(property, value);
    }

    public String getUnsafe(nl.medtechchain.proto.config.PlatformConfig.Config property) {
        var o = Optional.ofNullable(map.get(property));
        if (o.isPresent())
            return o.get();

        throw new IllegalStateException("Config property not set: " + property.name());
    }

    public nl.medtechchain.proto.config.PlatformConfig toPlatformConfig() {
        return PlatformConfig.newBuilder()
                .setId(id)
                .setTimestamp(timestamp)
                .addAllMap(map.entrySet().stream().map(e -> PlatformConfig.Entry.newBuilder().setKey(e.getKey()).setValue(e.getValue()).build()).toList())
                .build();
    }
}
