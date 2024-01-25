import { ReactElement, useEffect, useState } from "react";
import { useGameContext } from "@/src/app/context/game_context";
import { GameOver } from "@/models/GameOver.schema";
import { GameOverSubscribe } from "@/models/GameOverSubscribe.schema";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/src/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/src/components/ui/table";

import { Button } from "@/src/components/ui/button";
import Link from "next/link";
import { Medal } from "lucide-react";

interface GameOverComponentProps {
    externalOpen: boolean;
    onExternalOpenChange: (newOpen: boolean) => void;
}

export default function GameOverComponent({
    externalOpen,
    onExternalOpenChange,
}: GameOverComponentProps): ReactElement {
    const [localSocketConnId, setLocalSocketConnId] = useState<number>(-1);

    const gameContext = useGameContext();
    const [gameOver, setGameOver] = useState<GameOver | undefined>(undefined);
    const [open, setOpen] = useState(externalOpen);

    useEffect(() => {
        if (externalOpen !== open) {
            setOpen(externalOpen);
        }
    }, [externalOpen, open]);

    useEffect(() => {
        if (gameContext.socketConnectionId === localSocketConnId) {
            return;
        }

        setLocalSocketConnId(gameContext.socketConnectionId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [gameContext]);

    // Websocket
    useEffect(() => {
        if (localSocketConnId < 0) return;

        console.log(`Subscribing to gameOver. socketId: ${localSocketConnId}`);

        gameContext.gameOverSocket.addListener((event) => {
            let data: GameOver = JSON.parse(event.data);
            console.log("GameOver WebSocket message received", data);
            setGameOver(data);
            setOpen(true);
        });

        const subscribeMessage: GameOverSubscribe = {
            sessionId: gameContext.sessionId!,
        };
        gameContext.gameOverSocket.sendMessage(JSON.stringify(subscribeMessage));

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [localSocketConnId]);

    const renderStats = () => {
        const inputs: ReactElement[] = [];
        if (!gameOver?.playerScores) {
            return inputs;
        }

        const statMap: Map<number, string[]> = new Map();
        for (let i = 0; i < gameOver?.playerScores.length; i++) {
            const score = gameOver.playerScores[i];
            statMap.set(score, (statMap.get(score) || []).concat(gameContext.gameInfo!.playerNames[i]));
        }

        // Iterate over the sorted entries (high to low)
        Array.from(statMap.entries())
            .sort(([scoreA], [scoreB]) => scoreB - scoreA)
            .forEach(([score, playerNames], index) => {
                const element = (
                    <TableRow key={`tablerow-${playerNames}`}>
                        <TableCell key={`tablecell-${playerNames}`}>
                            {index == 0 ? (
                                <span className="flex justify-start items-center gap-3">
                                    <Medal /> {playerNames.join(", ")}
                                </span>
                            ) : (
                                playerNames.join(", ")
                            )}
                        </TableCell>
                        <TableCell key={`tablerow-${score}`}>{score}</TableCell>
                        <TableCell key={`tablerow-placement-${index + 1}`} className="font-medium text-right">
                            {index + 1}.
                        </TableCell>
                    </TableRow>
                );
                inputs.push(element);
            });

        return inputs;
    };

    if (!gameOver) {
        return <></>;
    }

    return (
        <Dialog
            open={open}
            onOpenChange={(newOpen) => {
                setOpen(newOpen);
                onExternalOpenChange(newOpen);
            }}
        >
            <DialogContent>
                <DialogHeader>
                    <DialogTitle className="text-2xl">Game Over</DialogTitle>
                    <DialogDescription>
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="w-[100px]">Player/s</TableHead>
                                    <TableHead>Score</TableHead>
                                    <TableHead className="text-right w-[50px]">Placement</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>{renderStats()}</TableBody>
                        </Table>
                    </DialogDescription>
                </DialogHeader>
                <DialogFooter>
                    <Button type="reset" variant="secondary" asChild>
                        <Link href="/">Home</Link>
                    </Button>
                    <Button type="submit" asChild>
                        <Link href="/?newGame=true">New Game</Link>
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

