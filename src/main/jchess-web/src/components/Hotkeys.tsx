"use client";

import { useRouter } from "next/navigation";
import { useHotkeys } from "@mantine/hooks";
import { useGameContext } from "@/src/app/context/game_context";

export const Hotkeys = () => {
    const router = useRouter();
    const { updateState: updateGameContext } = useGameContext();
    useHotkeys([
        ["alt+n", () => router.push(`/?newGame=true`)],
        ["escape", () => updateGameContext(undefined, {})],
    ]);

    return null;
};

