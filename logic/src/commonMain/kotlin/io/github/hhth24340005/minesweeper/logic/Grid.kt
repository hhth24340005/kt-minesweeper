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
