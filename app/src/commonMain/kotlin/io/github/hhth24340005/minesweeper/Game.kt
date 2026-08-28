package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.hhth24340005.minesweeper.logic.Stage
import io.github.hhth24340005.minesweeper.logic.Stage.RevealResult.Exploded
import io.github.hhth24340005.minesweeper.logic.Stage.RevealResult.Revealed

@Composable
public fun Game(
  cellPref: CellPreferences,
  width: Int,
  height: Int,
  mineDensity: Double,
) {
  require(0 < width)
  require(0 < height)
  val stage = remember { Stage(width, height, mineDensity) }
  val states = remember { mutableStateMapOf<Stage.CellId, CellState>() }
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    stage
      .rows
      .forEach { row ->
        Row {
          row.forEach { cellId ->
            Cell(
              states.getOrPut(cellId) { CellState.Concealed },
              cellPref,
              onLeftClick = {
                when (val result = stage.reveal(cellId)) {
                  is Exploded -> {
                    states[cellId] = CellState.RevealedMine
                  }

                  is Revealed -> {
                    result.revealedCells.forEach { (cellId, minesAround) ->
                      states[cellId] =
                        CellState.revealedOfFromMinesAround(minesAround)
                    }
                  }
                }
              },
              onRightClick = { },
              onMiddleClick = { },
            )
          }
        }
      }
  }
}
