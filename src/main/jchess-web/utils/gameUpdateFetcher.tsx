import { GameUpdate } from "@/models/message/GameUpdate.schema";

export async function fetchGameUpdate(): Promise<GameUpdate> {
    const response = await fetch("http://localhost:3000/api/gameUpdate");
    if (!response.ok) {
        throw new Error(`Error fetching GameUpdate: ${response.status} ${response.statusText}`);
    }
    try {
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Error fetching GameUpdate:", error);
        throw error;
    }
}

