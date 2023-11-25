import { NewGameModal } from "@/components/NewGameModal";
import Nav from "@/components/Nav";
type Props = {
    searchParams: Record<string, string> | null | undefined;
};
export default function Home({ searchParams }: Props) {
    const showNewGameModal = searchParams?.newGame;
    return (
        <main className="h-screen bg-accent">
            <Nav />
            <div className="flex flex-col" />

            {showNewGameModal && <NewGameModal />}
        </main>
    );
}

