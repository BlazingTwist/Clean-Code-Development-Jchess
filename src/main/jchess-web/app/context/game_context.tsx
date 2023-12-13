"use client";
import Config from "@/utils/config";
import { createContext, useContext, Dispatch, SetStateAction, useState, ReactNode, useEffect } from "react";

//TODO most of this will be handled by the server, so this file will be removed

/**
 * Represents the options for a chess game.
 */
type GameOptions = {
    playerNames: Array<string>;
    isWhiteOnTop: boolean;
    isTimeGame: boolean;
    timeGameAmountInSeconds: number;
};

/**
 * Represents the state of a player in a chess game.
 */
type PlayerState = {
    playerColor: Map<number, string>;
    playerTime: Map<number, Date>;
    playerHistory: Map<number, Array<string>>;
};

/**
 * Represents the serializable state of a player in a chess game.
 */
type SerializablePlayerState = {
    playerColor: [number, string][];
    playerTime: [number, string][];
    playerHistory: [number, string[]][];
};

// Serialize PlayerState to JSON
function serializePlayerStateToJson(playerState: PlayerState): string {
    const serialized: SerializablePlayerState = {
        playerColor: Array.from(playerState.playerColor.entries()),
        playerTime: Array.from(playerState.playerTime.entries()).map(([key, date]) => [key, date.toISOString()]),
        playerHistory: Array.from(playerState.playerHistory.entries()).map(([key, history]) => [key, history]),
    };
    return JSON.stringify(serialized);
}

// Deserialize JSON to PlayerState
function deserializeJsonToPlayerState(jsonString: string): PlayerState {
    if (jsonString === "{}") {
        return {
            playerColor: new Map<number, string>(),
            playerTime: new Map<number, Date>(),
            playerHistory: new Map<number, Array<string>>(),
        };
    }
    const serialized: SerializablePlayerState = JSON.parse(jsonString);
    const playerColor = new Map<number, string>(serialized.playerColor);
    const playerTime = new Map<number, Date>(
        serialized.playerTime.map(([key, isoString]) => [key, new Date(isoString)])
    );
    const playerHistory = new Map<number, Array<string>>(serialized.playerHistory);

    return { playerColor, playerTime, playerHistory };
}

/**
 * The properties provided by the GameContext.
 */
interface ContextProps {
    chessboardState: Array<Array<string>>;
    setChessboardState: Dispatch<SetStateAction<Array<Array<string>>>>;
    playerState: PlayerState;
    setPlayerState: Dispatch<SetStateAction<PlayerState>>;
    gameOptions: GameOptions;
    setGameOptions: Dispatch<SetStateAction<GameOptions>>;
}

/**
 * The context for managing the state of a chess game.
 * @defaultValue { chessboardState: [], setChessboardState: () => {},
 * playerState: { playerColor: new Map<number, string>(), playerTime: new Map<number, Date>(),
 * playerHistory: new Map<number, Array<string>>() }, setPlayerState: () => {},
 * gameOptions: { playerNames: [], isWhiteOnTop: false, isTimeGame: false, timeGameAmountInSeconds: 0 }, setGameOptions: () => {} }
 */
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

/**
 * The properties for the GameProvider component.
 */
interface GameProviderProps {
    children: ReactNode;
}

/**
 * Provides a context for managing the state of a chess game.
 * @param {GameProviderProps} props - The component properties.
 * @returns {JSX.Element} The JSX element.
 */
export const GameProvider: React.FC<GameProviderProps> = ({ children }) => {
    // Determine if cookies should be saved based on the environment variable
    const useLocalStorage = Config.useLocalStorage;

    // The key for storing game options and player state in localStorage
    const gameOptionsStorageKey = "gameOptions";
    const playerStateStorageKey = "playerState";

    // Initialize state for chessboard, game options, and player state
    const [chessboardState, setChessboardState] = useState<Array<Array<string>>>(Array<Array<string>>());
    const [gameOptions, setGameOptions] = useState<GameOptions>(() => {
        // Load the initial state from localStorage if saving cookies is enabled
        return useLocalStorage
            ? JSON.parse(localStorage.getItem(gameOptionsStorageKey) || "{}")
            : {
                  playerNames: [],
                  isWhiteOnTop: false,
                  isTimeGame: false,
                  timeGameAmountInSeconds: 0,
              };
    });
    const [playerState, setPlayerState] = useState<PlayerState>(() => {
        // Load the initial state from localStorage if saving cookies is enabled
        return useLocalStorage
            ? deserializeJsonToPlayerState(localStorage.getItem(playerStateStorageKey) || "{}")
            : {
                  playerColor: new Map<number, string>(),
                  playerTime: new Map<number, Date>(),
                  playerHistory: new Map<number, Array<string>>(),
              };
    });

    // Save game options and player state to localStorage whenever they change
    useEffect(() => {
        if (useLocalStorage) {
            localStorage.setItem(gameOptionsStorageKey, JSON.stringify(gameOptions));
        }
    }, [gameOptions, useLocalStorage]);

    useEffect(() => {
        if (useLocalStorage) {
            // Convert Map instances to plain objects before saving to localStorage
            localStorage.setItem(playerStateStorageKey, serializePlayerStateToJson(playerState));
        }
    }, [playerState, useLocalStorage]);

    // Provide the context value to be used by children components
    const contextValue: ContextProps = {
        chessboardState,
        setChessboardState,
        playerState,
        setPlayerState,
        gameOptions,
        setGameOptions,
    };

    // Provide the context to the children components
    return <GameContext.Provider value={contextValue}>{children}</GameContext.Provider>;
};

/**
 * Custom hook for accessing the GameContext.
 * @returns {ContextProps} The context properties.
 */
export const useGameContext = (): ContextProps => {
    // Get the context from the GameContext
    const context = useContext(GameContext);

    // Return the context properties
    return context!;
};

