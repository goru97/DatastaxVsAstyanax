package datastax;

import com.codahale.metrics.Meter;
import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import utils.*;

import java.net.InetSocketAddress;
import java.util.Collection;
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
    private static  Cluster cluster;
    private final static Meter hostMeter = utils.Metrics.meter(DatastaxIO.class, "Hosts Connected");
    private final static Meter openConnectionsMeter = utils.Metrics.meter(DatastaxIO.class, "Total Open Connections");
    private final static Meter inFlightQueriesMeter = utils.Metrics.meter(DatastaxIO.class, "Total InFlight Queries");
    private final static Meter trashedConnectionsMeter = utils.Metrics.meter(DatastaxIO.class, "Total Trashed Connections");
    private final static Meter maxLoadMeter = utils.Metrics.meter(DatastaxIO.class, "Maximum Load");

    static {
        connect();
        monitorConnection();
    }
    private static void connect()
    {
        String[] cassandra_hosts = new String[]{Constants.DATASTAX_HOSTS};
        Set<InetSocketAddress> contactPoints= new HashSet<InetSocketAddress>();
        for(String host:cassandra_hosts){
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host.split(":")[0], Integer.parseInt(host.split(":")[1]));
            contactPoints.add(inetSocketAddress);
        }

        cluster = Cluster.builder()
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

    private static void monitorConnection() {
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        scheduled.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Session.State state = getSession().getState();
                Collection<Host> hosts = state.getConnectedHosts();
                int totalHosts = hosts.size();
                long totalOpenConnections = 0;
                long totalInFlightQueries = 0;
                long totalTrashedConnections = 0;
                long totalMaxLoad =0;
                for (Host host : hosts) {
                    int openConnections = state.getOpenConnections(host);
                    int inFlightQueries = state.getInFlightQueries(host);
                    int trashedConnections = state.getTrashedConnections(host);
                    int maxLoad = openConnections * 128;
                    totalOpenConnections += openConnections;
                    totalInFlightQueries += inFlightQueries;
                    totalTrashedConnections += trashedConnections;
                    totalMaxLoad += maxLoad;
                    /*System.out.printf("%s connections=%d current load=%d max load=%d%n",
                            host, openConnections, inFlightQueries, openConnections * 128);*/
                }

                hostMeter.mark(totalHosts);
                openConnectionsMeter.mark(totalOpenConnections);
                inFlightQueriesMeter.mark(totalInFlightQueries);
                trashedConnectionsMeter.mark(totalTrashedConnections);
                maxLoadMeter.mark(totalMaxLoad);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    public void close() { //Not to be used with time-series data.
        session.close();
        cluster.close();
    }

    public static Session getSession() {
        return session;
    }
}
