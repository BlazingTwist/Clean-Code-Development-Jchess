import { NewGameModal } from "@/components/NewGameModal";
import Nav from "@/components/Nav";
import Body from "@/components/Body";

type Props = {
    searchParams: Record<string, string> | null | undefined;
};
export default function Home({ searchParams }: Props) {
    const showNewGameModal = searchParams?.newGame;

    return (
        <main className="min-h-screen bg-accent min-w-[400px] ">
            <Nav />
            <Body />
            {showNewGameModal && <NewGameModal />}
        </main>
    );
}

