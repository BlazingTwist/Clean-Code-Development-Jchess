import { GameClicked } from "@/models/message/GameClicked.schema";
import { GameCreate } from "@/models/message/GameCreate.schema";
import { GameModes } from "@/models/message/GameModes.schema";
import { Themes } from "@/models/message/Themes.schema";
import { Theme } from "@/models/types/Theme.schema";
import Config from "@/utils/config";

/**
 * @function postCreateGame
 * @description Creates a new game on the server.
 * @returns A promise that resolves to the session id of the new game.
 */
export async function postCreateGame(gameCreateBody: GameCreate): Promise<string> {
    const serverUri = Config.clientUri;
    const response = await fetch(`${serverUri}/api/game/create`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(gameCreateBody),
    });
    if (!response.ok) {
        throw new Error(`Error fetching ${serverUri}/api/game/create: ${response.status} ${response.statusText}`);
    }
    try {
        const data = await response.json();
        return data;
    } catch (error) {
        console.error(`Error fetching ${serverUri}/api/game/create:`, error);
        throw error;
    }
}

/**
 * @function postClick
 * @description Sends a click to the server.
 * @param body - The click to send.
 */
export async function postClick(body: GameClicked): Promise<void> {
    const serverUri = Config.clientUri;
    const response = await fetch(`${serverUri}/api/game/clicked`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
    });
    if (!response.ok) {
        throw new Error(`Error fetching ${serverUri}/api/game/create: ${response.status} ${response.statusText}`);
    }
}

/**
 * Fetches the themes from the nextJs server.
 * @returns A promise that resolves to an array of themes.
 */
export async function fetchThemes(): Promise<Themes> {
    return fetchData<Themes>("themes");
}

/**
 * Fetches the Game Modes from the nextJs server.
 * @returns A promise that resolves to an array of themes.
 */
export async function fetchGameModes(): Promise<GameModes> {
    return fetchData<GameModes>("modes");
}

/**
 * Fetches data from the nextjs endpoint.
 *
 * @param endpoint - The endpoint to fetch data from.
 * @returns A promise that resolves to the fetched data.
 * @throws An error if the fetch request fails or if there is an error parsing the response.
 */
export async function fetchData<T>(endpoint: string): Promise<T> {
    const serverUri = Config.clientUri;
    const response = await fetch(`${serverUri}/api/${endpoint}`);
    if (!response.ok) {
        throw new Error(`Error fetching ${endpoint}: ${response.status} ${response.statusText}`);
    }
    try {
        const data = await response.json();
        return data;
    } catch (error) {
        console.error(`Error fetching ${endpoint}:`, error);
        throw error;
    }
}

