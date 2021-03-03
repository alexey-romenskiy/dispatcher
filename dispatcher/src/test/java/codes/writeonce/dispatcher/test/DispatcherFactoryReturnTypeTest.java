package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.DispatcherFactory;
import codes.writeonce.dispatcher.JavassistDispatcherFactory;
import codes.writeonce.dispatcher.ReflectionDispatcherFactory;
import org.junit.Test;

public class DispatcherFactoryReturnTypeTest {

    private static final DispatcherFactory FACTORY1 = new JavassistDispatcherFactory();

    private static final DispatcherFactory FACTORY2 = new ReflectionDispatcherFactory();

    @Test
    public void wrap1() {
        FACTORY1.test(DispatcherInterface2.class, new Class<?>[]{Delegate2.class}, ClassA.class);
    }

    @Test
    public void wrap2() {
        FACTORY2.test(DispatcherInterface2.class, new Class<?>[]{Delegate2.class}, ClassA.class);
    }
}
