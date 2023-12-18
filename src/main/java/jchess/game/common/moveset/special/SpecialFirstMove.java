package jchess.game.common.moveset.special;

import jchess.ecs.Entity;
import jchess.game.common.IChessGame;
import jchess.game.common.components.PieceIdentifier;
import jchess.game.common.events.PieceMoveEvent;
import jchess.game.common.moveset.ISpecialRule;
import jchess.game.common.moveset.MoveIntention;
import jchess.game.el.CompiledTileExpression;
import jchess.game.el.TileExpression;

import java.util.Collections;
import java.util.List;

public class SpecialFirstMove implements ISpecialRule {
    private final IChessGame game;
    private final PieceIdentifier pieceIdentifier;
    private final CompiledTileExpression compiledFirstMove;
    private boolean hasMoved = false;

    public SpecialFirstMove(IChessGame game, PieceIdentifier pieceIdentifier, TileExpression firstMove) {
        this.game = game;
        this.pieceIdentifier = pieceIdentifier;
        this.compiledFirstMove = firstMove.compile(pieceIdentifier);

        game.getEventManager().<PieceMoveEvent>getEvent(PieceMoveEvent.class).addListener(move -> {
            if (move.toTile().piece.identifier == this.pieceIdentifier) {
                hasMoved = true;
            }
        });
    }

    @Override
    public List<MoveIntention> getSpecialMoves(Entity movingPiece) {
        if (hasMoved) {
            return Collections.emptyList();
        }

        return compiledFirstMove.findTiles(movingPiece)
                .map(move -> new MoveIntention(move, () -> game.movePiece(movingPiece, move, SpecialFirstMove.class)))
                .toList();
    }
}
