import Config from "./config";

export async function createGame(): Promise<string> {
    const serverUri = Config.clientUri;
    const response = await fetch(`${serverUri}/api/game/create`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({}),
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

