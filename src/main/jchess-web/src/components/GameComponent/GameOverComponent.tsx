import {ReactElement, useEffect, useState} from "react";
import {useGameContext} from "@/src/app/context/game_context";
import {GameOver} from "@/models/GameOver.schema";
import {GameOverSubscribe} from "@/models/GameOverSubscribe.schema";

export default function GameOverComponent(): ReactElement {
    const [localSocketConnId, setLocalSocketConnId] = useState<number>(-1);

    const gameContext = useGameContext();

    useEffect(() => {
        if (gameContext.socketConnectionId === localSocketConnId) {
            return;
        }

        setLocalSocketConnId(gameContext.socketConnectionId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [gameContext]);

    // Websocket
    useEffect(() => {
        if(localSocketConnId < 0) return;

        console.log(`Subscribing to gameOver. socketId: ${localSocketConnId}`);

        gameContext.gameOverSocket.addListener(event => {
            let data: GameOver = JSON.parse(event.data);
            console.log("GameOver WebSocket message received", data);
            // TODO handle gameOver
        });

        const subscribeMessage: GameOverSubscribe = {
            sessionId: gameContext.sessionId!,
        }
        gameContext.gameOverSocket.sendMessage(JSON.stringify(subscribeMessage))

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [localSocketConnId]);

    return <></> // TODO
}
