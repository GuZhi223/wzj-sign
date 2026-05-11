package com.wzj.sign.ui.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.wzj.sign.data.AccountRepository
import com.wzj.sign.data.BackupManager
import com.wzj.sign.data.PreferenceManager
import com.wzj.sign.log.SignLogger
import com.wzj.sign.service.ServiceManager
import kotlinx.coroutines.flow.debounce
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsScreen(
    preferenceManager: PreferenceManager,
    serviceManager: ServiceManager,
    backupManager: BackupManager,
    accountRepository: AccountRepository,
    logger: SignLogger,
    onDataImported: () -> Unit
) {
    val context = LocalContext.current

    var signCountText by remember { mutableStateOf(preferenceManager.signCount.toString()) }
    var intervalText by remember { mutableStateOf(preferenceManager.signInterval.toString()) }
    var gpsEnabled by remember { mutableStateOf(preferenceManager.isGpsEnabled) }
    var longitudeText by remember { mutableStateOf(preferenceManager.defaultLongitude) }
    var latitudeText by remember { mutableStateOf(preferenceManager.defaultLatitude) }
    var daemonEnabled by remember { mutableStateOf(preferenceManager.isDaemonEnabled) }
    var serviceRunning by remember { mutableStateOf(serviceManager.isServiceRunning) }

    LaunchedEffect(Unit) {
        snapshotFlow { signCountText }
            .debounce(500)
            .collect { text ->
                text.toIntOrNull()?.takeIf { it > 0 }?.let { count ->
                    preferenceManager.signCount = count
                    logger.info("Settings", "签到次数已保存: $count")
                }
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { intervalText }
            .debounce(500)
            .collect { text ->
                text.toIntOrNull()?.takeIf { it > 0 }?.let { interval ->
                    preferenceManager.signInterval = interval
                    logger.info("Settings", "签到间隔已保存: ${interval}ms")
                }
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { longitudeText }
            .debounce(500)
            .collect { preferenceManager.defaultLongitude = it }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { latitudeText }
            .debounce(500)
            .collect { preferenceManager.defaultLatitude = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsGroup(title = "签到参数") {
            SettingsInputRow(
                title = "签到次数",
                summary = "每个账号最多扫描次数",
                value = signCountText,
                onValueChange = { signCountText = it.filter(Char::isDigit) },
                suffix = "次",
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsInputRow(
                title = "间隔时间",
                summary = "两次扫描之间的等待",
                value = intervalText,
                onValueChange = { intervalText = it.filter(Char::isDigit) },
                suffix = "ms",
                keyboardType = KeyboardType.Number
            )
        }

        SettingsGroup(title = "模拟定位") {
            SettingsSwitchRow(
                title = "启用坐标",
                summary = "签到时使用自定义经纬度",
                checked = gpsEnabled,
                onCheckedChange = {
                    gpsEnabled = it
                    preferenceManager.isGpsEnabled = it
                    logger.info("Settings", "模拟定位: ${if (it) "已启用" else "已禁用"}")
                }
            )
            AnimatedVisibility(visible = gpsEnabled) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsInputRow(
                        title = "经度",
                        summary = "默认经度",
                        value = longitudeText,
                        onValueChange = { longitudeText = it },
                        keyboardType = KeyboardType.Decimal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SettingsInputRow(
                        title = "纬度",
                        summary = "默认纬度",
                        value = latitudeText,
                        onValueChange = { latitudeText = it },
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }
        }

        SettingsGroup(title = "后台守护") {
            SettingsSwitchRow(
                title = "前台服务",
                summary = if (serviceRunning) "运行中" else "未运行",
                checked = serviceRunning,
                onCheckedChange = {
                    daemonEnabled = it
                    preferenceManager.isDaemonEnabled = it
                    if (it) {
                        serviceManager.startSignService()
                        logger.info("Settings", "后台守护已启动")
                    } else {
                        serviceManager.stopSignService()
                        logger.info("Settings", "后台守护已停止")
                    }
                    serviceRunning = serviceManager.isServiceRunning
                }
            )
        }

        SettingsGroup(title = "数据管理") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CompactDataAction(
                    text = "导出数据",
                    onClick = {
                        accountRepository.getAll { accounts ->
                            if (accounts.isNullOrEmpty()) {
                                Toast.makeText(context, "没有数据可导出", Toast.LENGTH_SHORT).show()
                                return@getAll
                            }
                            val filePath = backupManager.exportAccounts(accounts)
                            if (filePath != null) {
                                Toast.makeText(context, "导出成功: $filePath", Toast.LENGTH_LONG).show()
                                logger.info("Settings", "数据导出成功: $filePath")
                            } else {
                                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                CompactDataAction(
                    text = "导入数据",
                    onClick = {
                        val backupFiles = backupManager.backupFiles
                        if (backupFiles.isNullOrEmpty()) {
                            Toast.makeText(context, "没有找到备份文件", Toast.LENGTH_SHORT).show()
                            return@CompactDataAction
                        }
                        val latest = backupFiles.last()
                        val accounts = backupManager.importAccounts(latest)
                        if (accounts == null) {
                            Toast.makeText(context, "导入失败: 无法读取备份文件", Toast.LENGTH_SHORT).show()
                            return@CompactDataAction
                        }
                        accountRepository.deleteAll {
                            if (accounts.isEmpty()) {
                                Toast.makeText(context, "导入成功: 0 条数据", Toast.LENGTH_SHORT).show()
                                return@deleteAll
                            }
                            var imported = 0
                            for (account in accounts) {
                                accountRepository.insert(account) {
                                    imported++
                                    if (imported == accounts.size) {
                                        Toast.makeText(context, "导入成功: $imported 条数据", Toast.LENGTH_SHORT).show()
                                        logger.info("Settings", "数据导入完成: $imported 条")
                                        onDataImported()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SettingsInputRow(
    title: String,
    summary: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MiuixTheme.colorScheme.onSurface,
                fontSize = 17.sp
            )
            Text(
                text = summary,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 13.sp
            )
        }
        Row(
            modifier = Modifier
                .background(MiuixTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(82.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(
                    color = MiuixTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End
                )
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = " $suffix",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CompactDataAction(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = MiuixTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MiuixTheme.colorScheme.onSurface,
                fontSize = 17.sp
            )
            Text(
                text = summary,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 14.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        SmallTitle(text = title)
        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp)) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), content = content)
        }
    }
}
