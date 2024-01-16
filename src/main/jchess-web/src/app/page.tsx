"use client";
import { NewGameModal } from "@/src/components/NewGameModal";
import Nav from "@/src/components/Nav";
import Body from "@/src/components/Body";
import { useGameContext } from "@/src/app/context/game_context";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Alert from "../components/Alert";

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
    const router = useRouter();
    const [isSessionNotFoundAlertOpen, setSessionNotFoundAlertOpen] = useState<boolean>(false);

    useEffect(() => {
        if (paramSessionId !== sessionId) {
            setSessionId(paramSessionId);
        }
    }, [paramSessionId, sessionId]);

    useEffect(() => {
        console.warn(`sessionId changed to '${sessionId}'`);
        gameContext.updateState(sessionId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [sessionId]);

    useEffect(() => {
        // if a session is requested && the gameContext has loaded the requested session, but no gameInfo was found
        if (paramSessionId && paramSessionId == gameContext.sessionId && !gameContext.gameInfo) {
            setSessionNotFoundAlertOpen(true);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [gameContext]);

    /**
     * Close the SessionNotFound Alert and redirect to the home page.
     */
    const closeSessionNotFoundAlert = () => {
        setSessionNotFoundAlertOpen(false);
        router.push("/");
    };

    return (
        <main className="min-h-screen bg-accent min-w-[400px] ">
            <Nav />
            <Body />
            {/* Display the SessionNotFound Alert if its not found */}
            {isSessionNotFoundAlertOpen && (
                <Alert
                    title="Oh Oh!"
                    descrption={`No Session found for sessionId: ${sessionId}`}
                    open={isSessionNotFoundAlertOpen}
                    onOpenChange={closeSessionNotFoundAlert}
                />
            )}
            {/* Display the NewGameModal if specified in the search parameters. */}
            {showNewGameModal && <NewGameModal />}
        </main>
    );
}

