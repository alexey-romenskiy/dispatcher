package codes.writeonce.dispatcher;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

public class Helper {

    @Nonnull
    public static <T> T find(
            @Nonnull Map<Class<?>, T> implTypeToDelegate,
            @Nonnull Method dispatcherMethod,
            @Nonnull Class<?> aClass
    ) {
        final var invocationEntry = implTypeToDelegate.get(aClass);
        if (invocationEntry != null) {
            return invocationEntry;
        }

        final var types = new ArrayList<Class<?>>();

        var aClass2 = aClass;

        while (true) {
            types.add(aClass2);
            aClass2 = aClass2.getSuperclass();
            if (aClass2 == null) {
                throw new DispatcherException(
                        "Unmappable parameter type: " + aClass + " for method: " + dispatcherMethod);
            }

            final var invocationEntry2 = implTypeToDelegate.get(aClass2);
            if (invocationEntry2 != null) {
                for (final var type : types) {
                    implTypeToDelegate.put(type, invocationEntry2);
                }
                return invocationEntry2;
            }
        }
    }
}
