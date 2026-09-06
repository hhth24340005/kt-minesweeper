package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

context(renderer: RendererScope)
public suspend fun useTitle(): TitleResult =
  renderer.renderCompletable(
    alignment = Alignment.BottomCenter,
  ) { complete ->
    Row(
      Modifier
        .fillMaxWidth()
        .padding(bottom = 20.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      Text(
        "Start",
        modifier =
          Modifier.clickable {
            complete?.invoke(TitleResult.Start)
          },
        fontSize = 2.em,
      )
      Text(
        "Leaderboard",
        modifier =
          Modifier.clickable {
            complete?.invoke(TitleResult.Leaderboard)
          },
        fontSize = 2.em,
      )
      Text(
        "Quit",
        modifier =
          Modifier.clickable {
            complete?.invoke(TitleResult.Quit)
          },
        fontSize = 2.em,
      )
    }
  }

public sealed interface TitleResult {
  public data object Start : TitleResult

  public data object Leaderboard : TitleResult

  public data object Quit : TitleResult
}
