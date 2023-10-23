/*
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Authors:
 * Mateusz Sławomir Lach ( matlak, msl )
 * Damian Marciniak
 */
package jchess;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Class to represent a piece (any kind) - this class should be extended to represent pawn, bishop etc.
 */
public abstract class Piece {

    Chessboard chessboard; // <-- this relations isn't in class diagram, but it's necessary :/
    public Square square;
    public Player player;
    String name;
    protected String symbol;
    protected static Image imageBlack;// = null;
    protected static Image imageWhite;// = null;
    public Image image;
    public static short value = 0;

    Piece(Chessboard chessboard, Player player) {
        this.chessboard = chessboard;
        this.player = player;
        if (player.color == Player.colors.black) {
            image = imageBlack;
        } else {
            image = imageWhite;
        }
        this.name = this.getClass().getSimpleName();

    }
    /* Method to draw piece on chessboard
     * @graph : where to draw
     */

    final void draw(Graphics g) {
        if (image == null) {
            System.err.println("Image of piece '" + this.name + "' is null");
            return;
        }

        try {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Point topLeft = this.chessboard.getTopLeftPoint();
            int edgeLength = this.chessboard.get_square_height();
            int x = (this.square.pozX * edgeLength) + topLeft.x;
            int y = (this.square.pozY * edgeLength) + topLeft.y;

            BufferedImage resized = new BufferedImage(edgeLength, edgeLength, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D imageGr = resized.createGraphics();
            imageGr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            imageGr.drawImage(image, 0, 0, edgeLength, edgeLength, null);
            imageGr.dispose();
            Image resizedSprite = resized.getScaledInstance(edgeLength, edgeLength, 0);
            g2d.drawImage(resizedSprite, x, y, null);

        } catch (java.lang.NullPointerException exc) {
            System.err.println("Something wrong when painting piece: " + exc.getMessage());
        }
    }

    void setImage() {
        if (this.player.color == Player.colors.black) {
            image = imageBlack;
        } else {
            image = imageWhite;
        }
    }
    //void setImages(String white, String black) {
    /* method set image to black or white (depends on player color)
     * @white: String with name of image with white piece
     * @black: String with name of image with black piece
     * */
    //    this.imageBlack = black;
    //     this.imageWhite = white;
    //     if(player.color == player.color.black) {
    //         this.image = GUI.loadImage(imageBlack);
    //     } else {
    //          this.image = GUI.loadImage(imageWhite);
    //     }
    //  }/*--endOf-setImages(String white, String black)--*/

    abstract public ArrayList<Square> allMoves();

    /**
     * Method is useful for out of bounds protection
     * @param x x position on chessboard
     * @param y y position on chessboard
     * @return true if parameters are out of bounds (array)
     */
    protected boolean isOutOfBounds(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            return true;
        }
        return false;
    }

    /**
     * @param x y position on chessboard
     * @param y y position on chessboard
     * @return true if can move, false otherwise
     */
    protected boolean canMoveTo(int x, int y) {
        if (chessboard.squares[x][y].piece != null
                && chessboard.squares[x][y].piece.name.equals("King")) {
            return false;
        }
        Piece piece = chessboard.squares[x][y].piece;
        if (piece == null || //if this square is empty
                piece.player != this.player) //or piece is another player
        {
            return true;
        }
        return false;
    }

    /**
     * Method check if piece has other owner than calling piece
     * @param x x position on chessboard
     * @param y y position on chessboard
     * @return true if owner(player) is different
     */
    protected boolean otherOwner(int x, int y) {
        Square sq = chessboard.squares[x][y];
        if (sq.piece == null) {
            return false;
        }
        if (this.player != sq.piece.player) {
            return true;
        }
        return false;
    }

    public String getSymbol() {
        return this.symbol;
    }
}
