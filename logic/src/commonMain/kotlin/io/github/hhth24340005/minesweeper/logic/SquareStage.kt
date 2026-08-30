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
) : Stage<Cell>,
  Grid<Cell> by SquareGrid(rows) {
  public companion object {
    public fun prepare(
      width: Int,
      height: Int,
      mineDensity: Double,
      random: Random = Random,
    ): UninitializedStage<UninitializedCell> {
      val rows =
        List(height) { List(width) { UninitializedCell() } }

      return object :
        UninitializedStage<UninitializedCell>,
        Grid<UninitializedCell> by SquareGrid(rows) {
        public override fun initialize(
          startingCell: UninitializedCell,
        ): SquareStage {
          val cellOf = rows.flatten().associateWith { Cell() }
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
      }
    }

    public class UninitializedCell internal constructor()
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
      if (cell.state == CellState.Marked) {
        return null
      }
      if (cell in mines) {
        cell.state = CellState.RevealedMine
        return null
      }
      val minesAround = adjacentCellsOf(cell).count { it in mines }
      cell.state = CellState.revealedOf(minesAround)
      return minesAround
    }

    if (cell.state.indicatedAdjacentMines != null) {
      val adjacentCells = adjacentCellsOf(cell)
      val adjacentMarked = adjacentCells.count { it.state == CellState.Marked }
      if (cell.state.indicatedAdjacentMines == adjacentMarked) {
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
    when (cell.state) {
      is CellState.Concealed -> {
        cell.state = CellState.Marked
      }

      is CellState.Marked -> {
        cell.state = CellState.Concealed
      }

      else -> { }
    }
  }

  public class Cell internal constructor() {
    public var state: CellState by mutableStateOf(CellState.Concealed)
      internal set
  }
}
