package com.example.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel : ViewModel() {
    var state by mutableStateOf(GameState())

    val boardItems: MutableMap<Int, BoardCellValue> = mutableMapOf(
        1 to BoardCellValue.NONE,
        2 to BoardCellValue.NONE,
        3 to BoardCellValue.NONE,
        4 to BoardCellValue.NONE,
        5 to BoardCellValue.NONE,
        6 to BoardCellValue.NONE,
        7 to BoardCellValue.NONE,
        8 to BoardCellValue.NONE,
        9 to BoardCellValue.NONE,
    )

    fun onAction(action: UserAction) {
        when (action) {
            is UserAction.BoardTapped -> {
                addValueToBoard(action.cellNo)
            }

            UserAction.PlayAgainButtonClicked -> {
                GlobalScope.launch {
                    delay(500)
                    gameReset()
                }
            }
        }
    }

    private fun gameReset() {
        boardItems.forEach { (i, _) ->
            boardItems[i] = BoardCellValue.NONE
        }
        if  (state.startingTurn == BoardCellValue.CIRCLE) {
            state = state.copy(
                hintText = "Player 'O' turn",
                currentTurn = BoardCellValue.CIRCLE,
                victoryType = VictoryType.NONE,
                hasWon = false,
                startingTurn = BoardCellValue.CROSS
            )
        } else if (state.startingTurn == BoardCellValue.CROSS) {
            state = state.copy(
                hintText = "Player 'X' turn",
                currentTurn = BoardCellValue.CROSS,
                victoryType = VictoryType.NONE,
                hasWon = false,
                startingTurn = BoardCellValue.CIRCLE
            )
            GlobalScope.launch {
                delay(1500)
                computerMove()
                state = state.copy(
                    hintText = "Player 'O' turn",
                    currentTurn = BoardCellValue.CIRCLE
                )
            }

        }
    }

    private fun addValueToBoard(cellNo: Int) {
        if (boardItems[cellNo] != BoardCellValue.NONE) {
            return
        }
        if (state.currentTurn == BoardCellValue.CIRCLE) {
            boardItems[cellNo] = BoardCellValue.CIRCLE
            state = if (checkForVictory(BoardCellValue.CIRCLE)) {
                state.copy(
                    hintText = "Player 'O' Won",
                    playerCircleCount = state.playerCircleCount + 1,
                    currentTurn = BoardCellValue.NONE,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw",
                    drawCount = state.drawCount + 1
                )
            } else {
                state.copy(
                    hintText = "Player 'X' turn",
                    currentTurn = BoardCellValue.CROSS
                )
            }
            GlobalScope.launch {
                delay(500)
                if (state.currentTurn == BoardCellValue.CROSS) {
                    computerMove()
                    state = if (checkForVictory(BoardCellValue.CROSS)) {
                        state.copy(
                            hintText = "Player 'X' Won",
                            playerCrossCount = state.playerCrossCount + 1,
                            currentTurn = BoardCellValue.NONE,
                            hasWon = true
                        )
                    } else if (hasBoardFull()) {
                        state.copy(
                            hintText = "Game Draw",
                            drawCount = state.drawCount + 1
                        )
                    } else {
                        state.copy(
                            hintText = "Player 'O' turn",
                            currentTurn = BoardCellValue.CIRCLE
                        )
                    }
                }
            }
        }
    }

    private fun checkForVictory(boardValue: BoardCellValue): Boolean {
        when {
            boardItems[1] == boardValue && boardItems[2] == boardValue && boardItems[3] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL1)
                return true
            }

            boardItems[4] == boardValue && boardItems[5] == boardValue && boardItems[6] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL2)
                return true
            }

            boardItems[7] == boardValue && boardItems[8] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[4] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL1)
                return true
            }

            boardItems[2] == boardValue && boardItems[5] == boardValue && boardItems[8] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL2)
                return true
            }

            boardItems[3] == boardValue && boardItems[6] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[5] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL1)
                return true
            }

            boardItems[3] == boardValue && boardItems[5] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL2)
                return true
            }

            else -> return false
        }
    }

    private fun canWin(): Boolean {
        for (i in 1..9) {
            if (boardItems[i] == BoardCellValue.NONE) {
                // ลองว่า X ในแต่ละช่องเพื่อเช็กว่ามีโอกาสชนะไหม
                boardItems[i] = BoardCellValue.CROSS
                if (checkForVictory(BoardCellValue.CROSS)) {
                    return true
                }
                // เล่นตาก่อนหน้าแล้วเพื่อไม่เปลี่ยนสถานะเกม
                boardItems[i] = BoardCellValue.NONE
            }
        }
        return false
    }

    private fun canBlock(): Boolean {
        for (i in 1..9) {
            if (boardItems[i] == BoardCellValue.NONE) {
                boardItems[i] = BoardCellValue.CIRCLE
                // ลองว่า O ในแต่ละช่องเพื่อเช็กว่าผู้เล่นมีโอกาสชนะไหม
                if (checkForVictory(BoardCellValue.CIRCLE)) {
                    // บอทสามารถบล็อกการชนะของผู้เล่น O
                    boardItems[i] = BoardCellValue.CROSS
                    return true
                }
                // เล่นตาก่อนหน้าแล้วเพื่อไม่เปลี่ยนสถานะเกม
                boardItems[i] = BoardCellValue.NONE
            }
        }
        return false
    }

    private fun middleFree(): Boolean {
        if (boardItems[5] == BoardCellValue.NONE) {
            boardItems[5] = BoardCellValue.CROSS
            return true
        }
        return false
    }

    private fun computerMove() {
        if (canWin()) {
        } else if (canBlock()) {
        } else if (middleFree()) {
        } else {
            val availablePositions = mutableListOf<Int>()

            // ค้นหาตำแหน่งที่ว่างบนบอร์ด
            for (i in 1..9) {
                if (boardItems[i] == BoardCellValue.NONE) {
                    availablePositions.add(i)
                }
            }

            // สุ่มตำแหน่งที่ว่าง
            if (availablePositions.isNotEmpty()) {
                val randomIndex = Random.nextInt(availablePositions.size)
                val randomPosition = availablePositions[randomIndex]

                // วางเครื่องหมาย X ในตำแหน่งที่สุ่มได้
                boardItems[randomPosition] = BoardCellValue.CROSS
            }
        }
    }

    fun hasBoardFull(): Boolean {
        return !boardItems.containsValue(BoardCellValue.NONE)
    }
}