package codes.writeonce.dispatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;

public class Helper {

    @Nonnull
    public static <T> T find(
            @Nonnull Map<Class<?>, InheritedEntry<T>> implTypeToDelegate,
            @Nonnull Method dispatcherMethod,
            @Nonnull Class<?> aClass
    ) {
        final var entry = implTypeToDelegate.get(aClass);

        if (entry != null) {
            return entry.impl;
        }

        final var entry2 = findMissing(implTypeToDelegate, dispatcherMethod, aClass, aClass);

        if (entry2 == null) {
            throw new UnmappableTypeDispatcherException(
                    "Unmappable type: " + aClass + " for method: " + dispatcherMethod);
        }

        implTypeToDelegate.put(aClass, entry2);
        return entry2.impl;
    }

    @Nullable
    private static <T> InheritedEntry<T> find(
            @Nonnull Map<Class<?>, InheritedEntry<T>> implTypeToDelegate,
            @Nonnull Method dispatcherMethod,
            @Nonnull Class<?> originalClass,
            @Nonnull Class<?> aClass
    ) {
        final var entry = implTypeToDelegate.get(aClass);

        if (entry != null) {
            return entry;
        }

        final var entry2 = findMissing(implTypeToDelegate, dispatcherMethod, originalClass, aClass);

        if (entry2 != null) {
            implTypeToDelegate.put(aClass, entry2);
        }

        return entry2;
    }

    @Nullable
    private static <T> InheritedEntry<T> findMissing(
            @Nonnull Map<Class<?>, InheritedEntry<T>> implTypeToDelegate,
            @Nonnull Method dispatcherMethod,
            @Nonnull Class<?> originalClass,
            @Nonnull Class<?> aClass
    ) {
        final var superclass = aClass.getSuperclass();
        final Class<?>[] interfaces = aClass.getInterfaces();
        final var length = interfaces.length;

        if (superclass != null) {
            final var entry = find(implTypeToDelegate, dispatcherMethod, originalClass, superclass);
            if (entry != null) {
                return check(implTypeToDelegate, dispatcherMethod, originalClass, interfaces, length, 0, entry);
            }
        }

        return first(implTypeToDelegate, dispatcherMethod, originalClass, interfaces, length);
    }

    @Nullable
    private static <T> InheritedEntry<T> first(
            @Nonnull Map<Class<?>, InheritedEntry<T>> implTypeToDelegate,
            @Nonnull Method dispatcherMethod,
            @Nonnull Class<?> originalClass,
            @Nonnull Class<?>[] interfaces,
            int length
    ) {
        for (int i = 0; i < length; i++) {
            final var entry = find(implTypeToDelegate, dispatcherMethod, originalClass, interfaces[i]);
            if (entry != null) {
                return check(implTypeToDelegate, dispatcherMethod, originalClass, interfaces, length, i + 1, entry);
            }
        }
        return null;
    }

    private static <T> InheritedEntry<T> check(
            @Nonnull Map<Class<?>, InheritedEntry<T>> implTypeToDelegate,
            @Nonnull Method dispatcherMethod,
            @Nonnull Class<?> originalClass,
            @Nonnull Class<?>[] interfaces,
            int length,
            int i,
            @Nonnull InheritedEntry<T> entry
    ) {
        for (; i < length; i++) {
            final var entry2 = find(implTypeToDelegate, dispatcherMethod, originalClass, interfaces[i]);
            if (entry2 != null && entry2 != entry) {
                final Class<?> baseType = entry.baseType;
                final Class<?> baseType2 = entry2.baseType;
                final var assignable1 = baseType.isAssignableFrom(baseType2);
                final var assignable2 = baseType2.isAssignableFrom(baseType);
                if (assignable1 && !assignable2) {
                    entry = entry2;
                } else if (assignable1 || !assignable2) {
                    throw new AmbiguousTypeDispatcherException(
                            "Ambiguous mapping for type: " + originalClass + " for method: " + dispatcherMethod +
                            " (could be " + baseType + " or " + baseType2 + ")");
                }
            }
        }
        return entry;
    }
}
