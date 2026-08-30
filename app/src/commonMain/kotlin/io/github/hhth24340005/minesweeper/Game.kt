package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.hhth24340005.minesweeper.logic.CellState
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage

@Composable
context(cellPreferences: CellPreferences)
public fun Game(
  uninitializedStage: MinesweeperStage.Uninitialized,
) {
  LaunchedRenderer(uninitializedStage) {
    layerOf().use { layer0 ->
      val stage =
        layer0 { complete ->
          uninitializedStage.rows.renderCells { cell ->
            Cell(
              CellState.Concealed,
              onLeftClick = {
                complete(uninitializedStage.initialize(cell))
              },
            )
          }
        }
      layer0<Nothing> {
        stage.rows.renderCells { cell ->
          Cell(
            cell.status,
            onLeftClick = {
              stage.reveal(cell)
            },
            onRightClick = {
              stage.toggleMark(cell)
            },
          )
        }
      }
    }
  }
}

@Composable
private fun <T> List<List<T>>.renderCells(
  cell: @Composable (T) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    forEach { row ->
      Row {
        row.forEach {
          cell(it)
        }
      }
    }
  }
}
