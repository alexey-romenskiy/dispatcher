package codes.writeonce.dispatcher;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

abstract class AbstractDispatcherFactory implements DispatcherFactory {

    @Override
    @Nonnull
    public <T> T wrap(@Nonnull Class<T> dispatcherType, @Nonnull Object... delegates) {

        if (!dispatcherType.isInterface()) {
            throw new DispatcherException("First parameter must be interface: " + dispatcherType);
        }

        final var baseTypeAndParamsToDispatcherMethod = new HashMap<Class<?>, Map<List<Class<?>>, Method>>();
        final var dispatcherMethodAndImplTypeToDelegate = new HashMap<Method, Map<Class<?>, InvocationEntry>>();

        for (final var method : dispatcherType.getMethods()) {
            final var parameterTypes = method.getParameterTypes();
            if (parameterTypes.length < 1) {
                throw new DispatcherException("Dispatcher method has too few parameters: " + method);
            }
            final var baseType = parameterTypes[0];
            if (baseTypeAndParamsToDispatcherMethod.computeIfAbsent(baseType, k -> new HashMap<>())
                        .put(getParameters(parameterTypes), method) != null) {
                throw new DispatcherException("Duplicate dispatcher method signature: " + method);
            }
            if (dispatcherMethodAndImplTypeToDelegate.put(method, new HashMap<>()) != null) {
                throw new DispatcherException("Duplicate dispatcher method: " + method);
            }
        }

        for (final var delegate : delegates) {
            final var delegateClass = delegate.getClass();
            final var methods = delegateClass.getMethods();
            checkClass(delegateClass, new HashSet<>(asList(methods)));
            for (final var method : methods) {
                if (method.isAnnotationPresent(Endpoint.class)) {
                    final var parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length < 1) {
                        throw new DispatcherException("Delegate method has too few parameters: " + method);
                    }
                    final var type = parameterTypes[0];
                    if (type.isAnnotation()) {
                        throw new DispatcherException("First delegate parameter must not be annotation: " + method);
                    }
                    if (type.isInterface()) {
                        throw new DispatcherException("First delegate parameter must not be interface: " + method);
                    }
                    for (final var entry : baseTypeAndParamsToDispatcherMethod.entrySet()) {
                        if (entry.getKey().isAssignableFrom(type)) {
                            final var dispatcherMethod = entry.getValue().get(getParameters(parameterTypes));
                            if (dispatcherMethod != null &&
                                method.getReturnType() == dispatcherMethod.getReturnType()) {

                                final var exceptionTypes = dispatcherMethod.getExceptionTypes();

                                for (final var exceptionType : method.getExceptionTypes()) {
                                    checkException(exceptionType, exceptionTypes, method, dispatcherMethod);
                                }

                                final var invocationEntry = dispatcherMethodAndImplTypeToDelegate.get(dispatcherMethod)
                                        .put(type, new InvocationEntry(method, delegate));

                                if (invocationEntry != null) {
                                    throw new DispatcherException(
                                            "Duplicate delegate method signature: " + invocationEntry.method + " on " +
                                            invocationEntry.target + " and " + method + " on " + delegate);
                                }
                            }
                        }
                    }
                }
            }
        }

        return createStub(dispatcherType, dispatcherMethodAndImplTypeToDelegate);
    }

    private static void checkException(
            @Nonnull Class<?> exceptionType,
            @Nonnull Class<?>[] exceptionTypes,
            @Nonnull Method method,
            @Nonnull Method dispatcherMethod
    ) {
        if (RuntimeException.class.isAssignableFrom(exceptionType) || Error.class.isAssignableFrom(exceptionType)) {
            return;
        }

        for (final var type : exceptionTypes) {
            if (type.isAssignableFrom(exceptionType)) {
                return;
            }
        }

        throw new DispatcherException(
                "Endpoint " + method + " throws " + exceptionType + " not declared in " + dispatcherMethod);
    }

    private static void checkClass(@Nonnull Class<?> aClass, @Nonnull Set<Method> methods) {

        while (true) {
            checkMethods(aClass, methods);
            checkInterfaces(aClass, methods);
            aClass = aClass.getSuperclass();
            if (aClass == null) {
                return;
            }
        }
    }

    private static void checkInterfaces(@Nonnull Class<?> aClass, @Nonnull Set<Method> methods) {

        for (final var anInterface : aClass.getInterfaces()) {
            checkMethods(aClass, methods);
            checkInterfaces(anInterface, methods);
        }
    }

    private static void checkMethods(@Nonnull Class<?> aClass, @Nonnull Set<Method> methods) {

        for (final var method : aClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Endpoint.class)) {
                if (Modifier.isStatic(method.getModifiers())) {
                    throw new DispatcherException("Static endpoints not allowed: " + method);
                }
                if (!methods.contains(method)) {
                    throw new DispatcherException("Inaccessible endpoint: " + method);
                }
            }
        }
    }

    @Nonnull
    private static List<Class<?>> getParameters(@Nonnull Class<?>[] parameterTypes) {
        return asList(parameterTypes).subList(1, parameterTypes.length);
    }

    @Nonnull
    protected abstract <T> T createStub(
            @Nonnull Class<T> dispatcherType,
            @Nonnull Map<Method, Map<Class<?>, InvocationEntry>> map
    );

    protected static final class InvocationEntry {

        @Nonnull
        public final Method method;

        @Nonnull
        public final Object target;

        public InvocationEntry(@Nonnull Method method, @Nonnull Object target) {
            this.method = method;
            this.target = target;
        }
    }
}
