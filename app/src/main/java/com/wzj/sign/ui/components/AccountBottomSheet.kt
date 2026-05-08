package com.wzj.sign.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wzj.sign.Account
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AccountBottomSheet(
    editAccount: Account?,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    val context = LocalContext.current
    var uin by remember { mutableStateOf(editAccount?.uin ?: "") }
    var openid by remember { mutableStateOf(editAccount?.openid ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            cornerRadius = 28.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (editAccount != null) "编辑账号" else "添加账号",
                    style = MiuixTheme.textStyles.title2,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = uin,
                    onValueChange = { uin = it },
                    label = "备注",
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = openid,
                    onValueChange = { openid = it },
                    label = "OpenID 或链接",
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        text = "取消",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        text = "保存",
                        onClick = {
                            if (uin.isBlank()) {
                                Toast.makeText(context, "请输入备注", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            val extracted = extractOpenid(openid.trim())
                            if (extracted == null) {
                                Toast.makeText(context, "无法识别OpenID", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            onSave(Account(uin.trim(), extracted, editAccount?.longitude ?: "", editAccount?.latitude ?: ""))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun extractOpenid(input: String): String? {
    if (input.isBlank()) return null
    if (input.length > 20 && input.all { it.isLetterOrDigit() || it == '_' || it == '-' }) {
        return input
    }
    try {
        val uri = android.net.Uri.parse(input)
        uri.getQueryParameter("openid")?.let { return it }
        uri.getQueryParameter("openId")?.let { return it }
        uri.getQueryParameter("open_id")?.let { return it }
        uri.fragment?.let { fragment ->
            for (param in fragment.split("&")) {
                val parts = param.split("=", limit = 2)
                if (parts.size == 2 && parts[0] in listOf("openid", "openId", "open_id")) {
                    return parts[1]
                }
            }
        }
    } catch (_: Exception) {}
    val patterns = listOf(
        Regex("openid[=:](\\w+)"),
        Regex("openId[=:](\\w+)"),
        Regex("open_id[=:](\\w+)")
    )
    for (pattern in patterns) {
        pattern.find(input)?.groupValues?.getOrNull(1)?.let { return it }
    }
    return null
}
