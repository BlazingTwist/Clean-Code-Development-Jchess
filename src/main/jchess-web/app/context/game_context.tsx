"use client";
import { createContext, useContext, Dispatch, SetStateAction, use, useState } from "react";

type GameOptions = {
    playerNames: Array<string>;
    isWhiteOnTop: boolean;
    isTimeGame: boolean;
    timeGameAmountInSeconds: number;
};

type PlayerState = {
    // each map is accessed by player id
    playerColor: Map<number, string>;
    playerTime: Map<number, Date>;
    playerHistory: Map<number, Array<string>>;
};

interface ContextProps {
    chessboardState: Array<Array<string>>;
    setChessboardState: Dispatch<SetStateAction<Array<Array<string>>>>;
    playerState: PlayerState;
    setPlayerState: Dispatch<SetStateAction<PlayerState>>;
    gameOptions: GameOptions;
    setGameOptions: Dispatch<SetStateAction<GameOptions>>;
}

const GameContext = createContext<ContextProps>({
    chessboardState: Array<Array<string>>(),
    setChessboardState: () => {},
    playerState: {
        playerColor: new Map<number, string>(),
        playerTime: new Map<number, Date>(),
        playerHistory: new Map<number, Array<string>>(),
    },
    setPlayerState: () => {},
    gameOptions: {
        playerNames: [],
        isWhiteOnTop: false,
        isTimeGame: false,
        timeGameAmountInSeconds: 0,
    },
    setGameOptions: () => {},
});

export const GameProvider = ({ children }: { children: React.ReactNode }) => {
    const [chessboardState, setChessboardState] = useState<Array<Array<string>>>(Array<Array<string>>());
    const [gameOptions, setGameOptions] = useState<GameOptions>({
        playerNames: [],
        isWhiteOnTop: false,
        isTimeGame: false,
        timeGameAmountInSeconds: 0,
    });
    const [playerState, setPlayerState] = useState<PlayerState>({
        playerColor: new Map<number, string>(),
        playerTime: new Map<number, Date>(),
        playerHistory: new Map<number, Array<string>>(),
    });

    return (
        <GameContext.Provider
            value={{ chessboardState, setChessboardState, playerState, setPlayerState, gameOptions, setGameOptions }}
        >
            {children}
        </GameContext.Provider>
    );
};

export const useGameContext = () => useContext(GameContext);

