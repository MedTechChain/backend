package nl.tudelft.medtechchain.config;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * A configuration class for the Fabric Gateway.
 * The code is taken from:
 * <a href="https://hyperledger-fabric.readthedocs.io/en/latest/write_first_app.html">Hyperledger Fabric</a>
 * <a href="https://github.com/hyperledger/fabric-samples/tree/main/asset-transfer-basic/application-gateway-java">Fabric Samples</a>
 */
@Configuration
public class GatewayConfig {
    private static final String MSP_ID =
            System.getenv().getOrDefault("MSP_ID", "Org1MSP");

    // Path to crypto materials.
    private static final Path CRYPTO_PATH =
            Paths.get("../../test-network/organizations/peerOrganizations/org1.example.com");

    // Path to user certificate.
    private static final Path CERT_DIR_PATH =
            CRYPTO_PATH.resolve(Paths.get("users/User1@org1.example.com/msp/signcerts"));

    // Path to user private key directory.
    private static final Path KEY_DIR_PATH =
            CRYPTO_PATH.resolve(Paths.get("users/User1@org1.example.com/msp/keystore"));

    // Path to peer tls certificate.
    private static final Path TLS_CERT_PATH =
            CRYPTO_PATH.resolve(Paths.get("peers/peer0.org1.example.com/tls/ca.crt"));

    // Gateway peer end point.
    private static final String PEER_ENDPOINT = "localhost:7051";
    private static final String OVERRIDE_AUTH = "peer0.org1.example.com";

    /**
     * Creates the Fabric Gateway bean for the blockchain, based on the specified configuration.
     *
     * @return                          the created Gateway bean, based on the configuration
     * @throws IOException              if something goes wrong during the creation of the gateway,
     *                                   (methods `newGrpcConnection`, `newIdentity`, `newSigner`)
     * @throws InterruptedException     if something goes wrong during the channel shutdown
     * @throws CertificateException     if something goes wrong during identity creation
     *                                    (client identity is used to transact with the network)
     * @throws InvalidKeyException      if something goes wrong during the signing process
     *                                    (generating digital signatures for the client identity)
     */
    @Bean
    public Gateway gateway() throws IOException, InterruptedException,
            CertificateException, InvalidKeyException {
        // The gRPC client connection should be shared by all Gateway connections to this endpoint
        var channel = newGrpcConnection();

        var builder = Gateway
                .newInstance()            // Create new Gateway
                .identity(newIdentity())  // Client identity is used to transact with the network
                .signer(newSigner())      // Generating digital signatures for the client identity
                .connection(channel)
                // Default timeouts for different gRPC calls
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

        try {
            return builder.connect();
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /**
     * Creates a new gRPC connection to the Fabric Gateway.
     *
     * @return                          the created new gRPC connection
     * @throws IOException              if something goes wrong during the creation
     *                                    of TLS channel credentials
     */
    private static ManagedChannel newGrpcConnection() throws IOException {
        var credentials = TlsChannelCredentials.newBuilder()
                .trustManager(TLS_CERT_PATH.toFile())
                .build();
        return Grpc.newChannelBuilder(PEER_ENDPOINT, credentials)
                .overrideAuthority(OVERRIDE_AUTH)
                .build();
    }

    /**
     * Creates a new client identity, which is used to transact with the network.
     *
     * @return                          the created client identity
     * @throws IOException              if something goes wrong during certificate reading
     * @throws CertificateException     if something goes wrong during certificate reading
     */
    private static Identity newIdentity() throws IOException, CertificateException {
        try (var certReader = Files.newBufferedReader(getFirstFilePath(CERT_DIR_PATH))) {
            var certificate = Identities.readX509Certificate(certReader);
            return new X509Identity(MSP_ID, certificate);
        }
    }

    /**
     * Creates a new signer, which is used to generate digital signatures for the client identity.
     *
     * @return                          the created signer
     * @throws IOException              if something goes wrong during private key reading
     * @throws InvalidKeyException      if something goes wrong during private key reading
     */
    private static Signer newSigner() throws IOException, InvalidKeyException {
        try (var keyReader = Files.newBufferedReader(getFirstFilePath(KEY_DIR_PATH))) {
            var privateKey = Identities.readPrivateKey(keyReader);
            return Signers.newPrivateKeySigner(privateKey);
        }
    }

    /**
     * Gets the path of the first file.
     *
     * @param dirPath           the directory path to files
     * @return                  the path to the first file
     * @throws IOException      if an I/O error occurs when opening the file
     */
    private static Path getFirstFilePath(Path dirPath) throws IOException {
        try (var keyFiles = Files.list(dirPath)) {
            return keyFiles.findFirst().orElseThrow();
        }
    }
}
