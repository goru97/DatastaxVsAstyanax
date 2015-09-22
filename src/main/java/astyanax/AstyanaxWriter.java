package astyanax;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.DoubleSerializer;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import utils.Metrics;

import java.util.Random;
import java.util.concurrent.Future;

/**
 * Created by GauravBajaj on 8/10/15.
 */
public class AstyanaxWriter {
    private final static Timer batchWriteDurationTimer = Metrics.timer(AstyanaxWriter.class, "Astyanax Batch Write Duration");
    private final static Meter writeMeter = Metrics.meter(AstyanaxWriter.class, "Astyanax Write Operations");
    private static int ttl = 172800; //Two days
    public static void writeBatchToDB(int size) {
        MutationBatch mb = AstyanaxIO.getKeyspace().prepareMutationBatch();
        for (int i=size; i<(2*size);i++) {
            String metricName = "15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met."+ i;
            mb.withRow(new ColumnFamily("metrics_full", StringSerializer.get(),LongSerializer.get()), metricName)
                    .putColumn(System.currentTimeMillis(), new Random().nextDouble(), DoubleSerializer.get(), ttl);
        }
        try {
            final Timer.Context actualWriteCtx = batchWriteDurationTimer.time();
            long astyanaxStartTime = System.currentTimeMillis();
            //mb.execute();
            Future future = mb.executeAsync();
            boolean listen = true;
            while(listen) {
                if (future.isDone()) {
                    writeMeter.mark();
                    actualWriteCtx.stop();
                    long astyanaxEndTime = System.currentTimeMillis();
                    System.out.println("Astyanax Batch Write Execution Time " + (astyanaxEndTime - astyanaxStartTime));
                    listen=false;
                }
            }

        }
        catch (ConnectionException e){
            e.printStackTrace();
        }
    }

    public static void writeToDB() {
        MutationBatch mb = AstyanaxIO.getKeyspace().prepareMutationBatch();

        String metricName = "15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met."+ new Random().nextInt();
        mb.withRow(new ColumnFamily("metrics_full", new StringSerializer(), new LongSerializer()), metricName)
                .putColumn(System.currentTimeMillis(), new Random().nextDouble(), null);

        try {
            final Timer.Context actualWriteCtx = batchWriteDurationTimer.time();
            long astyanaxStartTime = System.currentTimeMillis();
            mb.execute();
            writeMeter.mark();
            actualWriteCtx.stop();
            long astyanaxEndTime = System.currentTimeMillis();
            System.out.println("Astyanax Batch Write Execution Time " + (astyanaxEndTime - astyanaxStartTime));
            writeMeter.mark();
        }
        catch (ConnectionException e){
            e.printStackTrace();
        }
    }
}
