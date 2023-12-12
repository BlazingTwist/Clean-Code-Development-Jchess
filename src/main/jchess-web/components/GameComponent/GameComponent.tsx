"use client";
import DefaultChessboard from "./Chessboard/default_chessboard";
import { useGameContext } from "@/app/context/game_context";
import HexChessboard from "./Chessboard/hex_chessboard";
import TimeGameComponent from "./TimeGameComponent";
import HistoryComponent from "./HistoryComponent";
import { useGameUpdateContext } from "@/app/context/game_update_context";
import { useThemeContext } from "@/app/context/theme_context";
import { Vector2I } from "@/models/message/GameUpdate.schema";

export default function GameComponment() {
    const { gameOptions } = useGameContext();

    const { gameUpdate } = useGameUpdateContext();
    const { getCurrentTheme } = useThemeContext();

    const renderBoard = () => {
        const theme = getCurrentTheme();

        const tiles = [];
        const pieces = [];
        const markers = [];

        if (gameUpdate && theme?.tileAspectRatio && theme?.tileStride) {
            for (const entity of gameUpdate.boardState) {
                const tile = entity.tile;
                const piece = entity.piece;
                const marker = entity.marker;

                let tileObject = null;
                if (tile) {
                    tileObject = {
                        position: readVector2I(tile.position),
                        iconId: tile.iconId,
                    };
                    tiles.push(tileObject);
                }
                if (piece && tileObject) {
                    pieces.push({
                        identifier: piece.identifier,
                        tile: tileObject,
                    });
                }
                if (marker && tileObject) {
                    markers.push({
                        markerType: marker.markerType,
                        tile: tileObject,
                    });
                }
            }

            let minTilePos;
            let maxTilePos;

            {
                let minX = Number.POSITIVE_INFINITY;
                let minY = Number.POSITIVE_INFINITY;
                let maxX = Number.NEGATIVE_INFINITY;
                let maxY = Number.NEGATIVE_INFINITY;
                for (let tile of tiles) {
                    minX = Math.min(minX, tile.position[0]);
                    maxX = Math.max(maxX, tile.position[0]);
                    minY = Math.min(minY, tile.position[1]);
                    maxY = Math.max(maxY, tile.position[1]);
                }
                minTilePos = [minX, minY];
                maxTilePos = [maxX, maxY];
            }

            let rawBoardWidth = theme.tileAspectRatio.x + theme.tileStride.y * (maxTilePos[0] - minTilePos[0]);
            let rawBoardHeight = theme.tileAspectRatio.y + theme.tileStride.y * (maxTilePos[1] - minTilePos[0]);

            const iconMap: { [key: string]: string } = theme.icons.reduce(
                (map: { [key: string]: string }, icon: any) => {
                    map[icon.iconId] = icon.iconPath;
                    return map;
                },
                {}
            );

            const serverUri = "http://127.0.0.1:8880/";
            const canvas: JSX.Element[] = [];
            const scaleFactor = 1;
            for (const tile of tiles) {
                let tileX = tile.position[0] - minTilePos[0];
                let offsetX = tileX * theme.tileStride.x * scaleFactor;
                let tileY = tile.position[1] - minTilePos[1];
                let offsetY = tileY * theme.tileStride.y * scaleFactor;
                let iconPath = iconMap[tile.iconId];

                canvas.push(
                    <div>
                        <img
                            className="absolute"
                            style={{
                                left: offsetX,
                                top: offsetY,
                                width: theme.tileAspectRatio.x * scaleFactor,
                                height: theme.tileAspectRatio.y * scaleFactor,
                            }}
                            src={serverUri + iconPath}
                        />
                    </div>
                );
            }

            for (const piece of pieces) {
                let tilePos = piece.tile.position;
                let x = tilePos[0] - minTilePos[0];
                let offsetX = x * theme.tileStride.x * scaleFactor;
                let y = tilePos[1] - minTilePos[1];
                let offsetY = y * theme.tileStride.y * scaleFactor;
                let iconPath = iconMap[piece.identifier.iconId];

                canvas.push(
                    <div>
                        <img
                            className="absolute"
                            style={{
                                left: offsetX,
                                top: offsetY,
                                width: theme.tileAspectRatio.x * scaleFactor,
                                height: theme.tileAspectRatio.y * scaleFactor,
                            }}
                            src={serverUri + iconPath}
                        />
                    </div>
                );
            }
            return canvas;
        }

        return <></>;
    };

    function readVector2I(vector: Vector2I) {
        return [vector.x, vector.y];
    }

    return (
        <div className="grid grid-cols-1 gap-2 p-12 items-center sm:grid-cols-2 lg:grid-cols-3  sm:grid-row-2 max-w-[2000px] mx-auto">
            <div className="w-[80vw] h-[80vw] md:w-[55vw] md:h-[55vw] lg:w-full lg:h-full min-w-[200px] min-h-[200px] max-w-[80vh] max-h-[80vh]  justify-self-center sm:row-span-2 sm:col-span-2 relative">
                {renderBoard()}
            </div>

            {gameOptions.isTimeGame && <TimeGameComponent />}
            {<HistoryComponent />}
        </div>
    );
}

