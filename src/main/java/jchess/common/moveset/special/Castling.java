package jchess.common.moveset.special;

import dx.schema.types.PieceType;
import jchess.common.IChessGame;
import jchess.common.components.PieceIdentifier;
import jchess.common.events.BoardInitializedEvent;
import jchess.common.events.PieceMoveEvent;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.common.state.impl.BooleanState;
import jchess.ecs.Entity;
import jchess.el.CompiledTileExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static jchess.el.v2.TileExpression.filter;
import static jchess.el.v2.TileExpression.neighbor;
import static jchess.el.v2.TileExpression.repeat;

public class Castling implements ISpecialRule {
    private static final Logger logger = LoggerFactory.getLogger(Castling.class);

    private final IChessGame game;
    private final CompiledTileExpression kingMove;
    private final PieceIdentifier kingId;
    private final PieceType rookTypeId;
    private final int kingToRookDir;
    private final CompiledTileExpression stepToRookExpression;
    private final CompiledTileExpression stepToKingExpression;
    private final BooleanState kingMoved;
    private final BooleanState rookMoved;

    private Entity rookTile;
    private PieceIdentifier rookId;

    public Castling(
            IChessGame game, PieceIdentifier kingId, PieceType rookTypeId, int kingToRookDir, int numStepsKing
    ) {
        this.game = game;
        this.kingMove = repeat(filter(neighbor(kingToRookDir), this::kingStepFilter), numStepsKing, numStepsKing, true).toV1(kingId);
        this.kingId = kingId;
        this.rookTypeId = rookTypeId;
        this.kingToRookDir = kingToRookDir;
        this.stepToRookExpression = neighbor(kingToRookDir).toV1(kingId);
        this.stepToKingExpression = neighbor(kingToRookDir + 180).toV1(kingId);

        kingMoved = new BooleanState(false);
        rookMoved = new BooleanState(false);

        game.getStateManager().registerState(kingMoved, rookMoved);

        game.getEventManager().getEvent(BoardInitializedEvent.class).addListener(_void -> lookupRook());
        game.getEventManager().getEvent(PieceMoveEvent.class).addListener(this::onPieceMove);
    }

    private boolean kingStepFilter(Entity moveTo) {
        // Castling requires that:
        // - the tiles the king moves over are empty
        // - the tiles the king moves over are not attacked
        return moveTo.piece == null && !moveTo.isAttacked(kingId.ownerId());
    }

    private void lookupRook() {
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

        List<Entity> rooks = findRooks(kingEntity, kingToRookDir);
        if (rooks.size() != 1) {
            logger.error("Expected exactly 1 rook in direction {} from king, but found {}", kingToRookDir, rooks.size());
            return;
        }

        rookTile = rooks.get(0);
        assert rookTile.piece != null;
        rookId = rookTile.piece.identifier;
    }

    private List<Entity> findRooks(Entity fromTile, int direction) {
        assert fromTile.piece != null;
        return repeat(neighbor(direction), 1, -1, true)
                .toV1(fromTile.piece.identifier)
                .findTiles(fromTile)
                .filter(tile -> tile.piece != null && tile.piece.identifier.pieceType() == rookTypeId)
                .toList();
    }

    private void onPieceMove(PieceMoveEvent.PieceMove move) {
        if (rookId == null) {
            logger.error("Castling observed PieceMove, but Rook was not identified yet! RookId is null.");
        }

        assert move.toTile().piece != null; // move always contains to moved piece in 'toTile'
        PieceIdentifier movedPiece = move.toTile().piece.identifier;
        if (movedPiece == this.kingId) {
            kingMoved.setValue(true);
        } else if (movedPiece == rookId) {
            rookMoved.setValue(true);
        }
    }

    private MoveIntention getCastleMove(Entity king) {
        Entity kingMoveTile = kingMove.findTiles(king).findFirst().orElse(null);
        if (kingMoveTile == null) { // tile not empty, or king would be attacked on the path
            return null;
        }
        if (!checkTilesToRookEmpty(kingMoveTile, rookTile)) {
            return null; // all tiles between king and rook must be empty
        }

        Entity rookMoveTile = stepToKingExpression.findTiles(kingMoveTile).findFirst().orElse(null);
        return getCastleMove(king, kingMoveTile, rookTile, rookMoveTile);
    }

    private boolean checkTilesToRookEmpty(Entity currentTile, Entity rookTile) {
        if (currentTile == null) {
            logger.error("Exceeded board bounds while searching for rookTile. Is Castle direction correct?");
            return false;
        }
        if (currentTile == rookTile) return true;
        if (currentTile.piece != null) return false;
        return checkTilesToRookEmpty(stepToRookExpression.findTiles(currentTile).findFirst().orElse(null), rookTile);
    }

    private MoveIntention getCastleMove(Entity kingStart, Entity kingEnd, Entity rookStart, Entity rookEnd) {
        CastlingSimulator simulator = new CastlingSimulator(game, rookStart, rookEnd, kingStart, kingEnd);
        return MoveIntention.fromMoveSimulator(game, kingEnd, simulator);
    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity king, Stream<MoveIntention> baseMoves) {
        if (kingMoved.getValue() || king.isAttacked() || rookMoved.getValue()) {
            return baseMoves;
        }

        List<MoveIntention> moves = new ArrayList<>();

        MoveIntention castleMove = getCastleMove(king);
        if (castleMove != null) moves.add(castleMove);

        return Stream.concat(baseMoves, moves.stream());
    }

    private record CastlingSimulator(
            IChessGame game, Entity rookStartTile, Entity rookEndTile, Entity kingStartTile, Entity kingEndTile
    ) implements MoveIntention.IMoveSimulator {

        @Override
        public void simulate() {
            rookEndTile.piece = rookStartTile.piece;
            rookStartTile.piece = null;
            kingEndTile.piece = kingStartTile.piece;
            kingStartTile.piece = null;
            game.notifyPieceMove(kingStartTile, kingEndTile, Castling.class);
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
