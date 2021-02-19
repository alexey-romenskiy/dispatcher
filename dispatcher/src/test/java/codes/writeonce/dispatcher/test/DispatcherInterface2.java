package codes.writeonce.dispatcher.test;

import java.io.IOException;

interface DispatcherInterface2 {

    boolean dispatchBoolean(ClassA type) throws IOException;

    char dispatchChar(ClassA type) throws IOException;

    byte dispatchByte(ClassA type) throws IOException;

    short dispatchShort(ClassA type) throws IOException;

    int dispatchInt(ClassA type) throws IOException;

    long dispatchLong(ClassA type) throws IOException;

    float dispatchFloat(ClassA type) throws IOException;

    double dispatchDouble(ClassA type) throws IOException;

    void dispatchVoid(ClassA type) throws IOException;

    String dispatchString(ClassA type) throws IOException;
}
