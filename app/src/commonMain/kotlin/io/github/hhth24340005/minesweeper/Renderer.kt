package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

@Composable
internal fun LaunchedRenderer(
  key1: Any?,
  vararg keys: Any?,
  action: suspend RendererScope.() -> Unit,
) {
  val rendererStates =
    remember {
      mutableStateListOf<MutableState<@Composable () -> Unit>>()
    }
  Box(modifier = Modifier.fillMaxSize()) {
    rendererStates.forEach { key(it) { it.value() } }
  }
  LaunchedEffect(key1, *keys) {
    val scope =
      object : RendererScope, CoroutineScope by this {
        override fun layerOf(): Layer =
          object : Layer {
            val rendererState = mutableStateOf<@Composable () -> Unit>({})
            val mutex = Mutex()

            init {
              rendererStates += rendererState
            }

            override suspend fun <T> invoke(
              render: @Composable ((complete: (T) -> Unit) -> Unit),
            ): T =
              mutex.withLock {
                suspendCancellableCoroutine { cont ->
                  rendererState.value = {
                    render {
                      if (cont.isActive) {
                        cont.resume(it)
                      }
                    }
                  }
                }
              }

            override fun close() {
              rendererStates -= rendererState
            }
          }
      }
    try {
      scope.action()
    } finally {
      rendererStates.clear()
    }
  }
}

internal interface RendererScope : CoroutineScope {
  fun layerOf(): Layer
}

internal interface Layer : AutoCloseable {
  suspend operator fun <T> invoke(
    render: @Composable ((complete: (T) -> Unit) -> Unit),
  ): T

  override fun close()
}
