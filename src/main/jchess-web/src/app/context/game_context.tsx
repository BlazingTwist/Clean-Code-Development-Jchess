"use client";
import React, { createContext, useContext, useState, ReactNode } from "react";
import { GameInfo } from "@/models/GameInfo.schema";
import { getGameInfo } from "@/src/services/rest_api_service";
import Config from "@/src/utils/config";

class SocketHandler {
    isOpen: boolean = false;
    messageQueue: string[] = [];
    socket: WebSocket | undefined;
    socketUrl: string;
    listeners: ((event: MessageEvent<any>) => void)[];

    constructor(url: string) {
        this.socketUrl = url;
        this.listeners = [];
    }

    open() {
        if (this.socket) {
            this.close();
        }

        const handlerInstance = this;
        console.log(`opening socket to '${this.socketUrl}'`);
        this.socket = new WebSocket(this.socketUrl);
        this.socket.onopen = () => {
            handlerInstance.onSocketOpened();
        };
        this.socket.onmessage = (event) => {
            handlerInstance.onMessageReceived(event);
        };
        this.socket.onclose = (event) => {
            console.log(`socket to '${this.socketUrl}' closed`, event);
        };
    }

    close() {
        this.isOpen = false;
        this.messageQueue.length = 0;
        this.listeners.length = 0;
        this.socket?.close();
    }

    onSocketOpened() {
        this.isOpen = true;
        for (let dataString of this.messageQueue) {
            this.socket?.send(dataString);
        }
        this.messageQueue.length = 0;
    }

    onMessageReceived(event: MessageEvent<any>) {
        for (let listener of this.listeners) {
            listener(event);
        }
    }

    sendMessage(data: string): void {
        if (this.isOpen) {
            this.socket?.send(data);
        } else {
            this.messageQueue.push(data);
        }
    }

    addListener(listener: (event: MessageEvent<any>) => void): void {
        this.listeners.push(listener);
    }
}

/**
 * Contains data that remains constant during a game-session
 */
export interface SessionData {
    sessionId?: string;
    gameInfo?: GameInfo;

    socketConnectionId: number;
    boardUpdateSocket: SocketHandler;
    chatSocket: SocketHandler;
    pieceSelectionSocket: SocketHandler;
    gameOverSocket: SocketHandler;

    isGameOver: boolean;
    setIsGameOver: (isGameOver: boolean) => void;

    updateState: (sessionId: string | undefined) => void;
}

const defaultContext: SessionData = {
    socketConnectionId: -1,
    boardUpdateSocket: new SocketHandler(`${Config.socketServerUri}/api/board/update`),
    chatSocket: new SocketHandler(`${Config.socketServerUri}/api/chat`),
    pieceSelectionSocket: new SocketHandler(`${Config.socketServerUri}/api/pieceSelection`),
    gameOverSocket: new SocketHandler(`${Config.socketServerUri}/api/board/gameOver`),
    isGameOver: false,
    setIsGameOver: (_) => {},
    updateState: (_) => {},
};

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
    const [isGameOver, setIsGameOver] = useState<boolean>(false);
    const context: SessionData = {
        ...state,
        updateState(sessionId) {
            console.log(`update game_context. state.sessionId: ${state.sessionId} | sessionId: ${sessionId}`);
            if (state.sessionId === sessionId) {
                console.log("updated State with identical sessionId?");
                return;
            }

            if (!sessionId) {
                state.boardUpdateSocket.close();
                state.chatSocket.close();
                state.pieceSelectionSocket.close();
                state.gameOverSocket.close();

                console.log("Clearing SessionData");
                setState({ ...state, sessionId: undefined, gameInfo: undefined, isGameOver: false });
            } else {
                console.log(`fetching game info, state: ${JSON.stringify(state)}`);
                getGameInfo(sessionId).then((info) => {
                    setState({
                        ...state,
                        socketConnectionId: state.socketConnectionId + 1,
                        sessionId: sessionId,
                        gameInfo: info,
                    });

                    if (info) {
                        state.boardUpdateSocket.open();
                        state.chatSocket.open();
                        state.pieceSelectionSocket.open();
                        state.gameOverSocket.open();
                    }
                });
            }
        },
        isGameOver: isGameOver,
        setIsGameOver: setIsGameOver,
    };

    // Provide the context to the children components
    return <GameContext.Provider value={context}>{props.children}</GameContext.Provider>;
};

/**
 * Custom hook for accessing the GameContext.
 * @returns {SessionData} The context properties.
 */
export const useGameContext = (): SessionData => {
    return useContext(GameContext);
};

