"use client";
import { createContext, useContext, Dispatch, SetStateAction, useState } from "react";

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

    /*
    // UseEffect to fetch initial state from the server
    useEffect(() => {
        const fetchInitialState = async () => {
            try {
                const response = await fetch('/api/game-state');
                const initialState = await response.json();

                // Update the context state with the fetched data
                setChessboardState(initialState.chessboardState);
                setGameOptions(initialState.gameOptions);
                setPlayerState(initialState.playerState);
            } catch (error) {
                console.error('Error fetching initial state:', error);
            }
        };

        // Check if the game has started before fetching the initial state
        if (gameStarted) {
            fetchInitialState();
        }
    }, []);
    */

    return (
        <GameContext.Provider
            value={{ chessboardState, setChessboardState, playerState, setPlayerState, gameOptions, setGameOptions }}
        >
            {children}
        </GameContext.Provider>
    );
};

export const useGameContext = () => useContext(GameContext);

