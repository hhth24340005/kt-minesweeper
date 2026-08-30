package io.github.hhth24340005.minesweeper.logic

import kotlinx.collections.immutable.toImmutableList

private val hexNeighborsEven =
  setOf(
    0 to -1, 1 to -1,
    -1 to 0, 1 to 0,
    0 to 1, 1 to 1,
  )

private val hexNeighborsOdd =
  setOf(
    -1 to -1, 0 to -1,
    -1 to 0, 1 to 0,
    -1 to 1, 0 to 1,
  )

private fun hexOffsets(
  y: Int,
): Set<Pair<Int, Int>> =
  if (y % 2 == 0) {
    hexNeighborsEven
  } else {
    hexNeighborsOdd
  }

public fun <T> hexGridOf(
  width: Int,
  height: Int,
  init: (x: Int, y: Int) -> T,
): Grid<T> {
  require(1 < width)
  require(0 < height)
  return Grid(
    List(height) { y ->
      List(width - (y + 1) % 2) { x ->
        init(x, y)
      }.toImmutableList()
    }.toImmutableList(),
    ::hexOffsets,
  )
}
