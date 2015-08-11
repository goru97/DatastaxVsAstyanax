package datastax;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;

import java.nio.ByteBuffer;

/**
 * Created by GauravBajaj on 8/11/15.
 */
public class DatastaxSerializer{
    public static class StringSerializer{
        public static ByteBuffer serialize(String value) {
            DataType datatype = DataType.text();
            return datatype.serialize(value, ProtocolVersion.V3);
        }

        public static String deserialize(ByteBuffer value) {
            DataType datatype = DataType.text();
            return (String)datatype.deserialize(value, ProtocolVersion.V3);
        }
    }

    public static class LongSerializer{
        public static ByteBuffer serialize(Long value) {
            DataType datatype = DataType.counter();
            return datatype.serialize(value, ProtocolVersion.V3);
        }

        public static Long deserialize(ByteBuffer value) {
            DataType datatype = DataType.counter();
            return (Long)datatype.deserialize(value, ProtocolVersion.V3);
        }
    }

    public static class DoubleSerializer{
        public static ByteBuffer serialize(Double value) {
            DataType datatype = DataType.cdouble();
            return datatype.serialize(value, ProtocolVersion.V3);
        }

        public static Double deserialize(ByteBuffer value) {
            DataType datatype = DataType.cdouble();
            return (Double)datatype.deserialize(value, ProtocolVersion.V3);
        }
    }
}
