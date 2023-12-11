"use client";
import DefaultChessboard from "./Chessboard/default_chessboard";
import { useGameContext } from "@/app/context/game_context";
import HexChessboard from "./Chessboard/hex_chessboard";
import TimeGameComponent from "./TimeGameComponent";
import HistoryComponent from "./HistoryComponent";
import { useGameUpdateContext } from "@/app/context/game_update_context";

export default function GameComponment() {
    const { gameOptions } = useGameContext();

    const { gameUpdate } = useGameUpdateContext();

    return (
        <div className="grid grid-cols-1 gap-2 p-12 items-center sm:grid-cols-2 lg:grid-cols-3  sm:grid-row-2 max-w-[2000px] mx-auto">
            <div className="w-[80vw] h-[80vw] md:w-[55vw] md:h-[55vw] lg:w-full lg:h-full min-w-[200px] min-h-[200px] max-w-[80vh] max-h-[80vh]  justify-self-center sm:row-span-2 sm:col-span-2">
                {gameOptions.playerNames.length == 3 && <HexChessboard />}
                {gameOptions.playerNames.length == 2 && <DefaultChessboard />}
            </div>

            {gameOptions.isTimeGame && <TimeGameComponent />}
            {<HistoryComponent />}
        </div>
    );
}

