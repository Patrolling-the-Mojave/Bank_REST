package com.example.bankcards.util;

import com.example.bankcards.exception.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class EncryptionUtil {
    @Value("${app.encryption.key}")
    private String encryptionKey;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public String encrypt(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    padKey(encryptionKey).getBytes(),
                    ALGORITHM
            );
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptionException("Ошибка шифрования данных", e);
        }
    }


    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    padKey(encryptionKey).getBytes(),
                    ALGORITHM
            );
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new EncryptionException("Ошибка расшифровки данных", e);
        }
    }

    private String padKey(String key) {
        StringBuilder sb = new StringBuilder(key);
        while (sb.length() < 32) {
            sb.append("0");
        }
        return sb.substring(0, 32);
    }


}
