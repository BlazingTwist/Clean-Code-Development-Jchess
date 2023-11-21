package jchess.game.el;

import jchess.ecs.Entity;

import java.util.function.Function;
import java.util.stream.Stream;

public class CompiledTileExpression {

    private final Function<Stream<Entity>, Stream<Entity>> operator;

    public CompiledTileExpression(Function<Stream<Entity>, Stream<Entity>> operator) {
        this.operator = operator;
    }

    public Stream<Entity> findTiles(Entity startTile) {
        return operator.apply(Stream.of(startTile)).distinct();
    }
}
