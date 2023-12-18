package jchess.game.common.components;

import jchess.ecs.Entity;
import jchess.game.common.IChessGame;
import jchess.game.common.moveset.ISpecialRule;
import jchess.game.common.moveset.ISpecialRuleProvider;
import jchess.game.common.moveset.MoveIntention;
import jchess.game.common.moveset.NormalMove;
import jchess.game.el.CompiledTileExpression;
import jchess.game.el.TileExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PieceComponent {
    private final IChessGame game;
    public final PieceIdentifier identifier;
    public final CompiledTileExpression baseMoveSet;
    public final List<ISpecialRule> specialMoveSet = new ArrayList<>();

    public PieceComponent(IChessGame game, PieceIdentifier identifier, TileExpression baseMoveSet) {
        this.game = game;
        this.identifier = identifier;
        this.baseMoveSet = baseMoveSet.compile(identifier);
    }

    public void addSpecialMoves(ISpecialRuleProvider... specialRuleProviders) {
        if (specialRuleProviders == null || specialRuleProviders.length == 0) {
            return;
        }

        specialMoveSet.addAll(
                Arrays.stream(specialRuleProviders)
                        .map(provider -> provider.createRule(game, identifier))
                        .toList()
        );
    }

    public Stream<MoveIntention> findValidMoves(Entity thisTile, boolean verifyKingSafe) {
        if (thisTile.tile == null) return Stream.empty();

        Stream<MoveIntention> moves = Stream.empty();
        if (baseMoveSet != null) {
            moves = Stream.concat(
                    moves,
                    baseMoveSet.findTiles(thisTile).map(toTile -> NormalMove.getMove(game, thisTile, toTile))
            );
        }
        moves = Stream.concat(
                moves,
                specialMoveSet.stream().flatMap(rule -> rule.getSpecialMoves(thisTile).stream())
        );

        if(verifyKingSafe) {
            // Constraint: after moving, the King must always be not in check
            // TODO erja, for every move, verify that king is not in check
            // moves = verifyKingSafe(moves);
        }

        return moves;
    }

}
