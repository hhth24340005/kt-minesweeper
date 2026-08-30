package io.github.hhth24340005.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import io.github.hhth24340005.minesweeper.logic.hexGridOf

@Composable
@Preview
public fun App() {
  var stage by mutableStateOf(stageOf())

  val deferred =
    Game(
      gridComposer = GridComposer.hexOf(),
      uninitializedStage = stage,
    )
  LaunchedEffect(deferred) {
    deferred.await()
    stage = stageOf()
  }
}

private fun stageOf(): MinesweeperStage.Uninitialized =
  MinesweeperStage.prepare(
    width = 9,
    height = 9,
    gridFactory = ::hexGridOf,
    mineDensity = 0.2,
  )
