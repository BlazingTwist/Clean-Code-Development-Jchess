"use client";
import { Theme } from "@/models/message/Themes.schema";
import { createContext, useContext, Dispatch, SetStateAction, useState, useEffect } from "react";

interface ContextProps {
    theme: string;
    setTheme: Dispatch<SetStateAction<string>>;
    themeMap: Map<string, Theme>;
    getCurrentTheme: () => Theme | undefined;
}

const ThemeContext = createContext<ContextProps>({
    theme: "",
    setTheme: () => {},
    themeMap: new Map<string, Theme>(),
    getCurrentTheme: () => undefined,
});

export const ThemeProvider = ({ children }: { children: React.ReactNode }) => {
    const [theme, setTheme] = useState<string>("default");
    const [themeMap, setThemeMap] = useState<Map<string, Theme>>(new Map<string, Theme>());

    useEffect(() => {
        console.log("Fetching Themes");
        fetch("http://localhost:8880/api/themes")
            .then((response) => {
                if (!response.ok) {
                    throw new Error(`Error fetching Themes: ${response.status} ${response.statusText}`);
                }
                return response.json();
            })
            .then((data) => {
                const themeMap = new Map<string, Theme>();
                console.log("Themes:", data);

                data["themes"].forEach((theme: Theme) => {
                    themeMap.set(theme.name, theme);
                });
                setThemeMap(themeMap);
            })
            .catch((error) => {
                console.error("Error fetching Themes:", error);
            });
        console.log("Fetching Themes Done");
    }, []);

    const getCurrentTheme = (): Theme => {
        return themeMap.get(theme) ?? themeMap.get("default")!;
    };

    return (
        <ThemeContext.Provider value={{ theme, setTheme, themeMap, getCurrentTheme }}>{children}</ThemeContext.Provider>
    );
};

export const useThemeContext = () => useContext(ThemeContext);

