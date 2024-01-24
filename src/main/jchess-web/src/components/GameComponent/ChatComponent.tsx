"use client";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/src/components/ui/card";
import { useGameContext } from "@/src/app/context/game_context";
import { useEffect, useRef, useState } from "react";
import { ChatMessage } from "@/models/ChatMessage.schema";
import { ScrollArea } from "../ui/scroll-area";
import ChatMessageComponent from "./ChatMessageComponent";
import { Separator } from "../ui/separator";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { useBoardUpdateContext } from "@/src/app/context/board_update_context";
import { cn } from "@/src/utils/tailwindMergeUtils";

import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/src/components/ui/dropdown-menu";

/**
 * ChatComponent allows Multiplayer users to chat with each other.
 */
export default function ChatComponent({ className }: { className?: string }) {
    const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
    const [input, setInput] = useState<string>("");
    const [localSocketConnId, setLocalSocketConnId] = useState<number>(-1);
    const [playerName, setPlayerName] = useState<string | undefined>(undefined);

    const scrollAreaRef = useRef<{ scrollToBottom: () => void }>(null);

    // Extract game options and player state using the game context hook.

    const gameContext = useGameContext();
    const { boardUpdate } = useBoardUpdateContext();

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
        if (playerName === undefined) return; // Don't subscribe to chat if the player hasn't chosen a username yet.

        console.log(`Subscribing to chat. socketId: ${localSocketConnId}`);

        gameContext.chatSocket.addListener((event) => {
            const messages: ChatMessage[] = JSON.parse(event.data);
            setChatMessages((oldMessages) => [...oldMessages, ...messages]);
        });

        const message = { sessionId: gameContext.sessionId, msgType: "subscribe", userName: playerName };
        gameContext.chatSocket.sendMessage(JSON.stringify(message));

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [localSocketConnId, playerName]);

    /**
     * Scroll to the bottom of the chat when a new message is received.
     */
    useEffect(() => {
        scrollAreaRef.current?.scrollToBottom();
    }, [chatMessages]);

    return (
        <Card className={cn("self-start", className)}>
            <CardHeader>
                <CardTitle>Chat</CardTitle>
                <Separator />
            </CardHeader>
            {playerName === undefined && (
                <CardContent className="flex flex-col justify-center">
                    <p className="text-center mb-4">Choose a username to participate in the chat.</p>

                    <DropdownMenu>
                        <DropdownMenuTrigger>
                            <Button>Choose</Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent>
                            {Array.from(gameContext.gameInfo?.playerNames ?? []).map((value) => (
                                <DropdownMenuItem key={value} onClick={() => setPlayerName(value)}>
                                    {value}
                                </DropdownMenuItem>
                            ))}
                        </DropdownMenuContent>
                    </DropdownMenu>
                </CardContent>
            )}
            {playerName !== undefined && (
                <>
                    {chatMessages.length > 0 && (
                        <CardContent>
                            <ScrollArea className="h-[200px]" ref={scrollAreaRef}>
                                {chatMessages.map((chatMessage, index) => (
                                    <ChatMessageComponent key={index} chatMessage={chatMessage} index={index} />
                                ))}
                            </ScrollArea>

                            <Separator className="my-2" />
                        </CardContent>
                    )}
                    <CardFooter>
                        <form
                            className="flex flex-row justify-between flex-grow"
                            onSubmit={(event) => {
                                event.preventDefault();
                                console.log("Sending message", input);
                                const message = {
                                    msgType: "submit",
                                    data: input,
                                };
                                gameContext.chatSocket.sendMessage(JSON.stringify(message));
                                setInput("");
                            }}
                        >
                            <Input
                                placeholder="Send a message"
                                value={input}
                                onChange={(event) => setInput(event.target.value)}
                            />
                            <Button className="ml-2" type="submit">
                                Send
                            </Button>
                        </form>
                    </CardFooter>
                </>
            )}
        </Card>
    );
}

