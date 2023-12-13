import { fetchData } from "@/pages/api/api_utils";
import { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    await fetchData("modes", res);
}

