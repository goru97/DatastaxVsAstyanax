package service;

/**
 * Created by GauravBajaj on 8/10/15.
 */
import astyanax.AstyanaxReader;
import astyanax.AstyanaxWriter;
import datastax.DatastaxReader;
import datastax.DatastaxWriter;
import utils.Constants;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Service {

    private static void startQueryServices(String serviceType){
        ExecutorService pool = Executors.newFixedThreadPool(Constants.NUMBER_OF_READ_THREADS);
        for(int i=0; i<Constants.NUMBER_OF_READ_THREADS;i++){
            if(serviceType.equalsIgnoreCase("datastax"))
                pool.execute(new DatastaxReadThread());
            else if(serviceType.equalsIgnoreCase("astyanax"))
                pool.execute(new AstyanaxReadThread());
            else {
                System.out.println("Please choose the service type");
                pool.shutdown();
                break;
            }

        }
    }

    private static void startIngestServices(String serviceType){
        ExecutorService pool = Executors.newFixedThreadPool(Constants.NUMBER_OF_WRITE_THREADS);
        for(int i=0; i<Constants.NUMBER_OF_WRITE_THREADS;i++){
            if(serviceType.equalsIgnoreCase("datastax"))
                pool.execute(new DatastaxWriteThread());
            else if(serviceType.equalsIgnoreCase("astyanax"))
                pool.execute(new AstyanaxWriteThread());
            else {
                System.out.println("Please choose the service type");
                pool.shutdown();
                break;
            }

        }
    }

    private static class DatastaxReadThread implements Runnable{
        @Override
        public void run() {
            while(true) {
                DatastaxReader.readMetrics(Constants.READ_BATCH_SIZE);
            }
        }
    }

    private static class AstyanaxReadThread implements Runnable{
        @Override
        public void run() {
            while(true) {
                AstyanaxReader.readMetrics(Constants.READ_BATCH_SIZE);
            }
        }
    }

    private static class DatastaxWriteThread implements Runnable{
        @Override
        public void run() {
            while (true) {
                DatastaxWriter.writeBatchToDB(Constants.WRITE_BATCH_SIZE);
            }
        }
    }

    private static class AstyanaxWriteThread implements Runnable{
        @Override
        public void run() {
            while(true) {
                AstyanaxWriter.writeBatchToDB(Constants.WRITE_BATCH_SIZE);
            }
        }
    }
    public static void main(String[] args){
        if(args[1].equalsIgnoreCase("write"))
            startIngestServices(args[0]);
        else if(args[1].equalsIgnoreCase("read"))
            startQueryServices(args[0]);

    }
}