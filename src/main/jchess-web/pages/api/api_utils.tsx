import Config from "@/utils/config";
import type { NextApiRequest, NextApiResponse } from "next";

/**
 * Fetches data from the specified Undertow API endpoint and responds with the data.
 * @param apiEndpoint - The endpoint of the API to fetch data from.
 * @param res - The NextApiResponse object used to send the response.
 */
export async function fetchData(apiEndpoint: string, res: NextApiResponse) {
    try {
        const serverUri = Config.undertowServerUri;
        // Fetch data from the Undertow server
        const response = await fetch(`${serverUri}/api/${apiEndpoint}`);
        const data = await response.json();

        // Respond with data
        res.status(200).json(data);
    } catch (error) {
        console.error(`Error fetching ${apiEndpoint}:`, error);
        res.status(500).json({ error: error });
    }
}

