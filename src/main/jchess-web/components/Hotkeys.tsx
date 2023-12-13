"use client";

import { useRouter } from "next/navigation";
import { useHotkeys } from "@mantine/hooks";
import { useGameContext } from "@/app/context/game_context";

export const Hotkeys = () => {
    const router = useRouter();
    const { isGame, resetGame } = useGameContext();
    useHotkeys([
        ["alt+n", () => !isGame && router.push(`/?newGame=true`)], // only allow new game if not in game
        ["escape", () => isGame && resetGame()], // only allow exit game if in game
    ]);

    return null;
};

