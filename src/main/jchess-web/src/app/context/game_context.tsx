"use client";
import { useRouter } from "next/navigation";
import React, { createContext, useContext, useState, ReactNode } from "react";
import { GameInfo } from "@/models/GameInfo.schema";
import { getGameInfo } from "@/src/services/rest_api_service";

/**
 * Contains data that remains constant during a game-session
 */
export interface SessionData {
    sessionId?: string;
    gameInfo?: GameInfo;
    updateState: (sessionId: string | undefined, newState: Partial<SessionData>) => void;
}

const defaultContext: SessionData = {
    updateState: (_1, _2) => {
    }
}

/**
 * The context for managing the state of the chess game.
 */
const GameContext = createContext<SessionData>(defaultContext);

/**
 * The properties for the GameProvider component.
 */
interface GameProviderProps {
    children: ReactNode;
}

/**
 * Provides a context for managing the state of a chess game.
 */
export const GameProvider: React.FC<GameProviderProps> = (props: GameProviderProps) => {
    const [state, setState] = useState<SessionData>(defaultContext);
    const router = useRouter();

    const context: SessionData = {
        ...state,
        updateState(sessionId, newState) {
            console.log(`update game_context. state.sessionId: ${state.sessionId} | sessionId: ${sessionId}`);
            if (state.sessionId === sessionId) {
                console.log("updated State with identical sessionId?")
                return;
            }

            if (!sessionId) {
                // TODO close socket and tell server to end the game (probably should be done at caller)
                console.log("Clearing SessionData");
                setState({ ...state, sessionId: undefined, gameInfo: undefined })
                router.push("/");
            } else {
                getGameInfo(sessionId).then(info => {
                    setState({ ...state, ...newState, sessionId: sessionId, gameInfo: info });
                });
            }
        }
    };

    // Provide the context to the children components
    return <GameContext.Provider value={context}>
        {props.children}
    </GameContext.Provider>;
};

/**
 * Custom hook for accessing the GameContext.
 * @returns {SessionData} The context properties.
 */
export const useGameContext = (): SessionData => {
    return useContext(GameContext);
};

