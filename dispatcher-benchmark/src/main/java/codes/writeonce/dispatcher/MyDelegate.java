package codes.writeonce.dispatcher;

public class MyDelegate {

    @Endpoint
    public String accept(MyClass1 builder) {
        return "foo";
    }

    @Endpoint
    public String accept(MyClass2 builder) {
        return "bar";
    }
}
