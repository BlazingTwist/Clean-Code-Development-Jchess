import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

import { useGameContext } from "@/app/context/game_context";
import { useGameUpdateContext } from "@/app/context/game_update_context";
import { cn } from "@/utils/tailwindMergeUtils";

/**
 * Represents the TimeGameComponent that displays player information and time left.
 * @returns {JSX.Element} The rendered TimeGameComponent.
 */
export default function TimeGameComponent({ className }: { className?: string }) {
    // Extracting game options and player state using the custom hook.
    const { gameOptions, playerState } = useGameContext();
    const { gameUpdate } = useGameUpdateContext();
    return (
        <Card className={cn("self-start max-w-[500px]", className)}>
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
                            const playerColor = playerState.playerColor.get(index);

                            return (
                                <TableRow key={index}>
                                    <TableCell key={index} className={`${isCurrentPlayer && "underline"}`}>
                                        {playerName}
                                    </TableCell>
                                    <TableCell>
                                        <div
                                            key={index}
                                            className={`w-8 h-8 rounded-md bg-${playerColor}  border-primary border-2 `}
                                        >
                                            {isCurrentPlayer && (
                                                // if the player is the current player, display a ping animation
                                                <span
                                                    className={`animate-ping inline-flex h-full w-full rounded-full bg-${
                                                        playerColor === "white" ? "primary" : playerColor
                                                    } opacity-20`}
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

