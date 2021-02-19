package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.AmbiguousTypeDispatcherException;
import codes.writeonce.dispatcher.DispatcherFactory;
import codes.writeonce.dispatcher.JavassistDispatcherFactory;
import codes.writeonce.dispatcher.ReflectionDispatcherFactory;
import codes.writeonce.dispatcher.UnmappableTypeDispatcherException;
import org.junit.Test;

public class FakeDispatcherFactoryTest {

    private static final DispatcherFactory FACTORY1 = new JavassistDispatcherFactory();
    private static final DispatcherFactory FACTORY2 = new ReflectionDispatcherFactory();

    @Test(expected = UnmappableTypeDispatcherException.class)
    public void wrapA1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassA.class);
    }

    @Test(expected = UnmappableTypeDispatcherException.class)
    public void wrapA2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassA.class);
    }

    @Test(expected = UnmappableTypeDispatcherException.class)
    public void wrapB1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassB.class);
    }

    @Test(expected = UnmappableTypeDispatcherException.class)
    public void wrapB2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassB.class);
    }

    @Test
    public void wrapC1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassC.class);
    }

    @Test
    public void wrapC2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassC.class);
    }

    @Test
    public void wrapD1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassD.class);
    }

    @Test
    public void wrapD2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassD.class);
    }

    @Test
    public void wrapE1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassE.class);
    }

    @Test
    public void wrapE2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassE.class);
    }

    @Test(expected = AmbiguousTypeDispatcherException.class)
    public void wrapF1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassF.class);
    }

    @Test(expected = AmbiguousTypeDispatcherException.class)
    public void wrapF2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassF.class);
    }

    @Test
    public void wrapG1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassG.class);
    }

    @Test
    public void wrapG2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassG.class);
    }

    @Test
    public void wrapH1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassH.class);
    }

    @Test
    public void wrapH2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassH.class);
    }

    @Test(expected = AmbiguousTypeDispatcherException.class)
    public void wrapI1() {
        FACTORY1.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassI.class);
    }

    @Test(expected = AmbiguousTypeDispatcherException.class)
    public void wrapI2() {
        FACTORY2.test(DispatcherInterface.class, new Object[]{new Delegate()}, ClassI.class);
    }
}
