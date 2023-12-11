"use client";
import Link from "next/link";
import {
    Menubar,
    MenubarContent,
    MenubarItem,
    MenubarMenu,
    MenubarSeparator,
    MenubarShortcut,
    MenubarTrigger,
} from "@/components/ui/menubar";
import { useGameUpdateContext } from "@/app/context/game_update_context";
export default function Nav() {
    const { isGame, resetGame } = useGameUpdateContext();

    return (
        <nav className="flex w-full items-center justify-between flex-wrap bg-gradient-to-r bg-primary p-6">
            <div className="flex items-center flex-shrink-0 text-white ">
                <span className="font-semibold text-xl tracking-tight">JChess</span>
            </div>
            <Menubar>
                <MenubarMenu>
                    <MenubarTrigger>Play</MenubarTrigger>
                    <MenubarContent>
                        {!isGame && <MenubarItem>Load Game</MenubarItem>}
                        {isGame && <MenubarItem>Save Game</MenubarItem>}
                        <MenubarSeparator />
                        {!isGame && (
                            <MenubarItem>
                                <Link href="/?newGame=true">
                                    New Game <MenubarShortcut>⌥+N | Altl+N</MenubarShortcut>
                                </Link>
                            </MenubarItem>
                        )}
                        {isGame && (
                            <MenubarItem onClick={() => resetGame()}>
                                Exit Game <MenubarShortcut>ESC</MenubarShortcut>
                            </MenubarItem>
                        )}
                    </MenubarContent>
                </MenubarMenu>
                <MenubarMenu>
                    <MenubarTrigger>Options</MenubarTrigger>
                    <MenubarContent>
                        <MenubarItem>Theme</MenubarItem>
                    </MenubarContent>
                </MenubarMenu>
                <MenubarMenu>
                    <MenubarTrigger>Help</MenubarTrigger>
                    <MenubarContent>
                        <MenubarItem>Rules</MenubarItem>
                    </MenubarContent>
                </MenubarMenu>
            </Menubar>
        </nav>
    );
}

