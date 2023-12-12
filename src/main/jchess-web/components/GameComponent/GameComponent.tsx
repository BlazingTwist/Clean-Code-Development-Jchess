"use client";
import { useGameContext } from "@/app/context/game_context";
import TimeGameComponent from "./TimeGameComponent";
import HistoryComponent from "./HistoryComponent";
import { useGameUpdateContext } from "@/app/context/game_update_context";
import { useThemeContext } from "@/app/context/theme_context";
import { Vector2I } from "@/models/message/GameUpdate.schema";
import { useEffect, useRef, useState } from "react";

export default function GameComponment() {
    const showCoordinates = process.env.NEXT_PUBLIC_BOARD_WITH_COORDINATES === "true"; // boolean flag in .env.local file to control if coordinates are shown on the board

    // Contexts
    const { gameOptions } = useGameContext(); // old code for client side state, later it probably will be removed
    const { gameUpdate } = useGameUpdateContext(); // this is the current game state comming from the server
    const { getCurrentTheme, themeMap } = useThemeContext(); // this is the current theme selected by the user

    const canvasRef = useRef<HTMLInputElement>(null);
    const [board, setBoard] = useState<JSX.Element[]>([]);

    const theme = getCurrentTheme();

    const renderBoard = () => {
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

            const pieceSizeAdjustment = 0.7; // this is needed so the bounding box of the piece is not overlapping with other pieces
            const pieceTranslationOffset = 22.5; // this is used to center the piece on the tile and is unique for each pieceSizeAdjustment
            const translationString = `translate(${pieceTranslationOffset}%,${pieceTranslationOffset}%)`; // css translation string

            let rawBoardWidth = theme.tileAspectRatio.x + theme.tileStride.x * (maxTilePos[0] - minTilePos[0]);
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
            const offsetWidthFromCanvasRef = canvasRef.current?.offsetWidth || 1;
            const offsetHeightFromCanvasRef = canvasRef.current?.offsetHeight || 1;
            let scaleFactor = offsetWidthFromCanvasRef / rawBoardWidth;

            for (const tile of tiles) {
                let tileX = tile.position[0] - minTilePos[0];
                let offsetX = tileX * theme.tileStride.x * scaleFactor;
                let tileY = tile.position[1] - minTilePos[1];
                let offsetY = tileY * theme.tileStride.y * scaleFactor;
                let iconPath = iconMap[tile.iconId];
                let tileKey = `tile-${tile.iconId}-${tile.position[0]}-${tile.position[1]}`;
                canvas.push(
                    <div
                        key={tileKey}
                        className="absolute"
                        style={{
                            left: offsetX,
                            top: offsetY,
                            width: theme.tileAspectRatio.x * scaleFactor,
                            height: theme.tileAspectRatio.y * scaleFactor,
                        }}
                    >
                        <img className="absolute w-full h-full" src={serverUri + iconPath} />
                        {showCoordinates && (
                            <span className="absolute inset-0 flex items-center justify-center">
                                {tile.position[0] + ":" + tile.position[1]}
                            </span>
                        )}
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
                let pieceKey = `piece-${piece.identifier.iconId}-${tilePos[0]}-${tilePos[1]}`;

                let pieceWidth = theme.tileAspectRatio.x * scaleFactor * pieceSizeAdjustment;
                let pieceHeight = theme.tileAspectRatio.y * scaleFactor * pieceSizeAdjustment;

                canvas.push(
                    <div key={pieceKey} className="">
                        <img
                            onClick={() => console.log("Clicked: " + pieceKey)}
                            className={`absolute`}
                            style={{
                                left: offsetX,
                                top: offsetY,
                                width: pieceWidth,
                                height: pieceHeight,
                                transform: translationString,
                            }}
                            src={serverUri + iconPath}
                        />
                    </div>
                );
            }
            setBoard(canvas);
        } else {
            console.log("No board to render", gameUpdate, theme?.tileAspectRatio, theme?.tileStride);
        }
    };

    const handleResize = () => {
        renderBoard();
    };

    function readVector2I(vector: Vector2I) {
        return [vector.x, vector.y];
    }

    useEffect(() => {
        // add event listener to resize the canvas when the window is resized
        window.addEventListener("resize", handleResize);

        // call renderBoard() once to render the board
        renderBoard();
        // cleanup function
        return () => {
            window.removeEventListener("resize", handleResize);
        };
    }, []);

    return (
        <div className="grid grid-cols-1 gap-2 p-12 items-center sm:grid-cols-2 lg:grid-cols-3  sm:grid-row-2 max-w-[2000px] mx-auto">
            <div
                ref={canvasRef}
                className="w-[80vw] h-[80vw] md:w-[55vw] md:h-[55vw] lg:w-full lg:h-[100%] min-w-[200px] min-h-[200px] max-w-[80vh] max-h-[80vh]  justify-self-center sm:row-span-2 sm:col-span-2 relative"
            >
                {board}
            </div>

            {gameOptions.isTimeGame && <TimeGameComponent />}
            {<HistoryComponent />}
        </div>
    );
}

