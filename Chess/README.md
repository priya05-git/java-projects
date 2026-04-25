# Java Chess Game ♟

A complete, two-player implementation of classic Chess written entirely in Java and powered by a custom Swing graphical user interface.

## Overview

This project provides a fully playable Chess game in a single Java file (`Chess.java`). It features automatic enforcement of all chess rules, visual indicators for moves, check states, and a clean, responsive graphical board. 

## Features

- **Full Chess Rules Validation:** 
  - Validates all piece movements.
  - Supports special moves: Castling, En Passant, and Pawn Promotion (auto-queens by default).
- **Game State Detection:**
  - Automatic detection of Check.
  - End-game state detection for Checkmate and Stalemate.
- **Visual Aids:**
  - Highlights the most recent move.
  - Displays legal move target dots when a piece is selected.
  - Highlights the King's square in red when in check.
- **Side Panel Information:**
  - Indicates which player's turn it is.
  - Displays a visual collection of captured pieces.
  - Real-time status bar showing game events.
  - "New Game" button to reset the board instantly.
- **Clean Aesthetic UI:**
  - Custom colors and styling utilizing Java Swing properties and Unicode chess symbols.

## Requirements

- Java Development Kit (JDK) 8 or higher.

## How to Compile and Run

1. Open your terminal or command prompt.
2. Navigate to the directory containing `Chess.java`.
3. Compile the Java file using the `javac` compiler:
   ```bash
   javac Chess.java
   ```
4. Run the compiled application using the `java` command:
   ```bash
   java Chess
   ```

## Gameplay Instructions

- **Select a Piece**: Click on one of your pieces (White starts first) to view its legal moves, represented by small dots on the board.
- **Move**: Click on one of the highlighted squares to execute a move. 
- **Capturing**: If an enemy piece is on a highlighted square, clicking it will capture the piece and move yours to that square.
- **Restarting**: If the game ends in Checkmate/Stalemate or you just want to restart, click the "New Game" button on the right-hand panel.

## License

This project is open-source and free to use for personal or educational purposes.

---
*Made by Priya*
