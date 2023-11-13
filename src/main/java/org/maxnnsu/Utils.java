package org.maxnnsu;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class Utils {

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
            if (Objects.isNull(properties.getProperty("testMode"))) {
                throw new RuntimeException("required property: testMode - is missing");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
