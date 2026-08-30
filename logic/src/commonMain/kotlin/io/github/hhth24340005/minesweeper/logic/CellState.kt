package io.github.hhth24340005.minesweeper.logic

public sealed interface CellState {
  public companion object {
    public fun revealedOf(
      adjacentMines: Int,
    ): CellState =
      when (adjacentMines) {
        0 -> Revealed0
        1 -> Revealed1
        2 -> Revealed2
        3 -> Revealed3
        4 -> Revealed4
        5 -> Revealed5
        6 -> Revealed6
        7 -> Revealed7
        8 -> Revealed8
        else -> throw IllegalArgumentException("$adjacentMines is not allowed")
      }
  }

  public data object Concealed : CellState

  public data object Marked : CellState

  public data object Revealed0 : CellState

  public data object Revealed1 : CellState

  public data object Revealed2 : CellState

  public data object Revealed3 : CellState

  public data object Revealed4 : CellState

  public data object Revealed5 : CellState

  public data object Revealed6 : CellState

  public data object Revealed7 : CellState

  public data object Revealed8 : CellState

  public data object RevealedMine : CellState
}

public val CellState.indicatedAdjacentMines: Int?
  get() =
    when (this) {
      is CellState.Revealed0 -> 0
      is CellState.Revealed1 -> 1
      is CellState.Revealed2 -> 2
      is CellState.Revealed3 -> 3
      is CellState.Revealed4 -> 4
      is CellState.Revealed5 -> 5
      is CellState.Revealed6 -> 6
      is CellState.Revealed7 -> 7
      is CellState.Revealed8 -> 8
      else -> null
    }

