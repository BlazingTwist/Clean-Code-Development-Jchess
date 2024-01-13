import { Card, CardContent, CardHeader, CardTitle } from "@/src/components/ui/card";
import { OfferPieceSelection } from "@/models/OfferPieceSelection.schema";

import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from "@/src/components/ui/carousel";
import Config from "@/src/utils/config";
import React, { ReactElement, useEffect, useState } from "react";
import { useGameContext } from "@/src/app/context/game_context";
import { useThemeHelperContext } from "@/src/app/context/theme_helper_context";

export default function PieceSelectionComponent(): ReactElement {
    const [pieceSelectionOffer, setPieceSelectionOffer] = useState<OfferPieceSelection | undefined>(undefined);

    const gameContext = useGameContext();
    const { themeHelper } = useThemeHelperContext();

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
            ws.send(JSON.stringify({ sessionId: gameContext.sessionId, msgType: "subscribe" }));
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
    }, [gameContext.sessionId]);

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
                                const iconPath = themeHelper!.getPieceIconByType(piece.pieceTypeId, piece.playerIdx);
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
                                                {/* eslint-disable-next-line @next/next/no-img-element */}
                                                <img src={"api/" + iconPath} alt={""}/>
                                            </div>
                                        </div>
                                    </CarouselItem>
                                );
                            })}
                        </CarouselContent>
                        <CarouselPrevious/>
                        <CarouselNext/>
                    </Carousel>
                </CardContent>
            </Card>
        </div>
    );
}

