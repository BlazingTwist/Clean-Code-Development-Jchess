import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    try {
        const serverUri = process.env.JCHESS_UNDERTOW_SERVER_URI;
        if (serverUri === undefined) {
            throw new Error("JCHESS_UNDERTOW_SERVER_URI is undefined, make sure to set it in .env.local");
        }

        // Fetch data from undertow server
        const response = await fetch(`${serverUri}/api/gameUpdate`);
        const data = await response.json();

        // Respond with data
        res.status(200).json(data);
    } catch (error) {
        console.error("Error fetching GameUpdate:", error);
        res.status(500).json({ error: error });
    }
}

