package io.github.hhth24340005.minesweeper.logic

public class SquareGrid<T>(rows: List<List<T>>) : Grid<T> {
  override val rows: List<List<T>> = rows.map { it.toList() }

  override fun adjacentCellsOf(
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
