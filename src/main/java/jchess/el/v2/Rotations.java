package jchess.el.v2;

import jchess.common.components.PieceIdentifier;

public class Rotations implements ExpressionCompiler {
    private final ExpressionCompiler expression;
    private final int numRotations;

    /**
     * @param expression   expression to be rotated
     * @param numRotations the number of rotations to create (e.g. 6 = 6 rotations, each 60 degrees apart)
     */
    public Rotations(ExpressionCompiler expression, int numRotations) {
        if (360 % numRotations != 0) {
            throw new IllegalArgumentException("numRotations (" + numRotations + ") does not evenly divide 360.");
        }

        this.expression = expression;
        this.numRotations = numRotations;
    }

    @Override
    public TileExpression.CompiledExpression compile(PieceIdentifier movingPiece) {
        if (numRotations <= 1) {
            return expression.compile(movingPiece);
        }

        TileExpression.CompiledExpression[] rotations = new TileExpression.CompiledExpression[numRotations];
        int stepOffset = 360 / numRotations;
        for (int i = 0; i < numRotations; i++) {
            int newForwardBasis = (movingPiece.forwardBasis() + (stepOffset * i)) % 360;
            PieceIdentifier rotatedPiece = new PieceIdentifier(movingPiece.pieceType(), movingPiece.shortName(), movingPiece.ownerId(), newForwardBasis);
            rotations[i] = expression.compile(rotatedPiece);
        }
        return Or.or(rotations);
    }
}
