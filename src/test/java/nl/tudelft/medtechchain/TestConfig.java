package nl.tudelft.medtechchain;

import static org.mockito.ArgumentMatchers.anyString;

import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;


@Configuration
public class TestConfig {

    /**
     * Creates a test mock for the JavaMailSender object.
     *
     * @return      the created mock of the JavaMailSender object
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        return Mockito.mock(JavaMailSender.class);
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
}