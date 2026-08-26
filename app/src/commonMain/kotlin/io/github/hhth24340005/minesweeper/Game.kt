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
