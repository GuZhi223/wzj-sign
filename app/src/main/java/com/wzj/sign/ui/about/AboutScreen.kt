package com.wzj.sign.ui.about

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wzj.sign.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
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
        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
            InfoRow(title = "微助教自动签到", summary = "安静地轮询签到任务")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(title = "版本", summary = "v1.0.0")
        }

        SmallTitle(text = "说明")
        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "本应用仅供学习交流使用，请勿用于任何违反学校规定或法律法规的用途。使用本应用产生的一切后果由用户自行承担。",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/GuZhi223/wzj-sign"))
                    context.startActivity(intent)
                },
            insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_github),
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("项目主页", style = MiuixTheme.textStyles.body1)
                    Text("GitHub", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, summary: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = MiuixTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
        Text(
            text = summary,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 14.sp
        )
    }
}
