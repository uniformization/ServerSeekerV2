package xyz.funtimes909.serverseekerv2.types.varlen;

import com.google.common.primitives.Bytes;

import java.util.List;

public abstract class AbstractVarType<T, S extends AbstractVarType<?, ?>> {
    protected T value;
    protected int size;

    protected AbstractVarType(T value, int size) {
        this.value = value;
        this.size = size;
    }


    /**
     * Attempts to decode the vartype at the start of a byte array
     * @return A Pair containing the value and the n bytes needed to get it in the byte array
     */
    public static <T, S> S decode(byte[] in) {
        return decode(in, 0);
    }
    /**
     * Attempts to decode the vartype at the start of a byte array
     * @param index The index into the array list
     * @return A Pair containing the value and the n bytes needed to get it in the byte array
     */
    public static <T, S> S decode(byte[] in, int index) {
        throw new RuntimeException("Decode not implemented");
    };
    /**
     * Attempts to encode the type into byte array
     */
    public static <T> byte[] encode(T in) {
        throw new RuntimeException("Encode not implemented");
    };


    protected static byte[] prefixSize(byte[] in) {
        List<Byte> bytes = VarInt.encode(in.length);
        bytes.addAll(Bytes.asList(in));
        return Bytes.toArray(bytes);
    }


    public T get() {
        return value;
    }
    public int getSize() {
        return size;
    }
}
