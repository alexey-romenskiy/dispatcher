package codes.writeonce.dispatcher;

interface Intf {

    @Endpoint
    default String accept(MyClass2 builder) {
        return "foo";
    }
}
