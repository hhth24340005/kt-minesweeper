package io.github.hhth24340005.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import io.github.hhth24340005.minesweeper.logic.hexGridOf

@Composable
@Preview
public fun App() {
  Game(
    gridComposer = GridComposer.hexOf(),
    uninitializedStage =
      MinesweeperStage.prepare(
        width = 9,
        height = 9,
        gridFactory = ::hexGridOf,
        mineDensity = 0.2,
      ),
  )
}
