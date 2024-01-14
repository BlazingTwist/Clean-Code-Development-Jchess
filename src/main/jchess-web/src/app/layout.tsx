import type { Metadata } from "next";
import { Play } from "next/font/google";
import { Hotkeys } from "@/src/components/Hotkeys";
import "./globals.css";
import React from "react";
import { BoardUpdateProvider } from "@/src/app/context/board_update_context";
import { GameProvider } from "@/src/app/context/game_context";
import { ServerDataProvider } from "@/src/app/context/server_data_context";

const playFont = Play({ weight: "400", style: "normal", subsets: ["latin"] });

export const metadata: Metadata = {
    title: "JChess",
    description: "N-Player Chess App",
};

/**
 * Root layout component for the entire application.
 * @param {React.ReactNode} children - The content to be rendered inside the layout.
 */
export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="en">
            <body className={playFont.className}>
                <ServerDataProvider>
                    <BoardUpdateProvider>
                        <GameProvider>
                            <Hotkeys/>
                            {children}
                        </GameProvider>
                    </BoardUpdateProvider>
                </ServerDataProvider>
            </body>
        </html>
    );
}

