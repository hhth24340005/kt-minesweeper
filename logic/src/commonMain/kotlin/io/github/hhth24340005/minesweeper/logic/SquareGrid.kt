package io.github.hhth24340005.minesweeper.logic

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

public class SquareGrid<T> private constructor(
  public override val rows: ImmutableList<ImmutableList<T>>,
) : Grid<T> {
  public val width: Int get() = rows.first().size
  public val height: Int get() = rows.size

  public constructor(
    width: Int,
    height: Int,
    init: (x: Int, y: Int) -> T,
  ) : this(
    run {
      require(0 < width)
      require(0 < height)
      List(height) { y ->
        List(width) { x ->
          init(x, y)
        }.toImmutableList()
      }.toImmutableList()
    },
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
    cellToPos[cell]?.second
      ?: throw IllegalArgumentException(
        "This ($this) is not the owner for the cell ($cell)",
      )

  override fun columnOf(
    cell: T,
  ): Int =
    cellToPos[cell]?.first
      ?: throw IllegalArgumentException(
        "This ($this) is not the owner for the cell ($cell)",
      )

  override fun adjacentCellsOf(
    cell: T,
  ): Set<T> {
    val y = rowOf(cell)
    val x = columnOf(cell)
    return (-1..1)
      .flatMap { yOffset ->
        (-1..1).mapNotNull { xOffset ->
          if (xOffset == 0 && yOffset == 0) {
            return@mapNotNull null
          }
          val row = rows.getOrNull(y + yOffset)
          row?.getOrNull(x + xOffset)
        }
      }.toSet()
  }

  public override fun toString(): String =
    "SquareGrid($width x $height)@${hashCode()}"

  public override fun equals(
    other: Any?,
  ): Boolean {
    if (this === other) return true
    if (other !is SquareGrid<*>) return false
    return rows == other.rows
  }

  public override fun hashCode(): Int =
    rows.hashCode()
}
