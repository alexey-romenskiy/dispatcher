package codes.writeonce.dispatcher;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ReflectionDispatcherFactoryTest {

    private static final DispatcherFactory FACTORY = new JavassistDispatcherFactory();
//    private static final DispatcherFactory FACTORY = new ReflectionDispatcherFactory();

    @Test
    public void wrap1() throws IOException {
        assertEquals("foo", FACTORY.wrap(MyInterface.class, new MyDelegate1()).dispatch(new MyClass2()));
    }

    @Test
    public void wrap2() throws IOException {
        assertEquals("foo", FACTORY.wrap(MyInterface.class, new MyDelegate1()).dispatch(new MyClass4()));
    }

    @Test
    public void wrap3() throws IOException {
        assertEquals("bar", FACTORY.wrap(MyInterface.class, new MyDelegate1()).dispatch(new MyClass5()));
    }

    @Test(expected = IOException.class)
    public void wrap4() throws IOException {
        FACTORY.wrap(MyInterface.class, new MyDelegate1()).dispatch(new MyClass3());
    }

    @Test(expected = DispatcherException.class)
    public void wrap5() throws IOException {
        FACTORY.wrap(MyInterface.class, new MyDelegate1()).dispatch(new MyClass1());
    }
}
