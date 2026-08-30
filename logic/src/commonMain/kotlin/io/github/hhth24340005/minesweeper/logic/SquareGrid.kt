package io.github.hhth24340005.minesweeper.logic

import kotlinx.collections.immutable.toImmutableList

private val squareOffsets =
  setOf(
    -1 to -1, 0 to -1, 1 to -1,
    -1 to 0, 1 to 0,
    -1 to 1, 0 to 1, 1 to 1,
  )

public fun <T> squareGridOf(
  width: Int,
  height: Int,
  init: (x: Int, y: Int) -> T,
): Grid<T> {
  require(0 < width)
  require(0 < height)
  return Grid(
    List(height) { y ->
      List(width) { x ->
        init(x, y)
      }.toImmutableList()
    }.toImmutableList(),
  ) { squareOffsets }
}

public fun <T> squareGridOf(
  rows: List<List<T>>,
): Grid<T> {
  val width = rows.maxOf { it.size }
  require(0 < width)
  return Grid(
    rows
      .map {
        require(it.size == width) { "All rows must have the same width" }
        it.toImmutableList()
      }.toImmutableList(),
  ) { squareOffsets }
}
