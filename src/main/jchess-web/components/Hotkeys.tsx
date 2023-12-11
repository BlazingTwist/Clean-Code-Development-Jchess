"use client";

import { useRouter } from "next/navigation";
import { useHotkeys } from "@mantine/hooks";
import { useGameUpdateContext } from "@/app/context/game_update_context";

export const Hotkeys = () => {
    const router = useRouter();
    const { resetGame, isGame } = useGameUpdateContext();
    useHotkeys([
        ["alt+n", () => !isGame && router.push(`/?newGame=true`)], // only allow new game if not in game
        ["escape", () => isGame && resetGame()], // only allow exit game if in game
    ]);

    return null;
};

