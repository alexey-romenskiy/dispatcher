package codes.writeonce.dispatcher;

import java.io.Serial;

public class UnmappableTypeDispatcherException extends DispatcherException {

    @Serial
    private static final long serialVersionUID = -7696579525279959531L;

    public UnmappableTypeDispatcherException(String message) {
        super(message);
    }
}
