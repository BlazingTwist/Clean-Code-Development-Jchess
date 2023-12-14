import type { NextApiRequest, NextApiResponse } from "next";
import { fetchData } from "@/pages/api/api_utils";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    await fetchData("gameUpdate", res);
}

