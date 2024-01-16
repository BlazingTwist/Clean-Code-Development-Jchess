"use client";
import React, { createContext, useContext, useState, ReactNode, useEffect } from "react";
import { PieceType, Vector2I } from "@/models/Themes.schema";
import { MarkerComponent, PieceComponent, TileComponent } from "@/models/GameUpdate.schema";
import { useServerDataContext } from "@/src/app/context/server_data_context";
import { useGameContext } from "@/src/app/context/game_context";

export interface ThemeHelper {
    getPlayerColors: () => string[];
    getTileSize: () => Vector2I;
    getTileStride: () => Vector2I;
    getTileIcon: (tile: TileComponent) => string;
    getPieceIcon: (piece: PieceComponent) => string;
    getPieceIconByType: (pieceType: PieceType, player: number) => string;
    getMarkerIcon: (marker: MarkerComponent) => string;
}

export interface ContextProps {
    themeHelper?: ThemeHelper;
}

const ThemeHelperContext = createContext<ContextProps>({});

interface ProviderProps {
    children: ReactNode;
}

export const ThemeHelperProvider: React.FC<ProviderProps> = (props: ProviderProps) => {
    const serverData = useServerDataContext();
    const gameContext = useGameContext();

    const [state, setState] = useState<ContextProps>({});

    useEffect(() => {
            console.log("useEffect in ThemeHelper")
            if (!serverData.allThemes) {
                console.log("Themes not loaded yet, cannot provide ThemeHelper");
                return;
            }

            if (!gameContext.gameInfo) {
                console.log("No gameInfo available yet, cannot provide ThemeHelper");
                return;
            }

            const themeName = gameContext.gameInfo.themeName;
            const activeTheme = serverData.allThemes.themes.find(theme => theme.displayName === themeName);
            if (!activeTheme) {
                console.warn(`Unable to find theme with name: '${themeName}'`);
                return;
            }

            const layoutId = gameContext.gameInfo.layoutId;
            const activeLayout = activeTheme.boardTheme?.layouts.find(x => x.layoutId === layoutId);
            if (!activeLayout) {
                console.warn(`Unable to find layout in themes. LayoutId : '${layoutId}'`);
                return undefined;
            }

            const pieceTheme = activeTheme.piecesTheme!;
            const iconPrefix = serverData.allThemes.resourcePrefix;

            setState({
                themeHelper: {
                    getPlayerColors(): string[] {
                        return pieceTheme.playerColors.map(x => x.colorCode);
                    },
                    getTileSize(): Vector2I {
                        return activeLayout.tileSize;
                    },
                    getTileStride(): Vector2I {
                        return activeLayout.tileStride;
                    },
                    getTileIcon(tile: TileComponent): string {
                        return iconPrefix + "/" + activeLayout.tiles[tile.tileColorIndex];
                    },
                    getPieceIcon(piece: PieceComponent): string {
                        let pieceThemeEntry = pieceTheme.pieces.find(x => x.pieceType == piece.identifier.pieceTypeId);
                        let pieceColor = pieceTheme.playerColors[piece.identifier.ownerId];
                        return iconPrefix + "/" + pieceThemeEntry?.pathPrefix + pieceColor?.fileSuffix + pieceThemeEntry?.pathSuffix;
                    },
                    getPieceIconByType(pieceType: PieceType, player: number): string {
                        let pieceThemeEntry = pieceTheme.pieces.find(x => x.pieceType == pieceType);
                        let pieceColor = pieceTheme.playerColors[player];
                        return iconPrefix + "/" + pieceThemeEntry?.pathPrefix + pieceColor?.fileSuffix + pieceThemeEntry?.pathSuffix;
                    },
                    getMarkerIcon(marker: MarkerComponent): string {
                        return iconPrefix + "/" + activeLayout.markers.find(x => x.markerType == marker.markerType)?.icon;
                    }
                }
            });
        },
        [serverData, gameContext, setState]
    )

    // Provide the context to the children components
    return <ThemeHelperContext.Provider value={{ ...state }}>
        {props.children}
    </ThemeHelperContext.Provider>;
};

/**
 * Custom hook for accessing the GameContext.
 * @returns {SessionData} The context properties.
 */
export const useThemeHelperContext = (): ContextProps => {
    return useContext(ThemeHelperContext);
};

