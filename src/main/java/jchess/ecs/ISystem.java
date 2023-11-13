package jchess.ecs;

public interface ISystem<TData> {
    boolean acceptsEntity(Entity entity);
    void update(Entity entity, TData data);
}
