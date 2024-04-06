package nl.medtechchain.protoutils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.Set;
import nl.medtechchain.protos.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * A custom deserializer for the Query (protobuf) object.
 * It is used when receiving a query request with JSON body
 *  which has to be forwarded to the blockchain.
 */
public class JsonToProtobufDeserializer extends JsonDeserializer<Query> {
    private final Set<String> requiredFields = Set.of("query_type");

    /**
     * Deserializes a JSON string to a Query (protobuf) object.
     *
     * @param jsonParser        a JSON parser with the received JSON body
     * @param ctx               the deserialization context (not used in the method)
     * @return                  the deserialized Query (protobuf) object
     * @throws IOException      when something goes wrong during the parsing process
     */
    @Override
    public Query deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        Query.Builder queryBuilder = Query.newBuilder();

        TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);

        for (String requiredField : requiredFields) {
            if (treeNode.get(requiredField) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Could not find the required field %s", requiredField));
            }
        }
        String json = treeNode.toString();

        JsonFormat.parser().merge(json, queryBuilder);
        return queryBuilder.build();
    }
}
