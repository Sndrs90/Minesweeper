package minesweeper

import java.util.Scanner

const val ROWS = 9
const val COLUMNS = 9

fun main() {
    var gameState = true
    val scanner = Scanner(System.`in`)
    val minesLocations: MutableSet<Pair<Int, Int>> = mutableSetOf()
    val safeCells: MutableSet<Pair<Int, Int>> = mutableSetOf()
    val answers: MutableSet<Pair<Int, Int>> = mutableSetOf()

    println("How many mines do you want on the field?")
    val mines = readln().toInt()
//    val mines = 1
    val currentField: MutableList<MutableList<String>> = createCurrentField()
    printField(currentField)

    println("Set/unset mines marks or claim a cell as free:")
    var x = scanner.nextInt() - 1
    var y = scanner.nextInt() - 1
    var command = scanner.next()
    val firstCell = Pair(y, x)

    val field = createField(ROWS, COLUMNS, mines, firstCell)
    val newField = createFieldWithCountedMines(field, ROWS, COLUMNS)
//    printField(newField)

    field.forEachIndexed { i, lines ->
        lines.forEachIndexed { j, symbol ->
            val pair = Pair(i, j)
            when (symbol) {
                "X" -> minesLocations.add(pair)
                "." -> safeCells.add(pair)
            }
            if (symbol == "X") {
                val pair = Pair(i, j)
                minesLocations.add(pair)
            }
        }
    }
    firstRound(x, y, command, newField, currentField, answers, safeCells)
    printField(currentField)

    while(gameState) {
        println("Set/unset mines marks or claim a cell as free:")
        x = scanner.nextInt() - 1
        y = scanner.nextInt() - 1
        command = scanner.next()
        when(command) {
            "free" ->{
                when (newField[y][x]) {
                    "." -> {
                       // currentField[y][x] = "/"
                        showNeighbors(y, x, newField, currentField, safeCells)
                        if (safeCells.isEmpty()) gameState = false
                    }
                    "X" -> {
                        currentField[y][x] = "X"
                        println("You stepped on a mine and failed!")
                        gameState = false
                    }
                    else -> {
                        currentField[y][x] = newField[y][x]
                        safeCells.remove(Pair(y, x))
                        if (safeCells.isEmpty()) gameState = false
                    }
                }

            }
            "mine" -> {
                when (currentField[y][x]) {
                    "*" -> {
                        currentField[y][x] = "."
                        answers.remove(Pair(y, x))
                        if (checkAnswers(minesLocations, answers)) gameState = false
                    }
                    else -> {
                        currentField[y][x] = "*"
                        answers.add(Pair(y, x))
                        if (checkAnswers(minesLocations, answers)) gameState = false
                    }
                }
            }
            "exit" -> gameState = false
        }
        printField(currentField)
    }
    println("Congratulations! You found all the mines!")
}

fun createCurrentField(): MutableList<MutableList<String>> {
    val currentField: MutableList<MutableList<String>> = mutableListOf()
    repeat(ROWS) {
        val line: MutableList<String> = mutableListOf()
        repeat(COLUMNS) {
            line.add(".")
        }
        currentField.add(line)
    }
    return currentField
}

fun createField(rows: Int, columns: Int, mines: Int, firstCell: Pair<Int, Int>): List<List<String>> =
    createShuffledList(rows, columns, mines, firstCell).chunked(columns)

fun createShuffledList(rows: Int, columns: Int, mines: Int, firstCell: Pair<Int, Int>): List<String> {
    var line = mutableListOf<String>()
    repeat(mines) {
        line.add("X")
    }
    while (line.size < rows * columns - 1) {
        line.add(".")
    }
    val index = firstCell.first * columns + firstCell.second
    line = line.shuffled().toMutableList()
    line.add(index, ".")
    return line
}

fun createFieldWithCountedMines(field:List<List<String>>, rows: Int, columns: Int): MutableList<MutableList<String>> {
    val newField: MutableList<MutableList<String>> = mutableListOf()
    for (row in field) {
        newField.add(row.toMutableList())
    }
    field.forEachIndexed { ii, lines ->
        lines.forEachIndexed { jj, smbl ->
            if (smbl != "X") {
                var count = 0
                for (i in maxOf(0, ii - 1)..minOf(rows - 1,ii + 1)) {
                    for (j in maxOf(0,jj - 1)..minOf(columns - 1,jj+1)) {
                        if (field[i][j] == "X") count++
                    }
                }
                if (count == 0) newField[ii][jj] = "."
                else newField[ii][jj] = count.toString()
            }
        }
    }
    return newField
}

fun printField(field: MutableList<MutableList<String>>) {
    println(" |123456789|")
    println("-|---------|")
    for (i in 0 until ROWS) {
        print("${i + 1}|")
        print(field[i].joinToString(""))
        println("|")
    }
    println("-|---------|")
}

fun firstRound(
    x: Int, y: Int,
    command: String,
    newField:MutableList<MutableList<String>>,
    currentField:MutableList<MutableList<String>>,
    answers: MutableSet<Pair<Int, Int>>,
    safeCells: MutableSet<Pair<Int, Int>>
) {
    when(command) {
        "free" -> {
            if (newField[y][x] == ".") {
                //currentField[y][x] = "/"
                showNeighbors(y, x, newField, currentField, safeCells)
            } else {
                currentField[y][x] = newField[y][x]
            }
        }
        "mine" -> {
            currentField[y][x] = "*"
            //newField[y][x] = "*"
            answers.add(Pair(y, x))
        }
    }
}
fun checkAnswers(minesLocations: MutableSet<Pair<Int, Int>>, answers: MutableSet<Pair<Int, Int>>): Boolean = minesLocations == answers

fun showNeighbors(
    y: Int, x: Int,
    newField: MutableList<MutableList<String>>,
    currentField: MutableList<MutableList<String>>,
    safeCells: MutableSet<Pair<Int, Int>>
) {
    val prevC = "."
    val newC = "/"
    floodFillUtil(newField, currentField, y, x, prevC, newC, safeCells)
}


// A recursive function to replace previous color 'prevC' at '(x, y)'
// and all surrounding pixels of (x, y) with new color 'newC' and
/*fun floodFillUtil(
    newField: MutableList<MutableList<String>>,
    currentField: MutableList<MutableList<String>>,
    y: Int,
    x: Int,
    prevC: String,
    newC: String,
){
    // Base cases
    if (y < 0 || y >= ROWS || x < 0 || x >= COLUMNS) return
    if (newField[y][x] != prevC) return

    // Replace the color at (x, y)
    newField[y][x] = newC
    currentField[y][x] = newField[y][x]
    // Recur for north, east, south and west
    floodFillUtil(newField, currentField, y+1, x, prevC, newC)
    floodFillUtil(newField, currentField, y-1, x, prevC, newC)
    floodFillUtil(newField, currentField, y, x+1, prevC, newC)
    floodFillUtil(newField, currentField, y, x-1, prevC, newC)
}*/

fun floodFillUtil(
    newField: MutableList<MutableList<String>>,
    currentField: MutableList<MutableList<String>>,
    y: Int,
    x: Int,
    prevC: String,
    newC: String,
    safeCells: MutableSet<Pair<Int, Int>>
){
    // Base cases
    if (y < 0 || y >= ROWS || x < 0 || x >= COLUMNS) return

    if (  newField[y][x] != "*" || newField[y][x] != "X" || newField[y][x] != "/") {
        currentField[y][x] = newField[y][x]
        safeCells.remove(Pair(y, x))
    }

    if (newField[y][x] != prevC) return

    // Replace the color at (x, y)
    newField[y][x] = newC
    currentField[y][x] = newField[y][x]
    safeCells.remove(Pair(y, x))
    // Recur for north, east, south and west
    floodFillUtil(newField, currentField, y+1, x, prevC, newC, safeCells)
    floodFillUtil(newField, currentField, y-1, x, prevC, newC, safeCells)
    floodFillUtil(newField, currentField, y, x+1, prevC, newC, safeCells)
    floodFillUtil(newField, currentField, y, x-1, prevC, newC, safeCells)
    floodFillUtil(newField, currentField, y-1, x-1, prevC, newC, safeCells)
    floodFillUtil(newField, currentField, y+1, x-1, prevC, newC, safeCells)
    floodFillUtil(newField, currentField, y-1, x+1, prevC, newC, safeCells)
    floodFillUtil(newField, currentField, y+1, x+1, prevC, newC, safeCells)

}