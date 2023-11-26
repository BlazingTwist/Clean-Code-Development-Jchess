import DefaultChessboard from "./ui/default_chessboard";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

export default function GameComponment() {
    return (
        <div className="grid grid-cols-1 gap-2 p-12 items-center md:grid-cols-2 md:grid-row-2 max-w-[2000px] mx-auto">
            <div className="w-[80vw] h-[80vw] md:w-[45vw] md:h-[45vw] max-w-[60vh] max-h-[60vh] justify-self-center md:row-span-2">
                <DefaultChessboard />
            </div>
            <div>
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
                                <TableRow>
                                    <TableCell>Player 1</TableCell>
                                    <TableCell>
                                        <div className="w-8 h-8 rounded-md bg-black border-primary border-2" />
                                    </TableCell>
                                    <TableCell className="text-right">02:52</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Player 2</TableCell>
                                    <TableCell>
                                        <div className="w-8 h-8 rounded-md bg-white border-primary border-2" />
                                    </TableCell>
                                    <TableCell className="text-right">04:21</TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </CardContent>
                </Card>
                <Card className="self-start mt-6">
                    <CardHeader>
                        <CardTitle>History</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Player1</TableHead>
                                    <TableHead>Player2</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                <TableRow>
                                    <TableCell>e2-e4</TableCell>
                                    <TableCell>e7-e6</TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}

