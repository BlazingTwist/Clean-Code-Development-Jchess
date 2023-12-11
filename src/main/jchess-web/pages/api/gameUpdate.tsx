import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    try {
        // Fetch data from undertow server
        const response = await fetch("http://127.0.0.1:8880/api/gameUpdate");
        const data = await response.json();

        // Respond with data
        res.status(200).json(data);
    } catch (error) {
        console.error("Error fetching GameUpdate:", error);
        res.status(500).json({ error: error });
    }
}

