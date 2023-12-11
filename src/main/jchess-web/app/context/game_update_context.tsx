"use client";
import { GameUpdate } from "@/models/message/GameUpdate.schema";
import { createContext, useContext, Dispatch, SetStateAction, useState, useEffect } from "react";

interface ContextProps {
    isGame: boolean;
    gameUpdate: GameUpdate | undefined;
    setGameUpdate: Dispatch<SetStateAction<GameUpdate | undefined>>;
    resetGame: () => void;
}

const GameUpdateContext = createContext<ContextProps>({
    isGame: false,
    gameUpdate: undefined,
    setGameUpdate: () => {},
    resetGame: () => {},
});

export const GameUpdateProvider = ({ children }: { children: React.ReactNode }) => {
    const [gameUpdate, setGameUpdate] = useState<GameUpdate | undefined>(() => {
        // Load the initial state from localStorage
        const storedGameUpdate = localStorage.getItem("gameUpdate");
        return storedGameUpdate ? JSON.parse(storedGameUpdate) : undefined;
    });

    // Save the gameUpdate to localStorage whenever it changes
    useEffect(() => {
        if (gameUpdate) {
            localStorage.setItem("gameUpdate", JSON.stringify(gameUpdate));
        } else {
            localStorage.removeItem("gameUpdate");
        }
    }, [gameUpdate]);

    const isGame = gameUpdate !== undefined;

    const resetGame = () => {
        console.log("resetting game");
        // Reset the gameUpdate to undefined
        setGameUpdate(undefined);
        // Remove the gameUpdate from localStorage
        localStorage.removeItem("gameUpdate");
    };

    return (
        <GameUpdateContext.Provider value={{ isGame, gameUpdate, setGameUpdate, resetGame }}>
            {children}
        </GameUpdateContext.Provider>
    );
};

export const useGameUpdateContext = () => useContext(GameUpdateContext);

