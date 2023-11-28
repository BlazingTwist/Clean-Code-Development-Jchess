import React from "react";
import { HexTile } from "./hex_tile";

export default function HexChessboard() {
    const numTilesVertical = 17;
    const widthPercentage = 100 / numTilesVertical;
    const spacePercentage = widthPercentage * 0.3;
    const numTilesHorizontal = 17 + 16;

    const renderChessboard = () => {
        // TODO replace this with a real chessboard coming from the server
        const chessState = Array.from({ length: numTilesHorizontal }, () => Array(numTilesVertical).fill(""));
        chessState[8][16] = "r";
        chessState[24][16] = "r";
        chessState[32][8] = "r";
        chessState[24][0] = "r";
        chessState[8][0] = "r";
        chessState[0][8] = "r";

        chessState[10][16] = "n";
        chessState[22][16] = "n";
        chessState[31][7] = "n";
        chessState[25][1] = "n";
        chessState[7][1] = "n";
        chessState[1][7] = "n";

        chessState[12][16] = "b";
        chessState[16][16] = "b";
        chessState[20][16] = "b";
        chessState[30][6] = "b";
        chessState[28][4] = "b";
        chessState[26][2] = "b";
        chessState[6][2] = "b";
        chessState[4][4] = "b";
        chessState[2][6] = "b";

        chessState[14][16] = "q";
        chessState[29][5] = "q";
        chessState[5][3] = "q";

        chessState[18][16] = "k";
        chessState[27][3] = "k";
        chessState[3][5] = "k";

        for (let i = 0; i < 10; i++) {
            chessState[7 + i * 2][15] = "p";
            chessState[31 - i][9 - i] = "p";
            chessState[10 - i][i] = "p";
        }

        for (let i = 0; i < 9; i++) {
            chessState[8 + i * 2][14] = "p";
            chessState[29 - i][9 - i] = "p";
            chessState[11 - i][1 + i] = "p";
        }

        const board: JSX.Element[] = [];
        for (let y = 0; y < numTilesVertical; y++) {
            const row: JSX.Element[] = [];
            const x0 = Math.abs(8 - y);
            const x1 = 32 - x0;
            for (let x = x0; x <= x1; x += 2) {
                row.push(
                    <HexTile key={`${x},${y}`} widthPercentage={widthPercentage} onClick={() => console.log(x, y)}>
                        {chessState[x][y] != "" && <span className="select-none">{chessState[x][y]}</span>}
                    </HexTile>
                );
            }
            board.push(
                <div key={y} className="flex flex-row justify-center" style={{ marginTop: `-${spacePercentage}%` }}>
                    {row}
                </div>
            );
        }

        return board;
    };

    return <div className="w-full h-full">{renderChessboard()}</div>;
}

