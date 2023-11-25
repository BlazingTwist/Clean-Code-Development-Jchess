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
export default function Nav() {
    return (
        <nav className="flex w-full items-center justify-between flex-wrap bg-gradient-to-r bg-primary p-6">
            <div className="flex items-center flex-shrink-0 text-white ">
                <span className="font-semibold text-xl tracking-tight">JChess</span>
            </div>
            <Menubar>
                <MenubarMenu>
                    <MenubarTrigger>Play</MenubarTrigger>
                    <MenubarContent>
                        <MenubarItem>
                            <Link href="/?newGame=true">
                                New Game <MenubarShortcut>⌘+N | Ctrl+N</MenubarShortcut>
                            </Link>
                        </MenubarItem>
                        <MenubarSeparator />
                        <MenubarItem>Load Game</MenubarItem>
                        <MenubarItem>Save Game</MenubarItem>
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

