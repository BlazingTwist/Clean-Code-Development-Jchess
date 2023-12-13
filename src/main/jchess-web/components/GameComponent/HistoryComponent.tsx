import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

import { useGameContext } from "@/app/context/game_context";

export default function HistoryComponent() {
    const { gameOptions, playerState } = useGameContext();
    const numMoves = playerState.playerHistory.get(0)?.length;
    return (
        <Card
            className={`self-start  max-w-[500px] ${
                gameOptions.isTimeGame ? "" : "sm:col-span-2 lg:col-span-1 justify-self-center "
            }`}
        >
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

