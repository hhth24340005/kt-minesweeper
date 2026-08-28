package io.github.hhth24340005.minesweeper.logic

import kotlin.collections.contains
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
) : Matrix<Stage.CellId> {
  private var isInitialized: Boolean = false
  private val mines: MutableSet<CellId> = mutableSetOf()

  public override val rows: List<List<CellId>> =
    List(height) { List(width) { CellId() } }

  init {
    require(0 < width)
    require(0 < height)
    require(mineDensity in 0.0..1.0)
  }

  public fun reveal(
    cell: CellId,
  ): RevealResult {
    if (!isInitialized) {
      initialize(cell)
      isInitialized = true
    }
    if (cell in mines) {
      return RevealResult.Exploded
    }

    fun revealInner(
      cell: CellId,
      map: MutableMap<CellId, Int>,
    ) {
      val minesAround = adjacentCellsOf(cell).count { it in mines }
      map[cell] = minesAround
      if (minesAround != 0) {
        return
      }

      adjacentCellsOf(cell)
        .filter { it !in map }
        .forEach {
          revealInner(it, map)
        }
    }
    val ret = mutableMapOf<CellId, Int>()
    revealInner(cell, ret)
    return RevealResult.Revealed(ret.toMap())
  }

  public class CellId

  public sealed interface RevealResult {
    public data object Exploded : RevealResult

    public data class Revealed(val revealedCells: Map<CellId, Int>) :
      RevealResult
  }

  private fun initialize(
    startingCell: CellId,
  ) {
    val candidates = rows.flatten().toMutableSet()
    candidates -= startingCell
    candidates -= adjacentCellsOf(startingCell)
    val minesCount = ceil(mineDensity * candidates.size).toInt()
    mines.addAll(candidates.shuffled(random).take(minesCount))
  }
}

