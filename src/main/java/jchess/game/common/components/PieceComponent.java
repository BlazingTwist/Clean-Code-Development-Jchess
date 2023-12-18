package jchess.game.common.components;

import jchess.ecs.Entity;
import jchess.game.common.IChessGame;
import jchess.game.common.moveset.ISpecialRule;
import jchess.game.common.moveset.ISpecialRuleProvider;
import jchess.game.common.moveset.MoveIntention;
import jchess.game.common.moveset.NormalMove;
import jchess.game.el.CompiledTileExpression;
import jchess.game.el.TileExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PieceComponent {
    private static final Logger logger = LoggerFactory.getLogger(PieceComponent.class);
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

        if (verifyKingSafe) {
            moves = verifyKingSafe(moves);
        }

        return moves;
    }

    private Stream<MoveIntention> verifyKingSafe(Stream<MoveIntention> allMoves) {
        int ownPlayerId = identifier.ownerId();
        int kingTypeId = game.getKingTypeId();

        return allMoves.filter(move -> {
            MoveIntention.IMoveSimulator simulator = move.moveSimulator();
            simulator.simulate();

            Entity ownKing = findKing(ownPlayerId, kingTypeId);
            if (ownKing == null) {
                logger.warn("Unable to find King on Board after simulated move.");
                return false;
            }

            boolean kingInCheckAfterMove = game.getEntityManager().getEntities().stream()
                    .filter(entity -> entity.piece != null && entity.piece.identifier.ownerId() != ownPlayerId)
                    .anyMatch(entity -> entity
                            .findValidMoves(false)
                            .anyMatch(moveTo -> moveTo.displayTile() == ownKing)
                    );

            simulator.revert();

            return !kingInCheckAfterMove;
        });
    }

    private Entity findKing(int playerId, int kingTypeId) {
        return game.getEntityManager().getEntities().stream()
                .filter(entity -> {
                    if (entity.piece == null) return false;

                    PieceIdentifier pieceId = entity.piece.identifier;
                    return pieceId.ownerId() == playerId && pieceId.pieceTypeId() == kingTypeId;
                })
                .findFirst().orElse(null);
    }

}
