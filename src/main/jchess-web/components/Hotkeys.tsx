"use client";

import { useRouter } from "next/navigation";
import { useHotkeys } from "@mantine/hooks";
import { useGameContext } from "@/app/context/game_context";

export const Hotkeys = () => {
    const router = useRouter();
    const { resetGame } = useGameContext();
    useHotkeys([
        ["alt+n", () => router.push(`/?newGame=true`)],
        ["escape", () => resetGame()],
    ]);

    return null;
};

