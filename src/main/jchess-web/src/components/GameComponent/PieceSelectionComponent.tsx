import { Card, CardContent, CardHeader, CardTitle } from "@/src/components/ui/card";
import { OfferPieceSelection } from "@/models/OfferPieceSelection.schema";

import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from "@/src/components/ui/carousel";
import React, { ReactElement, useEffect, useState } from "react";
import { useGameContext } from "@/src/app/context/game_context";
import { useThemeHelperContext } from "@/src/app/context/theme_helper_context";

export default function PieceSelectionComponent(): ReactElement {
    const [pieceSelectionOffer, setPieceSelectionOffer] = useState<OfferPieceSelection>();
    const [localSocketConnId, setLocalSocketConnId] = useState<number>(-1);

    const gameContext = useGameContext();
    const { themeHelper } = useThemeHelperContext();

    useEffect(() => {
        if (gameContext.socketConnectionId === localSocketConnId) {
            return;
        }

        setLocalSocketConnId(gameContext.socketConnectionId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [gameContext]);

    // Websocket
    useEffect(() => {
        if(localSocketConnId < 0) return;

        console.log(`Subscribing to pieceSelection. socketId: ${localSocketConnId}`);

        gameContext.pieceSelectionSocket.addListener(event => {
            let data = JSON.parse(event.data);
            console.log("PieceSelection WebSocket message received", data);
            setPieceSelectionOffer(data);
        });

        const subscribeMessage = {
            sessionId: gameContext.sessionId,
            msgType: "subscribe"
        }
        gameContext.pieceSelectionSocket.sendMessage(JSON.stringify(subscribeMessage))

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [localSocketConnId]);

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
                                                    try {
                                                        const message = {
                                                            msgType: "pieceSelected",
                                                            data: {
                                                                pieceTypeId: piece.pieceTypeId,
                                                            },
                                                        };
                                                        gameContext.pieceSelectionSocket.sendMessage(JSON.stringify(message));
                                                        setPieceSelectionOffer(undefined);
                                                    } catch (error) {
                                                        console.error("Failed to send message:", error);
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

