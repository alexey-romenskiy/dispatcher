package codes.writeonce.dispatcher;

import java.io.Serial;

public class AmbiguousTypeDispatcherException extends DispatcherException {

    @Serial
    private static final long serialVersionUID = -2424190306022793859L;

    public AmbiguousTypeDispatcherException(String message) {
        super(message);
    }
}
