"use client";
import React, { useEffect, useRef, useState } from "react";
import { useGameContext } from "@/app/context/game_context";
import { useGameUpdateContext } from "@/app/context/game_update_context";
import { useThemeContext } from "@/app/context/theme_context";
import { Vector2I } from "@/models/message/GameUpdate.schema";
import Config from "@/utils/config";
import TimeGameComponent from "./TimeGameComponent";
import HistoryComponent from "./HistoryComponent";
import { postClick } from "@/services/rest_api_service";
import PlayerOverviewComponent from "./PlayerOverviewComponent";
import PieceSelectionComponent from "./PieceSelectionComponent";

export default function GameComponment({ sessionId }: { sessionId: string }) {
    const showCoordinates = Config.boardWithCoordinates; // boolean flag in .env.local file to control if coordinates are shown on the board

    // Contexts
    const { gameOptions } = useGameContext(); // old code for client side state, later it probably will be removed
    const { gameUpdate } = useGameUpdateContext(); // this is the current game state comming from the server
    const { getCurrentTheme, getCurrentIconMap } = useThemeContext(); // this is the current theme selected by the user

    // State
    const canvasRef = useRef<HTMLInputElement>(null);
    const [board, setBoard] = useState<JSX.Element[]>([]);

    const theme = getCurrentTheme();
    const iconMap = getCurrentIconMap();

    /**
     * @function calculateMinMaxTilePosition
     * @description Calculates the min and max tile positions from a tiles array.
     * @param {any[]}
     */
    function calculateMinMaxTilePosition(tiles: { position: number[]; iconId: string }[]) {
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
        return { maxTilePos, minTilePos };
    }

    /**
     * @function renderBoard
     * @description Renders the board based on the current gameUpdate state and theme.
     */
    const renderBoard = () => {
        if (gameUpdate && theme?.tileAspectRatio && theme?.tileStride) {
            // get the tiles, pieces and markers arrays from the gameUpdate
            const { tiles, pieces, markers } = getEntityArrays();
            // calculate the min and max tile positions
            const { maxTilePos, minTilePos } = calculateMinMaxTilePosition(tiles);

            // calculate the piece size adjustment and translation offset to ensure clickability of the pieces
            const pieceSizeAdjustment = 0.7; // this is needed so the bounding box of the piece is not overlapping with other pieces
            const pieceTranslationOffset = 22.5; // this is used to center the piece on the tile and is unique for each pieceSizeAdjustment
            const translationString = `translate(${pieceTranslationOffset}%,${pieceTranslationOffset}%)`; // css translation string

            // create the canvas
            // TODO: add markers
            // TODO: add click handlers
            const canvas: JSX.Element[] = getBoardCanvas(
                maxTilePos,
                minTilePos,
                tiles,
                pieces,
                markers,
                pieceSizeAdjustment,
                translationString
            );
            setBoard(canvas);
        } else {
            console.log("waiting for game update", gameUpdate, theme);
        }
    };

    const handleResize = () => {
        renderBoard();
    };

    /**
     * @function getBoardCanvas
     * @description Creates the canvas for the board. Populates the canvas with images for tiles, pieces and markers.
     * @param {number[]} maxTilePos - The maximum tile position.
     * @param {number[]} minTilePos - The minimum tile position.
     * @param {any[]} tiles - The tiles array.
     * @param {any[]} pieces - The pieces array.
     * @param {any[]} markers - The markers array.
     * @param {number} pieceSizeAdjustment - The piece size adjustment.
     * @param {string} translationString - The translation string.
     */
    function getBoardCanvas(
        maxTilePos: number[],
        minTilePos: number[],
        tiles: { position: number[]; iconId: string }[],
        pieces: {
            identifier: import("@/models/message/GameUpdate.schema").PieceIdentifier;
            tile: { position: number[]; iconId: string };
        }[],
        markers: {
            markerType: import("@/models/message/GameUpdate.schema").MarkerType;
            tile: { position: number[]; iconId: string };
            iconId: string;
        }[],
        pieceSizeAdjustment: number,
        translationString: string
    ) {
        const canvas: JSX.Element[] = [];
        const serverUri = Config.clientUri + "/api/";
        const offsetWidthFromCanvasRef = canvasRef.current?.offsetWidth || 1;
        const offsetHeightFromCanvasRef = canvasRef.current?.offsetHeight || 1;
        const rawBoardWidth = theme!.tileAspectRatio!.x + theme!.tileStride!.x * (maxTilePos[0] - minTilePos[0]);
        const rawBoardHeight = theme!.tileAspectRatio!.y + theme!.tileStride!.y * (maxTilePos[1] - minTilePos[0]);

        let scaleFactor = offsetWidthFromCanvasRef / rawBoardWidth;

        for (const tile of tiles) {
            const tileX = tile.position[0] - minTilePos[0];
            const offsetX = tileX * theme!.tileStride!.x * scaleFactor;
            const tileY = tile.position[1] - minTilePos[1];
            const offsetY = tileY * theme!.tileStride!.y * scaleFactor;
            const iconPath = iconMap[tile.iconId];
            const tileKey = `tile-${tile.iconId}-${tile.position[0]}-${tile.position[1]}`;
            canvas.push(
                <div
                    key={tileKey}
                    className="absolute"
                    style={{
                        left: offsetX,
                        top: offsetY,
                        width: theme!.tileAspectRatio!.x * scaleFactor,
                        height: theme!.tileAspectRatio!.y * scaleFactor,
                    }}
                >
                    <img
                        className="absolute w-full h-full"
                        src={serverUri + iconPath}
                        onClick={() => {
                            console.log("Clicked: " + tileKey);

                            postClick({
                                sessionId: sessionId,
                                clickPos: {
                                    x: tile.position[0],
                                    y: tile.position[1],
                                },
                            });
                        }}
                    />
                    {showCoordinates && (
                        <span className="absolute inset-0 flex items-center justify-center">
                            {tile.position[0] + ":" + tile.position[1]}
                        </span>
                    )}
                </div>
            );
        }

        for (const piece of pieces) {
            const tilePos = piece.tile.position;
            const x = tilePos[0] - minTilePos[0];
            const offsetX = x * theme!.tileStride!.x * scaleFactor;
            const y = tilePos[1] - minTilePos[1];
            const offsetY = y * theme!.tileStride!.y * scaleFactor;
            const iconPath = iconMap[piece.identifier.iconId];
            const pieceKey = `piece-${piece.identifier.iconId}-${tilePos[0]}-${tilePos[1]}`;

            const pieceWidth = theme!.tileAspectRatio!.x * scaleFactor * pieceSizeAdjustment;
            const pieceHeight = theme!.tileAspectRatio!.y * scaleFactor * pieceSizeAdjustment;

            canvas.push(
                <div key={pieceKey} className="">
                    <img
                        className={`absolute`}
                        style={{
                            left: offsetX,
                            top: offsetY,
                            width: pieceWidth,
                            height: pieceHeight,
                            transform: translationString,
                            pointerEvents: "none",
                        }}
                        src={serverUri + iconPath}
                    />
                </div>
            );
        }

        for (const marker of markers) {
            const markerPos = marker.tile.position;
            const x = markerPos[0] - minTilePos[0];
            const offsetX = x * theme!.tileStride!.x * scaleFactor;
            const y = markerPos[1] - minTilePos[1];
            const offsetY = y * theme!.tileStride!.y * scaleFactor;
            const markerKey = `marker-${marker.iconId}-${markerPos[0]}-${markerPos[1]}`;
            const iconPath = iconMap[marker.iconId];

            canvas.push(
                <div key={markerKey} className="">
                    <img
                        style={{
                            top: offsetY,
                            left: offsetX,
                            position: "absolute",
                            width: theme!.tileAspectRatio!.x * scaleFactor,
                            height: theme!.tileAspectRatio!.y * scaleFactor,
                            pointerEvents: "none",
                        }}
                        src={serverUri + iconPath}
                    ></img>
                </div>
            );
        }

        return canvas;
    }

    /**
     * @function getEntityArrays
     * @description Creates the tiles, pieces and markers arrays from the gameUpdate.
     */
    function getEntityArrays() {
        const tiles = [];
        const pieces = [];
        const markers = [];
        // populate the tiles, pieces and markers arrays
        for (const entity of gameUpdate!.boardState) {
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
                    iconId: marker.iconId,
                    tile: tileObject,
                });
            }
        }
        return { tiles, pieces, markers };
    }

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
    }, [gameUpdate, getCurrentTheme()]);

    return (
        <div className="grid grid-cols-1 gap-2 p-12 items-center sm:grid-cols-2 lg:grid-cols-3  sm:grid-row-2 max-w-[2000px] mx-auto">
            <div
                ref={canvasRef}
                className="w-[80vw] h-[80vw] md:w-[55vw] md:h-[55vw] lg:w-full lg:h-[100%] min-w-[200px] min-h-[200px] max-w-[80vh] max-h-[80vh]  justify-self-center sm:row-span-2 sm:col-span-2 relative"
            >
                <PieceSelectionComponent sessionId={sessionId} iconMap={iconMap} />

                {board}
            </div>

            {gameOptions.isTimeGame && <TimeGameComponent />}
            {!gameOptions.isTimeGame && <PlayerOverviewComponent />}
            {<HistoryComponent />}
        </div>
    );
}

