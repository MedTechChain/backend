package nl.tudelft.medtechchain;

import io.grpc.CallOptions;
import java.util.function.UnaryOperator;
import org.hyperledger.fabric.client.BlockAndPrivateDataEventsRequest;
import org.hyperledger.fabric.client.BlockEventsRequest;
import org.hyperledger.fabric.client.ChaincodeEvent;
import org.hyperledger.fabric.client.ChaincodeEventsRequest;
import org.hyperledger.fabric.client.CloseableIterator;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.FilteredBlockEventsRequest;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.hyperledger.fabric.protos.common.Block;
import org.hyperledger.fabric.protos.peer.BlockAndPrivateData;
import org.hyperledger.fabric.protos.peer.FilteredBlock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;


@Configuration
public class TestConfig {

    /**
     * Creates a test mock for JavaMailSender object.
     *
     * @return      the created mock of the JavaMailSender object
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    /**
     * Creates a test mock for Gateway object.
     *
     * @return      the created mock of the Gateway object
     */
    @Bean
    @ConditionalOnProperty(name = "gateway.mock", havingValue = "true")
    public Gateway getGateway() {
        Gateway gatewayMock = Mockito.mock(Gateway.class);
        Mockito.when(gatewayMock.getNetwork(Mockito.any())).thenReturn(new Network() {
            @Override
            public Contract getContract(String s) {
                return null;
            }

            @Override
            public Contract getContract(String s, String s1) {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public CloseableIterator<ChaincodeEvent> getChaincodeEvents(
                    String string, UnaryOperator<CallOptions> unaryOperator) {
                return null;
            }

            @Override
            public ChaincodeEventsRequest.Builder newChaincodeEventsRequest(String string) {
                return null;
            }

            @Override
            public CloseableIterator<Block> getBlockEvents(
                    UnaryOperator<CallOptions> unaryOperator) {
                return null;
            }

            @Override
            public BlockEventsRequest.Builder newBlockEventsRequest() {
                return null;
            }

            @Override
            public CloseableIterator<FilteredBlock> getFilteredBlockEvents(
                    UnaryOperator<CallOptions> unaryOperator) {
                return null;
            }

            @Override
            public FilteredBlockEventsRequest.Builder newFilteredBlockEventsRequest() {
                return null;
            }

            @Override
            public CloseableIterator<BlockAndPrivateData> getBlockAndPrivateDataEvents(
                    UnaryOperator<CallOptions> unaryOperator) {
                return null;
            }

            @Override
            public BlockAndPrivateDataEventsRequest.Builder newBlockAndPrivateDataEventsRequest() {
                return null;
            }
        });
        return gatewayMock;
    }
}