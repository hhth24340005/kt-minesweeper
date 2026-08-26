package io.github.hhth24340005.minesweeper

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

public fun main() {
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "Minesweeper",
    ) {
      App()
    }
  }
}
