package nl.tudelft.medtechchain.config;

import static org.mockito.ArgumentMatchers.anyString;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


/**
 * A configuration class for the Fabric Gateway.
 * The code is taken from:
 * <a href="https://hyperledger-fabric.readthedocs.io/en/latest/write_first_app.html">Hyperledger Fabric</a>
 * <a href="https://github.com/hyperledger/fabric-samples/tree/main/asset-transfer-basic/application-gateway-java">Fabric Samples</a>
 */
@Configuration
public class GatewayConfig {

    /**
     * Creates a Gateway connection to access any of the Networks (analogous to channels) accessible
     *   to the Fabric Gateway, and subsequently smart Contracts deployed to those networks.
     * A Gateway connection has three requirements:
     *    1. A gRPC connection to the Fabric Gateway
     *    2. Client identity used to transact with the network
     *    3. Signing implementation used to generate digital signatures for the client identity
     * See <a href="https://hyperledger-fabric.readthedocs.io/en/latest/write_first_app.html">Running a Fabric Application</a>.
     * The configuration properties are taken from the application.properties file.
     *
     * @param env                       the Spring environment (to access the defined properties)
     * @return                          the created Gateway bean, based on the configuration
     * @throws IOException              if something goes wrong during the creation of the gateway,
     *                                   (methods `newGrpcConnection`, `newIdentity`, `newSigner`)
     * @throws CertificateException     if something goes wrong during identity creation
     *                                    (client identity is used to transact with the network)
     * @throws InvalidKeyException      if something goes wrong during the signing process
     *                                    (generating digital signatures for the client identity)
     */
    @Bean
    @ConditionalOnProperty(name = "gateway.mock", havingValue = "false")
    public Gateway gateway(Environment env)
            throws IOException, CertificateException, InvalidKeyException {
        Path cryptoPath = Paths.get(env.getProperty("gateway.crypto-path", ""));
        Path certDirPath = cryptoPath
                .resolve(Paths.get(env.getProperty("gateway.cert-dir-path", "")));
        Path keyDirPath = cryptoPath
                .resolve(Paths.get(env.getProperty("gateway.key-dir-path", "")));
        Path tlsCertPath = cryptoPath
                .resolve(Paths.get(env.getProperty("gateway.tls-cert-path", "")));
        String mspId = env.getProperty("gateway.msp-id", "");
        String peerEndpoint = env.getProperty("gateway.peer-endpoint", "");
        String overrideAuth = env.getProperty("gateway.override-auth", "");

        // The gRPC client connection should be shared by all Gateway connections to this endpoint
        ManagedChannel channel = newGrpcConnection(tlsCertPath, peerEndpoint, overrideAuth);

        return Gateway
                .newInstance()
                .identity(newIdentity(certDirPath, mspId))
                .signer(newSigner(keyDirPath))
                .connection(channel)
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .connect();
    }

    /**
     * Creates a test mock for the Gateway object.
     *
     * @return      the created mock of the Gateway object
     */
    @Bean
    @ConditionalOnProperty(name = "gateway.mock", havingValue = "true")
    public Gateway getGateway() {
        Gateway gatewayMock = Mockito.mock(Gateway.class);
        Network networkMock = Mockito.mock(Network.class);
        Contract contractMock = Mockito.mock(Contract.class);

        Mockito.when(gatewayMock.getNetwork(anyString())).thenReturn(networkMock);
        Mockito.when(networkMock.getContract(anyString())).thenReturn(contractMock);
        Mockito.when(networkMock.getContract(anyString(), anyString())).thenReturn(contractMock);
        Mockito.when(networkMock.getName()).thenReturn("GatewayMock");

        return gatewayMock;
    }

    /**
     * Creates a gRPC connection using the TLS certificate of the signing certificate authority,
     *   so that the authenticity of the gateway's TLS certificate can be verified.
     * For a TLS connection to be successfully established, the endpoint address used by the client
     *   must match the address in the gateway's TLS certificate.
     * See <a href="https://hyperledger-fabric.readthedocs.io/en/latest/write_first_app.html">Running a Fabric Application</a>.
     *
     * @param tlsCertPath               the path to the peer tls certificate
     * @param peerEndpoint              the address of the peer endpoint (e.g. localhost:7051)
     * @param overrideAuth              force this endpoint address to be interpreted as the
     *                                    gateway's configured hostname
     * @return                          the created new gRPC connection
     * @throws IOException              if something goes wrong during the creation
     *                                    of TLS channel credentials
     */
    private ManagedChannel newGrpcConnection(Path tlsCertPath, String peerEndpoint,
                                             String overrideAuth) throws IOException {
        ChannelCredentials tlsCredentials = TlsChannelCredentials.newBuilder()
                .trustManager(tlsCertPath.toFile())
                .build();
        return Grpc.newChannelBuilder(peerEndpoint, tlsCredentials)
                .overrideAuthority(overrideAuth)
                .build();
    }

    /**
     * Creates a new client identity, which is used to transact with the network.
     *
     * @param certDirPath               the path to the user certificate
     * @param mspId                     ID of the Membership Service Provider
     * @return                          the created client identity
     * @throws IOException              if something goes wrong during certificate reading
     * @throws CertificateException     if something goes wrong during certificate reading
     */
    private Identity newIdentity(Path certDirPath, String mspId)
            throws IOException, CertificateException {
        try (BufferedReader reader = Files.newBufferedReader(this.getFirstFile(certDirPath))) {
            X509Certificate certificate = Identities.readX509Certificate(reader);
            return new X509Identity(mspId, certificate);
        }
    }

    /**
     * Creates a new signer, which is used to generate digital signatures for the client identity.
     *
     * @param keyDirPath                the path to the user private key directory
     * @return                          the created signer
     * @throws IOException              if something goes wrong during private key reading
     * @throws InvalidKeyException      if something goes wrong during private key reading
     */
    private Signer newSigner(Path keyDirPath) throws IOException, InvalidKeyException {
        try (BufferedReader reader = Files.newBufferedReader(this.getFirstFile(keyDirPath))) {
            PrivateKey privateKey = Identities.readPrivateKey(reader);
            return Signers.newPrivateKeySigner(privateKey);
        }
    }

    /**
     * Gets the path of the first file in the specified directory.
     *
     * @param dirPath           the directory path to files
     * @return                  the path to the first file
     * @throws IOException      if an I/O error occurs when opening the file
     */
    private Path getFirstFile(Path dirPath) throws IOException {
        try (Stream<Path> keyFiles = Files.list(dirPath)) {
            return keyFiles.findFirst().orElseThrow();
        }
    }
}
