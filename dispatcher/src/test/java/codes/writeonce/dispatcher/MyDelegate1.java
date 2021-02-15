package codes.writeonce.dispatcher;

import java.io.IOException;

class MyDelegate1 implements Intf {

    @Endpoint
    public String accept(MyClass2 builder) {
        return "foo";
    }

    @Endpoint
    public String accept(MyClass5 builder) {
        return "bar";
    }

    private String accept(MyClass1 builder) {
        return "123";
    }

    @Endpoint
    public String accept(MyClass3 builder) throws IOException {
        throw new IOException();
    }
}
