package io.github.hhth24340005.minesweeper.logic

public interface Grid<T> {
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
  ): Set<T>
}
