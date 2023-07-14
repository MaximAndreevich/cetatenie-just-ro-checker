package org.maxnnsu;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class TimerUtil {
    private Instant startTime;

    public TimerUtil() {
        start();
    }

    public void start() {
        startTime = Instant.now();
    }

    public void reset() {
        startTime = Instant.now();
    }

    public String stop() {
        if (Objects.isNull(startTime)) {
            return "0 sec";
        }
        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds() % 60;

        return minutes + "min " + seconds + "sec";
    }
}
