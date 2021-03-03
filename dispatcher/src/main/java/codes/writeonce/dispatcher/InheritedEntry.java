package codes.writeonce.dispatcher;

public class InheritedEntry<T> {

    public final Class<?> baseType;

    public final T impl;

    public InheritedEntry(Class<?> baseType, T impl) {
        this.baseType = baseType;
        this.impl = impl;
    }
}
