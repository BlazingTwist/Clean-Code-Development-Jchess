package jchess.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EcsEvent<TData> {

    private final List<Consumer<TData>> listeners = new ArrayList<>();

    public void fire(TData data) {
        for (Consumer<TData> listener : listeners) {
            listener.accept(data);
        }
    }

    public void addListener(Consumer<TData> listener) {
        listeners.add(listener);
    }
}
