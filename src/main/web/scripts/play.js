document.addEventListener("DOMContentLoaded", function () {
    const playButton = document.getElementById("playButton");
    const modal = document.getElementById("modal");
    const startGameButton = document.getElementById("startGameButton");
    const numberOfPlayersInput = document.getElementById("numberOfPlayers");
    const errorMessage = document.getElementById("errorMessage");

    playButton.addEventListener("click", function () {
        // Show the modal
        modal.classList.remove("hidden");

        // Handle the "Start Game" button click
        startGameButton.addEventListener("click", function () {
            const numberOfPlayers = numberOfPlayersInput.value;

            // Validate the input
            if (isValidNumberOfPlayers(numberOfPlayers)) {
                errorMessage.classList.add("hidden"); // Hide the error message
                numberOfPlayersInput.classList.remove("border", "border-red-500"); // Remove error styling

                modal.classList.add("hidden");
            } else {
                errorMessage.classList.remove("hidden"); // Show the error message
                numberOfPlayersInput.classList.add("border", "border-red-500"); // Apply error styling
            }
        });
    });

    // Validate the number of players
    function isValidNumberOfPlayers(number) {
        return !isNaN(number) && number >= 2 && number <= 5;
    }
});

