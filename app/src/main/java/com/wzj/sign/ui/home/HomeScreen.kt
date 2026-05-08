package com.wzj.sign.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wzj.sign.Account
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomeScreen(
    accounts: List<Account>,
    isSignRunning: Boolean,
    signStatusText: String,
    onAddAccount: () -> Unit,
    onEditAccount: (Int) -> Unit,
    onDeleteAccount: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Sign status card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "签到状态",
                    style = MiuixTheme.textStyles.subtitle,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = signStatusText,
                    style = MiuixTheme.textStyles.title2,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已配置账号",
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface
            )
            TextButton(text = "添加", onClick = onAddAccount)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Account list or empty state
        if (accounts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "还没有配置账号",
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击上方「添加」按钮配置账号",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(text = "前往设置", onClick = onNavigateToSettings)
            }
        } else {
            accounts.forEachIndexed { index, account ->
                AccountCard(
                    account = account,
                    onClick = { onEditAccount(index) },
                    onDelete = { onDeleteAccount(index) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
