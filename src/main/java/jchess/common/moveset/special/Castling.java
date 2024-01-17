package jchess.common.moveset.special;

import dx.schema.types.PieceType;
import jchess.common.IChessGame;
import jchess.common.components.PieceIdentifier;
import jchess.common.events.BoardInitializedEvent;
import jchess.common.events.PieceMoveEvent;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.el.CompiledTileExpression;
import jchess.el.v2.ExpressionCompiler;
import jchess.el.v2.TileExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Castling implements ISpecialRule {
    private static final Logger logger = LoggerFactory.getLogger(Castling.class);

    private final IChessGame game;
    private final CompiledTileExpression kingMoveLeft;
    private final CompiledTileExpression kingMoveRight;
    private final PieceIdentifier kingId;
    private final PieceType rookTypeId;
    private final int rightRookDirection;
    private final int leftRookDirection;
    private final CompiledTileExpression stepLeftExpression;
    private final CompiledTileExpression stepRightExpression;

    private PieceIdentifier leftRookId;
    private PieceIdentifier rightRookId;
    private boolean kingMoved = false;
    private boolean leftRookMoved = false;
    private boolean rightRookMoved = false;

    public Castling(
            IChessGame game, PieceIdentifier kingId, PieceType rookTypeId, int rightRookDirection, int leftRookDirection,
            ExpressionCompiler kingMoveLeft, ExpressionCompiler kingMoveRight
    ) {
        this.game = game;
        this.kingMoveLeft = kingMoveLeft.toV1(kingId);
        this.kingMoveRight = kingMoveRight.toV1(kingId);
        this.kingId = kingId;
        this.rookTypeId = rookTypeId;
        this.rightRookDirection = rightRookDirection;
        this.leftRookDirection = leftRookDirection;
        this.stepRightExpression = TileExpression.neighbor(rightRookDirection).toV1(kingId);
        this.stepLeftExpression = TileExpression.neighbor(leftRookDirection).toV1(kingId);

        game.getEventManager().getEvent(BoardInitializedEvent.class).addListener(_void -> lookupRooks());
        game.getEventManager().getEvent(PieceMoveEvent.class).addListener(this::onPieceMove);
    }

    private void lookupRooks() {
        Entity kingEntity = game.getEntityManager().getEntities().stream()
                .filter(entity -> entity.piece != null && entity.piece.identifier == kingId)
                .findFirst().orElse(null);

        if (kingEntity == null) {
            logger.error("Unable to find King on board. KingId: '{}'", kingId);
            return;
        }

        if (kingEntity.tile == null) {
            logger.error("King piece is not standing on any tile!");
            return;
        }

        List<Entity> rightRooks = findRooks(kingEntity, rightRookDirection);
        List<Entity> leftRooks = findRooks(kingEntity, leftRookDirection);
        if (rightRooks.size() != 1) {
            logger.error("Expected exactly 1 rook to the right of king, but found {}", rightRooks.size());
            return;
        }
        if (leftRooks.size() != 1) {
            logger.error("Expected exactly 1 rook to the left of king, but found {}", leftRooks.size());
            return;
        }

        //noinspection DataFlowIssue
        rightRookId = rightRooks.get(0).piece.identifier;
        //noinspection DataFlowIssue
        leftRookId = leftRooks.get(0).piece.identifier;
    }

    private List<Entity> findRooks(Entity fromTile, int direction) {
        assert fromTile.piece != null;
        return TileExpression.repeat(TileExpression.neighbor(direction), 1, -1, true)
                .toV1(fromTile.piece.identifier)
                .findTiles(fromTile)
                .filter(tile -> tile.piece != null && tile.piece.identifier.pieceType() == rookTypeId)
                .toList();
    }

    private void onPieceMove(PieceMoveEvent.PieceMove move) {
        if (leftRookId == null || rightRookId == null) {
            logger.error("Castling observed PieceMove, but Rooks were not identified yet! LeftRookId: {} | RightRookId: {}", leftRookId, rightRookId);
        }

        assert move.toTile().piece != null; // move always contains to moved piece in 'toTile'
        PieceIdentifier movedPiece = move.toTile().piece.identifier;
        if (movedPiece == this.kingId) {
            kingMoved = true;
        } else if (movedPiece == leftRookId) {
            leftRookMoved = true;
        } else if (movedPiece == rightRookId) {
            rightRookMoved = true;
        }
    }

    private MoveIntention getLeftCastle(Entity king) {
        return getCastleMove(king, kingMoveLeft, leftRookId, leftRookMoved, stepRightExpression, stepLeftExpression);
    }

    private MoveIntention getRightCastle(Entity king) {
        return getCastleMove(king, kingMoveRight, rightRookId, rightRookMoved, stepLeftExpression, stepRightExpression);
    }

    private MoveIntention getCastleMove(
            Entity king, CompiledTileExpression kingMove,
            PieceIdentifier rookId, boolean rookHasMoved,
            CompiledTileExpression stepRookToKing, CompiledTileExpression stepKingToRook
    ) {
        assert king.piece != null;
        if (rookHasMoved) {
            return null;
        }

        Entity kingMoveTile = checkKingMoveTile(king, kingMove);
        if (kingMoveTile == null) { // tile not empty, or king would be attacked on the path
            return null;
        }

        Entity rookMoveTile = stepRookToKing.findTiles(kingMoveTile).findFirst().orElse(null);
        Entity rookTile = kingMoveTile;
        while (true) {
            rookTile = stepKingToRook.findTiles(rookTile).findFirst().orElse(null);
            if (rookTile == null) {
                logger.error("Exceeded board bounds while searching for rook. Did rook already move?");
                return null;
            }
            if (rookTile.piece != null) {
                if (rookTile.piece.identifier == rookId) {
                    return getCastleMove(king, kingMoveTile, rookTile, rookMoveTile);
                } else {
                    return null;
                }
            }
        }
    }

    private Entity checkKingMoveTile(Entity king, CompiledTileExpression kingMove) {
        assert king.piece != null;
        // TODO erja - checkDetection is not correct.
        return kingMove.findTiles(king)
                .filter(tile -> tile.piece == null && !tile.isAttacked())
                .findFirst().orElse(null);
    }

    private MoveIntention getCastleMove(Entity kingStart, Entity kingEnd, Entity rookStart, Entity rookEnd) {
        return new MoveIntention(kingEnd, () -> {
            rookEnd.piece = rookStart.piece;
            rookStart.piece = null;
            game.movePiece(kingStart, kingEnd, Castling.class);
        }, new CastlingSimulator(rookStart, rookEnd, kingStart, kingEnd));
    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity king, Stream<MoveIntention> baseMoves) {
        if (kingMoved || king.isAttacked()) {
            return baseMoves;
        }

        List<MoveIntention> moves = new ArrayList<>();

        MoveIntention leftCastle = getLeftCastle(king);
        if (leftCastle != null) moves.add(leftCastle);

        MoveIntention rightCastle = getRightCastle(king);
        if (rightCastle != null) moves.add(rightCastle);

        return Stream.concat(baseMoves, moves.stream());
    }

    private record CastlingSimulator(
            Entity rookStartTile, Entity rookEndTile, Entity kingStartTile, Entity kingEndTile
    ) implements MoveIntention.IMoveSimulator {

        @Override
        public void simulate() {
            rookEndTile.piece = rookStartTile.piece;
            rookStartTile.piece = null;
            kingEndTile.piece = kingStartTile.piece;
            kingStartTile.piece = null;
        }

        @Override
        public void revert() {
            rookStartTile.piece = rookEndTile.piece;
            rookEndTile.piece = null;
            kingStartTile.piece = kingEndTile.piece;
            kingEndTile.piece = null;
        }
    }
}
