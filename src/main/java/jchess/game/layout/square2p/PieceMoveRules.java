package jchess.game.layout.square2p;

import jchess.ecs.Entity;
import jchess.game.common.components.PieceIdentifier;
import jchess.game.el.CompiledTileExpression;
import jchess.game.el.IPieceMoveRules;
import jchess.game.el.TileExpression;

import java.util.stream.Stream;

public class PieceMoveRules implements IPieceMoveRules {
    private final CompiledTileExpression baseMoveSet;

    public PieceMoveRules(PieceType pieceType, PieceIdentifier identifier) {
        baseMoveSet = pieceType.getBaseMoves().compile(identifier);
    }

    @Override
    public Stream<Entity> findValidMoves(Entity movingEntity) {
        Stream<Entity> baseMoves = this.baseMoveSet.findTiles(movingEntity);
        // TODO erja, gather special moves

        // Constraint: after moving, the King must always be not in check
        // TODO erja, for every move, verify that king is not in check

        return baseMoves;
    }

    public enum PieceType {
        Rook(
                0, "R",
                TileExpression.regex("0+ 90+ 180+ 270+", false)
        ),
        Knight(
                1, "N",
                TileExpression.regex("0.315 0.45 90.45 90.135 180.135 180.225 270.225 270.315", true)
        ),
        Bishop(
                2, "B",
                TileExpression.regex("45+ 135+ 225+ 315+", false)
        ),
        Queen(
                3, "Q",
                TileExpression.or(Rook.baseMoves, Bishop.baseMoves)
        ),
        King(
                4, "K",
                TileExpression.regex("0 45 90 135 180 225 270 315", false)
        ),
        Pawn(
                5, "",
                TileExpression.or(
                        TileExpression.filter(TileExpression.neighbor(0), TileExpression.FILTER_EMPTY_TILE),
                        TileExpression.filter(TileExpression.neighbor(45, 315), TileExpression.FILTER_CAPTURE)
                )
        );

        private final int id;
        private final String shortName;
        private final TileExpression baseMoves;

        PieceType(int id, String shortName, TileExpression baseMoves) {
            this.id = id;
            this.shortName = shortName;
            this.baseMoves = baseMoves;
        }

        public int getId() {
            return id;
        }

        public String getShortName() {
            return shortName;
        }

        public TileExpression getBaseMoves() {
            return baseMoves;
        }
    }
}
