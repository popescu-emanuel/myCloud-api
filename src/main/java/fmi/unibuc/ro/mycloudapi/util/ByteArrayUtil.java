package fmi.unibuc.ro.mycloudapi.util;

import org.springframework.stereotype.Component;

@Component
public class ByteArrayUtil {

    public byte[] additionToByteArray(byte[] bytes, int toAdd){
        if(bytes.length < 1){
            throw new UnsupportedOperationException();
        }

        byte[] result = new byte[bytes.length];

        for(int i = 0; i < bytes.length; i++){
            result[i] = (byte)(bytes[i] + toAdd);
        }
        return result;
    }

}
