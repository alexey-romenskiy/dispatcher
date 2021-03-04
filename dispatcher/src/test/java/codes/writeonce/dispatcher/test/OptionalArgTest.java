package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.DispatcherException;
import codes.writeonce.dispatcher.DispatcherFactory;
import codes.writeonce.dispatcher.JavassistDispatcherFactory;
import codes.writeonce.dispatcher.ReflectionDispatcherFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OptionalArgTest {

    private static final DispatcherFactory FACTORY1 = new JavassistDispatcherFactory();
    private static final DispatcherFactory FACTORY2 = new ReflectionDispatcherFactory();

    @Test
    public void wrapC1() throws IOException {
        assertEquals("foozxcvbna123", FACTORY1.wrap(DispatcherInterface3.class, new Delegate3()).dispatch(new ClassC() {
        }, "zxc", "vbn", 'a', 123));
    }

    @Test
    public void wrapC2() throws IOException {
        assertEquals("foozxcvbna123", FACTORY2.wrap(DispatcherInterface3.class, new Delegate3()).dispatch(new ClassC() {
        }, "zxc", "vbn", 'a', 123));
    }

    @Test(expected = DispatcherException.class)
    public void wrapC3() throws IOException {
        assertEquals("foozxcvbna123", FACTORY1.wrap(DispatcherInterface3.class, new Delegate4()).dispatch(new ClassC() {
        }, "zxc", "vbn", 'a', 123));
    }

    @Test(expected = DispatcherException.class)
    public void wrapC4() throws IOException {
        assertEquals("foozxcvbna123", FACTORY2.wrap(DispatcherInterface3.class, new Delegate4()).dispatch(new ClassC() {
        }, "zxc", "vbn", 'a', 123));
    }

    @Test(expected = DispatcherException.class)
    public void wrapC5() throws IOException {
        assertEquals("foozxcvbna123", FACTORY1.wrap(DispatcherInterface3.class, new Delegate5()).dispatch(new ClassC() {
        }, "zxc", "vbn", 'a', 123));
    }

    @Test(expected = DispatcherException.class)
    public void wrapC6() throws IOException {
        assertEquals("foozxcvbna123", FACTORY2.wrap(DispatcherInterface3.class, new Delegate5()).dispatch(new ClassC() {
        }, "zxc", "vbn", 'a', 123));
    }

    @Test
    public void wrapE1() throws IOException {
        assertEquals("bara123", FACTORY1.wrap(DispatcherInterface3.class, new Delegate3()).dispatch(new ClassE() {
        }, "zxc", "vbn", 'a', 123));
    }

    @Test
    public void wrapE2() throws IOException {
        assertEquals("bara123", FACTORY2.wrap(DispatcherInterface3.class, new Delegate3()).dispatch(new ClassE() {
        }, "zxc", "vbn", 'a', 123));
    }

    @Test
    public void wrapH1() throws IOException {
        assertEquals("iop123", FACTORY1.wrap(DispatcherInterface3.class, new Delegate3())
                .dispatch(new ClassH(), "zxc", "vbn", 'a', 123));
    }

    @Test
    public void wrapH2() throws IOException {
        assertEquals("iop123", FACTORY2.wrap(DispatcherInterface3.class, new Delegate3())
                .dispatch(new ClassH(), "zxc", "vbn", 'a', 123));
    }
}
