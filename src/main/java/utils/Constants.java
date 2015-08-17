package utils;

/**
 * Created by GauravBajaj on 8/15/15.
 */
public interface Constants {
    String ASTYANAX_HOSTS = "127.0.0.1:9160";
    String DATASTAX_HOSTS = "127.0.0.1:9042";
    String GRAPHITE_HOSTS = "127.0.0.1";
    int ASTYANAX_PORT = 9160;
    int GRAPHITE_PORT = 2003;
    int WRITE_BATCH_SIZE = 20;
    int READ_BATCH_SIZE = 200;
    int NUMBER_OF_WRITE_THREADS = 10;
    int NUMBER_OF_READ_THREADS = 10;
}
