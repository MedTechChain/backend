package nl.medtechchain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.medtechchain.protos.query.Query;
import nl.medtechchain.protoutils.JsonToProtobufDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration class for ObjectMapper. Here custom (de)serializers can be registered.
 * Note that configurable Jackson properties should be specified in the application.properties file.
 */
@Configuration
public class JacksonConfig {

    /**
     * Instantiates a bean ObjectMapper (from Jackson) for working with JSON.
     *
     * @return          the created ObjectMapper object
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Query.class, new JsonToProtobufDeserializer());

        objectMapper.registerModule(module);
        return objectMapper;
    }
}
