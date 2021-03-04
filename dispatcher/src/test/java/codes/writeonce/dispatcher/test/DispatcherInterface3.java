package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.OptionalArg;

import javax.annotation.Nonnull;
import java.io.IOException;

interface DispatcherInterface3 {

    String dispatch(ClassA type, @OptionalArg @Nonnull String p1, @OptionalArg @Nonnull String p2, @OptionalArg char p3,
            int p4) throws IOException;
}
