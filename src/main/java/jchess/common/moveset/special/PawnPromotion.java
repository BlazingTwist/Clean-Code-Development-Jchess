package jchess.common.moveset.special;

import dx.schema.message.Piece;
import jchess.common.IChessGame;
import jchess.common.events.OfferPieceSelectionEvent;
import jchess.common.events.PieceOfferSelectedEvent;
import jchess.common.events.RenderEvent;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This SpecialRule will turn all baseMoves matching the {@link #isPromotionTile} filter into Promoting Moves.
 * <p>If the move is taken, a window will open allowing the player to pick a different pieceType to replace this piece with
 */
public class PawnPromotion implements ISpecialRule {
    private final IChessGame game;
    private final Predicate<Entity> isPromotionTile;
    private final Piece[] pieces;
    private Promotion currentAwaitingPromotion;

    public PawnPromotion(IChessGame game, Predicate<Entity> isPromotionTile, Piece... pieces) {
        this.game = game;
        this.isPromotionTile = isPromotionTile;
        this.pieces = pieces;

        game.getEventManager().getEvent(PieceOfferSelectedEvent.class).addListener(selection -> {
            if (currentAwaitingPromotion != null) {
                Entity promotedPiece = currentAwaitingPromotion.moveFrom();
                int owner = promotedPiece.piece.identifier.ownerId();
                game.createPiece(promotedPiece, selection.getPieceTypeId(), owner);
                game.movePiece(promotedPiece, currentAwaitingPromotion.moveTo(), PawnPromotion.class);

                currentAwaitingPromotion = null;
                game.getEventManager().getEvent(RenderEvent.class).fire(null);
            }
        });
    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity movingPiece, Stream<MoveIntention> currentMoves) {
        return currentMoves.map(move -> {
            Entity targetTile = move.displayTile();
            if (isPromotionTile.test(targetTile)) {
                return new MoveIntention(
                        targetTile,
                        () -> {
                            currentAwaitingPromotion = new Promotion(movingPiece, targetTile);
                            OfferPieceSelectionEvent offerEvent = game.getEventManager().getEvent(OfferPieceSelectionEvent.class);
                            offerEvent.fire(new OfferPieceSelectionEvent.PieceSelection("Promote your Piece", pieces));
                        },
                        move.moveSimulator()
                );
            }
            return move;
        });
    }

    private record Promotion(Entity moveFrom, Entity moveTo) {
    }
}
