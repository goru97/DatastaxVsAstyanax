package astyanax;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.*;
import com.netflix.astyanax.query.RowQuery;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.util.RangeBuilder;
import utils.Constants;
import utils.Metrics;

import java.util.Random;

/**
 * Created by GauravBajaj on 8/15/15.
 */
public class AstyanaxReader {
    private final static Timer batchReadDurationTimer = Metrics.timer(AstyanaxReader.class, "Astyanax Batch Read Duration");
    private final static Meter readMeter = Metrics.meter(AstyanaxReader.class, "Astyanax read Operations");
    private static int ttl = 172800;
    public static void readMetrics(int limit) {
        RowQuery query = AstyanaxIO.getKeyspace().prepareQuery(new ColumnFamily("metrics_full", new StringSerializer(), new LongSerializer()))
                .getKey("15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met." + (new Random().nextInt((2 * Constants.WRITE_BATCH_SIZE - 1) - Constants.WRITE_BATCH_SIZE) + Constants.WRITE_BATCH_SIZE))
                .withColumnRange(
                        new RangeBuilder()
                                .setLimit(limit)
                                .setStart(System.currentTimeMillis()-ttl*1000)
                                .setEnd(System.currentTimeMillis())
                                .build());

        try {
            final Timer.Context actualReadCtx = batchReadDurationTimer.time();
            long astyanaxStartTime = System.currentTimeMillis();
            query.execute();
            readMeter.mark();
            actualReadCtx.stop();
            long astyanaxEndTime = System.currentTimeMillis();
            System.out.println("Astyanax Batch Read Execution Time " + (astyanaxEndTime - astyanaxStartTime));

        }
        catch (ConnectionException e){
            e.printStackTrace();
        }
    }

}
