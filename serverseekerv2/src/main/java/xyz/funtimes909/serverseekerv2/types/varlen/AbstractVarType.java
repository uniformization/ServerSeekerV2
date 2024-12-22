package xyz.funtimes909.serverseekerv2.types.varlen;

import java.util.List;

public abstract class AbstractVarType<T, S extends AbstractVarType<T, S>> {
    protected T value;
    protected int size;

    protected AbstractVarType(T value, int size) {
        this.value = value;
        this.size = size;
    }

    /** The type of the value (should be equivalent to `T.getClass()` or `get().getClass()`) */
    abstract public Class<?> getType();

    /**
     * Attempts to decode the vartype at the start of a byte array
     * @param index The index into the array list
     * @return A Pair containing the value and the n bytes needed to get it in the byte array
     */
    public static <T, S> S decode(byte[] in, int index) {
        throw new RuntimeException("Decode vartype not implemented");
    };
    /**
     * Attempts to encode the type into byte array
     */
    public static <T> List<Byte> encode(T in) {
        throw new RuntimeException("Encode not implemented");
    };
    abstract public List<Byte> encodeSelf();
    abstract public List<Byte> encodeValue(Object in);



    public T get() {
        return value;
    }
    public int getSize() {
        return size;
    }
}
