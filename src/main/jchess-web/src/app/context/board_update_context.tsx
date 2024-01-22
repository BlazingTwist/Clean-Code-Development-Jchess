"use client";
import React, {createContext, useContext, useState, ReactNode, ReactElement} from "react";
import {BoardUpdate} from "@/models/BoardUpdate.schema";

/**
 * The properties provided by the BoardUpdateContext.
 */
export interface BoardUpdateContextProps {
    boardUpdate?: BoardUpdate;
    updateState: (newState: Partial<BoardUpdateContextProps>) => void;
}

/**
 * The context for managing game updates.
 */
const BoardUpdateContext = createContext<BoardUpdateContextProps>({
    updateState: (_) => {
    }
});

/**
 * The properties for the BoardUpdateProvider component.
 */
interface BoardUpdateProviderProps {
    children: ReactNode;
}

/**
 * Provides a context for managing game updates.
 */
export const BoardUpdateProvider: React.FC<BoardUpdateProviderProps> = (props: BoardUpdateProviderProps): ReactElement => {
    const [state, setState] = useState({});

    const updateState = (newState: Partial<BoardUpdateContextProps>) => {
        console.log("update board_update_context");
        setState({...state, ...newState});
    }

    // Provide the context to the children components
    return <BoardUpdateContext.Provider value={{...state, updateState}}>
        {props.children}
    </BoardUpdateContext.Provider>;
};

/**
 * Custom hook for accessing the BoardUpdateContext.
 */
export const useBoardUpdateContext = (): BoardUpdateContextProps => {
    return useContext(BoardUpdateContext);
};

