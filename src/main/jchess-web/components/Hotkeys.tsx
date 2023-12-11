"use client";

import { useRouter } from "next/navigation";
import { useHotkeys } from "@mantine/hooks";

export const Hotkeys = () => {
    const router = useRouter();
    useHotkeys([
        ["alt+n", () => router.push(`/?newGame=true`)],
        ["escape", () => router.push(`/`)],
    ]);

    return null;
};

