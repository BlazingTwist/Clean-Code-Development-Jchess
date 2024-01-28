package jchess.common.moveset.special;

import jchess.common.IChessGame;
import jchess.common.components.PieceIdentifier;
import jchess.common.events.PieceMoveEvent;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.common.moveset.NormalMove.NormalMoveSimulator;
import jchess.common.state.impl.BooleanState;
import jchess.ecs.Entity;
import jchess.el.CompiledTileExpression;
import jchess.el.v2.ExpressionCompiler;

import java.util.stream.Stream;

public class SpecialFirstMove implements ISpecialRule {
    private final IChessGame game;
    private final PieceIdentifier pieceIdentifier;
    private final CompiledTileExpression compiledFirstMove;
    private final BooleanState hasMoved;

    public SpecialFirstMove(IChessGame game, PieceIdentifier pieceIdentifier, ExpressionCompiler firstMove) {
        this.game = game;
        this.pieceIdentifier = pieceIdentifier;
        this.compiledFirstMove = firstMove.toV1(pieceIdentifier);
        this.hasMoved = new BooleanState(false);

        game.getStateManager().registerState(hasMoved);

        game.getEventManager().getEvent(PieceMoveEvent.class).addListener(move -> {
            assert move.toTile().piece != null; // move always contains moved piece in toTile
            if (move.toTile().piece.identifier == this.pieceIdentifier) {
                hasMoved.setValue(true);
            }
        });
    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity movingPiece, Stream<MoveIntention> baseMoves) {
        if (hasMoved.getValue()) {
            return baseMoves;
        }

        return Stream.concat(
                baseMoves,
                compiledFirstMove.findTiles(movingPiece)
                        .map(toTile -> {
                            NormalMoveSimulator simulator = new NormalMoveSimulator(game, movingPiece, toTile, SpecialFirstMove.class);
                            return MoveIntention.fromMoveSimulator(game, toTile, simulator);
                        })
        );
    }

}
