package codes.writeonce.dispatcher;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static codes.writeonce.dispatcher.Helper.find;
import static java.lang.reflect.Proxy.newProxyInstance;

public class ReflectionDispatcherFactory extends AbstractDispatcherFactory {

    @Nonnull
    @Override
    protected <T> T createStub(
            @Nonnull Class<T> dispatcherType,
            @Nonnull Map<Method, Map<Class<?>, InheritedEntry<InvocationEntry>>> map
    ) {
        for (final var entry : map.entrySet()) {
            for (final var value : entry.getValue().values()) {
                final var invocationEntry = value.impl;
                final var method = invocationEntry.method;
                if (!method.canAccess(invocationEntry.target)) {
                    method.setAccessible(true);
                }
            }
        }
        return cast(newProxyInstance(dispatcherType.getClassLoader(), new Class[]{dispatcherType},
                (proxy, method, args) -> invoke(map, method, args[0].getClass(), args)));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private static <T> T cast(@Nonnull Object proxyInstance) {
        return (T) proxyInstance;
    }

    private static Object invoke(
            @Nonnull Map<Method, Map<Class<?>, InheritedEntry<InvocationEntry>>> dispatcherMethodAndImplTypeToDelegate,
            @Nonnull Method dispatcherMethod,
            @Nonnull Class<?> aClass,
            @Nonnull Object[] args
    ) throws Throwable {

        final var entry = find(dispatcherMethodAndImplTypeToDelegate.get(dispatcherMethod), dispatcherMethod, aClass);

        final Object[] args2;
        final var presentParameters = entry.presentParameters;
        final var size = presentParameters.size();
        if (size < args.length) {
            args2 = new Object[size];
            for (int i = 0, j = 0; i < args.length; i++) {
                if (presentParameters.contains(i)) {
                    args2[j++] = args[i];
                }
            }
        } else {
            args2 = args;
        }

        try {
            return entry.method.invoke(entry.target, args2);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
