package com.wzj.sign.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
            Text("微助教自动签到", style = MiuixTheme.textStyles.title2, color = MiuixTheme.colorScheme.onSurface)
            Text("v1.0.0", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, style = MiuixTheme.textStyles.paragraph)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "基于微助教微信 API 的自动签到工具。",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.paragraph
            )
        }

        SmallTitle(text = "说明")
        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
            Text(
                "本应用仅供学习交流使用，请勿用于任何违反学校规定或法律法规的用途。使用本应用产生的一切后果由用户自行承担。",
                style = MiuixTheme.textStyles.paragraph
            )
        }

        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("项目主页", style = MiuixTheme.textStyles.body1)
                    Text("GitHub", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, style = MiuixTheme.textStyles.footnote1)
                }
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(
                    text = "打开",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/GuZhi223/wzj-sign"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}
