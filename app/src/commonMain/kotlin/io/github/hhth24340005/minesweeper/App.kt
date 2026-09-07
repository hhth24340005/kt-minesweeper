package io.github.hhth24340005.minesweeper

import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import io.github.hhth24340005.minesweeper.logic.hexGridOf

context(renderer: RendererScope)
public suspend fun useApp() {
  while (true) {
    while (true) {
      when (useTitle()) {
        is TitleResult.Start -> break
        is TitleResult.Leaderboard -> continue
        is TitleResult.Quit -> return
      }
    }
    useGame(
      gridRenderer = GridRenderer.hex,
      uninitializedStage = stageOf(),
    )
  }
}

private fun stageOf(): MinesweeperStage.Uninitialized =
  MinesweeperStage.prepare(
    width = 19,
    height = 19,
    gridFactory = ::hexGridOf,
    mineDensity = 0.2,
  )
