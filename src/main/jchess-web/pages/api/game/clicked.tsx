import Config from "@/utils/config";
import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === "POST") {
        // Process a POST request
        const serverUri = Config.undertowServerUri;
        const endpoint = `${serverUri}/api/game/clicked`;
        const response = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(req.body),
        });

        if (!response.ok) {
            throw new Error(`Error fetching ${endpoint}: ${response.status} ${response.statusText}`);
        }

        res.status(200).end();
    } else {
        // only allow POST requests
        res.status(405).send("Method Not Allowed");
    }
}

