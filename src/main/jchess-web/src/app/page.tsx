"use client";
import { NewGameModal } from "@/src/components/NewGameModal";
import Nav from "@/src/components/Nav";
import Body from "@/src/components/Body";
import { useGameContext } from "@/src/app/context/game_context";
import { useEffect, useState } from "react";

type Props = {
    searchParams: Record<string, string> | null | undefined;
};

/**
 * Home component represents the whole content of the application.
 */
export default function Home({ searchParams }: Props) {
    const showNewGameModal = searchParams?.newGame;
    const paramSessionId = searchParams?.sessionId;
    const [sessionId, setSessionId] = useState<string | undefined>(undefined);

    const gameContext = useGameContext();
    useEffect(() => {
        if (paramSessionId !== sessionId) {
            setSessionId(paramSessionId);
        }
    }, [paramSessionId, sessionId])

    useEffect(() => {
        console.warn(`sessionId changed to '${sessionId}'`);
        gameContext.updateState(sessionId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [sessionId]);

    return (
        <main className="min-h-screen bg-accent min-w-[400px] ">
            <Nav/>
            <Body/>
            {/* Display the NewGameModal if specified in the search parameters. */}
            {showNewGameModal && <NewGameModal/>}
        </main>
    );
}

