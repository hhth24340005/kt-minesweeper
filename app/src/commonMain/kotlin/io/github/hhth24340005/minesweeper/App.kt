package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
public fun App() {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Row {
      ConcealedCell(isMarked = false)
      ConcealedCell(isMarked = false)
      ConcealedCell(isMarked = false)
    }
    Row {
      RevealedEmptyCell(numberOfMinesAround = 0)
      RevealedEmptyCell(numberOfMinesAround = 2)
      ConcealedCell(isMarked = true)
    }
    Row {
      ConcealedCell(isMarked = false)
      ConcealedCell(isMarked = false)
      RevealedMineCell()
    }
  }
}
