"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";

import { postCreateGame } from "@/services/rest_api_service";

import { useGameContext } from "@/app/context/game_context";
import { useThemeContext } from "@/app/context/theme_context";
import { GameMode } from "@/models/message/GameModes.schema";
import { GameCreate } from "@/models/message/GameCreate.schema";

/**
 * @function NewGameModal
 * @description React component for rendering a modal to start a new chess game.
 * @returns {JSX.Element} JSX Element representing the New Game Modal.
 */
export function NewGameModal() {
    const router = useRouter();

    // State variables for player information and game settings
    // values for the select elements
    const [timeGameValues, setTimeGameValues] = useState<string[]>([]);
    const [isWhiteOnTop, setWhiteOnTop] = useState(false);
    const [isTimeGame, setTimeGame] = useState(false);
    const [timeGameAmount, setTimeGameAmount] = useState("0");

    const { setGameOptions, setPlayerState } = useGameContext(); // old code for client side state, later it probably will be removed
    const { gameModeMap, setTheme } = useThemeContext();

    const [gameMode, setGameMode] = useState<GameMode | undefined>(undefined);

    /**
     * @function useEffect
     * @description Fetches possible values from the server when the component mounts.
     */
    useEffect(() => {
        // TODO fetch possible values from server:
        console.log("fetch possible time game amounts from server");
        setTimeGameValues(["1", "3", "5", "8", "10", "15", "20", "25", "30", "60", "120"]);
    }, []);

    /**
     * @function renderNameInputs
     * @description Renders input fields for player names based on the selected gameMode.
     * @returns {JSX.Element[]} Array of JSX Elements representing player name input fields.
     */
    const renderNameInputs = () => {
        if (!gameMode || !gameMode?.numPlayers) {
            return [];
        }
        const inputs: JSX.Element[] = [];
        for (let i = 1; i <= gameMode!.numPlayers; i++) {
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
     * @function renderThemeSelect
     * @description Renders a dropdown for selecting the theme for the gameMode.
     * @returns {JSX.Element} JSX Element representing the time select dropdown.
     */
    const renderThemeSelect = () => {
        const [selectedTheme, setSelectedTheme] = useState<string | undefined>(undefined);

        useEffect(() => {
            // Reset selected theme when gameMode changes
            setSelectedTheme(undefined);
        }, [gameMode]);

        if (!gameMode) {
            return [];
        }

        return (
            <Select
                onValueChange={(theme) => {
                    setTheme(theme);
                    setSelectedTheme(theme);
                }}
                value={selectedTheme} // Controlled component to manage the selected theme
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
     * @function renderTimeSelect
     * @description Renders a dropdown for selecting the time limit for the game.
     * @returns {JSX.Element} JSX Element representing the time select dropdown.
     */
    const renderTimeSelect = () => {
        return (
            <Select onValueChange={setTimeGameAmount}>
                <SelectTrigger id="time-game-amount">
                    <SelectValue placeholder="Select time game amount" />
                </SelectTrigger>
                <SelectContent position="popper">
                    {timeGameValues.map((value) => (
                        <SelectItem key={value} value={value}>
                            <div className="flex">
                                <div className="w-8 text-end">{value}</div>
                                <div className="pl-2">{value != "1" ? "minutes" : "minute"}</div>
                            </div>
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        );
    };

    type ColorMap = {
        [key: number]: {
            [key: number]: string;
        };
    };

    /**
     * @function handleSubmit
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
        console.log("isTimeGame:" + isTimeGame);
        console.log("timeGameAmount:" + timeGameAmount);

        // TODO send the player names to the server and retrieve the player colors and times
        // MOCK GameStart Endpoit on the Client
        const playerColors = new Map<number, string>();
        const playerTimes = new Map<number, Date>();
        const playerHistory = new Map<number, Array<string>>();

        // mock Color Map. TODO: remove and add to themes response
        const colorMap: ColorMap = {
            3: {
                0: "white",
                1: "destructive",
                2: "black",
            },
            2: {
                0: "white",
                1: "black",
            },
        };

        playerNames.forEach((playerName, index) => {
            const playerTime = new Date(Date.UTC(0, 0, 0, 0, parseInt(timeGameAmount), 0, 0));
            playerColors.set(index, colorMap[playerNames.length][index]);
            playerTimes.set(index, playerTime);
            playerHistory.set(index, ["e4:e5", " e5:e4"]);
        });

        console.log("post newGame to server");
        // TODO improve error handling

        postCreateGame({
            modeId: gameMode!.modeId,
        }).then((sessionId) => {
            console.log("sessionId:" + sessionId);
            setGameOptions({
                playerNames,
                isWhiteOnTop,
                isTimeGame,
                timeGameAmountInSeconds: parseInt(timeGameAmount) * 60,
                sessionId,
            });

            setPlayerState({
                playerColor: playerColors,
                playerTime: playerTimes,
                playerHistory: playerHistory,
            });
            router.push("/");
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
                            <Select onValueChange={(value) => setGameMode(gameModeMap.get(value))} required>
                                <SelectTrigger id="board-layout">
                                    <SelectValue placeholder="Select Game Mode" />
                                </SelectTrigger>
                                <SelectContent position="popper">
                                    {Array.from(gameModeMap.keys()).map((value) => (
                                        <SelectItem key={value} value={value}>
                                            {value}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {renderThemeSelect()}
                        </div>

                        <div className="flex flex-col space-y-1.5 mb-4">{renderNameInputs()}</div>
                        <div className="flex items-center space-x-2 mb-4 ">
                            <Checkbox
                                disabled // TODO implement
                                id="white-on-top"
                                onCheckedChange={(checked: boolean) => setWhiteOnTop(checked)}
                            />
                            <label
                                htmlFor="white-on-top"
                                className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                            >
                                White on top
                            </label>

                            <Checkbox id="time-game" onCheckedChange={(checked: boolean) => setTimeGame(checked)} />
                            <label
                                htmlFor="time-game"
                                className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                            >
                                Time Game
                            </label>
                        </div>
                        {isTimeGame && renderTimeSelect()}
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

