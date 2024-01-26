package jchess.gamemode.hex3p;

import dx.schema.types.PieceType;
import jchess.common.components.PieceIdentifier;
import jchess.common.moveset.special.*;
import jchess.ecs.Entity;
import jchess.common.moveset.special.ShapeShifting;
import jchess.el.CompiledTileExpression;
import jchess.el.v2.ExpressionCompiler;
import jchess.el.v2.TileExpression;
import jchess.gamemode.PieceStore;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static jchess.el.v2.TileExpression.neighbor;
import static jchess.el.v2.TileExpression.regex;
import static jchess.el.v2.TileExpression.rotations;

public enum Hex3pPieces implements PieceStore.IPieceDefinitionProvider {
    Rook(PieceType.ROOK, new PieceStore.PieceDefinition(
            "R",
            rotations(regex("30+", false), 6)
    )),
    Knight(PieceType.KNIGHT, new PieceStore.PieceDefinition(
            "N",
            rotations(regex("30.0 30.60", true), 6)
    )),
    Bishop(PieceType.BISHOP, new PieceStore.PieceDefinition(
            "B",
            rotations(regex("0+", false), 6)
    )),
    Queen(PieceType.QUEEN, new PieceStore.PieceDefinition(
            "Q",
            TileExpression.or(Rook.pieceDefinition.baseMoves(), Bishop.pieceDefinition.baseMoves())
    )),
    King(PieceType.KING, new PieceStore.PieceDefinition(
            "K",
            rotations(regex("0", false), 12),
            (game, kingIdentifier) -> new Castling(
                    game, kingIdentifier, Rook.pieceType, 90, 270,
                    regex("270.270.270", true), regex("90.90", true)
            )
    )),
    Pawn(PieceType.PAWN, new PieceStore.PieceDefinition(
            "",
            TileExpression.or(
                    TileExpression.filter(TileExpression.neighbor(330, 30), TileExpression.FILTER_EMPTY_TILE),
                    TileExpression.filter2(TileExpression.neighbor(300, 60), TileExpression.FILTER_CAPTURE)
            ),
            (game, pawnIdentifier) -> new SpecialFirstMove(
                    game, pawnIdentifier,
                    TileExpression.filter(regex("330.330 30.30", false), TileExpression.FILTER_EMPTY_TILE)
            ),
            (game, pawnId) -> new EnPassant(game, pawnId, PieceType.PAWN, new int[]{330, 30}, new int[]{300, 60}),
            (game, pawnId) -> {
                int owner = pawnId.ownerId();
                return new PawnPromotion(
                        game, getPromotionTilePredicate(TileExpression.neighbor(330, 30), pawnId),
                        Stream.of(Rook, Knight, Bishop, Queen).map(type -> getPiece(type, owner)).toArray(dx.schema.message.Piece[]::new)
                );
            }
    )),
    Archer(PieceType.ARCHER, new PieceStore.PieceDefinition(
            "A",
            TileExpression.filter(
                    rotations(regex("0{1,2}", false), 12),
                    TileExpression.FILTER_EMPTY_TILE
            ),
            (game, archerIdentifier) -> new RangedAttack(
                    game, archerIdentifier,
                    TileExpression.rotations(regex("(0 30 60){1,2}", true), 6)
            )
    )),

    Pegasus(PieceType.PEGASUS, new PieceStore.PieceDefinition(
            "PE",
            rotations(regex("(30 90){1,3}", true), 6)
    )),
    Catapult(PieceType.CATAPULT, new PieceStore.PieceDefinition(
            "C",
            TileExpression.filter(rotations(neighbor(0), 12), TileExpression.FILTER_EMPTY_TILE),
            (game, catapultId) -> new RangedAttack(
                    game, catapultId,
                    TileExpression.rotations(regex("(30 90){4,6}", true), 6)
            )
    )),
    Skrull(PieceType.SKRULL, new PieceStore.PieceDefinition(
            "S",
            Pawn.pieceDefinition.baseMoves(),
            (game, skrullId) -> new ShapeShifting(game, skrullId, 1, 2,
                    PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN, PieceType.ARCHER, PieceType.CATAPULT, PieceType.PAWN
            )
    ));


    private final PieceType pieceType;
    private final PieceStore.PieceDefinition pieceDefinition;

    Hex3pPieces(PieceType pieceType, PieceStore.PieceDefinition pieceDefinition) {
        this.pieceType = pieceType;
        this.pieceDefinition = pieceDefinition;
    }

    private static Predicate<Entity> getPromotionTilePredicate(ExpressionCompiler forwardTiles, PieceIdentifier pawnId) {
        CompiledTileExpression forwardExpression = forwardTiles.toV1(pawnId);
        return tile -> {
            if (tile.tile == null) return false;

            // if both forward tiles exit the board bounds (= empty result) -> pawn can promote
            return forwardExpression.findTiles(tile).findAny().isEmpty();
        };
    }

    private static dx.schema.message.Piece getPiece(Hex3pPieces pieceType, int ownerId) {
        dx.schema.message.Piece result = new dx.schema.message.Piece();
        result.setPieceTypeId(pieceType.pieceType);
        result.setPlayerIdx(ownerId);
        return result;
    }

    @Override
    public PieceType getPieceType() {
        return pieceType;
    }

    @Override
    public PieceStore.PieceDefinition getPieceDefinition() {
        return pieceDefinition;
    }
}
