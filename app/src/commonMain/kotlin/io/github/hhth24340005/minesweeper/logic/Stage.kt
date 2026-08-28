@file:OptIn(ExperimentalAtomicApi::class)

package io.github.hhth24340005.minesweeper.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.hhth24340005.minesweeper.logic.Stage.Cell
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.updateAndFetch
import kotlin.math.ceil
import kotlin.random.Random

public interface Matrix<T> {
  public val rows: List<List<T>>

  public fun rowOf(
    cell: T,
  ): Int =
    rows.indexOfFirst { it.contains(cell) }

  public fun columnOf(
    cell: T,
  ): Int =
    rows[rowOf(cell)].indexOf(cell)

  public fun adjacentCellsOf(
    cell: T,
  ): Set<T> =
    (-1..1)
      .flatMap { x ->
        (-1..1).mapNotNull { y ->
          if (x == 0 && y == 0) {
            return@mapNotNull null
          }
          val row = rows.getOrNull(rowOf(cell) + x)
          row?.getOrNull(columnOf(cell) + y)
        }
      }.toSet()
}

public class Stage private constructor(
  public override val rows: List<List<Cell>>,
  private val mines: Set<Cell>,
) : Matrix<Cell> {
  public companion object {
    public fun prepare(
      width: Int,
      height: Int,
      mineDensity: Double,
      random: Random = Random,
    ): UninitializedStage {
      val rows = List(height) { List(width) { Cell() } }

      return object :
        UninitializedStage {
        override val rows: List<List<Cell>> = rows
        var stage = AtomicReference<Stage?>(null)

        public override fun getOrInit(
          startingCell: Cell,
        ): Stage =
          stage.updateAndFetch {
            if (it != null) {
              return@updateAndFetch it
            }
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
            val mines = candidates.take(minesCount).toSet()
            Stage(rows, mines)
          }!!
      }
    }
  }

  init {
    require(rows.isNotEmpty())
    require(rows.all { it.isNotEmpty() })
  }

  public fun reveal(
    cell: Cell,
  ) {
    if (cell in mines) {
      cell.state = CellState.RevealedMine
      return
    }

    fun revealInner(
      cell: Cell,
      visited: MutableSet<Cell>,
    ) {
      if (cell in visited) {
        return
      }
      visited += cell
      val minesAround = adjacentCellsOf(cell).count { it in mines }
      cell.state = CellState.revealedOf(minesAround)
      if (minesAround != 0) {
        return
      }

      adjacentCellsOf(cell)
        .filter { it !in visited }
        .forEach {
          revealInner(it, visited)
        }
    }

    revealInner(cell, mutableSetOf())
  }

  public fun toggleMark(
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

public interface UninitializedStage : Matrix<Cell> {
  public fun getOrInit(
    startingCell: Cell,
  ): Stage
}

