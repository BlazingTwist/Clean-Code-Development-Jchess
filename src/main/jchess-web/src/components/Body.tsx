"use client";
import Link from "next/link";

import { Button } from "@/src/components/ui/button";
import GameComponent from "./GameComponent/GameComponent";
import { ReactElement, useEffect, useState } from "react";
import Config from "@/src/utils/config";
import { BoardUpdateSubscribe } from "@/models/BoardUpdateSubscribe.schema";
import { useGameUpdateContext } from "@/src/app/context/game_update_context";
import { useGameContext } from "@/src/app/context/game_context";
import { ThemeHelperProvider } from "@/src/app/context/theme_helper_context";

/**
 * Represents the main body component for the JChess application.
 * Displays either a welcome screen or the chess game component based on the game state.
 */
export default function Body(): ReactElement {
    // Retrieve the game state from the context
    const gameUpdateContext = useGameUpdateContext();
    const gameContext = useGameContext();

    const [wsSessionId, setWsSessionId] = useState<string>();
    const [ws, setUpdateWs] = useState<WebSocket>();

    useEffect(() => {
        if (!gameContext.gameInfo) {
            return;
        }
        if (ws !== undefined && ws.readyState !== WebSocket.CLOSED) {
            if (wsSessionId == gameContext.sessionId) {
                // sessionId matches -> do nothing
                return;
            }

            console.log("New sessionId, but socket is still open -> close board/update socket");
            ws.close();
        }

        // Open a websocket connection when the game starts
        console.log("Opening board/update WebSocket");
        const serverUri = Config.socketServerUri;
        const socketEndpoint = `${serverUri}/api/board/update`;

        const subscribeMessage: BoardUpdateSubscribe = {
            sessionId: gameContext.sessionId!,
            perspective: gameContext.gameInfo.playerPerspective ?? 0,
        };

        const _ws = new WebSocket(socketEndpoint);
        setUpdateWs(_ws);
        setWsSessionId(gameContext.sessionId);
        _ws.onopen = () => {
            console.log("WebSocket connection opened");
            _ws.send(JSON.stringify(subscribeMessage));
        };
        _ws.onmessage = (event) => {
            let data = JSON.parse(event.data);
            gameUpdateContext.updateState({ gameUpdate: data })
        };
    }, [gameContext, gameUpdateContext, ws, wsSessionId]);
    /**
     * Renders the appropriate content based on the game state.
     * If the game is not in progress, it displays a welcome screen with a "New Game" button.
     * If the game is in progress, it displays the chess game component.
     */
    const renderContent = (): ReactElement => {
        if (!gameContext.gameInfo) {
            return (
                <div className="flex flex-col justify-center items-center min-h-[90vh]">
                    <div className="flex flex-col justify-center text-center">
                        <h1 className="text-4xl font-bold pt-12">Welcome to JChess!</h1>
                        <p>Play chess with up to 3 friends!</p>
                    </div>
                    <div className="p-12">
                        <Button>
                            {/* Link to start a new game */}
                            <Link href="/?newGame=true">New Game</Link>
                        </Button>
                    </div>
                </div>
            );
        } else {
            // Render the chess game component when the game is in progress
            return (
                <div className="flex flex-col xl:justify-center items-center min-h-[90vh]">
                    <ThemeHelperProvider>
                        <GameComponent/> {" "}
                    </ThemeHelperProvider>
                </div>
            );
        }
    };

    // Render the main content
    return renderContent();
}

