package logback;

import ch.qos.logback.core.rolling.TriggeringPolicy;

import java.io.File;

public class StartupTriggeringPolicy<E> implements TriggeringPolicy<E> {
    private boolean started = false;
    private boolean didTrigger = false;

    @Override
    public boolean isTriggeringEvent(File file, E e) {
        if (didTrigger) return false;

        didTrigger = true;
        return true;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
