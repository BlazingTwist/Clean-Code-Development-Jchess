package jchess.common.events;

import dx.schema.message.PieceSelected;
import jchess.ecs.EcsEvent;

/**
 * Benachrichtigt, dass das Frontend eine Figur ausgewählt hat.
 *
 * @see OfferPieceSelectionEvent
 */
public class PieceOfferSelectedEvent extends EcsEvent<PieceSelected> {
}
