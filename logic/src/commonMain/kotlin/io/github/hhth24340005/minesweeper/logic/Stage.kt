package io.github.hhth24340005.minesweeper.logic

public interface Stage<T> : Grid<T> {
  public fun reveal(
    cell: T,
  )

  public fun toggleMark(
    cell: T,
  )
}

public interface UninitializedStage<T> : Grid<T> {
  public fun initialize(
    startingCell: T,
  ): SquareStage
}
