package jchess.common.moveset.special;

import dx.schema.conf.Piece;
import jchess.common.IChessGame;
import jchess.common.components.PieceIdentifier;
import jchess.common.events.BoardInitializedEvent;
import jchess.common.events.PieceMoveEvent;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.el.CompiledTileExpression;
import jchess.el.TileExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Castling implements ISpecialRule {
    private static final Logger logger = LoggerFactory.getLogger(Castling.class);

    private final IChessGame game;
    private final TileExpression kingMoveLeft;
    private final TileExpression kingMoveRight;
    private final PieceIdentifier kingId;
    private final Piece.PieceType rookTypeId;
    private final int rightRookDirection;
    private final int leftRookDirection;

    private PieceIdentifier leftRookId;
    private PieceIdentifier rightRookId;
    private boolean kingMoved = false;
    private boolean leftRookMoved = false;
    private boolean rightRookMoved = false;

    public Castling(
            IChessGame game, PieceIdentifier kingId, Piece.PieceType rookTypeId, int rightRookDirection, int leftRookDirection,
            TileExpression kingMoveLeft, TileExpression kingMoveRight
    ) {
        this.game = game;
        this.kingMoveLeft = kingMoveLeft;
        this.kingMoveRight = kingMoveRight;
        this.kingId = kingId;
        this.rookTypeId = rookTypeId;
        this.rightRookDirection = rightRookDirection;
        this.leftRookDirection = leftRookDirection;

        game.getEventManager().getEvent(BoardInitializedEvent.class).addListener(_void -> lookupRooks());
        game.getEventManager().<PieceMoveEvent>getEvent(PieceMoveEvent.class).addListener(this::onPieceMove);
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

        rightRookId = rightRooks.get(0).piece.identifier;
        leftRookId = leftRooks.get(0).piece.identifier;
    }

    private List<Entity> findRooks(Entity fromTile, int direction) {
        return TileExpression.repeat(TileExpression.neighbor(direction), 1, -1, true).compile(fromTile.piece.identifier)
                .findTiles(fromTile)
                .filter(tile -> tile.piece != null && tile.piece.identifier.pieceType() == rookTypeId)
                .toList();
    }

    private void onPieceMove(PieceMoveEvent.PieceMove move) {
        if (leftRookId == null || rightRookId == null) {
            logger.error("Castling observed PieceMove, but Rooks were not identified yet! LeftRookId: {} | RightRookId: {}", leftRookId, rightRookId);
        }

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
        return getCastleMove(king, kingMoveLeft, leftRookId, leftRookMoved, rightRookDirection, leftRookDirection);
    }

    private MoveIntention getRightCastle(Entity king) {
        return getCastleMove(king, kingMoveRight, rightRookId, rightRookMoved, leftRookDirection, rightRookDirection);
    }

    private MoveIntention getCastleMove(
            Entity king, TileExpression kingMove,
            PieceIdentifier rookId, boolean rookHasMoved,
            int rookToKingDirection, int kingToRookDirection
    ) {
        if (rookHasMoved) {
            return null;
        }

        Entity kingMoveTile = checkKingMoveTile(king, kingMove);
        if (kingMoveTile == null) { // tile not empty, or king would be attacked on the path
            return null;
        }

        CompiledTileExpression stepRookToKing = TileExpression.neighbor(rookToKingDirection).compile(king.piece.identifier);
        CompiledTileExpression stepKingToRook = TileExpression.neighbor(kingToRookDirection).compile(king.piece.identifier);

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

    private Entity checkKingMoveTile(Entity king, TileExpression kingMove) {
        return TileExpression.filter(kingMove, tile -> tile.piece == null && !tile.isAttacked())
                .compile(king.piece.identifier)
                .findTiles(king)
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
