package io.github.hhth24340005.minesweeper.logic

public interface Stage<T, C : Stage.Cell<T>> : Grid<C> {
  public fun reveal(
    cell: C,
  )

  public fun toggleMark(
    cell: C,
  )

  public interface Cell<T> {
    public val status: T
  }
}

public interface UninitializedStage<T0, T1, C : Stage.Cell<T1>> : Grid<T0> {
  public fun initialize(
    startingCell: T0,
  ): Stage<T1, C>
}
