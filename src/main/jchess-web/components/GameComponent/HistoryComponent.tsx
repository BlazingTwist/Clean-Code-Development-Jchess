import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

import { useGameContext } from "@/app/context/game_context";

/**
 * HistoryComponent displays the game history in a card format.
 * It shows the moves of each player in a table.
 */
export default function HistoryComponent() {
    // Extract game options and player state using the game context hook.
    const { gameOptions, playerState } = useGameContext();
    // Determine the number of moves made in the game.
    const numMoves = playerState.playerHistory.get(0)?.length;
    return (
        <Card className={`self-start  max-w-[500px]`}>
            <CardHeader>
                <CardTitle>History</CardTitle>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableHeader>
                        <TableRow>
                            {gameOptions.playerNames.map((playerName, index) => {
                                return <TableHead key={index}>{playerName}</TableHead>;
                            })}
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {Array.from({ length: numMoves! }, (_, i) => i).map((_, moveIdx) => {
                            return (
                                <TableRow key={moveIdx}>
                                    {gameOptions.playerNames.map((_, index) => {
                                        return (
                                            <TableCell key={moveIdx + " " + index}>
                                                {playerState.playerHistory.size > moveIdx
                                                    ? playerState.playerHistory.get(index)![moveIdx]
                                                    : ""}
                                            </TableCell>
                                        );
                                    })}
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
}

