package util;

import java.io.*;

public class mySerialization {
    public static boolean verbose = true;
    public static void serialize(Object obj, String path) throws Exception{
        File serializedFile = new File(path);
        FileOutputStream fileOut = new FileOutputStream(serializedFile.getPath());
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(obj);
        out.close();
        fileOut.close();
        if(verbose)
            System.out.println("Serialization of object has been saved to "+serializedFile.getPath());
    }
    public static Object deserialize(String path) throws Exception{
        File serializedFile = new File(path);
        Object obj = null;
        if(serializedFile.exists()){
            if(verbose)
                System.out.println("Serialization exists. Loading from "+serializedFile.getPath());
            FileInputStream fileIn = new FileInputStream(serializedFile.getPath());
            ObjectInputStream in = new ObjectInputStream(fileIn);
            obj = in.readObject();
            in.close();
            fileIn.close();
        }
        else{
            if(verbose)
                System.out.println("Serialization doesn't exist. Return null. ");
        }
        return obj;
    }
}
