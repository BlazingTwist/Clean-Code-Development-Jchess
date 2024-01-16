"use client";
import React, { ReactElement, useContext, useEffect, useState } from "react";
import { Themes } from "@/models/Themes.schema";
import { GameModes } from "@/models/GameModes.schema";
import { fetchGameModes, fetchThemes } from "@/src/services/rest_api_service";

interface Props {
    children: React.ReactNode;
}

/**
 * Contains all static server data.
 */
export interface ServerData {
    allThemes?: Themes;
    allGameModes?: GameModes;
}

const ServerDataContext = React.createContext<ServerData>({});

export const ServerDataProvider: React.FC<Props> = (props: Props): ReactElement => {
    const [state, setState] = useState<ServerData>({});

    // remember if resources were fetched instead of checking for null. (otherwise, we might fetch multiple times before first response returns)
    const [dataFetched, setDataFetched] = useState<boolean>(false);

    useEffect(() => {
        if (dataFetched) {
            return;
        }
        setDataFetched(true);

        console.log(`fetching themes and game modes. fetched: ${dataFetched}`)
        Promise.all([
            fetchThemes(), fetchGameModes()
        ]).then(data => {
            setState({ ...state, allThemes: data[0], allGameModes: data[1] });
        })
    }, [dataFetched, state]);

    return (
        <ServerDataContext.Provider value={{ ...state }}>
            {props.children}
        </ServerDataContext.Provider>
    );
};

export const useServerDataContext = (): ServerData => {
    return useContext(ServerDataContext);
};
