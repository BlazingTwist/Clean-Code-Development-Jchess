"use client";
import { createContext, useContext, Dispatch, SetStateAction, useState, useEffect, ReactNode } from "react";
import { Theme } from "@/models/message/Themes.schema";
import { fetchThemes } from "@/services/rest_api_service";
import Config from "@/utils/config";

/**
 * The properties provided by the ThemeContext.
 */
interface ThemeContextProps {
    theme: string;
    setTheme: Dispatch<SetStateAction<string>>;
    themeMap: Map<string, Theme>;
    getCurrentTheme: () => Theme | undefined;
}

/**
 * The context for managing themes.
 * @defaultValue { theme: "", setTheme: () => {}, themeMap: new Map<string, Theme>(), getCurrentTheme: () => undefined }
 */
const ThemeContext = createContext<ThemeContextProps>({
    theme: "",
    setTheme: () => {},
    themeMap: new Map<string, Theme>(),
    getCurrentTheme: () => undefined,
});

/**
 * The properties for the ThemeProvider component.
 */
interface ThemeProviderProps {
    children: ReactNode;
}

/**
 * Provides a context for managing themes.
 * @param {ThemeProviderProps} props - The component properties.
 * @returns {JSX.Element} The JSX element.
 */
export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
    // Determine if cookies should be saved based on the environment variable
    const useLocalStorage = Config.useLocalStorage;

    // The key for storing the themeMap in localStorage
    const storageKey = "themeMap";

    // Initialize state for the selected theme and theme map
    const [theme, setTheme] = useState<string>("default");
    const [themeMap, setThemeMap] = useState<Map<string, Theme>>(() => {
        // Load the initial state from localStorage if saving cookies is enabled
        return useLocalStorage
            ? new Map(JSON.parse(localStorage.getItem(storageKey) || "null"))
            : new Map<string, Theme>();
    });

    // Fetch themes and update themeMap on component mount
    useEffect(() => {
        console.log("Fetching Themes");

        fetchThemes().then((themeResponse) => {
            const newThemeMap = new Map<string, Theme>();
            themeResponse.themes.forEach((theme) => {
                newThemeMap.set(theme.name, theme);
            });
            setThemeMap(newThemeMap);

            // Save themeMap to localStorage if saving cookies is enabled
            Config.useLocalStorage &&
                localStorage.setItem(storageKey, JSON.stringify(Array.from(newThemeMap.entries())));
        });

        console.log("Fetching Themes Done");
    }, []);

    /**
     * Gets the current selected theme.
     * @returns {Theme} The current theme.
     */
    const getCurrentTheme = (): Theme => {
        return themeMap.get(theme) ?? themeMap.get("default")!;
    };

    // Create the context value to be provided
    const contextValue: ThemeContextProps = { theme, setTheme, themeMap, getCurrentTheme };

    // Provide the context to the children components
    return <ThemeContext.Provider value={contextValue}>{children}</ThemeContext.Provider>;
};

/**
 * Custom hook for accessing the ThemeContext.
 * @returns {ThemeContextProps} The context properties.
 */
export const useThemeContext = (): ThemeContextProps => {
    // Get the context from the ThemeContext
    const context = useContext(ThemeContext);

    // Return the context properties
    return context!;
};

