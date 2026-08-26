package io.github.hhth24340005.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
public fun App() {
  val cellPref = CellPreferences(32.dp)
  Game(cellPref)
}
