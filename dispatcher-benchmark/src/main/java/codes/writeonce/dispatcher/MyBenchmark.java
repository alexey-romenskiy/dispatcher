package codes.writeonce.dispatcher;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Thread;

@State(Thread)
@Fork(1)
@OutputTimeUnit(NANOSECONDS)
@BenchmarkMode(AverageTime)
public class MyBenchmark {

    private MyClass2 myClass2;

    private MyClass3 myClass3;

    private MyInterface javassistDispatcher;

    private MyInterface reflectionDispatcher;

    @Setup
    public void setup() {

        final var myDelegate = new MyDelegate();
        javassistDispatcher = new JavassistDispatcherFactory().wrap(MyInterface.class, myDelegate);
        reflectionDispatcher = new ReflectionDispatcherFactory().wrap(MyInterface.class, myDelegate);
        myClass2 = new MyClass2();
        myClass3 = new MyClass3();
    }

    @Benchmark
    public void testJavassist(@Nonnull Blackhole blackhole) throws IOException {

        blackhole.consume(javassistDispatcher.dispatch(myClass2));
        blackhole.consume(javassistDispatcher.dispatch(myClass3));
    }

    @Benchmark
    public void testReflection(@Nonnull Blackhole blackhole) throws IOException {

        blackhole.consume(reflectionDispatcher.dispatch(myClass2));
        blackhole.consume(reflectionDispatcher.dispatch(myClass3));
    }

    @Benchmark
    public void testBaseline(@Nonnull Blackhole blackhole) {

        blackhole.consume(null);
        blackhole.consume(null);
    }

    public static void main(String[] args) throws RunnerException, IOException {

        if (false) {
            final var wrap1 = new JavassistDispatcherFactory().wrap(MyInterface.class, new MyDelegate());
            wrap1.dispatch(new MyClass2());
            wrap1.dispatch(new MyClass3());

            final var wrap2 = new ReflectionDispatcherFactory().wrap(MyInterface.class, new MyDelegate());
            wrap2.dispatch(new MyClass2());
            wrap2.dispatch(new MyClass3());
        } else {
            final var options = new OptionsBuilder()
                    .include(MyBenchmark.class.getSimpleName())
                    .build();

            new Runner(options).run();
        }
    }
}
