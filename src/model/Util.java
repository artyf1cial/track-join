package model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Util {

    //in bytes and millis
    public static final double BANDWIDTH = 13.1072;
    public static final int LATENCY = 1;

    public static int getMemoryLength(Object object)
    {
        ByteArrayOutputStream byteObject = null;
        try {
            byteObject = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteObject);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            byteObject.close();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteObject.toByteArray().length;
    }
}
