package org.maxnnsu;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Properties;

public class Utils {

    public static int generatePDDocumentHash(PDDocument doc) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Получение байтового массива содержимого документа
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            byte[] content = baos.toByteArray();

            // Обновление хеш-функции с байтовым массивом содержимого
            md.update(content);

            // Получение сгенерированного хеш-кода в виде байтового массива
            byte[] hash = md.digest();

            // Преобразование байтового массива хеша в целочисленное значение
            return byteArrayToInt(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0; // или любое другое значение по умолчанию в случае ошибки
        }
    }

    private static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) + (bytes[i] & 0xff);
        }
        return value;
    }

    public static Properties loadAppProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = Main.class.getResourceAsStream("/configuration.properties")) {
            properties.load(inputStream);
            if(Objects.isNull(properties.getProperty("testMode"))){
                throw new RuntimeException("required property: testMode - is missing");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
