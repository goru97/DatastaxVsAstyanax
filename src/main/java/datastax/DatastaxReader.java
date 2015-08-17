package datastax;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Select;
import utils.Metrics;

/**
 * Created by GauravBajaj on 8/15/15.
 */
public class DatastaxReader {
    private final static Timer batchReadDurationTimer = Metrics.timer(DatastaxReader.class, "Datastax Batch Read Duration");
    private final static Meter readMeter = Metrics.meter(DatastaxReader.class, "Datastax Read Operations");
    public static void readMetrics(int limit){
        Session session = DatastaxIO.getSession();
        final Timer.Context actualReadCtx = batchReadDurationTimer.time();
        Select select = DatastaxQueryBuilder.getDummyMetrics(limit);
        long datastaxStartTime = System.currentTimeMillis();
        ResultSetFuture future = session.executeAsync(select);
        boolean listen = true;
        while(listen) {
            if (future.isDone()){
                readMeter.mark();
                actualReadCtx.stop();
                long datastaxEndTime = System.currentTimeMillis();
                System.out.println("Datastax Batch Read Execution Time " + (datastaxEndTime - datastaxStartTime));
                listen = false;
            }
        }
    }
}