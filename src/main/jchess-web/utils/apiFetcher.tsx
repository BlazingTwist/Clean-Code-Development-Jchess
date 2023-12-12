/**
 * Fetches data from the nextjs endpoint.
 *
 * @param endpoint - The endpoint to fetch data from.
 * @returns A promise that resolves to the fetched data.
 * @throws An error if the fetch request fails or if there is an error parsing the response.
 */
export async function fetchData<T>(endpoint: string): Promise<T> {
    const response = await fetch(`http://localhost:3000/api/${endpoint}`);
    if (!response.ok) {
        throw new Error(`Error fetching ${endpoint}: ${response.status} ${response.statusText}`);
    }
    try {
        const data = await response.json();
        return data;
    } catch (error) {
        console.error(`Error fetching ${endpoint}:`, error);
        throw error;
    }
}
