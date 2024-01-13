"use client";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/src/components/ui/card";
import { useGameContext } from "@/src/app/context/game_context";
import { useCallback, useEffect, useRef, useState } from "react";
import { ChatMessage } from "@/models/message/ChatMessage.schema";
import { ScrollArea } from "../ui/scroll-area";
import ChatMessageComponent from "./ChatMessageComponent";
import { Separator } from "../ui/separator";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { useGameUpdateContext } from "@/src/app/context/game_update_context";
import { cn } from "@/src/utils/tailwindMergeUtils";

/**
 * ChatComponent allows Multiplayer users to chat with each other.
 */
export default function ChatComponent({ className }: { className?: string }) {
    const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
    const [input, setInput] = useState<string>("");
    const [localSocketConnId, setLocalSocketConnId] = useState<number>(-1);

    const scrollAreaRef = useRef<{ scrollToBottom: () => void }>(null);

    // Extract game options and player state using the game context hook.

    const gameContext = useGameContext();
    const { gameUpdate } = useGameUpdateContext();

    useEffect(() => {
        if (gameContext.socketConnectionId === localSocketConnId) {
            return;
        }

        setLocalSocketConnId(gameContext.socketConnectionId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [gameContext]);

    const getCurrentUserName = useCallback(() => {
        let userName = gameContext.gameInfo!.playerNames[gameUpdate?.activePlayerId || 0];
        console.log("Username", userName);
        if (userName === undefined || userName === null || userName === "") {
            console.log("No username found");
            userName = prompt("Please enter your username ") || "no username";
        }
        return userName;
    }, [gameContext, gameUpdate?.activePlayerId]);

    // Websocket
    useEffect(() => {
        if (localSocketConnId < 0) return;

        console.log(`Subscribing to chat. socketId: ${localSocketConnId}`);

        gameContext.chatSocket.addListener(event => {
            const messages: ChatMessage[] = JSON.parse(event.data);
            setChatMessages((oldMessages) => [...oldMessages, ...messages]);
        });

        /* TODO aktuell ist der username immer name[0], da der gameContext noch nicht bereit ist...
        *   Ggf. reicht das schon, wenn man die Komponenten erst rendert, wenn der Kontext bereit ist - weiß aber nicht, wie sich das beim starten eines neuen spiels verhält*/
        const message = { sessionId: gameContext.sessionId, msgType: "subscribe", userName: getCurrentUserName() }
        gameContext.chatSocket.sendMessage(JSON.stringify(message));

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [localSocketConnId]);

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
                <Separator/>
            </CardHeader>
            {chatMessages.length > 0 && (
                <CardContent>
                    <ScrollArea className="h-[200px]" ref={scrollAreaRef}>
                        {chatMessages.map((chatMessage, index) => (
                            <ChatMessageComponent key={index} chatMessage={chatMessage} index={index}/>
                        ))}
                    </ScrollArea>

                    <Separator className="my-2"/>
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
        </Card>
    );
}

