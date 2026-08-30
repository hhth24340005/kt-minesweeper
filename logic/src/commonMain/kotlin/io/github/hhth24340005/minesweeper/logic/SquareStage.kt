package io.github.hhth24340005.minesweeper.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.hhth24340005.minesweeper.logic.SquareStage.Cell
import kotlin.math.ceil
import kotlin.random.Random

public class SquareStage private constructor(
  rows: List<List<Cell>>,
  private val mines: Set<Cell>,
) : Stage<CellState, Cell>,
  Grid<Cell> by SquareGrid(rows) {
  public companion object {
    public fun prepare(
      width: Int,
      height: Int,
      mineDensity: Double,
      random: Random = Random,
    ): Uninitialized =
      Uninitialized(
        rows = List(height) { List(width) { Uninitialized.Cell() } },
        mineDensity = mineDensity,
        random = random,
      )
  }

  init {
    require(rows.isNotEmpty())
    require(rows.all { it.isNotEmpty() })
  }

  public override fun reveal(
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
      val minesAround = adjacentCellsOf(cell).count { it in mines }
      cell.status = CellState.revealedOf(minesAround)
      return minesAround
    }

    if (cell.status.indicatedAdjacentMines != null) {
      val adjacentCells = adjacentCellsOf(cell)
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
          adjacentCellsOf(cell)
            .filter { it !in visited }
            .forEach {
              queue.addFirst(it)
            }
        }
      }
    }
  }

  public override fun toggleMark(
    cell: Cell,
  ) {
    when (cell.status) {
      is CellState.Concealed -> {
        cell.status = CellState.Marked
      }

      is CellState.Marked -> {
        cell.status = CellState.Concealed
      }

      else -> { }
    }
  }

  public class Cell : Stage.Cell<CellState> {
    override var status: CellState by mutableStateOf(CellState.Concealed)
  }

  public class Uninitialized internal constructor(
    rows: List<List<Uninitialized.Cell>>,
    private val mineDensity: Double,
    private val random: Random,
  ) : UninitializedStage<Uninitialized.Cell, CellState, Cell>,
    Grid<Uninitialized.Cell> by SquareGrid(rows) {
    init {
      require(mineDensity in 0.0..1.0)
    }

    public override fun initialize(
      startingCell: Uninitialized.Cell,
    ): SquareStage {
      val cellOf = rows.flatten().associateWith { SquareStage.Cell() }
      val cellRows = rows.map { row -> row.map { cellOf[it]!! } }
      val candidates =
        buildList {
          addAll(
            rows.flatten() -
              startingCell -
              adjacentCellsOf(startingCell),
          )
          shuffle(random)
        }
      val minesCount = ceil(mineDensity * candidates.size).toInt()
      val mines =
        candidates
          .take(minesCount)
          .map { cellOf[it]!! }
          .toSet()
      return SquareStage(cellRows, mines).also {
        it.reveal(cellOf[startingCell]!!)
      }
    }

    public class Cell internal constructor()
  }
}
