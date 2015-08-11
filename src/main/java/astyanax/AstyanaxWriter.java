package astyanax;

import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.serializers.StringSerializer;

import java.util.Random;

/**
 * Created by GauravBajaj on 8/10/15.
 */
public class AstyanaxWriter {

    public void writeToDB() {
        MutationBatch mb = AstyanaxIO.getKeyspace().prepareMutationBatch();
        for (int i=10000; i<20000;i++) {
            String metricName = "15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met."+ new Random().nextInt();
            mb.withRow(new ColumnFamily("metrics_full", new StringSerializer(), new LongSerializer()), metricName)
            .putColumn(System.currentTimeMillis(), i, null);
        }
        try {
            long astyanaxStartTime = System.currentTimeMillis();
            mb.execute();
            long astyanaxEndTime = System.currentTimeMillis();
            System.out.println("Astyanax Execution Time " + (astyanaxEndTime - astyanaxStartTime));
        }
        catch (ConnectionException e){
            e.printStackTrace();
        }
    }

}
