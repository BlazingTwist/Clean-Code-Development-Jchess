"use client";
import {createContext, Dispatch, ReactNode, SetStateAction, useContext, useEffect, useState} from "react";
import {LayoutId, PieceType, Theme, Vector2I} from "@/models/Themes.schema";
import {fetchGameModes, fetchThemes} from "@/services/rest_api_service";
import Config from "@/utils/config";
import {GameMode} from "@/models/GameModes.schema";
import {MarkerComponent, PieceComponent, TileComponent} from "@/models/GameUpdate.schema";

/**
 * The properties provided by the ThemeContext.
 */
interface ThemeContextProps {
    theme: string;
    setTheme: Dispatch<SetStateAction<string>>;
    setLayout: Dispatch<SetStateAction<LayoutId>>
    themeMap: Map<string, Theme>;
    getCurrentTheme: () => Theme | undefined;
    getThemeHelper: () => ThemeHelper | undefined;
    gameModeMap: Map<string, GameMode>;
}

export interface ThemeHelper {
    getTileSize: () => Vector2I;
    getTileStride: () => Vector2I;
    getTileIcon: (tile: TileComponent) => string;
    getPieceIcon: (piece: PieceComponent) => string;
    getPieceIconByType: (pieceType: PieceType, player: number) => string;
    getMarkerIcon: (marker: MarkerComponent) => string;
}

// TODO refactor contexts ThemeContext contains knowledge of themes and game modes, either split into two contexts or rename to something more generic

/**
 * The context for managing themes and game modes
 * @defaultValue { theme: "", setTheme: () => {}, themeMap: new Map<string, Theme>(), getCurrentTheme: () => undefined }
 */
const ThemeContext = createContext<ThemeContextProps>({
    theme: "",
    setTheme: () => {
    },
    setLayout: () => {
    },
    themeMap: new Map<string, Theme>(),
    getCurrentTheme: () => undefined,
    getThemeHelper: () => undefined,
    gameModeMap: new Map<string, GameMode>(),
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
export const ThemeProvider: React.FC<ThemeProviderProps> = ({children}) => {
    // Determine if localStorage should be used based on the environment variable
    const useLocalStorage = Config.useLocalStorage;

    // The key for storing the gameModeMap in localStorage
    const gameModeMapKey = "gameModeMap";

    // Initialize state for the game modes
    const [gameModeMap, setGameModeMap] = useState<Map<string, GameMode>>(() => {
        // Load the initial state from localStorage if saving cookies is enabled
        return useLocalStorage
            ? new Map(JSON.parse(localStorage.getItem(gameModeMapKey) || "null"))
            : new Map<string, GameMode>();
    });

    // The key for storing the themeMap in localStorage
    const themeMapStorageKey = "themeMap";

    // Initialize state for the selected theme and theme map
    const [iconPrefix, setIconPrefix] = useState<string>("");
    const [theme, setTheme] = useState<string>("default");
    const [curLayout, setLayout] = useState<LayoutId>("hex3p");
    const [themeMap, setThemeMap] = useState<Map<string, Theme>>(() => {
        // Load the initial state from localStorage if saving cookies is enabled
        return useLocalStorage
            ? new Map(JSON.parse(localStorage.getItem(themeMapStorageKey) || "null"))
            : new Map<string, Theme>();
    });

    // Fetch gameModes and themes and update themeMap on component mount
    useEffect(() => {
        // Fetch gameModes
        console.log("Fetching GameModes");
        fetchGameModes().then((gameModesResponse) => {
            const newGameModeMap = new Map<string, GameMode>();
            gameModesResponse.modes.forEach((gameMode) => {
                newGameModeMap.set(gameMode.displayName, gameMode);
            });
            setGameModeMap(newGameModeMap);
        });
        console.log("Fetching GameModes Done");

        console.log("Fetching Themes");
        fetchThemes().then((themeResponse) => {
            const newThemeMap = new Map<string, Theme>();
            themeResponse.themes.forEach((theme) => {
                newThemeMap.set(theme.displayName, theme);
            });
            setIconPrefix(themeResponse.resourcePrefix);
            setThemeMap(newThemeMap);

            // Save themeMap to localStorage if saving cookies is enabled
            if (Config.useLocalStorage) {
                localStorage.setItem(themeMapStorageKey, JSON.stringify(Array.from(newThemeMap.entries())));
                localStorage.setItem(gameModeMapKey, JSON.stringify(Array.from(newThemeMap.entries())));
            }
        });
        console.log("Fetching Themes Done");
    }, []);

    /**
     * Gets the current selected theme.
     * @returns {Theme} The current theme.
     */
    const getCurrentTheme = (): Theme | undefined => {
        return themeMap.get(theme);
    };

    const getThemeHelper = (): ThemeHelper | undefined => {
        const theme = getCurrentTheme();
        if (theme === undefined) {
            return undefined;
        }

        const layoutTheme = theme.boardTheme?.layouts.find(layout => layout.layoutId == curLayout);
        if (layoutTheme === undefined) {
            console.warn("layoutTheme is undefined");
            return undefined;
        }

        return {
            getTileSize() {
                return layoutTheme.tileSize;
            },
            getTileStride() {
                return layoutTheme.tileStride;
            },
            getTileIcon(tile) {
                return iconPrefix + "/" + layoutTheme.tiles[tile.tileColorIndex];
            },
            getPieceIcon(piece) {
                let pieceThemeEntry = theme.piecesTheme?.pieces.find(x => x.pieceType == piece.identifier.pieceTypeId);
                let pieceColor = theme.piecesTheme?.playerColors[piece.identifier.ownerId];
                return iconPrefix + "/" + pieceThemeEntry?.pathPrefix + pieceColor?.fileSuffix + pieceThemeEntry?.pathSuffix;
            },
            getPieceIconByType(pieceType, playerIdx) {
                let pieceThemeEntry = theme.piecesTheme?.pieces.find(x => x.pieceType == pieceType);
                let pieceColor = theme.piecesTheme?.playerColors[playerIdx];
                return iconPrefix + "/" + pieceThemeEntry?.pathPrefix + pieceColor?.fileSuffix + pieceThemeEntry?.pathSuffix;
            },
            getMarkerIcon(marker) {
                return iconPrefix + "/" + layoutTheme.markers.find(x => x.markerType == marker.markerType)?.icon;
            }
        }
    };

    // Create the context value to be provided
    const contextValue: ThemeContextProps = {
        theme,
        setTheme,
        setLayout,
        themeMap,
        getCurrentTheme,
        getThemeHelper,
        gameModeMap,
    };

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

