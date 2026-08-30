package io.github.hhth24340005.minesweeper.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.ceil
import kotlin.random.Random


public class MinesweeperStage private constructor(
  private val grid: Grid<Cell>,
  private val mines: Set<Cell>,
) {
  public val rows: List<List<Cell>> get() = grid.rows

  public fun reveal(
    cell: Cell,
  ) {
    fun open(
      cell: Cell,
    ): Int? {
      if (cell.status == CellState.Marked) {
        return null
      }
      if (cell in mines) {
        cell.status = CellState.RevealedMine
        return null
      }
      val minesAround = grid.adjacentCellsOf(cell).count { it in mines }
      cell.status = CellState.revealedOf(minesAround)
      return minesAround
    }

    if (cell.status.indicatedAdjacentMines != null) {
      val adjacentCells = grid.adjacentCellsOf(cell)
      val adjacentMarked =
        adjacentCells.count {
          it.status == CellState.Marked
        }
      if (cell.status.indicatedAdjacentMines == adjacentMarked) {
        adjacentCells.forEach {
          open(it)
        }
      }
      return
    }

    run greedyOpen@{
      val visited = mutableSetOf<Cell>()
      val queue = mutableListOf(cell)
      while (queue.isNotEmpty()) {
        val cell = queue.removeFirst()
        visited += cell
        val adjacentMines = open(cell)
        if (adjacentMines == 0) {
          grid.adjacentCellsOf(cell)
            .filter { it !in visited }
            .forEach {
              queue.addFirst(it)
            }
        }
      }
    }
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
}
