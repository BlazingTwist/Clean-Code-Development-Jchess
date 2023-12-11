export default function DefaultChessboard() {
    // TODO replace this with a real chessboard
    const chessState = [
        ["r", "n", "b", "q", "k", "b", "n", "r"],
        ["p", "p", "p", "p", "p", "p", "p", "p"],
        ["", "", "", "", "", "", "", ""],
        ["", "", "", "", "", "", "", ""],
        ["", "", "", "", "", "", "", ""],
        ["", "", "", "", "", "", "", ""],
        ["P", "P", "P", "P", "P", "P", "P", "P"],
        ["R", "N", "B", "Q", "K", "B", "N", "R"],
    ];

    const renderChessboard = () => {
        const board: JSX.Element[] = [];
        for (let i = 0; i < chessState.length; i++) {
            const row: JSX.Element[] = [];
            for (let j = 0; j < chessState[i].length; j++) {
                row.push(
                    <div
                        key={`${i},${j}`}
                        className={`w-[12.5%] h-full flex justify-center items-center aspect-square ${
                            (i + j) % 2 == 0 ? "bg-white" : "bg-black"
                        } `}
                    >
                        {chessState[i][j] != "" && (
                            <span className={`${(i + j) % 2 == 0 ? "text-black" : "text-white"}`}>
                                {chessState[i][j]}
                            </span>
                        )}
                    </div>
                );
            }
            board.push(
                <div key={i} className="flex flex-row h-[12.5%]">
                    {row}
                </div>
            );
        }

        return board;
    };

    return <div className="w-full h-full">{renderChessboard()}</div>;
}

