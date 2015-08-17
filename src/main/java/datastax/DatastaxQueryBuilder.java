package datastax;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import utils.Constants;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by GauravBajaj on 8/10/15.
 */
public class DatastaxQueryBuilder{
    protected static Batch getFullMetricsBatch(){
        return QueryBuilder.batch();
    }
    private static int ttl = 172800; //Two Days
    protected static Batch addDummyBatchOfMetrics(int size){
        Batch batch = getFullMetricsBatch();
        for(int i=0; i<size;i++) {

            ByteBuffer key = DatastaxSerializer.StringSerializer.serialize("15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met."+i);
            ByteBuffer column1 = DatastaxSerializer.LongSerializer.serialize(System.currentTimeMillis());
            ByteBuffer value = DatastaxSerializer.DoubleSerializer.serialize(new Random().nextDouble());

            Insert insertMetric = QueryBuilder
                    .insertInto("\"DATA\"", "metrics_full")
                    .value("key", key)
                    .value("column1", column1)
                    .value("value",value);
            insertMetric.using(QueryBuilder.ttl(ttl));
            batch.add(insertMetric);
        }
        return batch;
    }

    protected static Insert addDummyMetric(){
        ByteBuffer key = DatastaxSerializer.StringSerializer.serialize("15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met." + new Random().nextInt());
        ByteBuffer column1 = DatastaxSerializer.LongSerializer.serialize(System.currentTimeMillis());
        ByteBuffer value = DatastaxSerializer.DoubleSerializer.serialize(new Random().nextDouble());

        Insert insertMetric = QueryBuilder
                .insertInto("\"DATA\"", "metrics_full")
                .value("key", key)
                .value("column1", column1)
                .value("value",value);

        return insertMetric;
    }

    protected static Select getDummyMetrics(int limit) {
        Select statement = QueryBuilder
                .select()
                .all()
                .from("\"DATA\"", "metrics_full")
                .where(QueryBuilder.eq("key", "15581.int.abcdefg.hijklmnop.qrstuvw.xyz.ABCDEFG.HIJKLMNOP.QRSTUVW.XYZ.abcdefg.hijklmnop.qrstuvw.xyz.met." + (new Random().nextInt((Constants.WRITE_BATCH_SIZE-1) - 0) + 0)))
                .and(QueryBuilder.gt("column1", System.currentTimeMillis() - ttl * 1000))
                .and(QueryBuilder.lt("column1", System.currentTimeMillis()))
                .limit(limit);
        return statement;
    }
}
