import { NewGameModal } from "@/components/NewGameModal";
import Nav from "@/components/Nav";
import Body from "@/components/Body";

type Props = {
    searchParams: Record<string, string> | null | undefined;
};

/**
 * Home component represents the whole content of the application.
 *
 * @param {HomeProps} props - The properties for the Home component.
 */
export default function Home({ searchParams }: Props) {
    const showNewGameModal = searchParams?.newGame;
    const sessionId = searchParams?.sessionId;

    return (
        <main className="min-h-screen bg-accent min-w-[400px] ">
            <Nav sessionId={sessionId} />
            <Body sessionId={sessionId} />
            {/* Display the NewGameModal if specified in the search parameters. */}
            {showNewGameModal && <NewGameModal />}
        </main>
    );
}

