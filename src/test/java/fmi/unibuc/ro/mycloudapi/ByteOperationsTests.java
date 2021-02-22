package fmi.unibuc.ro.mycloudapi;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.Arrays;

@SpringBootTest
@Slf4j
public class ByteOperationsTests {

    @Test
    void givenRandom128bitNumber_whenAddAndRemoveOne_thenSameNumber(){
        SecureRandom random = new SecureRandom();
        byte[] random128bitKey = new byte[16]; // 128 bits are converted to 16 bytes;
        random.nextBytes(random128bitKey);

        log.debug("Generated sequence: {}", random128bitKey);

        byte[] additionResult = additionToByteArray(random128bitKey, 1);
        log.debug("Addition sequence: {}", additionResult);

        byte[] subtractResult = additionToByteArray(additionResult, -1);
        log.debug("Subtract sequence: {}", additionResult);

        assert Arrays.equals(random128bitKey, subtractResult);
    }

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
