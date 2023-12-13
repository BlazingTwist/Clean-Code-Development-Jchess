import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

import { useGameContext } from "@/app/context/game_context";

export default function TimeGameComponent() {
    const { gameOptions, playerState } = useGameContext();
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
                            return (
                                <TableRow key={index}>
                                    <TableCell key={index}>{playerName}</TableCell>
                                    <TableCell>
                                        <div
                                            key={index}
                                            className={`w-8 h-8 rounded-md bg-${playerState.playerColor.get(
                                                index
                                            )}  border-primary border-2`}
                                        />
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

