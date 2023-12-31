import type { Metadata } from "next";
import { Play } from "next/font/google";
import { Hotkeys } from "@/components/Hotkeys";
import "./globals.css";
import { GameProvider } from "./context/game_context";
import { ThemeProvider } from "./context/theme_context";
import { GameUpdateProvider } from "./context/game_update_context";

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
                <ThemeProvider>
                    <GameProvider>
                        <GameUpdateProvider>
                            <Hotkeys />
                            {children}
                        </GameUpdateProvider>
                    </GameProvider>
                </ThemeProvider>
            </body>
        </html>
    );
}

