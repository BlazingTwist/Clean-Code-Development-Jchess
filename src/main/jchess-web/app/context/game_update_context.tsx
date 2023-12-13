"use client";
import { createContext, useContext, Dispatch, SetStateAction, useState, useEffect, ReactNode } from "react";
import { GameUpdate } from "@/models/message/GameUpdate.schema";
import Config from "@/utils/config";

/**
 * The properties provided by the GameUpdateContext.
 */
interface GameUpdateContextProps {
    gameUpdate: GameUpdate | undefined;
    setGameUpdate: Dispatch<SetStateAction<GameUpdate | undefined>>;
}

/**
 * The context for managing game updates.
 * @defaultValue undefined
 */
const GameUpdateContext = createContext<GameUpdateContextProps | undefined>(undefined);

/**
 * The properties for the GameUpdateProvider component.
 */
interface GameUpdateProviderProps {
    children: ReactNode;
}

/**
 * Provides a context for managing game updates.
 * @param {GameUpdateProviderProps} props - The component properties.
 * @returns {JSX.Element} The JSX element.
 */
export const GameUpdateProvider: React.FC<GameUpdateProviderProps> = ({ children }) => {
    // Determine if cookies should be saved based on the environment variable
    const useLocalStorage = Config.useLocalStorage;

    // The key for storing game updates in localStorage
    const storageKey = "gameUpdate";

    // Initialize state for game updates
    const [gameUpdate, setGameUpdate] = useState<GameUpdate | undefined>(() => {
        // Load the initial state from localStorage if saving cookies is enabled
        return useLocalStorage ? JSON.parse(localStorage.getItem(storageKey) || "null") : undefined;
    });

    // Save the gameUpdate to localStorage whenever it changes
    useEffect(() => {
        if (useLocalStorage) {
            localStorage.setItem(storageKey, JSON.stringify(gameUpdate || null));
        }
    }, [gameUpdate, useLocalStorage]);

    // Create the context value to be provided
    const contextValue: GameUpdateContextProps = { gameUpdate, setGameUpdate };

    // Provide the context to the children components
    return <GameUpdateContext.Provider value={contextValue}>{children}</GameUpdateContext.Provider>;
};

/**
 * Custom hook for accessing the GameUpdateContext.
 * @returns {GameUpdateContextProps} The context properties.
 * @throws {Error} Throws an error if used outside of a GameUpdateProvider.
 */
export const useGameUpdateContext = (): GameUpdateContextProps => {
    // Get the context from the GameUpdateContext
    const context = useContext(GameUpdateContext);

    // Throw an error if the hook is used outside of a GameUpdateProvider
    if (!context) {
        throw new Error("useGameUpdateContext must be used within a GameUpdateProvider");
    }

    // Return the context properties
    return context;
};

