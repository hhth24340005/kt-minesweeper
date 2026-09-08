package io.github.hhth24340005.minesweeper

import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import io.github.hhth24340005.minesweeper.logic.hexGridOf

context(renderer: RendererScope)
public suspend fun useApp() {
  while (true) {
    val difficulty =
      when (val result = useTitle()) {
        is TitleResult.Start -> result.difficulty
        is TitleResult.Quit -> return
      }
    useGame(
      gridRenderer = GridRenderer.hex,
      uninitializedStage = stageOf(difficulty),
    )
  }
}

private fun stageOf(
  difficulty: GameDifficulty,
): MinesweeperStage.Uninitialized =
  MinesweeperStage.prepare(
    width = difficulty.width,
    height = difficulty.height,
    gridFactory = ::hexGridOf,
    mineDensity = difficulty.mineDensity,
  )
