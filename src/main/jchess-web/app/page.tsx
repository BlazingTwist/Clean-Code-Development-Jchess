import { NewGameModal } from "@/components/NewGameModal";
import Nav from "@/components/Nav";
import GameComponment from "@/components/GameComponent";
type Props = {
    searchParams: Record<string, string> | null | undefined;
};
export default function Home({ searchParams }: Props) {
    const showNewGameModal = searchParams?.newGame;
    const isGame = searchParams?.game;

    return (
        <main className="min-h-screen bg-accent min-w-[400px] ">
            <Nav />

            {!isGame && (
                <div className="flex flex-col justify-center flex-grow text-center">
                    <h1 className="text-4xl font-bold pt-12">Welcome to JChess!</h1>
                    <p>Play chess with up to 3 friends!</p>
                </div>
            )}

            {isGame && <GameComponment />}
            {showNewGameModal && <NewGameModal />}
        </main>
    );
}

