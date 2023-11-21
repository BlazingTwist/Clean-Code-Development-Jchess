package jchess.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

public class EcsEvent<TData> {
    public static final int PRIORITY_HIGH = 100;
    public static final int PRIORITY_NORMAL = 50;
    public static final int PRIORITY_LOW = 0;

    private final TreeMap<Integer, List<ISystem<TData>>> systemsByPriority = new TreeMap<>(
            (a, b) -> Integer.compare(b, a)
    );
    private final List<Consumer<TData>> preListeners = new ArrayList<>();
    private final List<Consumer<TData>> postListeners = new ArrayList<>();
    private final EntityManager entityManager;

    public EcsEvent(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Registers a System with normal priority ({@link #PRIORITY_NORMAL})
     */
    public void registerSystem(ISystem<TData> system) {
        registerSystem(system, PRIORITY_NORMAL);
    }

    /**
     * Registers a System with a given priority.
     * @param system   the System to register
     * @param priority higher priority System execute first
     */
    public void registerSystem(ISystem<TData> system, int priority) {
        List<ISystem<TData>> systems = systemsByPriority.computeIfAbsent(priority, x -> new ArrayList<>());
        systems.add(system);
    }

    public void fire(TData data) {
        for (Consumer<TData> preListener : preListeners) {
            preListener.accept(data);
        }
        for (List<ISystem<TData>> systems : systemsByPriority.values()) {
            for (ISystem<TData> system : systems) {
                entityManager.runSystem(system, data);
            }
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
