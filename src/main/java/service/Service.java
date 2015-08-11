package service;

/**
 * Created by GauravBajaj on 8/10/15.
 */
import astyanax.AstyanaxWriter;
import datastax.DatastaxIO;
import datastax.DatastaxWriter;

import javax.xml.crypto.Data;

public class Service {

    private static void startIngestServices(){
        DatastaxIO.connect();

        Thread t1 = new Thread(new Runnable() {
            public void run() {
                int count =0;
                while(true) {
                    if(count == 30)
                        break;
                    count++;
                    new AstyanaxWriter().writeToDB();
                    try {
                        Thread.sleep(100);
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            public void run() {
                int count =0;
                while(true) {
                    if(count == 30)
                        break;
                    count++;
                    new DatastaxWriter().writeToDB();
                    try {
                        Thread.sleep(100);
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
                DatastaxIO.getSession().close();
            }
        });
        t2.start();
        t1.start();

    }
    public static void main(String[] args){

        startIngestServices();
    }
}
