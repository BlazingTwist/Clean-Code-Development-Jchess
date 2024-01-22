package jchess.ecs;

import java.util.HashMap;
import java.util.Map;

public class EcsEventManager {
    private final Map<Class<?>, EcsEvent<?>> events = new HashMap<>();

    public <T extends EcsEvent<?>> void registerEvent(T event) {
        events.put(event.getClass(), event);
    }

    @SuppressWarnings("unchecked")
    public <V, T extends EcsEvent<V>> T getEvent(Class<? extends EcsEvent<V>> eventClass) {
        return (T) events.get(eventClass);
    }
}
