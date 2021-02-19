package codes.writeonce.dispatcher;

import javax.annotation.Nonnull;

public interface DispatcherFactory {

    @Nonnull
    <T> T wrap(@Nonnull Class<T> type, @Nonnull Object... delegates) throws DispatcherException;

    void test(@Nonnull Class<?> type, @Nonnull Object[] delegates, @Nonnull Class<?>... subtypes)
            throws DispatcherException;
}
