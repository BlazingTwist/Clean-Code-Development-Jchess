package jchess.ecs;

import java.util.ArrayList;
import java.util.List;

public class EntityManager {
    private final List<Entity> entities = new ArrayList<>();

    public Entity createEntity() {
        Entity entity = new Entity();
        entities.add(entity);
        return entity;
    }

    public List<Entity> getEntities() {
        return entities;
    }

}
