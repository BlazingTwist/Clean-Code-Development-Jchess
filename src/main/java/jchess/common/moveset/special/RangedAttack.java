package jchess.common.moveset.special;

import jchess.common.IChessGame;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.events.PieceMoveEvent;
import jchess.common.moveset.ISpecialRule;
import jchess.common.moveset.MoveIntention;
import jchess.ecs.Entity;
import jchess.el.TileExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RangedAttack implements ISpecialRule {
    private final IChessGame game;
    private final PieceIdentifier thisRangedPieceID;
    public RangedAttack(IChessGame game, PieceIdentifier thisRangedPieceID){
        this.game=game;
        this.thisRangedPieceID=thisRangedPieceID;

    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity thisRangedPiece, Stream<MoveIntention> baseMoves) {
        List<MoveIntention> result = new ArrayList<>();
        for(int i=0; i<12;i++) {
            Entity targetTile= TileExpression.neighbor(i*30).compile(thisRangedPieceID).findTiles(thisRangedPiece).findFirst().orElse(null);
            if(targetTile != null ){
                if(targetTile.piece != null){
                    if(thisRangedPieceID.ownerId() != targetTile.piece.identifier.ownerId()) {
                        result.add(getRangedAttackMove(thisRangedPiece, targetTile));
                    }
                }
            }
        }
        return Stream.concat(baseMoves, result.stream());
    }

    private MoveIntention getRangedAttackMove(Entity thisRangedPiece, Entity targetTile) {
        return new MoveIntention(targetTile, () ->{
            targetTile.piece=null;
            game.movePieceStationary(thisRangedPiece, RangedAttack.class);
        }, new RangedAttackSimulator(thisRangedPiece, targetTile.piece,targetTile));
    }

    private record RangedAttackSimulator(
            Entity attackerTile, PieceComponent attackedPiece, Entity attackedPieceTile
    ) implements MoveIntention.IMoveSimulator {

        @Override
        public void simulate() {
            attackedPieceTile.piece = null;
        }

        @Override
        public void revert() {
            attackedPieceTile.piece = attackedPiece;
        }
    }
}

/* archive kamikaze(single target, stirbt dabei
 public class RangedAttack implements ISpecialRule {
 private final IChessGame game;
 private final PieceIdentifier thisRangedPieceID;
 public RangedAttack(IChessGame game, PieceIdentifier thisRangedPieceID){
 this.game=game;
 this.thisRangedPieceID=thisRangedPieceID;

 }

 @Override
 public Stream<MoveIntention> getSpecialMoves(Entity thisRangedPiece, Stream<MoveIntention> baseMoves) {
 List<MoveIntention> result = new ArrayList<>();
 for(int i=0; i<12;i++) {
 Entity targetTile= TileExpression.neighbor(i*30).compile(thisRangedPieceID).findTiles(thisRangedPiece).findFirst().orElse(null);
 if(targetTile != null ){
 if(targetTile.piece != null){
 if(thisRangedPieceID.ownerId() != targetTile.piece.identifier.ownerId()) {
 result.add(getRangedAttackMove(thisRangedPiece, targetTile));
 }
 }
 }
 }
 return Stream.concat(baseMoves, result.stream());
 }

 private MoveIntention getRangedAttackMove(Entity thisRangedPiece, Entity targetTile) {
 return new MoveIntention(targetTile, () ->{
 targetTile.piece=null;
 game.movePiece(thisRangedPiece,thisRangedPiece, RangedAttack.class);
 }, new RangedAttackSimulator(thisRangedPiece, targetTile.piece,targetTile));
 }

 private record RangedAttackSimulator(
 Entity attackerTile, PieceComponent attackedPiece, Entity attackedPieceTile
 ) implements MoveIntention.IMoveSimulator {

 @Override
 public void simulate() {
 attackedPieceTile.piece = null;
 }

 @Override
 public void revert() {
 attackedPieceTile.piece = attackedPiece;
 }
 }
 }
 */