"use client";
import { NewGameModal } from "@/src/components/NewGameModal";
import Nav from "@/src/components/Nav";
import Body from "@/src/components/Body";
import { useGameContext } from "@/src/app/context/game_context";
import { useEffect } from "react";

type Props = {
    searchParams: Record<string, string> | null | undefined;
};

/**
 * Home component represents the whole content of the application.
 */
export default function Home({ searchParams }: Props) {
    const showNewGameModal = searchParams?.newGame;
    const sessionId = searchParams?.sessionId;

    const gameContext = useGameContext();
    useEffect(() => {
        gameContext.updateState(sessionId, {});
    }, [gameContext, sessionId])

    return (
        <main className="min-h-screen bg-accent min-w-[400px] ">
            <Nav/>
            <Body/>
            {/* Display the NewGameModal if specified in the search parameters. */}
            {showNewGameModal && <NewGameModal/>}
        </main>
    );
}

