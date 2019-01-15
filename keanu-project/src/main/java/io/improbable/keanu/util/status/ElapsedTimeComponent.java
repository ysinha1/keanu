package io.improbable.keanu.util.status;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

public class ElapsedTimeComponent implements StatusBarComponent {
    @Getter
    private final Instant startTime = Instant.now();

    @Override
    public String render() {
        Duration elapsed = Duration.between(startTime, Instant.now());
        return "Elapsed time: " + elapsed.toString().substring(2);
    }
}
