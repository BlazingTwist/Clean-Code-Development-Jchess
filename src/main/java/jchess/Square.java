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

import jchess.piece.Piece;

/**
 * Class to represent a chessboard square
 */
public class Square
{

    public int pozX; // 0-7, because 8 squares for row/column
    public int pozY; // 0-7, because 8 squares for row/column
    public Piece piece = null;//object Piece on square (and extending Piece)

    Square(int pozX, int pozY, Piece piece)
    {
        this.pozX = pozX;
        this.pozY = pozY;
        this.piece = piece;
    }/*--endOf-Square--*/


    Square(Square square)
    {
        this.pozX = square.pozX;
        this.pozY = square.pozY;
        this.piece = square.piece;
    }

    void setPiece(Piece piece)
    {
        this.piece = piece;
        this.piece.square = this;
    }
}
