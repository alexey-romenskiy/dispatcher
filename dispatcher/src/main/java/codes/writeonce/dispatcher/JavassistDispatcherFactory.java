package codes.writeonce.dispatcher;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class JavassistDispatcherFactory extends AbstractDispatcherFactory {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final ClassPool classPool = ClassPool.getDefault();

    @Nonnull
    @Override
    protected <T> T createStub(
            @Nonnull Class<T> dispatcherType,
            @Nonnull Map<Method, Map<Class<?>, InheritedEntry<InvocationEntry>>> map
    ) {
        try {
            final var myClass = makeClass(dispatcherType);

            final var length = map.size();

            final var parameters = new ArrayList<CtClass>(length * 2);
            final var parameters2 = new ArrayList<Class<?>>(length * 2);
            final var values = new ArrayList<>(length * 2);

            for (int i = 0; i < length; i++) {

                parameters.add(get(Map.class));
                parameters.add(get(Method.class));

                parameters2.add(Map.class);
                parameters2.add(Method.class);

                myClass.addField(CtField.make("private final java.util.Map map" + i + ";", myClass));
                myClass.addField(CtField.make("private final java.lang.reflect.Method method" + i + ";", myClass));
            }

            myClass.addConstructor(CtNewConstructor.make(
                    parameters.toArray(CtClass[]::new),
                    new CtClass[0],
                    "{ " + IntStream.range(0, length).mapToObj(this::mapConstructor).collect(joining()) + " }",
                    myClass
            ));

            int i = 0;

            for (final Map.Entry<Method, Map<Class<?>, InheritedEntry<InvocationEntry>>> entry : map.entrySet()) {

                final var method = entry.getKey();
                final Map<Class<?>, InheritedEntry<T>> map2 = new HashMap<>();

                for (final Map.Entry<Class<?>, InheritedEntry<InvocationEntry>> entry2 : entry.getValue().entrySet()) {
                    final var value = entry2.getValue();
                    map2.put(entry2.getKey(),
                            new InheritedEntry<>(value.baseType, createStub2(dispatcherType, method, value.impl)));
                }

                final var parameterTypes = method.getParameterTypes();

                myClass.addMethod(CtNewMethod.make(
                        get(method.getReturnType()),
                        method.getName(),
                        get(parameterTypes),
                        get(method.getExceptionTypes()),
                        "{ return ((" + dispatcherType.getName() + ") codes.writeonce.dispatcher.Helper.find(map" + i +
                        ", method" + i + ", $1.getClass()))." + method.getName() + "(" +
                        IntStream.range(1, parameterTypes.length + 1).mapToObj(e -> "$" + e).collect(joining(", ")) +
                        "); }",
                        myClass
                ));

                values.add(map2);
                values.add(method);

                i++;
            }

            final var aClass = myClass.toClass(dispatcherType);

            return cast(aClass.getConstructor(parameters2.toArray(Class[]::new))
                    .newInstance(values.toArray(Object[]::new)));
        } catch (DispatcherException e) {
            throw e;
        } catch (Exception e) {
            throw new DispatcherException(e);
        }
    }

    @Nonnull
    private String mapConstructor(int i) {
        return "map" + i + " = $" + (i * 2 + 1) + "; method" + i + " = $" + (i * 2 + 2) + ";";
    }

    private <T> T createStub2(
            @Nonnull Class<T> dispatcherType,
            @Nonnull Method method,
            @Nonnull InvocationEntry invocationEntry
    ) {
        try {
            final var myClass = makeClass(dispatcherType);

            final var delegate = invocationEntry.target;
            final var delegateType = delegate.getClass();
            final var delegateClass = get(delegateType);

            myClass.addField(CtField.make("private final " + delegateType.getName() + " delegate;", myClass));
            myClass.addConstructor(CtNewConstructor
                    .make(new CtClass[]{delegateClass}, new CtClass[0], "{ delegate = $1; }", myClass));

            final var parameterTypes = method.getParameterTypes();
            final Class<?> declaringClass = invocationEntry.method.getDeclaringClass();
            final var delegateExpr =
                    declaringClass == delegateType ? "delegate" : "((" + declaringClass.getName() + ") delegate)";
            final var args = IntStream.range(1, parameterTypes.length + 1).mapToObj(e -> mapArg(invocationEntry, e))
                    .collect(joining(", "));
            myClass.addMethod(CtNewMethod.make(
                    get(method.getReturnType()),
                    method.getName(),
                    get(parameterTypes),
                    get(method.getExceptionTypes()),
                    "{ return " + delegateExpr + "." + invocationEntry.method.getName() + "(" + args + "); }",
                    myClass
            ));

            final var aClass = myClass.toClass(dispatcherType);
            return cast(aClass.getConstructor(new Class[]{delegateType}).newInstance(delegate));
        } catch (DispatcherException e) {
            throw e;
        } catch (Exception e) {
            throw new DispatcherException(e);
        }
    }

    @Nonnull
    private String mapArg(@Nonnull InvocationEntry invocationEntry, int e) {
        return e == 1 ? "(" + invocationEntry.method.getParameterTypes()[0].getName() + ") $" + e : "$" + e;
    }

    @Nonnull
    private <T> CtClass makeClass(@Nonnull Class<T> dispatcherType) throws NotFoundException {
        final var myClass =
                classPool.makeClass(dispatcherType.getPackageName() + "._dispatcher_" + SEQUENCE.incrementAndGet());
        myClass.addInterface(get(dispatcherType));
        return myClass;
    }

    @Nonnull
    private CtClass[] get(@Nonnull Class<?>[] aClass) throws NotFoundException {
        final var ctClasses = new CtClass[aClass.length];
        for (int i = 0; i < aClass.length; i++) {
            ctClasses[i] = get(aClass[i]);
        }
        return ctClasses;
    }

    @Nonnull
    private CtClass get(@Nonnull Class<?> aClass) throws NotFoundException {
        return classPool.get(aClass.getName());
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private static <T> T cast(@Nonnull Object proxyInstance) {
        return (T) proxyInstance;
    }
}
