"use client";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import Link from "next/link";
import { Checkbox } from "@/components/ui/checkbox";
import { useRouter } from "next/navigation";

import { useState, ChangeEvent } from "react";
import { useGameContext } from "@/app/context/game_context";
import { parse } from "path";

export function NewGameModal() {
    const router = useRouter();
    const [numberOfPlayers, setNumberOfPlayers] = useState("0");
    const [isWhiteOnTop, setWhiteOnTop] = useState(false);
    const [isTimeGame, setTimeGame] = useState(false);
    const [timeGameAmount, setTimeGameAmount] = useState("0");

    const { setGameOptions, setPlayerState } = useGameContext();

    const renderNameInputs = () => {
        const inputs: JSX.Element[] = [];
        for (let i = 1; i <= parseInt(numberOfPlayers); i++) {
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

    const renderTimeSelect = () => {
        const values = ["1", "3", "5", "8", "10", "15", "20", "25", "30", "60", "120"];
        return (
            <Select onValueChange={setTimeGameAmount}>
                <SelectTrigger id="time-game-amount">
                    <SelectValue placeholder="Select time game amount" />
                </SelectTrigger>
                <SelectContent position="popper">
                    {values.map((value) => (
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

    const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault(); // Prevents the default form submission behavior

        // Access the inputted player names
        const playerNames = Array.from(
            { length: parseInt(numberOfPlayers) },
            (_, i) => (document.getElementById(`player-${i + 1}`) as HTMLInputElement).value
        );

        const playerColors = new Map<string, string>();
        const playerTimes = new Map<string, Date>();
        const playerHistory = new Map<string, Array<string>>();

        playerNames.forEach((playerName, index) => {
            const playerTime = new Date(Date.UTC(0, 0, 0, 0, parseInt(timeGameAmount), 0, 0));
            playerColors.set(playerName, index == 0 ? "black" : index == 1 ? "white" : "destructive");
            playerTimes.set(playerName, playerTime);
            playerHistory.set(playerName, ["irgendein zug"]);
        });

        // Use the inputted information as needed
        console.log("Number of Players:", numberOfPlayers);
        console.log("Player Names:", playerNames);
        console.log("Is Time Game:", isTimeGame);
        console.log("Time Game Amount:", timeGameAmount);

        setGameOptions({
            playerNames,
            isWhiteOnTop,
            isTimeGame,
            timeGameAmountInSeconds: parseInt(timeGameAmount) * 60,
        });

        setPlayerState({
            playerColor: playerColors,
            playerTime: playerTimes,
            playerHistory: playerHistory,
        });

        // Add further logic here, such as sending data to a server
        router.push("/?game=true");
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
                            <Label htmlFor="framework">Number of Players</Label>
                            <Select onValueChange={setNumberOfPlayers} required>
                                <SelectTrigger id="board-layout">
                                    <SelectValue placeholder="Select Number of Players" />
                                </SelectTrigger>
                                <SelectContent position="popper">
                                    <SelectItem value="2">2</SelectItem>
                                    <SelectItem value="3">3</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                        <div className="flex flex-col space-y-1.5 mb-4">{renderNameInputs()}</div>
                        <div className="flex items-center space-x-2 mb-4 ">
                            <Checkbox
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

