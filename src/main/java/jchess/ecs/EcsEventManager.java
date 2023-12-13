package jchess.ecs;

import java.util.HashMap;
import java.util.Map;

public class EcsEventManager {
    private final Map<Class<?>, EcsEvent<?>> events = new HashMap<>();

    public <T extends EcsEvent<?>> void registerEvent(T event) {
        events.put(event.getClass(), event);
    }

    @SuppressWarnings("unchecked")
    public <T extends EcsEvent<?>> T getEvent(Class<? extends EcsEvent<?>> eventClass) {
        return (T) events.get(eventClass);
    }
}
