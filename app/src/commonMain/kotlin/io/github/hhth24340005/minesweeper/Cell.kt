package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

public data class CellPreferences(val cellSize: Dp)

@Composable
context(pref: CellPreferences)
public fun ConcealedCell(
  isMarked: Boolean,
) {
  Box(modifier = Modifier.size(pref.cellSize)) {
    Text(
      if (isMarked) {
        "🚩"
      } else {
        "O"
      },
    )
  }
}

@Composable
context(pref: CellPreferences)
public fun RevealedEmptyCell(
  numberOfMinesAround: Int,
) {
  Box(modifier = Modifier.size(pref.cellSize)) {
    Text("$numberOfMinesAround")
  }
}

@Composable
context(pref: CellPreferences)
public fun RevealedMineCell() {
  Box(modifier = Modifier.size(pref.cellSize)) {
    Text("💣")
  }
}
