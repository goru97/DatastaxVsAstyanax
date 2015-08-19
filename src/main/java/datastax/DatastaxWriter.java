package datastax;

import com.codahale.metrics.Meter;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Batch;
import com.codahale.metrics.Timer;
import com.datastax.driver.core.querybuilder.Insert;
import utils.Constants;
import utils.Metrics;

import java.util.Random;
import java.util.concurrent.Future;
/**
 * Created by GauravBajaj on 8/10/15.
 */
public class DatastaxWriter {

    private static final Timer batchWriteDurationTimer = Metrics.timer(DatastaxWriter.class, "Datastax Batch Write Duration");
    private static final Meter writeMeter = Metrics.meter(DatastaxWriter.class, "Datastax Write Operations");

    public static void writeBatchToDB(int size){
        Session session = DatastaxIO.getSession();
        final Timer.Context actualWriteCtx = batchWriteDurationTimer.time();
        Batch batch = DatastaxQueryBuilder.addDummyBatchOfMetrics(size);
        long datastaxStartTime = System.currentTimeMillis();
        Future future = session.executeAsync(batch);
        boolean listen = true;
       while(listen) {
            if (future.isDone()){
                writeMeter.mark();
                actualWriteCtx.stop();
                long datastaxEndTime = System.currentTimeMillis();
                System.out.println("Datastax Batch Write Execution Time " + (datastaxEndTime - datastaxStartTime));
                listen = false;
            }
        }
    }

    public void writeToDB(){
        Session session = DatastaxIO.getSession();
        Insert insert = DatastaxQueryBuilder.addDummyMetric();
        long datastaxStartTime = System.currentTimeMillis();
        session.execute(insert);
        long datastaxEndTime = System.currentTimeMillis();
        System.out.println("Datastax Write Execution Time " + (datastaxEndTime - datastaxStartTime));
        writeMeter.mark();
    }

    public static void writePrepareToDB(){
        Session session = DatastaxIO.getSession();
        //Insert insert = DatastaxQueryBuilder.addDummyMetric();
        String psInsert = "INSERT INTO \"DATA\".metrics_full (key, column1, value) VALUES (?,?,?)";
        PreparedStatement preparedStatement = session.prepare(psInsert);
        long datastaxStartTime = System.currentTimeMillis();
        session.execute(preparedStatement.bind("15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met."+(new Random().nextInt(20 -1) + 1), System.currentTimeMillis(), DatastaxSerializer.DoubleSerializer.serialize(new Random().nextDouble())));
        long datastaxEndTime = System.currentTimeMillis();
        System.out.println("Datastax Write Execution Time " + (datastaxEndTime - datastaxStartTime));
        writeMeter.mark();
    }
}
