package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
public fun Game(
  cellPref: CellPreferences,
  width: Int,
  height: Int,
) {
  require(0 < width)
  require(0 < height)
  val cells =
    MutableList(width * height) {
      CellState.Concealed as CellState
    }
  val center = ((width - 1) / 2) + ((height - 1) / 2) * width

  cells[center - width - 1] = CellState.RevealedMine
  cells[center - width] = CellState.RevealedMine
  cells[center - width + 1] = CellState.RevealedMine

  cells[center - 1] = CellState.RevealedMine
  cells[center] = CellState.Revealed8
  cells[center + 1] = CellState.RevealedMine

  cells[center + width - 1] = CellState.RevealedMine
  cells[center + width] = CellState.RevealedMine
  cells[center + width + 1] = CellState.RevealedMine
  context(cellPref) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      cells
        .chunked(width)
        .forEach { row ->
          Row {
            row.forEach { it.draw() }
          }
        }
    }
  }
}
