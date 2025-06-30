# ♟️ Java Chess Engine

A high-performance, custom-built chess engine written in **Java**. This project is both a **learning journey** and a **performance-focused** exploration into chess programming.

It features **bitboard-based move generation**, **magic bitboards** for sliding pieces, and numerous **bitwise optimizations** for speed and efficiency.

## ✅ Features Implemented

* ⚡ **Fast move generation** (\~80M moves/sec average) ** Need to make a more robust test **
* ♜ **Full support for all move types**:
  * Normal moves
  * Castling
  * En passant
  * Promotions
* 🧠 **Bitboard-based board representation**
* ✨ **Magic bitboards** for efficient sliding piece attacks
* 📦 **Moves packed** into a single 30-bit `int`
* ♻️ `makeMove` v2 using optimized bitboard logic
* ⏪ **Undo move** functionality
* 🧾 **FEN** support for board initialization
* 🧮 Basic **evaluation function**
* 🤖 Basic bot using **minimax + evaluation**
* ✨  Minimax algorithm
* 🧮  King check detection via simulated bitboard updates

* * 🗃️ `Piece` class deprecated – now used only for constants

## 🔧 TO DO

### 🧩 Core Mechanics

* [ ] CLI/GUI board visualization

### ♟️ Engine Logic

* [ ] Alpha-beta pruning
* [ ] Quiescence search
* [ ] Iterative deepening
* [ ] Move ordering (captures, promotions, killer moves)
* [ ] Transposition table
* [ ] Zobrist hashing
* [ ] Evaluation improvements (king safety, pawn structure, etc.)
* [ ] Null move pruning
* [ ] Killer move & history heuristics
* [ ] Time management (clock awareness)
* [ ] Opening book (basic support)
* ✅ **Perft mode** for validating move generation

### 🧠 Advanced Features

* [ ] Endgame tablebases (3–4 pieces)
* [ ] Late Move Reductions (LMR)

### ⚙️ Performance & Infrastructure

* [ ] Multithreaded search (parallel evaluation, work stealing)
* [ ] Thread-safe transposition table
* [ ] Search abort / time cutoff handling

### 🛠️ Debugging & Tools

* [ ] PGN input/output
* [ ] UCI protocol support
* [ ] Search visualizer / debug output
* [ ] Command-line match mode

## 📌 Notes

This roadmap is a *living document*, and features may evolve over time as the engine grows in complexity, performance, and strength.
