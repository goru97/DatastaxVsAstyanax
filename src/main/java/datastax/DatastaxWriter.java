package datastax;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Batch;

/**
 * Created by GauravBajaj on 8/10/15.
 */
public class DatastaxWriter {

    public void writeToDB(){
        Session session = DatastaxIO.getSession();
        //TODO: Report Codahale metrics to graphite
        Batch batch = DatastaxQueryBuilder.addDummyMetrics(10000);
        long datastaxStartTime = System.currentTimeMillis();
        session.execute(batch);
        long datastaxEndTime = System.currentTimeMillis();
        System.out.println("Datastax Execution Time " + (datastaxEndTime - datastaxStartTime));
        //session.close();
    }
}
