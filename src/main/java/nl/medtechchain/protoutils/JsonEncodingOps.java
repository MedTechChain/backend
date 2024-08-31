package nl.medtechchain.protoutils;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

public class JsonEncodingOps {
    public static <T extends GeneratedMessageV3> String printJson(T m) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(m);
    }

    public static <T extends GeneratedMessageV3> T parseJson(String json, Message.Builder builder) throws InvalidProtocolBufferException {
        JsonFormat.parser().merge(json, builder);
        return (T) builder.build();
    }
}
