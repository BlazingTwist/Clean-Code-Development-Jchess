import { Card, CardContent, CardHeader, CardTitle } from "@/src/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/src/components/ui/table";

import { useGameContext } from "@/src/app/context/game_context";
import { useGameUpdateContext } from "@/src/app/context/game_update_context";
import { ReactElement } from "react";
import { useThemeHelperContext } from "@/src/app/context/theme_helper_context";

/**
 * Represents the PlayerOverviewComponent that displays player information
 * @returns The rendered PlayerOverviewComponent.
 */
export default function PlayerOverviewComponent(): ReactElement | null {
    // Extracting game options and player state using the custom hook.
    const gameContext = useGameContext();
    const { gameUpdate } = useGameUpdateContext();
    const { themeHelper } = useThemeHelperContext();

    const themePlayerColors = themeHelper?.getPlayerColors();
    const playerNames = gameContext.gameInfo!.playerNames;
    const playerColors = playerNames.map((_name, index) => {
        return (themePlayerColors) ? themePlayerColors[index] : "#000000";
    })
    const activePlayerId = gameUpdate?.activePlayerId;

    return (
        <Card className="self-start mb-6 max-w-[500px]">
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
                        {playerNames.map((playerName: string, index: number) => {
                            const isCurrentPlayer = activePlayerId === index;
                            const playerColor = playerColors[index];

                            return (
                                <TableRow key={index}>
                                    <TableCell key={index} className={`${isCurrentPlayer && "underline"}`}>
                                        {playerName}
                                    </TableCell>
                                    <TableCell>
                                        <div
                                            key={index}
                                            className={`w-8 h-8 rounded-md border-primary border-2 `}
                                            style={{ backgroundColor: playerColor }}
                                        >
                                            {isCurrentPlayer && (
                                                // if the player is the current player, display a ping animation
                                                <span
                                                    className={`animate-ping inline-flex h-full w-full rounded-full opacity-40`}
                                                    style={{ backgroundColor: "#000000" }}
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

