import kotlin.random.Random
import java.util.Scanner

enum class CellType(val char: Char) {
    MINE('X'),
    UNEXPLORED('.'),
    MARKED('*'),
    EMPTY('/')
}

enum class CommandType(val type: String) {
    FREE("free"),
    MINE("mine")
}

class MinesField(private val minesCount: Int, private val n: Int = 9) {
    private val playGrid: Array<Array<Char>> = Array<Array<Char>>(n) { Array<Char>(n){ '.' } }
    private val stateGrid: Array<Array<Char>> = Array<Array<Char>>(n) { Array<Char>(n){ '/' } }
    private val mines: Array<Array<Int>> = Array<Array<Int>>(minesCount) { Array<Int>(2){ 0 } }
    private var markedCount = 0
    private var minesFoundCount = 0
    private var freeCount = 0
    fun generateMines(row: Int, col: Int) {
        var counter = 0
        repeat (minesCount) {
            var x: Int
            var y: Int
            do {
                x = Random.nextInt(n)
                y = Random.nextInt(n)
            } while(stateGrid[x][y] == CellType.MINE.char || (x == row && y == col))
            stateGrid[x][y] = CellType.MINE.char
            mines[counter++] = arrayOf(x, y)
            if (playGrid[x][y] == CellType.MARKED.char) {
                minesFoundCount++;
            }
        }
        countMines()
    }

    fun countMines() {
        for (row in stateGrid.indices) {
            for (col in stateGrid[row].indices) {
                if (stateGrid[row][col] == CellType.MINE.char) continue
                var count = 0
                for (i in row - 1..row + 1) {
                    for (j in col - 1..col + 1) {
                        if ((i != row || j != col) && stateGrid.isInside(i, j) && stateGrid[i][j] == CellType.MINE.char) {
                            count++
                        }
                    }
                }
                if (count > 0) stateGrid[row][col] = '0' + count
            }
        }
    }


    fun isMine(row: Int, col: Int) = stateGrid[row][col] == CellType.MINE.char

    fun print() {
        playGrid.print()
    }

    fun askUser() = println("Set/unset mines marks or claim a cell as free: ")

    fun printFailed() = println("You stepped on a mine and failed!")

    fun printWon() = println("Congratulations! You found all the mines!")

    fun markQuery(row: Int, col: Int): Boolean {
        if (playGrid[row][col] == CellType.UNEXPLORED.char) {
            playGrid[row][col] = CellType.MARKED.char
            markedCount++
            if (stateGrid[row][col] == CellType.MINE.char) {
                minesFoundCount++
            }
        } else if (playGrid[row][col] == CellType.MARKED.char) {
            playGrid[row][col] = CellType.UNEXPLORED.char
            markedCount--
            if (stateGrid[row][col] == CellType.MINE.char) {
                minesFoundCount--
            }
        }
        return true
    }

    fun freeQuery(row: Int, col: Int): Boolean {
        when (stateGrid[row][col]) {
            CellType.EMPTY.char -> {
                val list = mutableListOf<Array<Int>>()
                list.add(arrayOf(row, col))
                playGrid[row][col] = CellType.EMPTY.char
                freeCount++
                var listPointer = 0
                while (listPointer < list.size) {
                    val r = list[listPointer][0]
                    val c = list[listPointer][1]
                      for (i in -1..1) {
                        for( j in -1..1) {
                            val curR = r + i
                            val curC = c + j
                            if (curR == r && curC == c) continue
                            if (!stateGrid.isInside(curR, curC)) continue
                            if (stateGrid[curR][curC] == CellType.EMPTY.char &&
                                playGrid[curR][curC] != CellType.EMPTY.char) {
                                list.add(arrayOf(curR, curC))
                            }
                            if(playGrid[curR][curC] == CellType.MARKED.char ||
                                    playGrid[curR][curC] == CellType.UNEXPLORED.char) {
                                playGrid[curR][curC] = stateGrid[curR][curC]
                                freeCount++
                            }
                        }
                    }
                    listPointer++
                }
                return true
            }
            CellType.MINE.char -> {
                for(mine in mines) {
                    playGrid[mine[0]][mine[1]] = CellType.MINE.char
                }
                return false
            }
            else -> {
                playGrid[row][col] = stateGrid[row][col]
                freeCount++
                return true
            }
         }
    }

    fun query(row: Int, col: Int, type: String): Boolean {
        var result = true
        if (type == CommandType.MINE.type) {
            result = markQuery(row, col)
        }
        else if (type == CommandType.FREE.type) {
            result = freeQuery(row, col)
        }
        print()
        return result
    }

    fun notWin() = (minesFoundCount != minesCount && freeCount != n * n - minesCount)
}

fun Array<Array<Char>>.isInside(row: Int, col: Int): Boolean {
    val rowCount = this.size
    val colCount = if (rowCount == 0) 0 else this[0].size
    return row in 0 until rowCount && col in 0 until colCount
}

fun Array<Array<Char>>.print() {
    val n = this.size
    val arr = Array<Char>(n) { '1' + it }
    println(" |${arr.joinToString("")}|")
    println("—|${"—".repeat(n)}|")
    for (i in this.indices) {
        println("${i + 1}|${this[i].joinToString("")}|")
    }
    println("—|${"—".repeat(n)}|")
}

fun main() {
    val scanner = Scanner(System.`in`)
    println("How many mines do you want on the field?")
    val minesCount = scanner.nextInt()
    val minesField = MinesField(minesCount)
    minesField.print()
    minesField.askUser()
    var x = scanner.nextInt()
    var y = scanner.nextInt()
    var type = scanner.next()
    while(type == CommandType.MINE.type) {
        minesField.query(y - 1, x - 1, type)
        minesField.askUser()
        x = scanner.nextInt()
        y = scanner.nextInt()
        type = scanner.next()
    }
    minesField.generateMines(y - 1, x - 1)
    minesField.query(y - 1, x - 1, type)

    var lost = false
    while (minesField.notWin() && !lost) {
        minesField.askUser()
        x = scanner.nextInt()
        y = scanner.nextInt()
        type = scanner.next()
        lost = !minesField.query(y - 1, x - 1, type)
    }

    if (lost) {
        minesField.printFailed()
    }
    else {
        minesField.printWon()
    }
}

