"use client";
import { GameUpdate } from "@/models/message/GameUpdate.schema";
import { createContext, useContext, Dispatch, SetStateAction, useState, useEffect } from "react";

interface ContextProps {
    isGame: boolean;
    gameUpdate: GameUpdate | undefined;
}

const GameUpdateContext = createContext<ContextProps>({
    isGame: false,
    gameUpdate: undefined,
});

export const GameUpdateProvider = ({ children }: { children: React.ReactNode }) => {
    const [gameUpdate, setGameUpdate] = useState<GameUpdate>();

    /*
    useEffect(() => {
        console.log("Fetching GameUpdate");
        fetch("http://localhost:8880/api/gameUpdate")
            .then((response) => {
                if (!response.ok) {
                    throw new Error(`Error fetching GameUpdate: ${response.status} ${response.statusText}`);
                }
                return response.json();
            })
            .then((data) => {
                console.log("GameUpdate:", data);
                setGameUpdate(data);
            })
            .catch((error) => {
                console.error("Error fetching GameUpdate:", error);
            });
        console.log("Fetching GameUpdate Done");
    }, []);
    */

    const isGame = gameUpdate !== undefined;

    return <GameUpdateContext.Provider value={{ isGame, gameUpdate }}>{children}</GameUpdateContext.Provider>;
};

export const useGameUpdateContext = () => useContext(GameUpdateContext);

