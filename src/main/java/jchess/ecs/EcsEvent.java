package jchess.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EcsEvent<TData> {

    private final List<Consumer<TData>> preListeners = new ArrayList<>();
    private final List<Consumer<TData>> postListeners = new ArrayList<>();

    public void fire(TData data) {
        for (Consumer<TData> preListener : preListeners) {
            preListener.accept(data);
        }
        for (Consumer<TData> postListener : postListeners) {
            postListener.accept(data);
        }
    }

    public void addPreEventListener(Consumer<TData> listener) {
        preListeners.add(listener);
    }

    public void addPostEventListener(Consumer<TData> listener) {
        postListeners.add(listener);
    }
}
