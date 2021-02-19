package codes.writeonce.dispatcher.test;

import codes.writeonce.dispatcher.Endpoint;

import java.io.IOException;

class Delegate2 {

    @Endpoint
    public boolean dispatchBoolean(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public char dispatchChar(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public byte dispatchByte(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public short dispatchShort(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public int dispatchInt(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public long dispatchLong(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public float dispatchFloat(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public double dispatchDouble(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public void dispatchVoid(ClassA type) throws IOException {
        throw new IOException();
    }

    @Endpoint
    public String dispatchString(ClassA type) throws IOException {
        throw new IOException();
    }
}
