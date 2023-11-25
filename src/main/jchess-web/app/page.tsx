type Props = {
    searchParams: Record<string, string> | null | undefined;
};
export default function Home({ searchParams }: Props) {
    const showNewGameModal = searchParams?.newGame;
    return <main className="h-screen bg-accent">Hello World!</main>;
}

