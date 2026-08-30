package io.github.hhth24340005.minesweeper.logic

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

public class Grid<T>(
  public val rows: ImmutableList<ImmutableList<T>>,
  private val adjacencyOffsets: (y: Int) -> Set<Pair<Int, Int>>,
) {
  init {
    require(rows.isNotEmpty() && rows.all { it.isNotEmpty() })
  }

  private val cellToPos: Map<T, Pair<Int, Int>> =
    rows
      .flatMapIndexed { y, row ->
        row.mapIndexed { x, cell ->
          cell to (x to y)
        }
      }.toMap()

  public fun rowOf(
    cell: T,
  ): Int =
    cellToPos[cell]?.second
      ?: throw IllegalArgumentException(
        "This ($this) is not the owner for the cell ($cell)",
      )

  public fun columnOf(
    cell: T,
  ): Int =
    cellToPos[cell]?.first
      ?: throw IllegalArgumentException(
        "This ($this) is not the owner for the cell ($cell)",
      )

  public fun adjacentCellsOf(
    cell: T,
  ): Set<T> {
    val x = columnOf(cell)
    val y = rowOf(cell)
    return adjacencyOffsets(y)
      .mapNotNull { (dx, dy) ->
        rows.getOrNull(y + dy)?.getOrNull(x + dx)
      }.toSet()
  }

  public fun <R> map(
    transform: (T) -> R,
  ): Grid<R> =
    Grid(
      rows
        .map { row ->
          row.map(transform).toImmutableList()
        }.toImmutableList(),
      adjacencyOffsets,
    )

  override fun toString(): String =
    "Grid(${rows.firstOrNull()?.size ?: 0} x ${rows.size})@${hashCode()}"

  override fun equals(
    other: Any?,
  ): Boolean {
    if (this === other) return true
    if (other !is Grid<*>) return false
    return rows == other.rows
  }

  override fun hashCode(): Int =
    rows.hashCode()
}
