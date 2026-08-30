package io.github.hhth24340005.minesweeper

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

public fun main() {
  application {
    Window(
      onCloseRequest = ::exitApplication,
      state = rememberWindowState(size = DpSize(960.dp, 900.dp)),
      resizable = false,
      title = "Minesweeper",
    ) {
      App()
    }
  }
}
