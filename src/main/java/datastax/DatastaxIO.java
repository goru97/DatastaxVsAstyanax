package datastax;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by GauravBajaj on 8/10/15.
 */
public class DatastaxIO {
    private static Session session;

    static {
        //connect();
    }
    public static void connect() //TODO: Make it private
    {
        String[] cassandra_hosts = new String[]{"127.0.0.1:9042"};
        Set<InetSocketAddress> contactPoints= new HashSet<InetSocketAddress>();
        for(String host:cassandra_hosts){
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host.split(":")[0], Integer.parseInt(host.split(":")[1]));
            contactPoints.add(inetSocketAddress);
        }

        final Cluster cluster = Cluster.builder()
                .withLoadBalancingPolicy(new DCAwareRoundRobinPolicy("datacenter1"))
                .withPoolingOptions(getPoolingOptions())
                .addContactPointsWithPorts(contactPoints)
                .build();
        final Metadata metadata = cluster.getMetadata();
        System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
        for (final Host host : metadata.getAllHosts())
        {
            System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
                    host.getDatacenter(), host.getAddress(), host.getRack());
        }
        try {
            session = cluster.connect();
        }
        catch (NoHostAvailableException e){
            e.printStackTrace();
        }
    }

    private static PoolingOptions getPoolingOptions(){
        final PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions
                .setCoreConnectionsPerHost(HostDistance.LOCAL,  4)
                .setMaxConnectionsPerHost(HostDistance.LOCAL, 10)
                .setCoreConnectionsPerHost(HostDistance.REMOTE, 2)
                .setMaxConnectionsPerHost(HostDistance.REMOTE, 4)
                .setHeartbeatIntervalSeconds(60); //Time after which driver will send a dummy request to the host so that the connection is not dropped by intermediate network devices (routers, firewalls…). The heartbeat interval should be set higher than SocketOptions.readTimeoutMillis
        return poolingOptions;
    }

    private void monitorConnection() { //TODO: Report the Connection Metrics to Graphite/Riemann
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        scheduled.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Session.State state = getSession().getState();
                for (Host host : state.getConnectedHosts()) {
                    int connections = state.getOpenConnections(host);
                    int inFlightQueries = state.getInFlightQueries(host);
                    System.out.printf("%s connections=%d current load=%d max load=%d%n",
                            host, connections, inFlightQueries, connections * 128);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }


    public static Session getSession() {
        return session;
    }
}
