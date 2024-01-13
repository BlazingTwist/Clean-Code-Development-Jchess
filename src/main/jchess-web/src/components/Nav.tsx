"use client";
/**
 * Navigation component for the JChess application.
 * This component includes a menu bar with Play, Options, and Help sections.
 * It handles game-related actions and provides navigation links.
 */
import Link from "next/link";
import {
    Menubar,
    MenubarContent,
    MenubarItem,
    MenubarMenu,
    MenubarSeparator,
    MenubarShortcut,
    MenubarTrigger,
} from "@/src/components/ui/menubar";
import { useGameContext } from "@/src/app/context/game_context";
import { ReactElement } from "react";
import { useRouter } from "next/navigation";

/**
 * Main navigation component for the application.
 * Displays a menu bar with Play, Options, and Help sections.
 */
export default function Nav(): ReactElement {
    // Use the game update context to check game status and reset the game.
    const gameContext = useGameContext();
    const router = useRouter();
    const isGame = gameContext.gameInfo != undefined;

    /**
     * Render the navigation component.
     * @returns {JSX.Element} The JSX element representing the navigation component.
     */
    return (
        <nav className="flex w-full items-center justify-between flex-wrap bg-gradient-to-r bg-primary p-6">
            {/* JCHESS logo */}
            <div className="flex items-center flex-shrink-0 text-white ">
                <span className="font-semibold text-xl tracking-tight">JChess</span>
            </div>

            {/* Menu bar for Play, Options, and Help */}
            <Menubar>
                {/* Play menu */}
                <MenubarMenu>
                    <MenubarTrigger>Play</MenubarTrigger>
                    <MenubarContent>
                        {!isGame && <MenubarItem disabled>Load Game</MenubarItem>}
                        {isGame && <MenubarItem disabled>Save Game</MenubarItem>}
                        <MenubarSeparator/>
                        {!isGame && (
                            <MenubarItem>
                                <Link href="/?newGame=true">
                                    New Game <MenubarShortcut>⌥+N | Altl+N</MenubarShortcut>
                                </Link>
                            </MenubarItem>
                        )}
                        {isGame && (
                            <MenubarItem onClick={() => router.push("/")}>
                                Exit Game <MenubarShortcut>ESC</MenubarShortcut>
                            </MenubarItem>
                        )}
                    </MenubarContent>
                </MenubarMenu>

                {/* Options menu */}
                <MenubarMenu>
                    <MenubarTrigger>Options</MenubarTrigger>
                    <MenubarContent>
                        <MenubarItem disabled>Theme</MenubarItem>
                    </MenubarContent>
                </MenubarMenu>

                {/* Help menu */}
                <MenubarMenu>
                    <MenubarTrigger>Help</MenubarTrigger>
                    <MenubarContent>
                        <MenubarItem disabled>Rules</MenubarItem>
                    </MenubarContent>
                </MenubarMenu>
            </Menubar>
        </nav>
    );
}

