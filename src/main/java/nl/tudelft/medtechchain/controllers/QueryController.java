package nl.tudelft.medtechchain.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import nl.tudelft.medtechchain.jwt.JwtProvider;
import nl.tudelft.medtechchain.model.UserRole;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.Network;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 * A controller class that gets queries from researchers (or the admin) and sends them to the chain.
 * The code related to the Gateway API is taken from:
 * <a href="https://hyperledger-fabric.readthedocs.io/en/latest/write_first_app.html">Hyperledger Fabric</a>
 * <a href="https://github.com/hyperledger/fabric-samples/tree/main/asset-transfer-basic/application-gateway-java">Fabric Samples</a>
 */
@RestController
@RequestMapping("/api/queries")
public class QueryController {

    private final JwtProvider jwtProvider;

    private final ObjectMapper objectMapper;

    private final Contract contract;

    /**
     * Creates a new QueryController object, which is used for sending queries to the blockchain.
     *
     * @param env               the Spring environment (to access the defined properties)
     * @param gateway           the Fabric Gateway
     * @param jwtProvider       the JWT provider object, used for parsing and validating JWTs
     * @param objectMapper      the ObjectMapper object, used to read/write JSON strings
     */
    public QueryController(Environment env, Gateway gateway, JwtProvider jwtProvider,
                           ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;

        // Get a network instance representing the channel where the smart contract is deployed.
        Network network = gateway.getNetwork(env.getProperty("gateway.channel-name"));
        // Get the smart contract from the network.
        this.contract = network.getContract(env.getProperty("gateway.chaincode-name"));
    }

    /**
     * Sends a query (written by a researcher or the admin) to the chain.
     *
     * @param request           the received HTTP request
     * @param response          the HTTP response with the JWT that will be sent back
     * @throws IOException      if something goes wrong during the JSON deserialization process
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void queryChain(HttpServletRequest request,
                           HttpServletResponse response) throws IOException, GatewayException {
        Jws<Claims> jwtClaims = this.jwtProvider.resolveJwtClaims(request);
        if (this.jwtProvider.getRole(jwtClaims) == UserRole.UNKNOWN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation not allowed");
        }

        // TODO: create a more general API
        String version;
        try {
            version = request.getParameter("version");  // e.g. v0.0.1
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Return all the current assets on the ledger.
        String result = this.getAllAssets(version);

        // Query the chain
        String responseBody = this.objectMapper.createObjectNode().put("result", result).toString();
        response.getWriter().write(responseBody);
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    }

    /**
     * Evaluates a transaction to query ledger state.
     * The transaction (CountFirmwareVersionGreaterEqualThan) returns the number of devices with
     *  firmware version greater than or equal to the specified version.
     *
     * @throws GatewayException             if something goes wrong during transaction evaluation
     * @throws JsonProcessingException      if something goes wrong during JSON reading
     */
    private String getAllAssets(String version) throws GatewayException, JsonProcessingException {
        System.out.println("\n--> Evaluate Transaction: CountFirmwareVersionGreaterEqualThan,"
                + "function returns the number of devices with firmware version greater than or"
                + "equal to the specified version");

        byte[] resultInBytes = this.contract.evaluateTransaction("CountFirmwareVersionGreaterEqualThan", version);
        String result = new String(resultInBytes, StandardCharsets.UTF_8);

        System.out.println("*** Result: " + prettyJson(result));
        return result;
    }

    /**
     * A helper method used to print a JSON byte array in the pretty format.
     *
     * @param json                          the byte array with JSON that has to be pretty-printed
     * @return                              the pretty-printed JSON string
     * @throws JsonProcessingException      if something goes wrong during JSON reading
     */
    private String prettyJson(final byte[] json) throws JsonProcessingException {
        return this.prettyJson(new String(json, StandardCharsets.UTF_8));
    }

    /**
     * A helper method used to print a JSON string in the pretty format.
     *
     * @param json                          the string with JSON that has to be pretty-printed
     * @return                              the pretty-printed JSON string
     * @throws JsonProcessingException      if something goes wrong during JSON reading
     */
    private String prettyJson(final String json) throws JsonProcessingException {
        Object jsonObject = this.objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
    }
}
