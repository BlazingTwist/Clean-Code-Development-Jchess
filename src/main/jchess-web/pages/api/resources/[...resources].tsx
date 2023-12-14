import Config from "@/utils/config";
import { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    // iterate over the query parameters
    if (req.query && req.query["resources"]) {
        let query = "";
        if (Array.isArray(req.query["resources"])) {
            query = req.query["resources"].join("/");
        } else {
            query = req.query["resources"] as string;
        }

        const serverUri = Config.undertowServerUri;

        if (serverUri === undefined) {
            throw new Error("JCHESS_UNDERTOW_SERVER_URI is undefined, make sure to set it in .env.local");
        }

        const response = await fetch(`${serverUri}/resources/${query}`);
        const contentType = response.headers.get("content-type");

        if (contentType && contentType.startsWith("image/png")) {
            const imageArrayBuffer = await response.arrayBuffer(); // Convert the response body to ArrayBuffer
            const imageBuffer = Buffer.from(imageArrayBuffer); // Convert ArrayBuffer to Buffer

            // Set appropriate headers for the response
            res.setHeader("Content-Type", "image/png");
            res.setHeader("Cache-Control", "public, max-age=604800"); // You can adjust the cache control as needed

            // Send the image buffer as the response
            res.send(imageBuffer);
        } else {
            // Handle non-image response (e.g., return an error)
            res.status(400).json({ error: "Invalid content type or resource not found" });
        }
    } else {
        // Handle missing query parameter
        res.status(400).json({ error: 'Missing "resources" query parameter' });
    }
}

