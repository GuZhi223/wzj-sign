package com.wzj.sign.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wzj.sign.Account
import com.wzj.sign.R
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val uin = account.uin
            Text(
                text = if (uin.isNullOrEmpty()) "未填写备注" else uin,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = account.openid.orEmpty(),
                style = MiuixTheme.textStyles.paragraph,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        CompactActionIcon(
            iconRes = R.drawable.ic_edit,
            contentDescription = "编辑",
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            onClick = onClick
        )
        CompactActionIcon(
            iconRes = R.drawable.ic_delete,
            contentDescription = "删除",
            tint = MiuixTheme.colorScheme.error,
            onClick = onDelete
        )
    }
}

@Composable
private fun CompactActionIcon(
    iconRes: Int,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(iconRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick)
            .padding(10.dp),
        tint = tint
    )
}
