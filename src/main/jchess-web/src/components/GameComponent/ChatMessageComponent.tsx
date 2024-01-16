import { ChatMessage } from "@/models/message/ChatMessage.schema";
import { useEffect, useState } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "../ui/avatar";

import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/src/components/ui/tooltip";

export default function ChatMessageComponent({ chatMessage, index }: { chatMessage: ChatMessage; index: number }) {
    const [minutesAgo, setMinutesAgo] = useState(0);

    useEffect(() => {
        // calculate the time ago
        calculateTimeAgo();
        const interval = setInterval(calculateTimeAgo, 60000); // refresh every minute

        // cleanup
        return () => clearInterval(interval);
    }, [chatMessage]);

    if (chatMessage === undefined) {
        return <></>;
    }

    function getTimeAgoString() {
        if (minutesAgo < 1) {
            return "just now";
        } else {
            return `${minutesAgo} minutes ago`;
        }
    }

    const calculateTimeAgo = () => {
        const difference = Math.abs(Date.now() - chatMessage.timestamp);
        setMinutesAgo(Math.floor(difference / 1000 / 60));
    };

    return (
        <div className={` flex flex-col rounded-sm p-2  ${index % 2 == 1 ? "bg-muted" : ""}`}>
            <div>
                <TooltipProvider>
                    <Tooltip>
                        <TooltipTrigger>
                            <Avatar className="h-[24px] w-[24px]">
                                <AvatarImage src={`https://robohash.org/${chatMessage.userName}.png`} />
                                <AvatarFallback>CN</AvatarFallback>
                            </Avatar>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p>{chatMessage.userName}</p>
                        </TooltipContent>
                    </Tooltip>
                </TooltipProvider>
            </div>
            <div className="flex mt-1">
                <span>{chatMessage.message}</span>
            </div>
            <div className="flex flex-row items-center justify-end">
                <span className=" flex text-xs">{getTimeAgoString()}</span>
            </div>
        </div>
    );
}

