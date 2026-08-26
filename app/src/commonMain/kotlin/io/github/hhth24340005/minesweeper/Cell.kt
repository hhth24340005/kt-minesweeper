package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
public fun ConcealedCell(
  isMarked: Boolean,
) {
  Box {
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
public fun RevealedEmptyCell(
  numberOfMinesAround: Int,
) {
  Box {
    Text("$numberOfMinesAround")
  }
}

@Composable
public fun RevealedMineCell() {
  Box {
    Text("💣")
  }
}
