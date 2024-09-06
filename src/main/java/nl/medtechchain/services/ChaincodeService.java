package nl.medtechchain.services;

import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.annotation.PreDestroy;
import nl.medtechchain.proto.common.ChaincodeResponse;
import nl.medtechchain.proto.config.NetworkConfig;
import nl.medtechchain.proto.config.PlatformConfig;
import nl.medtechchain.proto.config.UpdateNetworkConfig;
import nl.medtechchain.proto.config.UpdatePlatformConfig;
import nl.medtechchain.proto.query.Query;
import nl.medtechchain.proto.query.QueryAsset;
import nl.medtechchain.proto.query.QueryAssetPage;
import nl.medtechchain.proto.query.ReadQueryAssetPage;
import nl.medtechchain.protoutils.PlatformConfigWrapper;
import org.hyperledger.fabric.client.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import static nl.medtechchain.protoutils.Base64EncodingOps.decode64;
import static nl.medtechchain.protoutils.Base64EncodingOps.encode64;

@Service
public class ChaincodeService {


    private static final Logger logger = Logger.getLogger(ChaincodeService.class.getName());

    private final Gateway gateway;
    private final Contract deviceDataContract;
    private final Contract configContract;

    public ChaincodeService(Environment env, Gateway gateway) {
        this.gateway = gateway;
        Network network = gateway.getNetwork(env.getProperty("gateway.channel-name"));
        this.deviceDataContract = network.getContract(env.getProperty("gateway.chaincode-name"),
                env.getProperty("gateway.data-contract-name"));

        this.configContract = network.getContract(env.getProperty("gateway.chaincode-name"),
                env.getProperty("gateway.config-contract-name"));
    }

    public PlatformConfigWrapper getPlatformConfig() {
        try {
            var response = configContract.evaluateTransaction("GetPlatformConfig");
            var chaincodeResponse = decode64(response, ChaincodeResponse::parseFrom);
            if (chaincodeResponse.getChaincodeResponseCase() == ChaincodeResponse.ChaincodeResponseCase.SUCCESS) {
                return new PlatformConfigWrapper(decode64(chaincodeResponse.getSuccess().getMessage(), PlatformConfig::parseFrom));
            }

            if (chaincodeResponse.getChaincodeResponseCase() == ChaincodeResponse.ChaincodeResponseCase.ERROR)
                throw new IllegalStateException(chaincodeResponse.getError().toString());

            throw new IllegalStateException("Unrecognized chaincode response");

        } catch (Throwable e) {
            logger.severe("Cannot retrieve platform config: " + e);
            throw new IllegalStateException("Cannot retrieve platform config:", e);
        }
    }

    public NetworkConfig getNetworkConfig() {
        try {
            var response = configContract.evaluateTransaction("GetNetworkConfig");
            var chaincodeResponse = decode64(response, ChaincodeResponse::parseFrom);
            if (chaincodeResponse.getChaincodeResponseCase() == ChaincodeResponse.ChaincodeResponseCase.SUCCESS) {
                return decode64(chaincodeResponse.getSuccess().getMessage(), NetworkConfig::parseFrom);
            }

            if (chaincodeResponse.getChaincodeResponseCase() == ChaincodeResponse.ChaincodeResponseCase.ERROR)
                throw new IllegalStateException(chaincodeResponse.getError().toString());

            throw new IllegalStateException("Unrecognized chaincode response");

        } catch (Throwable e) {
            logger.severe("Cannot retrieve network config: " + e);
            throw new IllegalStateException("Cannot retrieve network config:", e);
        }
    }


    public ChaincodeResponse submitQuery(Query query) throws EndorseException, CommitException, SubmitException, CommitStatusException, InvalidProtocolBufferException {
        return decode64(this.deviceDataContract.submitTransaction("Query", encode64(query)), ChaincodeResponse::parseFrom);
    }

    public ChaincodeResponse submitUpdatePlatformConfig(UpdatePlatformConfig updatePlatformConfig) throws EndorseException, CommitException, SubmitException, CommitStatusException, InvalidProtocolBufferException {
        return decode64(this.configContract.submitTransaction("UpdatePlatformConfig", encode64(updatePlatformConfig)), ChaincodeResponse::parseFrom);
    }

    public ChaincodeResponse submitUpdateNetworkConfig(UpdateNetworkConfig updateNetworkConfig) throws EndorseException, CommitException, SubmitException, CommitStatusException, InvalidProtocolBufferException {
        return decode64(this.configContract.submitTransaction("UpdateNetworkConfig", encode64(updateNetworkConfig)), ChaincodeResponse::parseFrom);
    }

    public List<QueryAsset> readQueries() throws IOException, GatewayException {
        var result = new ArrayList<QueryAsset>();

        var pageSize = 100;
        var pageNumber = 1;

        while (true) {
            var readPage = ReadQueryAssetPage.newBuilder().setPageNumber(pageNumber).setPageSize(pageSize).build();
            pageNumber++;
            var readPageResponse = decode64(this.deviceDataContract.evaluateTransaction("ReadQueries", encode64(readPage)), QueryAssetPage::parseFrom);
            result.addAll(readPageResponse.getAssetsList());
            if (readPageResponse.getAssetsList().size() < pageSize)
                break;
        }
        return result;
    }

    @PreDestroy
    public void destroy() {
        logger.info("Running a pre-destroy hook to close the gateway...");
        this.gateway.close();
        logger.info("Successfully closed the gateway");
    }
}
