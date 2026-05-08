package com.wzj.sign.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App info card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("微助教自动签到", style = MiuixTheme.textStyles.title2, color = MiuixTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text("版本: 1.0.0", style = MiuixTheme.textStyles.main, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("一个自动完成微助教签到的 Android 应用", style = MiuixTheme.textStyles.main, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Disclaimer card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("免责声明", style = MiuixTheme.textStyles.title3, color = MiuixTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "本应用仅供学习交流使用，请勿用于任何违反学校规定或法律法规的行为。使用本应用产生的一切后果由用户自行承担。",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GitHub button
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/GuZhi223/wzj-sign"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("查看 GitHub 仓库")
        }
    }
}
