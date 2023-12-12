import { Theme } from "@/models/message/Themes.schema";
import { fetchData } from "./apiFetcher";

type ThemeResponse = {
    themes: Theme[];
};

/**
 * Fetches the themes from the nextJs server.
 * @returns A promise that resolves to an array of themes.
 */
export async function fetchThemes(): Promise<ThemeResponse> {
    return fetchData<ThemeResponse>("themes");
}

