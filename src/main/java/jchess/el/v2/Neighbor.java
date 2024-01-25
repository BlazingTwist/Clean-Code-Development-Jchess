package jchess.el.v2;

import jchess.common.components.PieceIdentifier;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

final class Neighbor implements ExpressionCompiler {
    private final int[] directions;

    public Neighbor(int... directions) {
        this.directions = directions;
    }

    @Override
    public TileExpression.CompiledExpression compile(PieceIdentifier movingPiece) {
        if (directions.length == 0) {
            return startTiles -> Stream.empty();
        }

        if (directions.length == 1) {
            final int neighborDirection = (movingPiece.forwardBasis() + directions[0]) % 360;
            return startTiles -> startTiles.map(tile -> {
                        assert tile.tile != null;
                        return tile.tile.getTile(neighborDirection);
                    })
                    .filter(Objects::nonNull);
        }

        final int[] neighborDirections = Arrays.stream(directions).map(dir -> (movingPiece.forwardBasis() + dir) % 360).toArray();
        return startTiles -> startTiles.flatMap(tile -> {
            assert tile.tile != null;
            return Arrays.stream(neighborDirections)
                    .mapToObj(dir -> tile.tile.getTile(dir))
                    .filter(Objects::nonNull);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Neighbor other = (Neighbor) o;
        return Arrays.equals(directions, other.directions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(directions);
    }

    @Override
    public String toString() {
        return Arrays.toString(directions);
    }
}
