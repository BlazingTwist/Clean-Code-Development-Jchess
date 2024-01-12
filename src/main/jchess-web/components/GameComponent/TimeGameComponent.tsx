import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

import { useGameContext } from "@/app/context/game_context";
import { useGameUpdateContext } from "@/app/context/game_update_context";
import {useThemeContext} from "@/app/context/theme_context";
import {ReactElement} from "react";

/**
 * Represents the TimeGameComponent that displays player information and time left.
 * @returns The rendered TimeGameComponent.
 */
export default function TimeGameComponent() : ReactElement | null {
    // Extracting game options and player state using the custom hook.
    const { gameOptions, playerState } = useGameContext();
    const { getThemeHelper } = useThemeContext();
    const { gameUpdate } = useGameUpdateContext();
    return (
        <Card className="self-start mb-6 max-w-[500px]">
            <CardHeader>
                <CardTitle>Time Game</CardTitle>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[100px]">Player Name</TableHead>
                            <TableHead>Color</TableHead>
                            <TableHead>Time Left</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {gameOptions.playerNames.map((playerName: string, index: number) => {
                            const minutesLeft = playerState.playerTime.get(index)?.getUTCMinutes();
                            const secondsLeft = playerState.playerTime.get(index)?.getUTCSeconds();
                            const hoursLeft = playerState.playerTime.get(index)?.getUTCHours();
                            const isCurrentPlayer = gameUpdate?.activePlayerId === index;
                            const playerColor = getThemeHelper()!.getPlayerColors()![index];

                            return (
                                <TableRow key={index}>
                                    <TableCell key={index} className={`${isCurrentPlayer && "underline"}`}>
                                        {playerName}
                                    </TableCell>
                                    <TableCell>
                                        <div
                                            key={index}
                                            className={`w-8 h-8 rounded-md border-primary border-2 `}
                                            style={{backgroundColor: playerColor}}
                                        >
                                            {isCurrentPlayer && (
                                                // if the player is the current player, display a ping animation
                                                <span
                                                    className={`animate-ping inline-flex h-full w-full rounded-full opacity-40`}
                                                    style={{backgroundColor: "#000000"}}
                                                />
                                            )}
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        {hoursLeft != 0 && String(hoursLeft).padStart(2, "0") + ":"}
                                        {String(minutesLeft).padStart(2, "0")}:{String(secondsLeft).padStart(2, "0")}
                                    </TableCell>
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
}

