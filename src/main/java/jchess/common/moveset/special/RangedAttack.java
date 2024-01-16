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
    private final int maxRange;
    public RangedAttack(IChessGame game, PieceIdentifier thisRangedPieceID, int maxRange){
        this.game=game;
        this.thisRangedPieceID=thisRangedPieceID;
        this.maxRange=maxRange;

    }

    @Override
    public Stream<MoveIntention> getSpecialMoves(Entity thisRangedPiece, Stream<MoveIntention> baseMoves) {
        List<MoveIntention> result = new ArrayList<>();
        //
        if(this.maxRange==1){
            calculateSurroundingTargetTiles(result, thisRangedPiece);
        } else if(this.maxRange==2){
            for(int i=0; i<6;i++) {
                Entity firstTile = TileExpression.neighbor(i*60).compile(thisRangedPieceID).findTiles(thisRangedPiece).findFirst().orElse(null);
            }
        } else if(this.maxRange==3){

        }
        return Stream.concat(baseMoves, result.stream());
    }

    private void addTileToResult(List<MoveIntention> result, Entity thisRangedPiece,Entity targetTile){
        if(targetTile != null ){
            if(targetTile.piece != null){
                if(thisRangedPieceID.ownerId() != targetTile.piece.identifier.ownerId()) {
                    result.add(getRangedAttackMove(thisRangedPiece, targetTile));
                }
            }
        }
    }
    private void calculateSurroundingTargetTiles(List<MoveIntention> result, Entity thisRangedPiece){
        for(int i=0; i<12;i++) {
            Entity targetTile= TileExpression.neighbor(i*30).compile(thisRangedPieceID).findTiles(thisRangedPiece).findFirst().orElse(null);
            //Entity targetTile= TileExpression.repeat(TileExpression.regex("0 30 60 90 120 150 180 210 240 270 300 330",false), 0,1, false).
            //compile(thisRangedPieceID).findTiles(thisRangedPiece).findFirst().orElse(null);
            addTileToResult(result,thisRangedPiece,targetTile);
        }
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