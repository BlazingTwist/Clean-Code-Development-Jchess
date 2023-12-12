"use client";
import { Theme } from "@/models/message/Themes.schema";
import { fetchThemes } from "@/utils/themeFetcher";
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

        fetchThemes().then((ThemeResponse) => {
            const themeMap = new Map<string, Theme>();
            ThemeResponse.themes.forEach((theme) => {
                themeMap.set(theme.name, theme);
            });
            setThemeMap(themeMap);
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

