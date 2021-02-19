package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.AmbiguousTypeDispatcherException;
import codes.writeonce.dispatcher.DispatcherFactory;
import codes.writeonce.dispatcher.JavassistDispatcherFactory;
import codes.writeonce.dispatcher.ReflectionDispatcherFactory;
import codes.writeonce.dispatcher.UnmappableTypeDispatcherException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DispatcherFactoryTest {

    private static final DispatcherFactory FACTORY1 = new JavassistDispatcherFactory();
    private static final DispatcherFactory FACTORY2 = new ReflectionDispatcherFactory();

    @Test(expected = UnmappableTypeDispatcherException.class)
    public void wrapA1() throws IOException {
        FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassA() {
        });
    }

    @Test(expected = UnmappableTypeDispatcherException.class)
    public void wrapA2() throws IOException {
        FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassA() {
        });
    }

    @Test(expected = UnmappableTypeDispatcherException.class)
    public void wrapB1() throws IOException {
        FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassB());
    }

    @Test(expected = UnmappableTypeDispatcherException.class)
    public void wrapB2() throws IOException {
        FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassB());
    }

    @Test
    public void wrapC1() throws IOException {
        assertEquals("foo", FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassC() {
        }));
    }

    @Test
    public void wrapC2() throws IOException {
        assertEquals("foo", FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassC() {
        }));
    }

    @Test
    public void wrapD1() throws IOException {
        assertEquals("foo", FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassD() {
        }));
    }

    @Test
    public void wrapD2() throws IOException {
        assertEquals("foo", FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassD() {
        }));
    }

    @Test
    public void wrapE1() throws IOException {
        assertEquals("bar", FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassE() {
        }));
    }

    @Test
    public void wrapE2() throws IOException {
        assertEquals("bar", FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassE() {
        }));
    }

    @Test(expected = AmbiguousTypeDispatcherException.class)
    public void wrapF1() throws IOException {
        FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassF());
    }

    @Test(expected = AmbiguousTypeDispatcherException.class)
    public void wrapF2() throws IOException {
        FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassF());
    }

    @Test
    public void wrapG1() throws IOException {
        assertEquals("bar", FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassG() {
        }));
    }

    @Test
    public void wrapG2() throws IOException {
        assertEquals("bar", FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassG() {
        }));
    }

    @Test(expected = IOException.class)
    public void wrapH1() throws IOException {
        FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassH());
    }

    @Test(expected = IOException.class)
    public void wrapH2() throws IOException {
        FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassH());
    }

    @Test(expected = AmbiguousTypeDispatcherException.class)
    public void wrapI1() throws IOException {
        FACTORY1.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassI());
    }

    @Test(expected = AmbiguousTypeDispatcherException.class)
    public void wrapI2() throws IOException {
        FACTORY2.wrap(DispatcherInterface.class, new Delegate()).dispatch(new ClassI());
    }
}
