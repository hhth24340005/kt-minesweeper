package io.github.hhth24340005.minesweeper.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

public class Stage(
  width: Int,
  height: Int,
  private val mineDensity: Double,
  private val random: Random = Random,
) : Matrix<Stage.Cell> {
  private var isInitialized: Boolean = false
  private val mines: MutableSet<Cell> = mutableSetOf()

  public override val rows: List<List<Cell>> =
    List(height) { List(width) { Cell() } }

  init {
    require(0 < width)
    require(0 < height)
    require(mineDensity in 0.0..1.0)
  }

  public fun reveal(
    cell: Cell,
  ) {
    if (!isInitialized) {
      initialize(cell)
      isInitialized = true
    }
    if (cell in mines) {
      cell.state = CellState.RevealedMine
      return
    }

    fun revealInner(
      cell: Cell,
      visited: MutableSet<Cell>,
    ) {
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

  private fun initialize(
    startingCell: Cell,
  ) {
    val candidates = rows.flatten().toMutableSet()
    candidates -= startingCell
    candidates -= adjacentCellsOf(startingCell)
    val minesCount = ceil(mineDensity * candidates.size).toInt()
    mines.addAll(candidates.shuffled(random).take(minesCount))
  }

  public class Cell internal constructor() {
    public var state: CellState by mutableStateOf(CellState.Concealed)
      internal set
  }
}

