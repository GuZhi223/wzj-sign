package com.wzj.sign.ui.log

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wzj.sign.log.LogEntry
import com.wzj.sign.log.SignLogger
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LogScreen(logger: SignLogger) {
    val context = LocalContext.current
    val logEntries = remember { mutableStateListOf<LogEntry>() }

    DisposableEffect(logger) {
        val listener = object : SignLogger.OnLogListener {
            override fun onNewLog(entry: LogEntry) {
                logEntries.add(entry)
            }
        }
        logEntries.clear()
        logEntries.addAll(logger.entries)
        logger.addListener(listener)
        onDispose { logger.removeListener(listener) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("运行日志", style = MiuixTheme.textStyles.body1)
                        Text(
                            "${logEntries.size} 条记录",
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            style = MiuixTheme.textStyles.footnote1
                        )
                    }
                    CompactLogAction(
                        text = "清空",
                        onClick = {
                            logger.clear()
                            logEntries.clear()
                        }
                    )
                    CompactLogAction(
                        text = "导出",
                        onClick = {
                            val path = logger.exportToFile()
                            if (path != null) {
                                Toast.makeText(context, "日志已导出: $path", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "日志导出失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        if (logEntries.isEmpty()) {
            item {
                Text(
                    text = "暂无日志",
                    modifier = Modifier.padding(16.dp),
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
            }
        } else {
            item { SmallTitle(text = "最近") }
            items(logEntries.asReversed(), key = { "${it.timestamp}_${it.message}" }) { entry ->
                LogItem(entry)
            }
        }
    }
}

@Composable
private fun LogItem(entry: LogEntry) {
    val color = when (entry.level) {
        LogEntry.Level.ERROR -> MiuixTheme.colorScheme.error
        LogEntry.Level.WARNING -> MiuixTheme.colorScheme.primaryVariant
        LogEntry.Level.INFO -> MiuixTheme.colorScheme.primary
        LogEntry.Level.DEBUG -> MiuixTheme.colorScheme.onSurfaceVariantSummary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.message,
                style = MiuixTheme.textStyles.paragraph,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${entry.formattedTime}  ${entry.levelTag}",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.footnote1
            )
        }
    }
}

@Composable
private fun CompactLogAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        color = MiuixTheme.colorScheme.primary,
        style = MiuixTheme.textStyles.paragraph
    )
}
