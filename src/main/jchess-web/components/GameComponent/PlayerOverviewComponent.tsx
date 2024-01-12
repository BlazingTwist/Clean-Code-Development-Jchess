import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

import { useGameContext } from "@/app/context/game_context";
import { useGameUpdateContext } from "@/app/context/game_update_context";
import { cn } from "@/utils/tailwindMergeUtils";
import {useThemeContext} from "@/app/context/theme_context";
import {ReactElement} from "react";

/**
 * Represents the PlayerOverviewComponent that displays player information
 * @returns The rendered PlayerOverviewComponent.
 */
export default function PlayerOverviewComponent({ className }: { className?: string }): ReactElement | null {
    // Extracting game options and player state using the custom hook.
    const { gameOptions} = useGameContext();
    const { getThemeHelper } = useThemeContext();
    const { gameUpdate } = useGameUpdateContext();
    return (
        <Card className={cn("self-start max-w-[500px]", className)}>
            <CardHeader>
                <CardTitle>Player Overview</CardTitle>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="">Player Name</TableHead>
                            <TableHead>Color</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {gameOptions.playerNames.map((playerName: string, index: number) => {
                            const isCurrentPlayer = gameUpdate?.activePlayerId === index;
                            const playerColor = getThemeHelper()!.getPlayerColors()![index];
                            console.log(`playerColor: ${playerColor}`)

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
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
}

