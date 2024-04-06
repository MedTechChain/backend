package nl.medtechchain.controllers;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import nl.medtechchain.protos.query.Query;
import nl.medtechchain.protoutils.JsonToProtobufDeserializer;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.Network;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * A controller class that gets queries from researchers,
 * sends them to the chain and returns the result.
 * The code related to the Gateway API is taken from:
 * <a href="https://hyperledger-fabric.readthedocs.io/en/latest/write_first_app.html">Hyperledger Fabric</a>
 * <a href="https://github.com/hyperledger/fabric-samples/tree/main/asset-transfer-basic/application-gateway-java">Fabric Samples</a>
 */
@RestController
@RequestMapping(ApiEndpoints.QUERIES_API_PREFIX)
public class QueryController {

    private final Gateway gateway;

    private final Contract contract;

    @Value("${gateway.query-smart-contract-name}")
    private String queryContractName;

    /**
     * Creates a new QueryController object, which is used for sending queries to the blockchain.
     *
     * @param env     the Spring environment (to access the defined properties)
     * @param gateway the Fabric Gateway
     */
    public QueryController(Environment env, Gateway gateway) {
        this.gateway = gateway;

        // Get a network instance representing the channel where the smart contract is deployed
        Network network = gateway.getNetwork(env.getProperty("gateway.channel-name", ""));
        // Get the smart contract from the network
        this.contract = network.getContract(env.getProperty("gateway.chaincode-name", ""),
                env.getProperty("gateway.smart-contract-name", ""));
    }

    /**
     * Closes the gateway before the controller is destroyed.
     */
    @PreDestroy
    public void destroy() {
        System.out.println("Running a pre-destroy hook to close the gateway...");
        this.gateway.close();
        System.out.println("Successfully closed the gateway");
    }

    /**
     * Sends a query (written by a researcher) to the blockchain, and sends back the result.
     *
     * @param query                 the (protobuf) Query object to be sent to the blockchain
     * @param response              the HTTP response with the JWT that will be sent back
     * @throws IOException          if something goes wrong during the JSON deserialization process
     * @throws GatewayException     if something goes wrong when running the query
     */
    @PostMapping(ApiEndpoints.QUERIES)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void queryChain(@JsonDeserialize(using = JsonToProtobufDeserializer.class)
                           @RequestBody Query query,
                           HttpServletResponse response) throws IOException, GatewayException {

        String queryResult = this.runQuery(query);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(queryResult);
    }

    /**
     * Evaluates a transaction to run a query on the blockchain.
     *
     * @throws GatewayException               if something goes wrong when evaluating transaction
     * @throws InvalidProtocolBufferException if something goes wrong when parsing query result
     */
    private String runQuery(Query query) throws GatewayException, InvalidProtocolBufferException {
        System.out.printf("\n--> Evaluate Transaction:%n%s%n", query.toString());

        byte[] resultInBytes = this.contract.evaluateTransaction(this.queryContractName,
                JsonFormat.printer().print(query));

        String result = new String(resultInBytes, StandardCharsets.UTF_8);
        System.out.println("*** Result:\n" + result);

        return result;
    }
}
