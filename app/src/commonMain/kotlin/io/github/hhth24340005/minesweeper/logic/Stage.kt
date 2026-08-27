package io.github.hhth24340005.minesweeper.logic

import kotlin.collections.contains
import kotlin.random.Random

public class Stage(
  width: Int,
  height: Int,
  private val mineDensity: Double,
  private val random: Random = Random,
) {
  private var isInitialized: Boolean = false
  private val mines: MutableSet<CellId> = mutableSetOf()

  public val rows: List<List<CellId>> =
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
      val minesAround = cell.adjacentCells.count { it in mines }

      map[cell] = minesAround
      if (minesAround != 0) {
        return
      }

      cell.adjacentCells
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
    candidates -= startingCell.adjacentCells
    val minesCount = (mineDensity * candidates.size).toInt()
    mines.addAll(candidates.shuffled(random).take(minesCount))
  }

  private val CellId.adjacentCells: Set<CellId> get() =
    ((-1)..1)
      .flatMap { x ->
        ((-1)..1).mapNotNull { y ->
          if (x == 0 && y == 0) {
            return@mapNotNull null
          }
          val (currentX, currentY) = position
          rows
            .getOrNull(currentY + y)
            ?.getOrNull(currentX + x)
        }
      }.toSet()


  private val CellId.position: Pair<Int, Int> get() {
    val row = rows.first { it.contains(this) }
    val y = rows.indexOf(row)
    val x = row.indexOf(this)
    return x to y
  }
}
