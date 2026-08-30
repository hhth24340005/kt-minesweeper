package io.github.hhth24340005.minesweeper.logic

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

public class SquareGrid<T>(
  public override val rows: ImmutableList<ImmutableList<T>>,
) : Grid<T> {
  public constructor(
    width: Int,
    height: Int,
    init: (x: Int, y: Int) -> T,
  ) : this(
    List(height) { y ->
      List(width) { x ->
        init(x, y)
      }
    },
  )

  public constructor(
    rows: List<List<T>>,
  ) : this(rows.map { it.toImmutableList() }.toImmutableList())

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
