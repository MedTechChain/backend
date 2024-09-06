package nl.medtechchain.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import nl.medtechchain.proto.config.NetworkConfig;
import nl.medtechchain.proto.config.PlatformConfig;
import nl.medtechchain.proto.config.UpdateNetworkConfig;
import nl.medtechchain.proto.config.UpdatePlatformConfig;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.proto.query.QueryAsset;
import nl.medtechchain.proto.query.ReadQueryAssetPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static nl.medtechchain.protoutils.JsonEncodingOps.parseJson;
import static nl.medtechchain.protoutils.JsonEncodingOps.printJson;

/**
 * A configuration class for ObjectMapper. Here custom (de)serializers can be registered.
 * Note that configurable Jackson properties should be specified in the application.properties file.
 */
@Configuration
public class JacksonConfig {

    /**
     * Instantiates a bean ObjectMapper (from Jackson) for working with JSON.
     *
     * @return the created ObjectMapper object
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module = addCodec(module, Query.class, Query.newBuilder());
        module = addCodec(module, QueryAsset.class, QueryAsset.newBuilder());
        module = addCodec(module, ReadQueryAssetPage.class, ReadQueryAssetPage.newBuilder());
        module = addCodec(module, PlatformConfig.class, PlatformConfig.newBuilder());
        module = addCodec(module, UpdatePlatformConfig.class, UpdatePlatformConfig.newBuilder());
        module = addCodec(module, NetworkConfig.class, NetworkConfig.newBuilder());
        module = addCodec(module, UpdateNetworkConfig.class, UpdateNetworkConfig.newBuilder());

        objectMapper.registerModule(module);
        return objectMapper;
    }

    private <T extends GeneratedMessageV3> SimpleModule addCodec(SimpleModule module, Class<T> cls, Message.Builder builder) {
        return module.addSerializer(cls, new JsonSerializer<>() {
            public void serialize(T o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeRawValue(printJson(o));
            }
        }).addDeserializer(cls, new JsonDeserializer<>() {
            public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return parseJson(jsonParser.getValueAsString(), builder);
            }
        });
    }
}
