package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.Endpoint;

import java.io.IOException;

class Delegate implements DelegateInterface {

    public String accept(ClassA type) {
        return "123";
    }

    @Endpoint
    public String accept(ClassE type) {
        return "bar";
    }

    @Endpoint
    public String accept(ClassH type) throws IOException {
        throw new IOException();
    }
}
