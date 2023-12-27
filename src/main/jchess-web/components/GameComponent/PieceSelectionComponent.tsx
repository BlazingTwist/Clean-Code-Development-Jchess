import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { OfferPieceSelection } from "@/models/message/OfferPieceSelection.schema";

import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from "@/components/ui/carousel";
import Config from "@/utils/config";
import { useEffect, useState } from "react";

export default function PieceSelectionComponent({
    sessionId,
    iconMap,
}: {
    sessionId: string;
    iconMap: { [key: string]: string };
}) {
    const serverUri = Config.clientUri + "/api/";

    const [pieceSelectionOffer, setPieceSelectionOffer] = useState<OfferPieceSelection | undefined>(undefined);

    // Websocket
    const [pieceSelectionSocket, setPieceSelectionSocket] = useState<WebSocket | undefined>(undefined);
    useEffect(() => {
        console.log("Opening PieceSelection WebSocket connection");

        const serverUri = Config.socketServerUri;
        const socketEndpoint = `${serverUri}/api/pieceSelection`;

        const ws = new WebSocket(socketEndpoint);
        setPieceSelectionSocket(ws);

        ws.onopen = () => {
            console.log("PieceSelection WebSocket connection opened");
            ws.send(JSON.stringify({ sessionId: sessionId, msgType: "subscribe" }));
        };

        ws.onmessage = (event) => {
            let data = JSON.parse(event.data);
            console.log("PieceSelection WebSocket message received", data);
            setPieceSelectionOffer(data);
        };

        ws.onclose = (event) => {
            console.log("PieceSelection WebSocket connection closed", event);
            setPieceSelectionSocket(undefined);
        };
    }, []);

    // TODO Remove
    /*
    useEffect(() => {
        setPieceSelectionOffer({
            title: "Select a piece to promote",
            pieces: [
                { pieceTypeId: "queen", iconId: "piece.queen.light" },
                { pieceTypeId: "rook", iconId: "piece.rook.light" },
                { pieceTypeId: "bishop", iconId: "piece.bishop.light" },
                { pieceTypeId: "knight", iconId: "piece.knight.light" },
            ],
        });
    }, []);
    */

    if (pieceSelectionOffer === undefined) {
        return <></>;
    }

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-20 z-30">
            <Card className="w-3/4 md:w-2/3 lg:1/3 max-w-[300px] min-w-[200px]">
                <CardHeader>
                    <CardTitle>Select a piece</CardTitle>
                </CardHeader>
                <CardContent>
                    <Carousel orientation="horizontal" className="mx-12">
                        <CarouselContent>
                            {pieceSelectionOffer.pieces.map((piece) => {
                                const iconPath = iconMap[piece.iconId!];
                                return (
                                    <CarouselItem key={piece.pieceTypeId}>
                                        <div className="flex flex-col justify-center items-center ">
                                            <div
                                                className="hover:cursor-pointer hover:animate-pulse flex-col justify-center items-center flex px-2"
                                                onClick={() => {
                                                    if (pieceSelectionSocket?.readyState === WebSocket.OPEN) {
                                                        try {
                                                            pieceSelectionSocket.send(
                                                                JSON.stringify({
                                                                    msgType: "pieceSelected",
                                                                    data: {
                                                                        pieceTypeId: piece.pieceTypeId,
                                                                    },
                                                                })
                                                            );
                                                            setPieceSelectionOffer(undefined);
                                                        } catch (error) {
                                                            console.error("Failed to send message:", error);
                                                        }
                                                    } else {
                                                        console.error("WebSocket is not open");
                                                    }
                                                }}
                                            >
                                                <img src={serverUri + iconPath} />
                                            </div>
                                        </div>
                                    </CarouselItem>
                                );
                            })}
                        </CarouselContent>
                        <CarouselPrevious />
                        <CarouselNext />
                    </Carousel>
                </CardContent>
            </Card>
        </div>
    );
}

