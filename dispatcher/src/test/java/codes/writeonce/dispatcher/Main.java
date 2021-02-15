package codes.writeonce.dispatcher;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args)
            throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException, IOException {

        final var dispatcherClass = MyInterface.class;
        final var delegateClass = MyDelegate1.class;
        final var classPool = ClassPool.getDefault();
        final var parent = classPool.get(dispatcherClass.getName());
        final var delegate = classPool.get(delegateClass.getName());
        final var mytest = classPool.makeClass(dispatcherClass.getPackageName() + ".Mytest");
        mytest.addInterface(parent);
        mytest.addField(CtField.make("private final " + delegateClass.getName() + " delegate1;", mytest));
        mytest.addConstructor(
                CtNewConstructor.make(new CtClass[]{delegate}, new CtClass[0], "{ this.delegate1 = $1; }", mytest));
        for (final var method : parent.getMethods()) {
            if (method.getDeclaringClass().isInterface()) {
                mytest.addMethod(CtNewMethod.make(method.getReturnType(), method.getName(), method.getParameterTypes(),
                        method.getExceptionTypes(), "{ return \"abc\" + this.delegate1.toString(); }", mytest));
            }
        }
        final var aClass = mytest.toClass(dispatcherClass);
        final var o = (MyInterface) aClass.getConstructor(new Class[]{delegateClass}).newInstance(new MyDelegate1());
        System.out.println("o = " + o.dispatch(new MyClass2()));
        System.out.println(classPool.get(new int[0][].getClass().getName()));
        System.out.println(classPool.getCtClass(new int[0][].getClass().getName()));
    }
}
