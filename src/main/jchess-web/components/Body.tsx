"use client";
import Link from "next/link";

import { useGameContext } from "@/app/context/game_context";
import { Button } from "@/components/ui/button";
import GameComponent from "./GameComponent/GameComponent";
import { useEffect, useState } from "react";
import { useGameUpdateContext } from "@/app/context/game_update_context";
import Config from "@/utils/config";
import { useThemeContext } from "@/app/context/theme_context";
import { fetchThemes } from "@/services/rest_api_service";

/**
 * Represents the main body component for the JChess application.
 * Displays either a welcome screen or the chess game component based on the game state.
 */
export default function Body({ sessionId }: { sessionId: string | undefined }) {
    // Retrieve the game state from the context
    const { setGameUpdate } = useGameUpdateContext();
    const { getCurrentTheme, setTheme } = useThemeContext();

    const [ws, setWs] = useState<WebSocket | undefined>(undefined);

    const isGame = sessionId !== undefined;

    useEffect(() => {
        // Open a websocket connection when the game starts
        if (isGame) {
            console.log("Opening WebSocket connection");
            const serverUri = Config.socketServerUri;
            const socketEndpoint = `${serverUri}/api/board/update`;

            const ws = new WebSocket(socketEndpoint);
            setWs(ws);
            ws.onopen = () => {
                console.log("WebSocket connection opened");
                // TODO this is a hack to get the themes to load on a new socket connection (new tab, refresh, other client)
                if (getCurrentTheme() === undefined) {
                    fetchThemes().then((themes) => {
                        const selectedTheme = prompt(
                            "Please select a theme before starting a game. \n Themes are: " +
                                themes.themes.map((theme) => theme.name).join(", ")
                        );
                        setTheme(selectedTheme || "default");
                    });
                }
                ws.send(JSON.stringify({ sessionId: sessionId }));
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
    }, [isGame, sessionId]);
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
            return <GameComponent sessionId={sessionId} />;
        }
    };

    // Render the main content
    return renderContent();
}

