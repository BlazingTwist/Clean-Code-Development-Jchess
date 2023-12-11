"use client";

import { useGameUpdateContext } from "@/app/context/game_update_context";
import GameComponment from "./GameComponent/GameComponent";

export default function Body() {
    const { isGame } = useGameUpdateContext();
    return (
        <>
            {!isGame && (
                <div className="flex flex-col justify-center flex-grow text-center">
                    <h1 className="text-4xl font-bold pt-12">Welcome to JChess!</h1>
                    <p>Play chess with up to 3 friends!</p>
                </div>
            )}

            {isGame && <GameComponment />}
        </>
    );
}

