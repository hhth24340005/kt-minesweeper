package io.github.hhth24340005.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import io.github.hhth24340005.minesweeper.logic.hexGridOf
import kotlinx.coroutines.Job

@Composable
@Preview
public fun App(): Job =
  LaunchedRenderer(Unit) {
    val gridComposer = GridRenderer.hexOf()

    while (true) {
      when (useTitle()) {
        is TitleResult.Start -> {
          useGame(
            gridRenderer = gridComposer,
            uninitializedStage = stageOf(),
          )
        }

        is TitleResult.Leaderboard -> {
          continue
        }

        is TitleResult.Quit -> {
          break
        }
      }
    }
  }

private fun stageOf(): MinesweeperStage.Uninitialized =
  MinesweeperStage.prepare(
    width = 9,
    height = 9,
    gridFactory = ::hexGridOf,
    mineDensity = 0.2,
  )
