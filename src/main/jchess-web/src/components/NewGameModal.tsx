"use client";
import React, { ReactElement, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/src/components/ui/card";
import { Button } from "@/src/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/src/components/ui/select";
import { Input } from "@/src/components/ui/input";
import { Label } from "@/src/components/ui/label";
import { postCreateGame } from "@/src/services/rest_api_service";
import { GameMode } from "@/models/GameModes.schema";
import { useServerDataContext } from "@/src/app/context/server_data_context";
import { useGameContext } from "../app/context/game_context";

/**
 * @function NewGameModal
 * @description React component for rendering a modal to start a new chess game.
 */
export function NewGameModal(): ReactElement | null {
    const router = useRouter();
    const serverData = useServerDataContext();
    const gameContext = useGameContext();

    const gameModes = serverData.allGameModes?.modes ?? [];

    const [playerPerspective, setPlayerPerspective] = useState<string>("0");
    const [selectedTheme, setSelectedTheme] = useState<string | undefined>();
    const [gameMode, setGameMode] = useState<GameMode | undefined>();

    const onGameModeChanged = (newGameMode: GameMode | undefined) => {
        setGameMode(newGameMode);
        setSelectedTheme(undefined);
    };

    /**
     * @function renderNameInputs
     * @description Renders input fields for player names based on the selected gameMode.
     */
    const renderNameInputs = function (): ReactElement[] {
        if (!gameMode?.numPlayers) {
            return [];
        }
        const inputs: ReactElement[] = [];
        for (let i = 1; i <= gameMode.numPlayers; i++) {
            inputs.push(
                <div key={i}>
                    <div className="flex flex-col space-y-1.5">
                        <Label htmlFor={`player-${i}`}>Name of Player {i}</Label>
                        <Input id={`player-${i}`} placeholder={`Name of Player ${i}`} required />
                    </div>
                </div>
            );
        }
        return inputs;
    };

    /**
     * @description Renders a dropdown for selecting the theme for the gameMode.
     */
    const renderThemeSelect = function (): ReactElement | null {
        if (!gameMode) {
            return null;
        }

        return (
            <Select
                key={`theme-select__${selectedTheme}`}
                onValueChange={setSelectedTheme}
                value={selectedTheme}
                required
            >
                <SelectTrigger id="them-selection">
                    <SelectValue placeholder="Select Theme" />
                </SelectTrigger>
                <SelectContent position="popper">
                    {Array.from(gameMode.themeIds).map((value) => (
                        <SelectItem key={value} value={value}>
                            {value}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        );
    };

    /**
     * @description Renders a dropdown for selecting the player's perspective.
     */
    const renderPerspectiveSelect = function (): ReactElement | null {
        if (!gameMode) {
            return null;
        }

        return (
            <Select onValueChange={setPlayerPerspective} value={playerPerspective} required>
                <Label htmlFor={"player-perspective"}>Perspective</Label>
                <SelectTrigger id="perspective-selection">
                    <SelectValue placeholder="Select Perspective" />
                </SelectTrigger>
                <SelectContent position="popper" id="player-perspective">
                    {Array.from(Array(gameMode.numPlayers).keys()).map((index) => (
                        <SelectItem key={index} value={index.toString()}>
                            {"Player " + (index + 1)}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        );
    };

    /**
     * @description Handles the form submission, processes input values, and initiates a new game.
     * @param {React.FormEvent<HTMLFormElement>} event - The form submission event.
     */
    const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault(); // Prevents the default form submission behavior

        // Access the inputted player names
        const playerNames = Array.from(
            { length: gameMode?.numPlayers || 0 },
            (_, i) => (document.getElementById(`player-${i + 1}`) as HTMLInputElement).value
        );

        console.log("players are:" + playerNames);

        console.log("post newGame to server");
        // TODO improve error handling

        postCreateGame({
            gameModeId: gameMode?.modeId!,
            layoutId: gameMode!.layoutId!,
            themeName: selectedTheme!,
            playerNames: playerNames,
            playerPerspective: +playerPerspective,
        }).then((sessionId) => {
            console.log("sessionId:" + sessionId);
            gameContext.setIsGameOver(false);
            router.push("/?sessionId=" + sessionId);
        });
    };

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-20">
            <Card className="w-3/4 md:w-2/3 lg:1/3 max-w-[500px]">
                <CardHeader>
                    <CardTitle>New Game ♟️</CardTitle>
                    <CardDescription>How do you want to play?</CardDescription>
                </CardHeader>
                <form onSubmit={handleSubmit}>
                    <CardContent>
                        <div className="flex flex-col space-y-1.5 mb-4">
                            <Label htmlFor="framework">Game Mode</Label>
                            <Select onValueChange={(index) => onGameModeChanged(gameModes[+index])} required>
                                <SelectTrigger id="board-layout">
                                    <SelectValue placeholder="Select Game Mode" />
                                </SelectTrigger>
                                <SelectContent position="popper">
                                    {gameModes.map((mode, index) => (
                                        <SelectItem key={mode.modeId} value={index.toString()}>
                                            {mode.displayName}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {renderThemeSelect()}
                        </div>

                        <div className="flex flex-col space-y-1.5 mb-4">{renderNameInputs()}</div>
                        <div className="flex flex-col space-y-1.5 mb-4">{renderPerspectiveSelect()}</div>
                    </CardContent>
                    <CardFooter className="flex justify-between">
                        <Button type="button" variant="outline">
                            <Link href="/">Cancel</Link>
                        </Button>
                        <Button>Start</Button>
                    </CardFooter>
                </form>
            </Card>
        </div>
    );
}

