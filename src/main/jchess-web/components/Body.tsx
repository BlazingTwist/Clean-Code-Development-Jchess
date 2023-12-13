"use client";
import Link from "next/link";

import { useGameContext } from "@/app/context/game_context";
import { Button } from "@/components/ui/button";
import GameComponent from "./GameComponent/GameComponent";
import { useEffect, useState } from "react";
import { useGameUpdateContext } from "@/app/context/game_update_context";
import Config from "@/utils/config";

/**
 * Represents the main body component for the JChess application.
 * Displays either a welcome screen or the chess game component based on the game state.
 */
export default function Body() {
    // Retrieve the game state from the context
    const { isGame, gameOptions } = useGameContext();
    const { setGameUpdate } = useGameUpdateContext();

    const [ws, setWs] = useState<WebSocket | undefined>(undefined);

    useEffect(() => {
        // Open a websocket connection when the game starts
        if (isGame) {
            const serverUri = Config.socketServerUri;
            const socketEndpoint = `${serverUri}/api/board/update`;

            const ws = new WebSocket(socketEndpoint);
            setWs(ws);
            ws.onopen = () => {
                ws.send(JSON.stringify({ sessionId: gameOptions.sessionId }));
            };
            ws.onmessage = (event) => {
                let data = JSON.parse(event.data);
                setGameUpdate(data);
            };
            // Cleanup WebSocket on component unmount
            return () => {
                if (ws) {
                    ws.close();
                }
            };
        }
    }, [isGame, gameOptions.sessionId]);
    /**
     * Renders the appropriate content based on the game state.
     * If the game is not in progress, it displays a welcome screen with a "New Game" button.
     * If the game is in progress, it displays the chess game component.
     *
     * @returns {JSX.Element} The rendered component.
     */
    const renderContent = () => {
        if (!isGame) {
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
            return <GameComponent />;
        }
    };

    // Render the main content
    return renderContent();
}

