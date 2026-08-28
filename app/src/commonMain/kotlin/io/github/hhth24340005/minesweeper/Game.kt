package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.hhth24340005.minesweeper.logic.UninitializedStage

@Composable
public fun Game(
  cellPref: CellPreferences,
  stage: UninitializedStage,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    stage
      .rows
      .forEach { row ->
        Row {
          row.forEach { cell ->
            Cell(
              cell.state,
              cellPref,
              onLeftClick = {
                stage.getOrInit(cell).reveal(cell)
              },
              onRightClick = {
                stage.getOrInit(cell).toggleMark(cell)
              },
            )
          }
        }
      }
  }
}
