@file:OptIn(ExperimentalTypeInference::class)

package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.InternalForInheritanceCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.experimental.ExperimentalTypeInference

@Composable
public fun <R> LaunchedRenderer(
  key1: Any?,
  vararg keys: Any?,
  action: suspend RendererScope.() -> R,
): Deferred<R> {
  val deferred =
    remember(key1, *keys) {
      CompletableDeferred<R>()
    }
  val compositions =
    remember(key1, *keys) {
      mutableStateListOf<@Composable () -> Unit>()
    }
  Box(modifier = Modifier.fillMaxSize()) {
    compositions.forEach { composable ->
      key(composable) {
        composable()
      }
    }
  }
  LaunchedEffect(key1, *keys) {
    val scope =
      object : RendererScope, CoroutineScope by this {
        override suspend fun <R> renderWhile(
          modifier: Modifier,
          alignment: Alignment,
          runWhile: suspend (R) -> Boolean,
          content: @Composable () -> Flow<R>,
        ): R {
          val deferredComposition = CompletableDeferred<Flow<R>>()
          val composition: @Composable () -> Unit = {
            Box(
              modifier = modifier,
              contentAlignment = alignment,
            ) {
              deferredComposition.complete(content())
            }
          }
          compositions += composition
          try {
            return deferredComposition.await().first(runWhile)
          } finally {
            compositions -= composition
          }
        }
      }
    deferred.complete(scope.action())
  }
  @OptIn(InternalForInheritanceCoroutinesApi::class)
  return object : Deferred<R> by deferred {}
}

public interface RendererScope : CoroutineScope {
  @OverloadResolutionByLambdaReturnType
  public suspend fun <R> renderWhile(
    modifier: Modifier = Modifier.fillMaxSize(),
    alignment: Alignment = Alignment.Center,
    runWhile: suspend (R) -> Boolean,
    content: @Composable (() -> Flow<R>),
  ): R

  public suspend fun <R> renderCompletable(
    modifier: Modifier = Modifier.fillMaxSize(),
    alignment: Alignment = Alignment.Center,
    content: @Composable ((complete: ((R) -> Unit)?) -> Unit),
  ): R =
    renderAndGetFirst(modifier, alignment) {
      val future = remember { CompletableDeferred<R>() }
      var subscriptions by remember { mutableIntStateOf(0) }
      val isSubscribed = 0 < subscriptions
      val flow =
        remember {
          flow {
            subscriptions++
            try {
              emit(future.await())
            } finally {
              subscriptions--
            }
          }
        }
      key(isSubscribed) {
        if (isSubscribed) {
          content { future.complete(it) }
        } else {
          content(null)
        }
      }
      flow
    }

  @OverloadResolutionByLambdaReturnType
  public suspend fun <R> renderAndGetFirst(
    modifier: Modifier = Modifier.fillMaxSize(),
    alignment: Alignment = Alignment.Center,
    content: @Composable (() -> Flow<R>),
  ): R =
    renderWhile(modifier, alignment, { true }, content)

  @OverloadResolutionByLambdaReturnType
  public suspend fun <R> render(
    modifier: Modifier = Modifier.fillMaxSize(),
    alignment: Alignment = Alignment.Center,
    content: @Composable (() -> Deferred<R>),
  ): R =
    renderAndGetFirst {
      val flow =
        remember {
          MutableSharedFlow<R>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
          )
        }
      val deferred = content()
      LaunchedEffect(deferred) {
        flow.emit(deferred.await())
      }

      flow.asSharedFlow()
    }
}
