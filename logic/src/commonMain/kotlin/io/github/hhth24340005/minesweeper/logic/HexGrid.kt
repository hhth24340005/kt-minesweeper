package io.github.hhth24340005.minesweeper.logic

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

public class HexGrid<T> private constructor(
  public override val rows: ImmutableList<ImmutableList<T>>,
) : Grid<T> {
  public companion object {
    private val neighborsEven =
      setOf(
        0 to -1,
        1 to -1,
        -1 to 0,
        1 to 0,
        0 to 1,
        1 to 1,
      )

    private val neighborsOdd =
      setOf(
        -1 to -1,
        0 to -1,
        -1 to 0,
        1 to 0,
        -1 to 1,
        0 to 1,
      )
  }

  public val width: Int get() = rows.first().size
  public val height: Int get() = rows.size

  public constructor(
    width: Int,
    height: Int,
    init: (x: Int, y: Int) -> T,
  ) : this(
    run {
      require(1 < width)
      require(0 < height)
      List(height) { y ->
        List(width - (y + 1) % 2) { x ->
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
      require(1 < width)
      rows
        .mapIndexed { y, row ->
          if (y % 2 == 0) {
            require(
              row.size == width - 1,
            ) { "All rows at even index must have the same width" }
          } else {
            require(
              row.size == width,
            ) { "All rows at odd index must have the same width" }
          }
          row.toImmutableList()
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
    val x = columnOf(cell)
    val y = rowOf(cell)
    return if (y % 2 == 0) {
      neighborsEven
    } else {
      neighborsOdd
    }.mapNotNull { (dx, dy) ->
      rows.getOrNull(y + dy)?.getOrNull(x + dx)
    }.toSet()
  }

  override fun <R> map(
    transform: (T) -> R,
  ): Grid<R> =
    HexGrid(
      rows.map { row ->
        row.map(transform)
      },
    )

  public override fun toString(): String =
    "HexGrid($width x $height)@${hashCode()}"

  public override fun equals(
    other: Any?,
  ): Boolean {
    if (this === other) return true
    if (other !is HexGrid<*>) return false
    return rows == other.rows
  }

  public override fun hashCode(): Int =
    rows.hashCode()
}
