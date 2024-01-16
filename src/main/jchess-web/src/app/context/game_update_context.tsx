"use client";
import React, {createContext, useContext, useState, ReactNode, ReactElement} from "react";
import {GameUpdate} from "@/models/GameUpdate.schema";

/**
 * The properties provided by the GameUpdateContext.
 */
export interface GameUpdateContextProps {
    gameUpdate?: GameUpdate;
    updateState: (newState: Partial<GameUpdateContextProps>) => void;
}

/**
 * The context for managing game updates.
 */
const GameUpdateContext = createContext<GameUpdateContextProps>({
    updateState: (_) => {
    }
});

/**
 * The properties for the GameUpdateProvider component.
 */
interface GameUpdateProviderProps {
    children: ReactNode;
}

/**
 * Provides a context for managing game updates.
 */
export const GameUpdateProvider: React.FC<GameUpdateProviderProps> = (props: GameUpdateProviderProps): ReactElement => {
    const [state, setState] = useState({});

    const updateState = (newState: Partial<GameUpdateContextProps>) => {
        console.log("update game_update_context");
        setState({...state, ...newState});
    }

    // Provide the context to the children components
    return <GameUpdateContext.Provider value={{...state, updateState}}>
        {props.children}
    </GameUpdateContext.Provider>;
};

/**
 * Custom hook for accessing the GameUpdateContext.
 */
export const useGameUpdateContext = (): GameUpdateContextProps => {
    return useContext(GameUpdateContext);
};

