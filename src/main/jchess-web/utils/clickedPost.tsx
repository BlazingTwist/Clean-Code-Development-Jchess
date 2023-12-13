import { GameClicked } from "@/models/message/GameClicked.schema";
import Config from "./config";

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

