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
      }.toImmutableList()
    }.toImmutableList(),
  )

  public constructor(
    rows: List<List<T>>,
  ) : this(
    run {
      val width = rows.maxOf { it.size }
      require(0 < width)
      rows
        .map {
          require(it.size == width) { "All rows must have the same width" }
          it.toImmutableList()
        }.toImmutableList()
    },
  )

  private val cellToPos: Map<T, Pair<Int, Int>> =
    rows
      .flatMapIndexed { y, row ->
        row.mapIndexed { x, cell ->
          cell to (x to y)
        }
      }.toMap()

  override fun rowOf(
    cell: T,
  ): Int =
    cellToPos[cell]!!.second

  override fun columnOf(
    cell: T,
  ): Int =
    cellToPos[cell]!!.first

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
