import { GameClicked } from "@/models/GameClicked.schema";
import { GameCreate } from "@/models/GameCreate.schema";
import { GameModes } from "@/models/GameModes.schema";
import { Themes } from "@/models/Themes.schema";

/**
 * @function postCreateGame
 * @description Creates a new game on the server.
 * @returns A promise that resolves to the session id of the new game.
 */
export async function postCreateGame(gameCreateBody: GameCreate): Promise<string> {
    const response = await fetch(`api/game/create`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(gameCreateBody),
    });
    if (!response.ok) {
        throw new Error(`Error fetching api/game/create: ${response.status} ${response.statusText}`);
    }
    try {
        return await response.json();
    } catch (error) {
        console.error(`Error fetching api/game/create:`, error);
        throw error;
    }
}

/**
 * @function postClick
 * @description Sends a click to the server.
 * @param body - The click to send.
 */
export async function postClick(body: GameClicked): Promise<void> {
    const response = await fetch(`api/game/clicked`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
    });
    if (!response.ok) {
        throw new Error(`Error fetching api/game/create: ${response.status} ${response.statusText}`);
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
    const response = await fetch(`api/${endpoint}`);
    if (!response.ok) {
        throw new Error(`Error fetching ${endpoint}: ${response.status} ${response.statusText}`);
    }
    try {
        return await response.json();
    } catch (error) {
        console.error(`Error fetching ${endpoint}:`, error);
        throw error;
    }
}

