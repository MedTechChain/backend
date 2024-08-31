package nl.medtechchain.protoutils;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Base64;

public class Base64EncodingOps {
    public static <T extends GeneratedMessageV3> String encode64(T m) {
        return Base64.getEncoder().encodeToString(m.toByteArray());
    }

    public static <T extends GeneratedMessageV3> T decode64(String encoded, ProtobufParse<T> parse) throws InvalidProtocolBufferException {
        return parse.parseFrom(Base64.getDecoder().decode(encoded));
    }

    public static <T extends GeneratedMessageV3> T decode64(byte[] encoded, ProtobufParse<T> parse) throws InvalidProtocolBufferException {
        return parse.parseFrom(Base64.getDecoder().decode(encoded));
    }

    public interface ProtobufParse<T> {
        T parseFrom(byte[] data) throws InvalidProtocolBufferException;
    }
}
