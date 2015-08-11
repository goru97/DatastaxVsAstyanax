package astyanax;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolType;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.retry.RetryNTimes;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by GauravBajaj on 8/10/15.
 */
public class AstyanaxIO {

    private static final AstyanaxContext<Keyspace> context;
    private static final Keyspace keyspace;

    static {
        context = createPreferredHostContext();
        context.start();
        keyspace = context.getEntity();
    }

    protected AstyanaxIO() {
    }

    private static AstyanaxContext<Keyspace> createPreferredHostContext() {
        return createCustomHostContext(createPreferredAstyanaxConfiguration(), createPreferredConnectionPoolConfiguration());
    }

    private static AstyanaxContext<Keyspace> createCustomHostContext(AstyanaxConfigurationImpl configuration,
                                                                     ConnectionPoolConfigurationImpl connectionPoolConfiguration) {
        return new AstyanaxContext.Builder()
                .forCluster("Test Cluster")
                .forKeyspace("DATA")
                .withAstyanaxConfiguration(configuration)
                .withConnectionPoolConfiguration(connectionPoolConfiguration)
                .buildKeyspace(ThriftFamilyFactory.getInstance());
    }

    private static AstyanaxConfigurationImpl createPreferredAstyanaxConfiguration() {
        AstyanaxConfigurationImpl astyconfig = new AstyanaxConfigurationImpl()
                .setDiscoveryType(NodeDiscoveryType.NONE)
                .setConnectionPoolType(ConnectionPoolType.ROUND_ROBIN);

        int numRetries = 5;
        if (numRetries > 0) {
            astyconfig.setRetryPolicy(new RetryNTimes(numRetries));
        }

        return astyconfig;
    }

    private static ConnectionPoolConfigurationImpl createPreferredConnectionPoolConfiguration() {
        int port = 9160;
        Set<String> uniqueHosts = new HashSet<String>();
        Collections.addAll(uniqueHosts, "127.0.0.1:9160".split(","));
        int numHosts = uniqueHosts.size();
        int maxConns = 75;
        int timeout = 10000;

        int connsPerHost = maxConns / numHosts + (maxConns % numHosts == 0 ? 0 : 1);
        // This timeout effectively results in waiting a maximum of (timeoutWhenExhausted / numHosts) on each Host
        int timeoutWhenExhausted = 2000;
        timeoutWhenExhausted = Math.max(timeoutWhenExhausted, 1 * numHosts); // Minimum of 1ms per host

        final ConnectionPoolConfigurationImpl connectionPoolConfiguration = new ConnectionPoolConfigurationImpl("MyConnectionPool")
                .setPort(port)
                .setSocketTimeout(timeout)
                .setInitConnsPerHost(connsPerHost)
                .setMaxConnsPerHost(connsPerHost)
                .setMaxBlockedThreadsPerHost(5)
                .setMaxTimeoutWhenExhausted(timeoutWhenExhausted)
                .setInitConnsPerHost(connsPerHost / 2)
                .setSeeds("127.0.0.1:9160");
        return connectionPoolConfiguration;
    }

    public static Keyspace getKeyspace(){
        return keyspace;
    }
}
