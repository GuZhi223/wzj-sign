package com.wzj.sign.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.wzj.sign.Account
import com.wzj.sign.R
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomeScreen(
    accounts: List<Account>,
    isSignRunning: Boolean,
    signStatusText: String,
    signCount: Int,
    signInterval: Int,
    gpsEnabled: Boolean,
    serviceRunning: Boolean,
    logCount: Int,
    onStartSign: () -> Unit,
    onStopSign: () -> Unit,
    onAddAccount: () -> Unit,
    onEditAccount: (Int) -> Unit,
    onDeleteAccount: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StatusPanel(
            accounts = accounts,
            isSignRunning = isSignRunning,
            signStatusText = signStatusText,
            serviceRunning = serviceRunning,
            logCount = logCount
        )

        Button(
            onClick = { if (isSignRunning) onStopSign() else onStartSign() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSignRunning) "停止签到" else "开始签到", style = MiuixTheme.textStyles.button)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "账号",
                modifier = Modifier.weight(1f),
                color = MiuixTheme.colorScheme.onBackgroundVariant,
                style = MiuixTheme.textStyles.title3
            )
            if (accounts.size < 3) {
                AddIconButton(onClick = onAddAccount)
            }
        }

        if (accounts.isEmpty()) {
            EmptyAccountCard(onAddAccount)
        } else {
            Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(horizontal = 0.dp, vertical = 6.dp)) {
                accounts.forEachIndexed { index, account ->
                    AccountCard(
                        account = account,
                        onClick = { onEditAccount(index) },
                        onDelete = { onDeleteAccount(index) }
                    )
                }
            }
        }

        SmallTitle(text = "任务参数")
        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp)) {
            MetricRow("扫描次数", "$signCount 次")
            Spacer(modifier = Modifier.height(10.dp))
            MetricRow("扫描间隔", "$signInterval ms")
            Spacer(modifier = Modifier.height(10.dp))
            MetricRow("模拟定位", if (gpsEnabled) "已启用" else "未启用")
        }
    }
}

@Composable
private fun StatusPanel(
    accounts: List<Account>,
    isSignRunning: Boolean,
    signStatusText: String,
    serviceRunning: Boolean,
    logCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "当前状态",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.paragraph
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(signStatusText, color = MiuixTheme.colorScheme.onSurface, style = MiuixTheme.textStyles.title2)
            }
            Card(
                colors = CardDefaults.defaultColors(
                    color = if (isSignRunning) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.primary
                ),
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isSignRunning) "运行中" else "待命",
                    color = MiuixTheme.colorScheme.onPrimary,
                    style = MiuixTheme.textStyles.paragraph
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniMetric("账号", "${accounts.size}/3", Modifier.weight(1f))
            MiniMetric("日志", logCount.toString(), Modifier.weight(1f))
            MiniMetric("后台", if (serviceRunning) "开" else "关", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface),
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(value, style = MiuixTheme.textStyles.title3)
        Text(label, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, style = MiuixTheme.textStyles.footnote1)
    }
}

@Composable
private fun EmptyAccountCard(onAddAccount: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("还没有配置账号", style = MiuixTheme.textStyles.body1)
                Text(
                    text = "添加 OpenID 后即可开始轮询",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.paragraph
                )
            }
            AddIconButton(onClick = onAddAccount)
        }
    }
}

@Composable
private fun AddIconButton(onClick: () -> Unit) {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_add),
        contentDescription = "添加账号",
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick)
            .padding(9.dp),
        tint = MiuixTheme.colorScheme.primary
    )
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            style = MiuixTheme.textStyles.paragraph
        )
        Text(text = value, color = MiuixTheme.colorScheme.onSurface, style = MiuixTheme.textStyles.body1)
    }
}
