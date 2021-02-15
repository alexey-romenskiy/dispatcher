package codes.writeonce.dispatcher;

import java.io.Serial;

public class DispatcherException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1374483136194676490L;

    public DispatcherException(String message) {
        super(message);
    }

    public DispatcherException(Throwable cause) {
        super(cause);
    }
}
