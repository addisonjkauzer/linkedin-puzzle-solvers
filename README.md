# LinkedInPuzzleSolvers

Automatically solves LinkedIn's daily puzzles by scraping the game board from the browser and playing back the solution via Selenium keyboard inputs. Includes a visualization mode that lets you watch each algorithm work in real time, with recordings uploaded to S3.

## Supported Puzzles

### Zip
A path-finding puzzle where you must visit every cell on a grid in order, passing through numbered checkpoints. Solved with depth-first search and multi-threaded reachability pruning.

- **`fetchAndSubmit()`** - Solves the puzzle and plays back the solution instantly.
- **`visualizeAlgorithm(singleThreaded)`** - Watches the DFS explore and backtrack live in the browser.
- **`visualizeAlgorithm(multiThreaded)`** - Same visualization with parallel reachability checks that prune dead-end branches early. Logs cache hit rate on completion.

### Sudoku (Mini)
LinkedIn's 6x6 mini sudoku with 2x3 blocks. Solved with iterative constraint propagation - repeatedly filling in cells that have only one possible value.

- **`fetchAndSubmit()`** - Solves the puzzle first, then fills in the entire board.
- **`visualizeAlgorithm()`** - Navigates cell-by-cell as the solver iterates, filling in values as they are determined.

### Tango
A grid puzzle where each cell must be filled with a sun or moon, subject to row/column balance constraints and equality/inequality edge markers. Solved with constraint propagation.

- **`fetchAndSubmit()`** - Solves the puzzle and fills in only the empty cells.
- **`visualizeAlgorithm()`** - Navigates cell-by-cell as the solver iterates, filling in values as they are determined.

## Prerequisites

- **Java 25**
- **Google Chrome** installed

## Running

Tests are the primary entry point. Each E2E test opens Chrome, navigates to the LinkedIn game page, and runs the solver.

```bash
# Zip
./gradlew test --tests "com.Zip.ZipPuzzleE2ETest.solvePuzzle"
./gradlew test --tests "com.Zip.ZipPuzzleE2ETest.visualizeAlgorithm"
./gradlew test --tests "com.Zip.ZipPuzzleE2ETest.visualizeMultiThreadAlgorithm"

# Sudoku
./gradlew test --tests "com.Sudoku.SudokuPuzzleE2ETest.solvePuzzle"
./gradlew test --tests "com.Sudoku.SudokuPuzzleE2ETest.visualizeAlgorithm"

# Tango
./gradlew test --tests "com.Tango.TangoPuzzleE2ETest.solvePuzzle"
./gradlew test --tests "com.Tango.TangoPuzzleE2ETest.visualizeAlgorithm"
```

## How It Works

1. **Selenium** opens Chrome and navigates to the LinkedIn game page.
2. The page HTML is scraped and parsed with **Jsoup** to extract the board state.
3. The solver algorithm runs on the parsed board.
4. The solution is played back by sending arrow keys and other inputs through Selenium's Actions API, directly into the game iframe.

In visualization mode, the solver and the browser are coupled — the algorithm drives keyboard inputs as it runs, so you see the solver's decision-making process play out on screen.

## Lambda Deployment

The project can be deployed as an AWS Lambda function that runs all three visualizations on a schedule and uploads recordings to S3.

- Recordings are captured with **jcodec** and uploaded to the bucket specified by the `RECORDINGS_BUCKET` environment variable.
- The Lambda is packaged as a Docker container using a custom runtime (`lambda/bootstrap`).
- Solve times are published as CloudWatch metrics under the `LinkedInPuzzleSolvers` namespace.

```bash
docker build -t linkedin-puzzle-solvers .
```

## Tech Stack

- Spring Boot 4.0
- Selenium 4.29
- Jsoup 1.22
- jcodec
- AWS SDK (S3, CloudWatch)
- Lombok
- JUnit 5

## Disclaimer
I started this project with the sole intention of having fun coming up with algorithms and optimizations to solving these puzzles. To this end, the code to solve the puzzle after it's been converted to a 2d array is completely my own.
The remaining code to pull the puzzle and input the solution is brought to you by Claude Code
