package com.wzj.sign.ui.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wzj.sign.data.AccountRepository
import com.wzj.sign.data.BackupManager
import com.wzj.sign.data.PreferenceManager
import com.wzj.sign.log.SignLogger
import com.wzj.sign.service.ServiceManager
import kotlinx.coroutines.flow.debounce
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.SwitchPreference

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
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsGroup(title = "签到参数") {
            TextField(
                value = signCountText,
                onValueChange = { signCountText = it.filter(Char::isDigit) },
                label = "签到次数",
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = intervalText,
                onValueChange = { intervalText = it.filter(Char::isDigit) },
                label = "间隔时间(ms)",
                singleLine = true
            )
        }

        SettingsGroup(title = "模拟定位") {
            SwitchPreference(
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
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = longitudeText,
                        onValueChange = { longitudeText = it },
                        label = "经度",
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = latitudeText,
                        onValueChange = { latitudeText = it },
                        label = "纬度",
                        singleLine = true
                    )
                }
            }
        }

        SettingsGroup(title = "后台守护") {
            SwitchPreference(
                title = "前台服务",
                summary = if (serviceRunning) "运行中" else "未运行",
                checked = daemonEnabled,
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
                TextButton(
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
                TextButton(
                    text = "导入数据",
                    onClick = {
                        val backupFiles = backupManager.backupFiles
                        if (backupFiles.isNullOrEmpty()) {
                            Toast.makeText(context, "没有找到备份文件", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val latest = backupFiles.last()
                        val accounts = backupManager.importAccounts(latest)
                        if (accounts == null) {
                            Toast.makeText(context, "导入失败: 无法读取备份文件", Toast.LENGTH_SHORT).show()
                            return@TextButton
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
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        SmallTitle(text = title)
        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(0.dp)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), content = content)
        }
    }
}
