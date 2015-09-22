package datastax;

import com.codahale.metrics.Meter;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Batch;
import com.codahale.metrics.Timer;
import com.datastax.driver.core.querybuilder.Insert;
import utils.Constants;
import utils.Metrics;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
/**
 * Created by GauravBajaj on 8/10/15.
 */
public class DatastaxWriter {

    private static final Timer batchWriteDurationTimer = Metrics.timer(DatastaxWriter.class, "Datastax Batch Write Duration");
    private static final Meter writeMeter = Metrics.meter(DatastaxWriter.class, "Datastax Write Operations");
    private static final Session session = DatastaxIO.getSession();
    private static final String psInsert = "INSERT INTO \"DATA\".metrics_full (key, column1, value) VALUES (?,?,?)";
    private static final PreparedStatement preparedStatement = session.prepare(psInsert);

    public static void writeBatchToDB(int size){
        Session session = DatastaxIO.getSession();
        final Timer.Context actualWriteCtx = batchWriteDurationTimer.time();
        Batch batch = DatastaxQueryBuilder.addDummyBatchOfMetrics(size);
        long datastaxStartTime = System.currentTimeMillis();
        ResultSetFuture future = session.executeAsync(batch);
        boolean listen = true;
        try {
           ResultSet rs = future.get();
            writeMeter.mark();
            actualWriteCtx.stop();
            long datastaxEndTime = System.currentTimeMillis();
            System.out.println("Datastax Batch Write Execution Time " + (datastaxEndTime - datastaxStartTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void writeToDB(){
        Session session = DatastaxIO.getSession();
        final Timer.Context actualWriteCtx = batchWriteDurationTimer.time();
        Insert insert = DatastaxQueryBuilder.addDummyMetric();
        long datastaxStartTime = System.currentTimeMillis();
        session.executeAsync(insert);
        writeMeter.mark();
        actualWriteCtx.stop();
        long datastaxEndTime = System.currentTimeMillis();
        System.out.println("Datastax Write Execution Time " + (datastaxEndTime - datastaxStartTime));

    }

    public static void writePrepareToDB(){
        Session session = DatastaxIO.getSession();
        final Timer.Context actualWriteCtx = batchWriteDurationTimer.time();
        long datastaxStartTime = System.currentTimeMillis();
        session.executeAsync(preparedStatement.bind("15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met." + (new Random().nextInt(20 - 1) + 1), System.currentTimeMillis(), DatastaxSerializer.DoubleSerializer.serialize(new Random().nextDouble())));
        writeMeter.mark();
        actualWriteCtx.stop();
        long datastaxEndTime = System.currentTimeMillis();
        System.out.println("Datastax Write Execution Time " + (datastaxEndTime - datastaxStartTime));
    }
}
