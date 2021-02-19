package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.Endpoint;

interface DelegateInterface {

    default String accept(ClassB type) {
        return "456";
    }

    @Endpoint
    default String accept(ClassC type) {
        return "foo";
    }
}
