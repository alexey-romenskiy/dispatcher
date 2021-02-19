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
            @Nonnull Map<Method, Map<Class<?>, InvocationEntry>> map
    ) {
        for (final var entry : map.entrySet()) {
            for (final var value : entry.getValue().values()) {
                final var method = value.method;
                if (!method.canAccess(value.target)) {
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
            @Nonnull Map<Method, Map<Class<?>, InvocationEntry>> dispatcherMethodAndImplTypeToDelegate,
            @Nonnull Method dispatcherMethod,
            @Nonnull Class<?> aClass,
            @Nonnull Object[] args
    ) throws Throwable {

        final var entry = find(dispatcherMethodAndImplTypeToDelegate.get(dispatcherMethod), dispatcherMethod, aClass);

        try {
            return entry.method.invoke(entry.target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
