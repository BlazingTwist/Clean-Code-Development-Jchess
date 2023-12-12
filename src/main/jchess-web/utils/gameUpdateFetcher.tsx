import { GameUpdate } from "@/models/message/GameUpdate.schema";
import { fetchData } from "./apiFetcher";

/**
 * Fetches the game update from the nextJs server.
 * @returns A promise that resolves to a GameUpdate object.
 */
export async function fetchGameUpdate(): Promise<GameUpdate> {
    return fetchData<GameUpdate>("gameUpdate");
}

