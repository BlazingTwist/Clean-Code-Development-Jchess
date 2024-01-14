"use client";
import React, {ReactElement, useCallback, useEffect, useRef, useState} from "react";
import {useGameContext} from "@/src/app/context/game_context";
import {useBoardUpdateContext} from "@/src/app/context/board_update_context";
import {Entity} from "@/models/BoardUpdate.schema";
import Config from "@/src/utils/config";
import {postClick} from "@/src/services/rest_api_service";
import PlayerOverviewComponent from "./PlayerOverviewComponent";
import ChatComponent from "./ChatComponent";
import PieceSelectionComponent from "./PieceSelectionComponent";
import {useThemeHelperContext} from "@/src/app/context/theme_helper_context";
import GameOverComponent from "@/src/components/GameComponent/GameOverComponent";

export default function GameComponent(): ReactElement {
    const showCoordinates = Config.boardWithCoordinates; // boolean flag in .env.local file to control if coordinates are shown on the board

    // Contexts
    const gameContext = useGameContext();
    const {boardUpdate} = useBoardUpdateContext(); // this is the current game state coming from the server
    const {themeHelper} = useThemeHelperContext();

    // State
    const canvasRef = useRef<HTMLInputElement>(null);
    const [board, setBoard] = useState<ReactElement[]>([]);

    /**
     * @description Calculates the min and max tile positions from a tiles array.
     */
    function calculateMinMaxTilePosition(entities: Entity[]): { minTilePos: any; maxTilePos: any } {
        let minTilePos;
        let maxTilePos;
        {
            let minX = Number.POSITIVE_INFINITY;
            let minY = Number.POSITIVE_INFINITY;
            let maxX = Number.NEGATIVE_INFINITY;
            let maxY = Number.NEGATIVE_INFINITY;
            for (let entity of entities) {
                if (!entity.tile) continue;

                minX = Math.min(minX, entity.tile.displayPos.x);
                maxX = Math.max(maxX, entity.tile.displayPos.x);
                minY = Math.min(minY, entity.tile.displayPos.y);
                maxY = Math.max(maxY, entity.tile.displayPos.y);
            }
            minTilePos = [minX, minY];
            maxTilePos = [maxX, maxY];
        }
        return {maxTilePos, minTilePos};
    }

    /**
     * @description Creates the canvas for the board. Populates the canvas with images for tiles, pieces and markers.
     * @param maxTilePos - The maximum tile position.
     * @param minTilePos - The minimum tile position.
     * @param entities - The tiles array.
     * @param pieceSizeAdjustment - The piece size adjustment.
     * @param translationString - The translation string.
     */
    const getBoardCanvas = useCallback((
        maxTilePos: number[],
        minTilePos: number[],
        entities: Entity[],
        pieceSizeAdjustment: number,
        translationString: string
    ): ReactElement[] => {
        if (!themeHelper) {
            return [];
        }

        const canvas: ReactElement[] = [];
        const offsetWidthFromCanvasRef = canvasRef.current?.offsetWidth || 1;
        const offsetHeightFromCanvasRef = canvasRef.current?.offsetHeight || 1;
        const tileSize = themeHelper.getTileSize();
        const tileStride = themeHelper.getTileStride();
        const rawBoardWidth = tileSize!.x + tileStride.x * (maxTilePos[0] - minTilePos[0]);
        const rawBoardHeight = tileSize!.y + tileStride.y * (maxTilePos[1] - minTilePos[1]);
        let scaleFactor = offsetWidthFromCanvasRef / rawBoardWidth;

        const centerX = offsetWidthFromCanvasRef / 2;
        const centerY = offsetHeightFromCanvasRef / 2;

        // Calculate the total size of the tiles group
        const totalTilesWidth = rawBoardWidth * scaleFactor;
        const totalTilesHeight = rawBoardHeight * scaleFactor;

        // Calculate the starting point for the first tile
        const startOffsetX = centerX - totalTilesWidth / 2;
        const startOffsetY = centerY - totalTilesHeight / 2;

        entities.filter(e => e.tile).forEach(entity => {
            const tile = entity.tile!;
            const tileX = tile.displayPos.x - minTilePos[0];
            const offsetX = startOffsetX + tileX * tileStride!.x * scaleFactor;
            const tileY = tile.displayPos.y - minTilePos[1];
            const offsetY = startOffsetY + tileY * tileStride!.y * scaleFactor;
            const iconPath = themeHelper.getTileIcon(tile);
            const tileKey = `tile-${tile.tileColorIndex}-${tile.displayPos.x}-${tile.displayPos.y}`;
            canvas.push(
                <div
                    key={tileKey}
                    className="absolute"
                    style={{
                        left: offsetX,
                        top: offsetY,
                        width: tileSize!.x * scaleFactor,
                        height: tileSize!.y * scaleFactor,
                    }}
                >
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                        className="absolute w-full h-full"
                        src={"api/" + iconPath}
                        onClick={() => {
                            console.log("Clicked: " + tileKey);

                            postClick({
                                sessionId: gameContext.sessionId!,
                                clickedTile: tile.tileId,
                            }).then();
                        }}
                        alt={""}
                    />
                    {showCoordinates && (
                        <span className="absolute inset-0 flex items-center justify-center">
                            {tile.displayPos.x + ":" + tile.displayPos.y}
                        </span>
                    )}
                </div>
            );
        });

        entities.filter(e => e.tile && e.piece).forEach(entity => {
            const tilePos = entity.tile!.displayPos;
            const piece = entity.piece!;
            const x = tilePos.x - minTilePos[0];
            const offsetX = startOffsetX + x * tileStride!.x * scaleFactor;
            const y = tilePos.y - minTilePos[1];
            const offsetY = startOffsetY + y * tileStride!.y * scaleFactor;
            const iconPath = themeHelper.getPieceIcon(piece);
            const pieceKey = `piece-${piece.identifier.pieceTypeId}${piece.identifier.ownerId}-${tilePos.x}-${tilePos.y}`;

            const pieceWidth = tileSize!.x * scaleFactor * pieceSizeAdjustment;
            const pieceHeight = tileSize!.y * scaleFactor * pieceSizeAdjustment;

            canvas.push(
                <div key={pieceKey} className="">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
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
                        src={"api/" + iconPath}
                        alt={""}
                    />
                </div>
            );
        });

        entities.filter(e => e.tile && e.marker).forEach(entity => {
            const marker = entity.marker!;
            const markerPos = entity.tile!.displayPos;
            const x = markerPos.x - minTilePos[0];
            const offsetX = startOffsetX + x * tileStride!.x * scaleFactor;
            const y = markerPos.y - minTilePos[1];
            const offsetY = startOffsetY + y * tileStride!.y * scaleFactor;
            const markerKey = `marker-${marker.markerType}-${markerPos.x}-${markerPos.y}`;
            const iconPath = themeHelper.getMarkerIcon(marker);

            canvas.push(
                <div key={markerKey} className="">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                        style={{
                            top: offsetY,
                            left: offsetX,
                            position: "absolute",
                            width: tileSize!.x * scaleFactor,
                            height: tileSize!.y * scaleFactor,
                            pointerEvents: "none",
                        }}
                        src={"api/" + iconPath}
                        alt={""}
                    />
                </div>
            );
        });

        return canvas;
    }, [gameContext.sessionId, showCoordinates, themeHelper]);

    /**
     * @description Renders the board based on the current boardUpdate state and theme.
     */
    const renderBoard = useCallback(() => {
        if (!boardUpdate) {
            console.log("waiting for game update.");
            return;
        }
        if (!themeHelper) {
            console.log("waiting for themeHelper.");
            return;
        }

        // calculate the min and max tile positions
        const {maxTilePos, minTilePos} = calculateMinMaxTilePosition(boardUpdate.boardState);

        // calculate the piece size adjustment and translation offset to ensure clickability of the pieces
        const pieceSizeAdjustment = 0.7; // this is needed so the bounding box of the piece is not overlapping with other pieces
        const pieceTranslationOffset = 22.5; // this is used to center the piece on the tile and is unique for each pieceSizeAdjustment
        const translationString = `translate(${pieceTranslationOffset}%,${pieceTranslationOffset}%)`; // css translation string

        // create the canvas
        const canvas = getBoardCanvas(
            maxTilePos,
            minTilePos,
            boardUpdate.boardState,
            pieceSizeAdjustment,
            translationString
        );
        setBoard(canvas);
    }, [boardUpdate, getBoardCanvas, themeHelper]);

    const handleResize = useCallback(() => {
        renderBoard();
    }, [renderBoard]);

    useEffect(() => {
        // add event listener to resize the canvas when the window is resized
        window.addEventListener("resize", handleResize);

        // call renderBoard() once to render the board
        renderBoard();
        // cleanup function
        return () => {
            window.removeEventListener("resize", handleResize);
        };
    }, [handleResize, renderBoard]);

    return (
        <div className="p-12 max-w-[2000px] mx-auto flex flex-col xl:flex-row items-center md:justify-center gap-12">
            <div
                ref={canvasRef}
                className="w-[80vw] h-[80vw] xl:w-[50vw] xl:h-[50vw] md:w-[65vw] md:h-[65vw] min-w-[20px] min-h-[200px] max-w-[80vh] max-h-[80vh] justify-self-center relative"
            >
                <PieceSelectionComponent/>
                <GameOverComponent/>

                {board}
            </div>

            <div className="flex flex-col sm:flex-row xl:flex-col gap-2">
                <PlayerOverviewComponent className="w-full"/>
                <ChatComponent className="w-full sm:col-start-2 sm:col-end-3 sm:row-span-2 sm:row-start-1"/>
            </div>
        </div>
    );
}

