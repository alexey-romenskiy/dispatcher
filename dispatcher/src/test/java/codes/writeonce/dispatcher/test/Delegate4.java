package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.Endpoint;

import javax.annotation.Nonnull;

class Delegate4 implements DelegateInterface {

    @Endpoint
    public String accept(ClassC type, @Nonnull String p1, char p3, int p4) {
        return "foo" + p1 + p3 + p4;
    }

    @Endpoint
    public String accept(ClassE type, char p3, int p4) {
        return "bar" + p3 + p4;
    }

    @Endpoint
    public String accept(ClassH type, int p4) {
        return "iop" + p4;
    }
}
