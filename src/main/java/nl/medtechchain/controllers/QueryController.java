package nl.medtechchain.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.medtechchain.proto.common.ChaincodeResponse;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.proto.query.QueryResult;
import nl.medtechchain.services.AuthenticationService;
import nl.medtechchain.services.ChaincodeService;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.GatewayException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.logging.Logger;

import static nl.medtechchain.protoutils.Base64EncodingOps.decode64;
import static nl.medtechchain.protoutils.JsonEncodingOps.parseJson;
import static nl.medtechchain.protoutils.JsonEncodingOps.printJson;

@RestController
@RequestMapping(ApiEndpoints.QUERIES_API_PREFIX)
@RequiredArgsConstructor
public class QueryController {

    private static final Logger logger = Logger.getLogger(QueryController.class.getName());

    private final ObjectMapper objectMapper;

    private final AuthenticationService authenticationService;

    private final ChaincodeService chaincodeService;

    @PostMapping(ApiEndpoints.QUERIES)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void queryChain(@RequestBody String body, HttpServletResponse response) throws IOException, GatewayException, CommitException {
        String queryResult = this.runQuery(parseJson(body, Query.newBuilder()));
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(queryResult);
    }

    private String runQuery(Query query) throws GatewayException, InvalidProtocolBufferException, CommitException {
        logger.info(String.format("\n--> Evaluate Transaction:%n%s%n", query.toString()));
        query = query.toBuilder().setSubmitter(authenticationService.currentUserName()).build();
        var result = chaincodeService.submitQuery(query);

        if (result.getChaincodeResponseCase() == ChaincodeResponse.ChaincodeResponseCase.SUCCESS) {
            logger.info("*** Result:\n" + result);
            return printJson(decode64(result.getSuccess().getMessage(), QueryResult::parseFrom));
        }
        System.out.println(printJson(result));
        return printJson(result);
    }

    @GetMapping(ApiEndpoints.READ)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void readQueries(HttpServletResponse response) throws IOException, GatewayException {
        var result = chaincodeService.readQueries();
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        System.out.println(decode64("CAA=", QueryResult::parseFrom));
    }
}

