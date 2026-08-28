package io.github.hhth24340005.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.hhth24340005.minesweeper.resources.Res
import io.github.hhth24340005.minesweeper.resources.cell_concealed
import io.github.hhth24340005.minesweeper.resources.cell_marked
import io.github.hhth24340005.minesweeper.resources.cell_revealed_0
import io.github.hhth24340005.minesweeper.resources.cell_revealed_1
import io.github.hhth24340005.minesweeper.resources.cell_revealed_2
import io.github.hhth24340005.minesweeper.resources.cell_revealed_3
import io.github.hhth24340005.minesweeper.resources.cell_revealed_4
import io.github.hhth24340005.minesweeper.resources.cell_revealed_5
import io.github.hhth24340005.minesweeper.resources.cell_revealed_6
import io.github.hhth24340005.minesweeper.resources.cell_revealed_7
import io.github.hhth24340005.minesweeper.resources.cell_revealed_8
import io.github.hhth24340005.minesweeper.resources.cell_revealed_mine
import org.jetbrains.compose.resources.imageResource

@Composable
@Preview
public fun App() {
  val cellPref =
    CellPreferences(
      cellSize = 64.dp,
      textureConcealed = imageResource(Res.drawable.cell_concealed),
      textureMarked = imageResource(Res.drawable.cell_marked),
      textureRevealed0 = imageResource(Res.drawable.cell_revealed_0),
      textureRevealed1 = imageResource(Res.drawable.cell_revealed_1),
      textureRevealed2 = imageResource(Res.drawable.cell_revealed_2),
      textureRevealed3 = imageResource(Res.drawable.cell_revealed_3),
      textureRevealed4 = imageResource(Res.drawable.cell_revealed_4),
      textureRevealed5 = imageResource(Res.drawable.cell_revealed_5),
      textureRevealed6 = imageResource(Res.drawable.cell_revealed_6),
      textureRevealed7 = imageResource(Res.drawable.cell_revealed_7),
      textureRevealed8 = imageResource(Res.drawable.cell_revealed_8),
      textureRevealedMine = imageResource(Res.drawable.cell_revealed_mine),
    )
  Game(
    cellPref = cellPref,
    width = 10,
    height = 10,
    mineDensity = 0.2,
  )
}
