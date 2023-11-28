"use client";
import DefaultChessboard from "./ui/chessboard/default_chessboard";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useGameContext } from "@/app/context/game_context";
import HexChessboard from "./ui/chessboard/hex_chessboard";

export default function GameComponment() {
    const { gameOptions, playerState } = useGameContext();

    const renderTimeGame = () => {
        return (
            <Card className="self-start">
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
                            {gameOptions.playerNames.map((playerName, index) => {
                                const minutesLeft = playerState.playerTime.get(index)?.getUTCMinutes();
                                const secondsLeft = playerState.playerTime.get(index)?.getUTCSeconds();
                                const hoursLeft = playerState.playerTime.get(index)?.getUTCHours();
                                return (
                                    <TableRow>
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
                                            {String(minutesLeft).padStart(2, "0")}:
                                            {String(secondsLeft).padStart(2, "0")}
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        );
    };

    const renderHistory = () => {
        const numMoves = playerState.playerHistory.get(0)?.length;

        return (
            <Card className="self-start mt-6">
                <CardHeader>
                    <CardTitle>History</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                {gameOptions.playerNames.map((playerName, index) => {
                                    return <TableHead>{playerName}</TableHead>;
                                })}
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {Array.from({ length: numMoves! }, (_, i) => i).map((_, moveIdx) => {
                                return (
                                    <TableRow>
                                        {gameOptions.playerNames.map((_, index) => {
                                            return (
                                                <TableCell>
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
    };
    return (
        <div className="grid grid-cols-1 gap-2 p-12 items-center md:grid-cols-2 md:grid-row-2 max-w-[2000px] mx-auto">
            <div className="w-[80vw] h-[80vw] md:w-[45vw] md:h-[45vw] max-w-[60vh] max-h-[60vh] justify-self-center md:row-span-2">
                {gameOptions.playerNames.length == 3 && <HexChessboard />}
                {gameOptions.playerNames.length == 2 && <DefaultChessboard />}
            </div>
            <div>
                {gameOptions.isTimeGame && renderTimeGame()}
                {renderHistory()}
            </div>
        </div>
    );
}

