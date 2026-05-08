package com.wzj.sign

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.wzj.sign.data.AccountRepository
import com.wzj.sign.data.BackupManager
import com.wzj.sign.data.DataConverter
import com.wzj.sign.data.PreferenceManager
import com.wzj.sign.data.entity.AccountEntity
import com.wzj.sign.log.SignLogger
import com.wzj.sign.network.NetworkUtils
import com.wzj.sign.network.SignRepository
import com.wzj.sign.network.model.ActiveSignResponse
import com.wzj.sign.network.model.SignResponse
import com.wzj.sign.service.ServiceManager
import com.wzj.sign.ui.components.AccountBottomSheet
import com.wzj.sign.ui.about.AboutScreen
import com.wzj.sign.ui.home.HomeScreen
import com.wzj.sign.ui.log.LogScreen
import com.wzj.sign.ui.settings.SettingsScreen
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeController = remember {
                ThemeController(
                    colorSchemeMode = ColorSchemeMode.System,
                    paletteStyle = ThemePaletteStyle.TonalSpot
                )
            }
            MiuixTheme(controller = themeController) {
                App()
            }
        }
    }
}

private data class TabItem(val label: String, val iconRes: Int)

private val tabs = listOf(
    TabItem("签到", R.drawable.ic_home),
    TabItem("日志", R.drawable.ic_log),
    TabItem("设置", R.drawable.ic_settings),
    TabItem("关于", R.drawable.ic_info),
)

@Composable
private fun App() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Page navigation
    var currentPage by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 4 })

    // Sync pager state with current page
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    BackHandler(enabled = currentPage != 0) {
        currentPage = 0
        coroutineScope.launch { pagerState.animateScrollToPage(0) }
    }

    // Sign process state
    var isSignRunning by remember { mutableStateOf(false) }
    var signStatusText by remember { mutableStateOf("就绪") }

    // Accounts
    val accounts = remember { mutableStateListOf<Account>() }

    // Bottom sheet state
    var showAccountSheet by remember { mutableStateOf(false) }
    var editingAccountIndex by remember { mutableIntStateOf(-1) }

    // Java business logic instances
    val accountRepository = remember { AccountRepository(context) }
    val preferenceManager = remember { PreferenceManager(context) }
    val signRepository = remember { SignRepository() }
    val logger = remember { SignLogger.getInstance(context) }
    val serviceManager = remember { ServiceManager(context) }
    val backupManager = remember { BackupManager(context) }

    // Executor for sign process
    var executorService by remember { mutableStateOf(Executors.newFixedThreadPool(3)) }
    DisposableEffect(Unit) {
        onDispose { executorService.shutdownNow() }
    }

    // Load accounts on first composition
    LaunchedEffect(Unit) {
        accountRepository.getAll { entities ->
            accounts.clear()
            entities.mapTo(accounts) { DataConverter.toModel(it) }
        }
        logger.info("App", "应用启动")
    }

    // Save accounts helper
    fun saveAccounts() {
        val entities = accounts
            .filter { !it.uin.isNullOrEmpty() && !it.openid.isNullOrEmpty() }
            .map { DataConverter.toEntity(it) }
        if (entities.isEmpty()) return
        accountRepository.replaceAll(entities) {
            logger.info("App", "已保存 ${entities.size} 个账号")
        }
    }

    // Sign process
    fun startSign() {
        if (accounts.isEmpty()) {
            Toast.makeText(context, "请先添加账号", Toast.LENGTH_SHORT).show()
            return
        }
        if (isSignRunning) {
            Toast.makeText(context, "签到正在进行中", Toast.LENGTH_SHORT).show()
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Toast.makeText(context, "网络不可用，请检查网络连接", Toast.LENGTH_SHORT).show()
            return
        }

        var count = preferenceManager.signCount
        var interval = preferenceManager.signInterval
        if (interval < 100) {
            Toast.makeText(context, "强制调整为300ms防拦截", Toast.LENGTH_SHORT).show()
            interval = 300
            preferenceManager.signInterval = 300
        }

        val enableGps = preferenceManager.isGpsEnabled
        val longitude = preferenceManager.defaultLongitude
        val latitude = preferenceManager.defaultLatitude

        isSignRunning = true
        signStatusText = "签到中..."

        logger.info("App", "开始签到流程，共 ${accounts.size} 个账号")
        logger.info("App", "参数: 次数=$count, 间隔=${interval}ms, GPS=${if (enableGps) "启用" else "禁用"}")

        for (account in accounts) {
            if (account.uin.isEmpty() || account.openid.isEmpty()) {
                logger.warn("App", "账号信息不完整，跳过")
                continue
            }
            val finalLon = if (enableGps) (if (longitude.isEmpty()) account.longitude else longitude) else ""
            val finalLat = if (enableGps) (if (latitude.isEmpty()) account.latitude else latitude) else ""

            executorService.execute {
                logger.info("App", "[${account.uin}] 开始签到任务")
                for (i in 1..count) {
                    if (!isSignRunning) break
                    if (i == 1 || i == count || interval >= 1000 || i % 5 == 0) {
                        logger.info("App", "[${account.uin}] 扫描波次: $i/$count")
                    }

                    val latch = CountDownLatch(1)
                    var success = false

                    signRepository.getActiveSigns(account.openid, object : SignRepository.ResultCallback<List<ActiveSignResponse>> {
                        override fun onSuccess(data: List<ActiveSignResponse>?) {
                            if (data.isNullOrEmpty()) { latch.countDown(); return }
                            val sign = data[0]
                            logger.info("App", "发现签到任务: ${sign.signTypeName} Course=${sign.courseId}, Sign=${sign.signId}")

                            signRepository.submitSign(account.openid, sign.courseId, sign.signId,
                                enableGps && sign.requiresGps(), finalLon, finalLat,
                                object : SignRepository.ResultCallback<SignResponse> {
                                    override fun onSuccess(response: SignResponse) {
                                        success = response.isSignSuccess
                                        logger.info("App", "[${account.uin}] 服务器回包: ${response.displayMessage}")
                                        latch.countDown()
                                    }
                                    override fun onError(error: String) {
                                        logger.error("App", "[${account.uin}] 签到错误: $error")
                                        latch.countDown()
                                    }
                                })
                        }
                        override fun onError(error: String) {
                            logger.error("App", "[${account.uin}] 获取签到任务失败: $error")
                            latch.countDown()
                        }
                    })

                    try { latch.await(15, TimeUnit.SECONDS) } catch (_: InterruptedException) { Thread.currentThread().interrupt(); break }
                    if (success) {
                        logger.info("App", "[${account.uin}] 签到成功！")
                        break
                    }
                    if (i < count && isSignRunning) {
                        try { Thread.sleep(interval.toLong()) } catch (_: InterruptedException) { Thread.currentThread().interrupt(); break }
                    }
                }
                logger.info("App", "[${account.uin}] 签到任务结束")
            }
        }
    }

    fun stopSign() {
        isSignRunning = false
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow()
            }
        } catch (_: InterruptedException) {
            executorService.shutdownNow()
            Thread.currentThread().interrupt()
        }
        executorService = Executors.newFixedThreadPool(3)
        signStatusText = "已停止"
        logger.info("App", "签到流程已停止")
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = tabs[currentPage].label)
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = currentPage == index,
                        onClick = {
                            currentPage = index
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = ImageVector.vectorResource(tab.iconRes),
                        label = tab.label
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentPage == 0) {
                FloatingActionButton(onClick = {
                    if (isSignRunning) stopSign() else startSign()
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(if (isSignRunning) R.drawable.ic_stop else R.drawable.ic_play),
                        contentDescription = if (isSignRunning) "停止签到" else "开始签到"
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    accounts = accounts,
                    isSignRunning = isSignRunning,
                    signStatusText = signStatusText,
                    onAddAccount = {
                        editingAccountIndex = -1
                        showAccountSheet = true
                    },
                    onEditAccount = { index ->
                        editingAccountIndex = index
                        showAccountSheet = true
                    },
                    onDeleteAccount = { index ->
                        if (index in accounts.indices) {
                            val removed = accounts[index]
                            accounts.removeAt(index)
                            saveAccounts()
                            logger.info("Home", "移除了账号: ${removed.uin}")
                        }
                    },
                    onNavigateToSettings = {
                        currentPage = 2
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
                1 -> LogScreen(logger = logger)
                2 -> SettingsScreen(
                    preferenceManager = preferenceManager,
                    serviceManager = serviceManager,
                    backupManager = backupManager,
                    accountRepository = accountRepository,
                    logger = logger,
                    onDataImported = {
                        accountRepository.getAll { entities ->
                            accounts.clear()
                            entities.mapTo(accounts) { DataConverter.toModel(it) }
                        }
                    }
                )
                3 -> AboutScreen()
            }
        }
    }

    // Account bottom sheet
    if (showAccountSheet) {
        val editAccount = if (editingAccountIndex in accounts.indices) accounts[editingAccountIndex] else null
        AccountBottomSheet(
            editAccount = editAccount,
            onDismiss = { showAccountSheet = false },
            onSave = { saved ->
                if (editingAccountIndex >= 0 && editingAccountIndex < accounts.size) {
                    accounts[editingAccountIndex] = saved
                    logger.info("Home", "更新了账号: ${saved.uin}")
                } else {
                    accounts.add(saved)
                    logger.info("Home", "添加了新账号: ${saved.uin}")
                }
                saveAccounts()
                showAccountSheet = false
            }
        )
    }
}
