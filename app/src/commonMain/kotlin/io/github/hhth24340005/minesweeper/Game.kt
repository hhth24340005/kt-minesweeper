package io.github.hhth24340005.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.pointer.PointerButton
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import kotlinx.coroutines.flow.firstOrNull

@Composable
public fun Game(
  gridComposer: GridComposer,
  uninitializedStage: MinesweeperStage.Uninitialized,
) {
  LaunchedRenderer(uninitializedStage) {
    layerOf().use { layer0 ->
      val clickedCell =
        layer0 { complete ->
          val deferred =
            gridComposer.UninitializedGrid(uninitializedStage.rows)
          LaunchedEffect(deferred) {
            complete(deferred.await())
          }
        }
      val stage = uninitializedStage.initialize(clickedCell)
      layer0<Nothing> {
        val clicks = gridComposer.Grid(stage.rows)
        LaunchedEffect(clicks) {
          while (true) {
            val (click, cell) = clicks.firstOrNull() ?: break
            when (click) {
              PointerButton.Primary -> {
                stage.reveal(cell)
              }

              PointerButton.Secondary -> {
                stage.toggleMark(cell)
              }
            }
          }
        }
      }
    }
  }
}
