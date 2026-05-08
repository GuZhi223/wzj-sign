package com.wzj.sign.ui.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.wzj.sign.data.AccountRepository
import com.wzj.sign.data.BackupManager
import com.wzj.sign.data.PreferenceManager
import com.wzj.sign.data.entity.AccountEntity
import com.wzj.sign.log.SignLogger
import com.wzj.sign.service.ServiceManager
import kotlinx.coroutines.flow.debounce
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextButton
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

    // Debounced save for sign count
    LaunchedEffect(Unit) {
        snapshotFlow { signCountText }
            .debounce(500)
            .collect { text ->
                text.toIntOrNull()?.let { count ->
                    if (count > 0) {
                        preferenceManager.signCount = count
                        logger.info("Settings", "签到次数已保存: $count")
                    }
                }
            }
    }

    // Debounced save for interval
    LaunchedEffect(Unit) {
        snapshotFlow { intervalText }
            .debounce(500)
            .collect { text ->
                text.toIntOrNull()?.let { interval ->
                    if (interval > 0) {
                        preferenceManager.signInterval = interval
                        logger.info("Settings", "签到间隔已保存: ${interval}ms")
                    }
                }
            }
    }

    // Debounced save for longitude
    LaunchedEffect(Unit) {
        snapshotFlow { longitudeText }
            .debounce(500)
            .collect { text ->
                preferenceManager.defaultLongitude = text
            }
    }

    // Debounced save for latitude
    LaunchedEffect(Unit) {
        snapshotFlow { latitudeText }
            .debounce(500)
            .collect { text ->
                preferenceManager.defaultLatitude = text
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 签到参数
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("签到参数", style = MiuixTheme.textStyles.title3, color = MiuixTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = signCountText,
                    onValueChange = { signCountText = it },
                    label = "签到次数",
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = intervalText,
                    onValueChange = { intervalText = it },
                    label = "间隔时间(ms)",
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 模拟定位
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("模拟定位", style = MiuixTheme.textStyles.title3, color = MiuixTheme.colorScheme.onSurface)
                        Text("启用后将使用自定义坐标", style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                    Switch(
                        checked = gpsEnabled,
                        onCheckedChange = {
                            gpsEnabled = it
                            preferenceManager.isGpsEnabled = it
                            logger.info("Settings", "模拟定位: ${if (it) "已启用" else "已禁用"}")
                        }
                    )
                }
                AnimatedVisibility(visible = gpsEnabled) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 后台守护
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("后台守护", style = MiuixTheme.textStyles.title3, color = MiuixTheme.colorScheme.onSurface)
                    Text(
                        if (serviceRunning) "运行中" else "未运行",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
                Switch(
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 数据管理
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("数据管理", style = MiuixTheme.textStyles.title3, color = MiuixTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
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
                    ) {
                        Text("导出数据")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
}
