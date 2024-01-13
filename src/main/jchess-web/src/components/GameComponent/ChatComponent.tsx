"use client";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/src/components/ui/card";
import { useGameContext } from "@/src/app/context/game_context";
import { useCallback, useEffect, useRef, useState } from "react";
import Config from "@/src/utils/config";
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
    
    const scrollAreaRef = useRef<{ scrollToBottom: () => void }>(null);

    // Extract game options and player state using the game context hook.

    const { sessionId, gameInfo } = useGameContext();
    const { gameUpdate } = useGameUpdateContext();

    const getCurrentUserName = useCallback(() => {
        let userName = gameInfo!.playerNames[gameUpdate?.activePlayerId || 0];
        console.log("Username", userName);
        if (userName === undefined || userName === null || userName === "") {
            console.log("No username found");
            userName = prompt("Please enter your username ") || "no username";
        }
        return userName;
    }, [gameInfo, gameUpdate?.activePlayerId]);

    // Websocket
    const [chatSocket, setChatSocket] = useState<WebSocket | undefined>(undefined);
    useEffect(() => {
        console.log("Opening Chat WebSocket connection");

        const serverUri = Config.socketServerUri;
        const socketEndpoint = `${serverUri}/api/chat`;

        const ws = new WebSocket(socketEndpoint);
        setChatSocket(ws);

        ws.onopen = () => {
            console.log("Chat WebSocket connection opened");
            ws.send(JSON.stringify({ sessionId: sessionId, msgType: "subscribe", userName: getCurrentUserName() }));
        };

        ws.onmessage = (event) => {
            const first = JSON.parse(event.data);
            console.log("Type of once", typeof first);
            try {
                const data: ChatMessage[] = JSON.parse(first);
                console.log("Type of data", typeof data);

                console.log("Chat WebSocket message received", data);

                for (let i = 0; i < data.length; i++) {
                    const chatMessage = data[i];
                    if (chatMessage === undefined || chatMessage === null) {
                        console.log("Chat message is undefined or null");
                        continue;
                    }
                    console.log("Chat message", chatMessage);
                    setChatMessages((chatMessages) => [...chatMessages, chatMessage]);
                }
            } catch (e) {
                // why is this happening
                setChatMessages(first);
            }
            console.log("ChatMessages", chatMessages);
        };

        ws.onclose = (event) => {
            console.log("Chat WebSocket connection closed", event);
        };
    }, [sessionId, getCurrentUserName, chatMessages]);

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
                        if (chatSocket) {
                            chatSocket.send(
                                JSON.stringify({
                                    msgType: "submit",
                                    data: input,
                                })
                            );
                        }
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

