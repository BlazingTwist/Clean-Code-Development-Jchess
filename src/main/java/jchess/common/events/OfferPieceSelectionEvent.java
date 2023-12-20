package jchess.common.events;

import dx.schema.message.Piece;
import jchess.ecs.EcsEvent;

/**
 * Benachrichtigt das Frontend, dass eine Figuren-Auswahl gezeigt werden soll
 *
 * @see PieceOfferSelectedEvent
 */
public class OfferPieceSelectionEvent extends EcsEvent<OfferPieceSelectionEvent.PieceSelection> {

    /**
     * @param windowTitle   Titeltext für das Auswahlfenster
     * @param piecesToOffer Die Figuren, die zur Auswahl gestellt werden
     */
    public record PieceSelection(String windowTitle, Piece... piecesToOffer) {
    }
}
