package datastax;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by GauravBajaj on 8/10/15.
 */
public class DatastaxQueryBuilder {
    public static Batch getFullMetricsBatch(){
        return QueryBuilder.batch();
    }

    public static Batch addDummyMetrics(double size){

        Batch batch = getFullMetricsBatch();
        int ttl = 30;
        for(double i=0; i<size;i++) {

            ByteBuffer key = DatastaxSerializer.StringSerializer.serialize("15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met."+i);
            ByteBuffer column1 = DatastaxSerializer.LongSerializer.serialize(System.currentTimeMillis());
            ByteBuffer value = DatastaxSerializer.DoubleSerializer.serialize(i);

            Insert insertMetric = QueryBuilder
                    .insertInto("\"DATA\"", "metrics_full")
                    .value("key", key)
                    .value("column1", column1)
                    .value("value",value);

            /*Insert insertMetric = QueryBuilder
                    .insertInto("\"DATA\"", "metrics_full")
                    .value("key", "15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met."+ new Random().nextInt())
                    .value("column1", System.currentTimeMillis())
                    .value("value", "\" " + i + "\"");*/

            //insertMetric.using(QueryBuilder.ttl(ttl));
            batch.add(insertMetric);
        }
        //System.out.println(batch.toString());
        return batch;
    }

}
