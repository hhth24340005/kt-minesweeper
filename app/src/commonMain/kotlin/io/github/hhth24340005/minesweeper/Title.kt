package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right

context(renderer: RendererScope)
public suspend fun useTitle(): TitleResult {
  while (true) {
    return renderer
      .renderCompletable(
        alignment = Alignment.CenterEnd,
      ) { complete ->
        Column {
          val difficulties =
            listOf(
              GameDifficulty.Beginner,
              GameDifficulty.Intermediate,
              GameDifficulty.Expert,
            )
          var selection by remember { mutableStateOf(difficulties[0]) }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
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
                      complete?.invoke(
                        TitleResult.Start(selection).right(),
                      )
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
                      complete?.invoke(
                        TitleModalResult.Leaderboard.left(),
                      )
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
                      complete?.invoke(TitleResult.Quit.right())
                    }.background(Color.LightGray)
                    .padding(horizontal = 50.dp, vertical = 20.dp),
                fontSize = 3.em,
                fontFamily = FontFamily.Serif,
              )
            }
          }
          Spacer(Modifier.padding(vertical = 20.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            difficulties.forEach { difficulty ->
              Text(
                difficulty.label,
                modifier =
                  Modifier
                    .clip(HexagonShape)
                    .clickable {
                      selection = difficulty
                    }.background(
                      if (difficulty == selection) {
                        Color.Gray
                      } else {
                        Color.LightGray
                      },
                    ).padding(20.dp),
                fontSize = 2.em,
                fontFamily = FontFamily.Serif,
                color =
                  if (difficulty == selection) {
                    Color.White
                  } else {
                    Color.Black
                  },
              )
            }
          }
        }
      }.getOrElse {
        when (it) {
          TitleModalResult.Leaderboard -> {
            renderer.renderCompletable { complete ->
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  "Not implemented yet!",
                  color = Color.Black,
                  fontFamily = FontFamily.Serif,
                  fontSize = 2.em,
                )
                Spacer(Modifier.padding(vertical = 20.dp))
                Box(
                  modifier =
                    Modifier
                      .clip(HexagonShape)
                      .background(Color.Gray)
                      .clickable {
                        complete?.invoke(Unit)
                      }.padding(
                        horizontal = 50.dp,
                        vertical = 20.dp,
                      ),
                ) {
                  Text(
                    "Go back",
                    color = Color.White,
                    fontSize = 2.em,
                    fontFamily = FontFamily.Serif,
                  )
                }
              }
            }
          }
        }
        continue
      }
  }
}

public sealed interface TitleResult {
  public data class Start(public val difficulty: GameDifficulty) : TitleResult

  public data object Quit : TitleResult
}

private sealed interface TitleModalResult {
  data object Leaderboard : TitleModalResult
}

public interface GameDifficulty {
  public val label: String
  public val width: Int
  public val height: Int
  public val mineDensity: Double

  public data object Beginner : GameDifficulty {
    override val label: String = "Beginner"
    override val width: Int = 5
    override val height: Int = 5
    override val mineDensity: Double = 0.3
  }

  public data object Intermediate : GameDifficulty {
    override val label: String = "Intermediate"
    override val width: Int = 9
    override val height: Int = 9
    override val mineDensity: Double = 0.2
  }

  public data object Expert : GameDifficulty {
    override val label: String = "Expert"
    override val width: Int = 19
    override val height: Int = 19
    override val mineDensity: Double = 0.2
  }
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
