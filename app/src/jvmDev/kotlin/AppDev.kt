import androidx.compose.runtime.Composable
import io.github.hhth24340005.minesweeper.App
import org.jetbrains.compose.reload.DevelopmentEntryPoint

@DevelopmentEntryPoint(windowWidth = 480, windowHeight = 640)
@Composable
public fun AppDev() {
  App()
}
