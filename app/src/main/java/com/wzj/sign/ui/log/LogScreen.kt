package com.wzj.sign.ui.log

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wzj.sign.log.LogEntry
import com.wzj.sign.log.SignLogger
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LogScreen(logger: SignLogger) {
    val context = LocalContext.current
    val logEntries = remember { mutableStateListOf<LogEntry>() }
    val listState = rememberLazyListState()
    var isAtBottom by remember { mutableStateOf(true) }

    DisposableEffect(logger) {
        val listener = object : SignLogger.OnLogListener {
            override fun onNewLog(entry: LogEntry) {
                logEntries.add(entry)
            }
        }
        logEntries.addAll(logger.entries)
        logger.addListener(listener)
        onDispose { logger.removeListener(listener) }
    }

    LaunchedEffect(logEntries.size) {
        if (isAtBottom && logEntries.isNotEmpty()) {
            listState.animateScrollToItem(logEntries.size - 1)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisible == listState.layoutInfo.totalItemsCount - 1
        }.collect { atBottom -> isAtBottom = atBottom }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 72.dp)
        ) {
            items(logEntries, key = { "${it.timestamp}_${it.message}" }) { entry ->
                LogItem(entry)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    text = "清空日志",
                    onClick = {
                        logger.clear()
                        logEntries.clear()
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    text = "导出日志",
                    onClick = {
                        val path = logger.exportToFile()
                        if (path != null) {
                            Toast.makeText(context, "日志已导出: $path", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "日志导出失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LogItem(entry: LogEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = when (entry.level) {
            LogEntry.Level.WARNING -> Color(0xFFFFC107)
            LogEntry.Level.ERROR -> Color(0xFFF44336)
            else -> Color(0xFF4CAF50)
        }
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = entry.formattedTime,
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = entry.message,
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
