package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

context(renderer: RendererScope)
public suspend fun useTitle(): TitleResult =
  renderer.renderCompletable(
    alignment = Alignment.CenterEnd,
  ) { complete ->
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .clip(HexagonShape)
            .background(Color.Gray)
            .padding(horizontal = 150.dp, vertical = 200.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column {
          Text(
            "Hexa",
            fontSize = 3.em,
            fontFamily = FontFamily.Serif,
            color = Color.White,
          )
          Text(
            "Sweeper",
            fontSize = 3.em,
            fontFamily = FontFamily.Serif,
            color = Color.White,
          )
        }
      }
      Spacer(Modifier.padding(horizontal = 40.dp))
      Column(
        Modifier
          .fillMaxHeight(fraction = 0.7f)
          .padding(end = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
      ) {
        Text(
          "Start",
          modifier =
            Modifier
              .offset(x = 0.dp)
              .clip(HexagonShape)
              .clickable {
                complete?.invoke(TitleResult.Start)
              }.background(Color.LightGray)
              .padding(horizontal = 50.dp, vertical = 20.dp),
          fontSize = 3.em,
          fontFamily = FontFamily.Serif,
        )
        Text(
          "Data",
          modifier =
            Modifier
              .offset(x = 30.dp)
              .clip(HexagonShape)
              .clickable {
                complete?.invoke(TitleResult.Leaderboard)
              }.background(Color.LightGray)
              .padding(horizontal = 50.dp, vertical = 20.dp),
          fontSize = 3.em,
          fontFamily = FontFamily.Serif,
        )
        Text(
          "Quit",
          modifier =
            Modifier
              .offset(x = 60.dp)
              .clip(HexagonShape)
              .clickable {
                complete?.invoke(TitleResult.Quit)
              }.background(Color.LightGray)
              .padding(horizontal = 50.dp, vertical = 20.dp),
          fontSize = 3.em,
          fontFamily = FontFamily.Serif,
        )
      }
    }
  }

public sealed interface TitleResult {
  public data object Start : TitleResult

  public data object Leaderboard : TitleResult

  public data object Quit : TitleResult
}

private object HexagonShape : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val (width, height) = size
    val path =
      Path().apply {
        moveTo(width / 2f, 0f)
        lineTo(width, height / 4f)
        lineTo(width, height * 3f / 4f)
        lineTo(width / 2f, height)
        lineTo(0f, height * 3f / 4f)
        lineTo(0f, height / 4f)
        close()
      }
    return Outline.Generic(path)
  }
}
