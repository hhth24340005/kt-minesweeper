package io.github.hhth24340005.minesweeper.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first
import kotlin.math.ceil
import kotlin.random.Random


public class MinesweeperStage private constructor(
  private val grid: Grid<Cell>,
  private val mines: Set<Cell>,
) {
  public companion object {
    public fun prepare(
      width: Int,
      height: Int,
      gridFactory: (
        Int,
        Int,
        (Int, Int) -> Uninitialized.Cell,
      ) -> Grid<Uninitialized.Cell>,
      mineDensity: Double,
      random: Random = Random,
    ): Uninitialized =
      Uninitialized(
        gridFactory(width, height) { _, _ -> Uninitialized.Cell() },
        mineDensity,
        random,
      )
  }

  public val rows: List<List<Cell>> get() = grid.rows

  public var marked: Int by mutableIntStateOf(0)
    private set

  public var status: Status by mutableStateOf(Status.Playing)
    private set

  private val cellCount = grid.rows.sumOf { it.size }

  private var revealed: Int = 0

  public fun reveal(
    cell: Cell,
  ) {
    fun open(
      cell: Cell,
    ): Int? {
      if (cell.status == CellState.Marked ||
        cell.status.indicatedAdjacentMines != null
      ) {
        return null
      }
      if (cell in mines) {
        cell.status = CellState.RevealedMine
        status = Status.Lose
        return null
      }
      val minesAround = grid.adjacentCellsOf(cell).count { it in mines }
      cell.status = CellState.revealedOf(minesAround)
      revealed++
      if (cellCount - revealed <= mines.size) {
        status = Status.Win
      }
      return minesAround
    }

    fun greedyOpen(
      start: Cell,
    ) {
      val visited = mutableSetOf<Cell>()
      val queue = mutableListOf(start)
      while (queue.isNotEmpty()) {
        val cell = queue.removeFirst()
        visited += cell
        val adjacentMines = open(cell)
        if (adjacentMines == 0) {
          grid
            .adjacentCellsOf(cell)
            .filter { it !in visited }
            .forEach {
              queue.addFirst(it)
            }
        }
      }
    }

    if (cell.status.indicatedAdjacentMines != null) {
      val adjacentCells = grid.adjacentCellsOf(cell)
      val adjacentMarked =
        adjacentCells.count {
          it.status == CellState.Marked
        }
      if (cell.status.indicatedAdjacentMines == adjacentMarked) {
        adjacentCells.forEach {
          greedyOpen(it)
        }
      }
      return
    }

    greedyOpen(cell)
  }

  public fun toggleMark(
    cell: Cell,
  ) {
    when (cell.status) {
      is CellState.Concealed -> {
        cell.status = CellState.Marked
      }

      is CellState.Marked -> {
        cell.status = CellState.Concealed
      }

      else -> {}
    }
  }

  public suspend fun awaitWin() {
    snapshotFlow { status }.first { it == Status.Win }
  }

  public suspend fun awaitLose() {
    snapshotFlow { status }.first { it == Status.Lose }
  }

  public class Cell internal constructor() {
    public var status: CellState by mutableStateOf(CellState.Concealed)
      internal set
  }

  public class Uninitialized internal constructor(
    private val grid: Grid<Cell>,
    private val mineDensity: Double,
    private val random: Random,
  ) {
    init {
      require(mineDensity in 0.0..1.0)
    }

    public val rows: List<List<Cell>> get() = grid.rows

    public fun initialize(
      startingCell: Cell,
    ): MinesweeperStage {
      val cellOf =
        grid.rows
          .flatten()
          .associateWith { MinesweeperStage.Cell() }
      val candidates =
        buildList {
          addAll(
            grid.rows.flatten() -
              startingCell -
              grid.adjacentCellsOf(startingCell),
          )
          shuffle(random)
        }
      val minesCount = ceil(mineDensity * candidates.size).toInt()
      val mines =
        candidates
          .take(minesCount)
          .map { cellOf[it]!! }
          .toSet()
      return MinesweeperStage(grid.map { cellOf[it]!! }, mines).also {
        it.reveal(cellOf[startingCell]!!)
      }
    }

    public class Cell internal constructor()
  }

  public enum class Status {
    Playing,
    Win,
    Lose,
  }
}
