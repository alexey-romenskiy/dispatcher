package codes.writeonce.dispatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static codes.writeonce.dispatcher.Helper.find;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;

abstract class AbstractDispatcherFactory implements DispatcherFactory {

    @Override
    @Nonnull
    public <T> T wrap(@Nonnull Class<T> dispatcherType, @Nonnull Object... delegates) {
        return createStub(dispatcherType, generateMap(dispatcherType, Object::getClass, delegates));
    }

    @Override
    public void test(@Nonnull Class<?> dispatcherType, @Nonnull Class<?>[] delegates, @Nonnull Class<?>... subtypes)
            throws DispatcherException {

        final var map = generateMap(dispatcherType, identity(), delegates);

        for (final var method : dispatcherType.getMethods()) {
            for (final var subtype : subtypes) {
                find(map.get(method), method, subtype);
            }
        }
    }

    @SafeVarargs
    @Nonnull
    private <T, D> Map<Method, Map<Class<?>, InheritedEntry<InvocationEntry>>> generateMap(
            @Nonnull Class<T> dispatcherType,
            @Nonnull Function<D, Class<?>> delegateClassFunction,
            @Nonnull D... delegates
    ) {
        if (!dispatcherType.isInterface()) {
            throw new DispatcherException("First parameter must be interface: " + dispatcherType);
        }

        final var baseTypeAndParamsToDispatcherMethod =
                new HashMap<Class<?>, HashMap<Class<?>, Map<List<Class<?>>, Method>>>();
        final var dispatcherMethodAndImplTypeToDelegate =
                new HashMap<Method, Map<Class<?>, InheritedEntry<InvocationEntry>>>();
        final var dispatcherMethodOptionals = new HashMap<Method, Set<Integer>>();

        for (final var method : dispatcherType.getMethods()) {
            final var parameterAnnotations = method.getParameterAnnotations();
            final var parameterTypes = method.getParameterTypes();
            if (parameterTypes.length < 1) {
                throw new DispatcherException("Dispatcher method has too few parameters: " + method);
            }
            if (isAnnotationPresent(parameterAnnotations[0])) {
                throw new DispatcherException(
                        "Dispatcher method's first parameter may not have OptionalArg annotation: " + method);
            }
            final var optionals = new HashSet<Integer>();
            for (int i = 1; i < parameterAnnotations.length; i++) {
                if (isAnnotationPresent(parameterAnnotations[i])) {
                    optionals.add(i);
                }
            }
            if (baseTypeAndParamsToDispatcherMethod
                        .computeIfAbsent(method.getReturnType(), k -> new HashMap<>())
                        .computeIfAbsent(parameterTypes[0], k -> new HashMap<>())
                        .put(getParameters(parameterTypes), method) != null) {

                throw new DispatcherException("Duplicate dispatcher method signature: " + method);
            }
            if (dispatcherMethodAndImplTypeToDelegate.put(method, new HashMap<>()) != null) {
                throw new DispatcherException("Duplicate dispatcher method: " + method);
            }
            dispatcherMethodOptionals.put(method, optionals);
        }

        for (final var delegate : delegates) {
            final var delegateClass = delegateClassFunction.apply(delegate);
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
                    final var map = baseTypeAndParamsToDispatcherMethod.get(method.getReturnType());
                    if (map != null) {
                        for (final var entry : map.entrySet()) {
                            if (entry.getKey().isAssignableFrom(type)) {
                                final var dispatcherMethods =
                                        getDispatcherMethods(entry.getValue(), dispatcherMethodOptionals, method,
                                                parameterTypes);
                                for (final var entry2 : dispatcherMethods.entrySet()) {

                                    final var dispatcherMethod = entry2.getKey();
                                    final var presentParameters = entry2.getValue();

                                    final var exceptionTypes = dispatcherMethod.getExceptionTypes();

                                    for (final var exceptionType : method.getExceptionTypes()) {
                                        checkException(exceptionType, exceptionTypes, method, dispatcherMethod);
                                    }

                                    final var entryMap = dispatcherMethodAndImplTypeToDelegate.get(dispatcherMethod);
                                    final var inheritedEntry = entryMap.get(type);

                                    if (inheritedEntry != null) {
                                        final var impl = inheritedEntry.impl;
                                        if (extending(presentParameters, impl.presentParameters)) {
                                            entryMap.put(type, new InheritedEntry<>(type,
                                                    new InvocationEntry(method, delegate, presentParameters)));
                                        } else if (!extending(impl.presentParameters, presentParameters)) {
                                            throw new DispatcherException(
                                                    "Duplicate or ambiguous delegate method signature: " + impl.method +
                                                    " on " + impl.target + " and " + method + " on " + delegate);
                                        }
                                    } else {
                                        entryMap.put(type, new InheritedEntry<>(type,
                                                new InvocationEntry(method, delegate, presentParameters)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return dispatcherMethodAndImplTypeToDelegate;
    }

    private boolean isAnnotationPresent(@Nonnull Annotation[] parameterAnnotation) {

        for (final var annotation : parameterAnnotation) {
            if (annotation.annotationType() == OptionalArg.class) {
                return true;
            }
        }
        return false;
    }

    private boolean extending(@Nonnull Set<Integer> highPresentParameters, @Nonnull Set<Integer> lowPresentParameters) {

        final var set = new HashSet<>(highPresentParameters);

        for (final var parameter : lowPresentParameters) {
            if (!set.remove(parameter)) {
                return false;
            }
        }

        return !set.isEmpty();
    }

    @Nonnull
    private Map<Method, Set<Integer>> getDispatcherMethods(
            @Nonnull Map<List<Class<?>>, Method> map,
            @Nonnull Map<Method, Set<Integer>> dispatcherMethodOptionals,
            @Nonnull Method candidateMethod,
            @Nonnull Class<?>[] parameterTypes
    ) {
        final var methods = new HashMap<Method, Set<Integer>>();
        for (final var method : map.values()) {
            final var optionals = dispatcherMethodOptionals.get(method);
            final var presentParameters =
                    getPresentParameters(method, method.getParameterTypes(), optionals, candidateMethod, parameterTypes,
                            1, 1);
            if (presentParameters != null) {
                presentParameters.add(0);
                methods.put(method, presentParameters);
            }
        }
        return methods;
    }

    @Nullable
    private Set<Integer> getPresentParameters(
            @Nonnull Method method,
            @Nonnull Class<?>[] methodParameterTypes,
            @Nonnull Set<Integer> methodOptionals,
            @Nonnull Method candidateMethod,
            @Nonnull Class<?>[] parameterTypes,
            int i,
            int j
    ) {
        Set<Integer> set = null;
        while (true) {
            if (j == parameterTypes.length) {
                while (i < methodParameterTypes.length) {
                    if (!methodOptionals.contains(i)) {
                        return null;
                    }
                    i++;
                }
                return set == null ? new HashSet<>() : set;
            }
            while (true) {
                if (parameterTypes.length - j > methodParameterTypes.length - i) {
                    return null;
                }
                if (methodParameterTypes[i] == parameterTypes[j]) {
                    if (methodOptionals.contains(i)) {
                        final var set2 =
                                getPresentParameters(method, methodParameterTypes, methodOptionals, candidateMethod,
                                        parameterTypes, i + 1, j);
                        final var set3 =
                                getPresentParameters(method, methodParameterTypes, methodOptionals, candidateMethod,
                                        parameterTypes, i + 1, j + 1);
                        if (set2 == null) {
                            if (set3 == null) {
                                return null;
                            } else {
                                if (set == null) {
                                    set = new HashSet<>(set3.size() + 1);
                                }
                                set.add(i);
                                set.addAll(set3);
                                return set;
                            }
                        } else {
                            if (set3 == null) {
                                if (set == null) {
                                    return set2;
                                } else {
                                    set.addAll(set2);
                                    return set;
                                }
                            } else {
                                throw new AmbiguousTypeDispatcherException(
                                        "Ambiguous method parameters: " + candidateMethod + " for method: " + method);
                            }
                        }
                    } else {
                        if (set == null) {
                            set = new HashSet<>(methodParameterTypes.length - i);
                        }
                        set.add(i);
                        i++;
                        j++;
                        break;
                    }
                } else {
                    if (methodOptionals.contains(i)) {
                        i++;
                    } else {
                        return null;
                    }
                }
            }
        }
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
            @Nonnull Map<Method, Map<Class<?>, InheritedEntry<InvocationEntry>>> map
    );

    protected static final class InvocationEntry {

        @Nonnull
        public final Method method;

        @Nonnull
        public final Object target;

        @Nonnull
        public final Set<Integer> presentParameters;

        public InvocationEntry(@Nonnull Method method, @Nonnull Object target,
                @Nonnull Set<Integer> presentParameters) {
            this.method = method;
            this.target = target;
            this.presentParameters = presentParameters;
        }
    }
}
