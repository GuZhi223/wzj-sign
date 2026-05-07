// ==========================================
// 1. 基础导入与全局变量
// ==========================================
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

String USER_DATA_FILE = "wzj_users.txt"; 
boolean isDaemonRunning = false;
StringBuilder logBuffer = new StringBuilder(); 

// MD3 配色方案
int COLOR_SURFACE = Color.parseColor("#FDFBFF");
int COLOR_PRIMARY = Color.parseColor("#6750A4");
int COLOR_ON_PRIMARY = Color.parseColor("#FFFFFF");
int COLOR_SECONDARY_CONTAINER = Color.parseColor("#E8DEF8");
int COLOR_ON_SECONDARY_CONTAINER = Color.parseColor("#1D192B");
int COLOR_OUTLINE = Color.parseColor("#79747E");
int COLOR_TEXT_MAIN = Color.parseColor("#1C1B1F");
int COLOR_ERROR = Color.parseColor("#B3261E");

// 注册插件入口菜单
addItem("🎓 极客微助教中控", "openUI");

void openUI(String groupUin, String uin, int chatType) {
    Activity a = getThreadActivity();
    if (a != null) {
        a.runOnUiThread(new Runnable() {
            public void run() { 
                showWindow(getThreadActivity()); 
            }
        });
    } else {
        Toast("唤出失败：无法获取应用上下文");
    }
}

// ==========================================
// 2. 底层工具类
// ==========================================
void uiLog(String msg) {
    String time = now().length() >= 19 ? now().substring(11, 19) : ""; 
    String logLine = "[" + time + "] " + msg;
    logBuffer.insert(0, logLine + "\n");
    if (logBuffer.length() > 3000) logBuffer.setLength(3000);
    info(msg); 
}

String extractRegex(String regex, String text) {
    Matcher m = Pattern.compile(regex).matcher(text);
    return m.find() ? m.group(1) : "";
}

Map loadUsers() {
    Map users = new HashMap();
    String path = getScriptPath() + "/" + USER_DATA_FILE;
    if (!exists(path)) return users; 
    String content = read(path); 
    if (content == null || content.equals("")) return users;
    String[] lines = content.split("\n");
    for (int i = 0; i < lines.length; i++) {
        String[] parts = lines[i].split("=");
        if (parts.length == 2) users.put(parts[0].trim(), parts[1].trim());
    }
    return users;
}

// ==========================================
// 3. API 核心请求引擎
// ==========================================
String getActiveSigns(String openid) {
    String result = "";
    HttpURLConnection conn = null;
    InputStream is = null;
    try {
        URL url = new URL("https://v18.teachermate.cn/wechat-api/v1/class-attendance/student/active_signs");
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0");
        conn.setRequestProperty("Openid", openid); 
        conn.setRequestProperty("Host", "v18.teachermate.cn");

        int code = conn.getResponseCode();
        if (code == 200) {
            is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            result = sb.toString();
        }
    } catch (Exception e) {
    } finally {
        try { if (is != null) is.close(); } catch (Exception e) {}
        if (conn != null) conn.disconnect();
    }
    return result;
}

String submitSign(String courseId, String signId, String openid, String isGps, String lon, String lat) {
    String result = "";
    HttpURLConnection conn = null;
    OutputStream os = null;
    InputStream is = null;
    try {
        URL url = new URL("https://v18.teachermate.cn/wechat-api/v1/class-attendance/student-sign-in");
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);

        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0");
        conn.setRequestProperty("Openid", openid); 
        conn.setRequestProperty("Content-Type", "application/json"); 
        conn.setRequestProperty("Host", "v18.teachermate.cn");

        String postLat = "0", postLon = "0";
        if (isGps.equals("1") && !lon.equals("") && !lat.equals("")) {
            try {
                postLon = String.valueOf(Double.parseDouble(lon) + (Math.random() * 40 - 20) * 0.000001).substring(0, 9);
                postLat = String.valueOf(Double.parseDouble(lat) + (Math.random() * 40 - 20) * 0.000001).substring(0, 8);
            } catch (Exception e) {}
            conn.setRequestProperty("lat", postLat);
            conn.setRequestProperty("lon", postLon);
        }

        String jsonBody = "";
        if (isGps.equals("1")) {
            jsonBody = "{\"courseId\":" + courseId + ",\"signId\":" + signId + ",\"lat\":\"" + postLat + "\",\"lon\":\"" + postLon + "\"}";
        } else {
            jsonBody = "{\"courseId\":" + courseId + ",\"signId\":" + signId + "}";
        }

        os = conn.getOutputStream();
        os.write(jsonBody.getBytes("UTF-8"));
        os.flush();

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            result = sb.toString();
        }
    } catch (Exception e) {
    } finally {
        try { if (os != null) os.close(); } catch (Exception e) {}
        try { if (is != null) is.close(); } catch (Exception e) {}
        if (conn != null) conn.disconnect();
    }
    return result;
}

// ==========================================
// 4. 并发引擎逻辑
// ==========================================
boolean doSign(String uin, String openid, String lon, String lat) {
    String activeJson = getActiveSigns(openid);
    if (activeJson == null || activeJson.equals("") || activeJson.equals("[]")) {
        return false;
    }
    if (activeJson.contains("登录信息失效")) {
        uiLog("❌ [" + uin + "] 警告：OpenID已过期，请重新抓包绑定！");
        return false;
    }

    String courseId = extractRegex("\"courseId\"\\s*:\\s*(\\d+)", activeJson);
    String signId = extractRegex("\"signId\"\\s*:\\s*(\\d+)", activeJson);
    String isGps = extractRegex("\"isGPS\"\\s*:\\s*(\\d+)", activeJson);

    if (courseId.equals("") || signId.equals("")) return false;

    uiLog("🎯 API 锁定了最新签到任务! Course: " + courseId + ", Sign: " + signId);

    String result = submitSign(courseId, signId, openid, isGps, lon, lat);
    uiLog("UIN[" + uin + "] 发射完成！服务器回包: " + result);

    return result.contains("studentRank") || result.contains("你已经签到成功");
}

void launchRadarMatrix(int count, int intervalMs) {
    final int fCount = count;
    final int fInterval = intervalMs;
    Map users = loadUsers();
    if (users.isEmpty()) {
        uiLog("❌ 未配置任何账号，雷达启动失败！");
        return;
    }
    if (users.size() > 3) uiLog("⚠️ 账号数超过3个，容易风控，仅执行前3个！");
    
    uiLog("🚀 正在为 " + Math.min(users.size(), 3) + " 个目标分配独立雷达...");
    
    int index = 0;
    for (Object key : users.keySet()) {
        if (index >= 3) break; 
        index++;
        
        final String u = (String) key;
        String[] cfg = ((String) users.get(key)).split(",");
        final String o = cfg[0];
        final String lon = cfg.length >= 2 ? cfg[1] : "0";
        final String lat = cfg.length >= 3 ? cfg[2] : "0";
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    uiLog("🎯 [" + u + "] 专属雷达已就绪...");
                    for (int i = 1; i <= fCount; i++) {
                        if (i == 1 || i == fCount || fInterval >= 1000 || i % 5 == 0) {
                            uiLog("📡 [" + u + "] 扫描波次: " + i + "/" + fCount);
                        }
                        
                        boolean isSuccess = doSign(u, o, lon, lat);
                        if (isSuccess) {
                            uiLog("🏆 [" + u + "] 绝杀成功！主动回收该雷达。");
                            break; 
                        }
                        if (i < fCount) sleep((long) fInterval);
                    }
                    uiLog("🏁 [" + u + "] 扫描任务结束。");
                } catch (Throwable t) {
                    uiLog("❌ [" + u + "] 线程异常: " + t.toString());
                }
            }
        }).start();
    }
}

void startDaemon() {
    if (isDaemonRunning) return;
    isDaemonRunning = true;
    uiLog("🛡️ 后台轮询守护进程已启动 (周期: 20s)");
    new Thread(new Runnable() {
        public void run() {
            while (isDaemonRunning) {
                try {
                    Map users = loadUsers();
                    int index = 0;
                    for (Object key : users.keySet()) {
                        if (index >= 3) break; 
                        index++;
                        String uin = (String) key;
                        String[] config = ((String) users.get(key)).split(",");
                        if (config.length >= 1) {
                            doSign(uin, config[0], config.length >= 2 ? config[1] : "0", config.length >= 3 ? config[2] : "0");
                        }
                    }
                    sleep(20000); 
                } catch (Exception e) {}
            }
            uiLog("🛡️ 后台轮询已安全终止。");
        }
    }).start();
}

void stopDaemon() { isDaemonRunning = false; }

// ==========================================
// 5. 可视化 UI 渲染模块
// ==========================================
GradientDrawable makeRoundRect(int color, int radiusPx) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color); d.setCornerRadius(radiusPx); return d;
}

GradientDrawable makeStrokeRect(int strokeColor, int radiusPx) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(Color.TRANSPARENT); d.setStroke(3, strokeColor); d.setCornerRadius(radiusPx);
    return d;
}

int dp(Context ctx, int d) { return (int)(d * ctx.getResources().getDisplayMetrics().density + 0.5f); }

EditText makeInput(Activity ctx, String hint) {
    EditText et = new EditText(ctx);
    et.setHint(hint); et.setTextSize(14); et.setTextColor(COLOR_TEXT_MAIN);
    et.setBackground(makeStrokeRect(COLOR_OUTLINE, dp(ctx, 8)));
    et.setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12));
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
    lp.setMargins(0, 0, 0, dp(ctx, 10));
    et.setLayoutParams(lp);
    return et;
}

EditText makeCompactInput(Activity ctx, String hint) {
    EditText et = new EditText(ctx);
    et.setHint(hint); et.setTextSize(12); et.setTextColor(COLOR_TEXT_MAIN);
    et.setBackground(makeStrokeRect(COLOR_OUTLINE, dp(ctx, 8)));
    et.setPadding(dp(ctx, 10), dp(ctx, 10), dp(ctx, 10), dp(ctx, 10));
    return et;
}

TextView makeBtn(Activity ctx, String text, int textColor, int bgColor) {
    TextView tv = new TextView(ctx);
    tv.setText(text); tv.setTextSize(14); tv.setTextColor(textColor);
    tv.setGravity(Gravity.CENTER); tv.setBackground(makeRoundRect(bgColor, dp(ctx, 24))); 
    tv.setPadding(0, dp(ctx, 12), 0, dp(ctx, 12));
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
    lp.setMargins(0, dp(ctx, 4), 0, dp(ctx, 4));
    tv.setLayoutParams(lp);
    return tv;
}

void addAccountCard(Activity ctx, LinearLayout container, String uin, String openid, String lon, String lat) {
    LinearLayout c = new LinearLayout(ctx);
    c.setOrientation(LinearLayout.VERTICAL);
    c.setBackground(makeStrokeRect(COLOR_SECONDARY_CONTAINER, dp(ctx, 12)));
    c.setPadding(dp(ctx, 16), dp(ctx, 16), dp(ctx, 16), dp(ctx, 16));
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
    lp.setMargins(0, 0, 0, dp(ctx, 12));
    c.setLayoutParams(lp);

    EditText etUin = makeInput(ctx, "QQ号 (UIN)"); etUin.setText(uin); etUin.setTag("uin");
    EditText etOpenId = makeInput(ctx, "OpenID (必填)"); etOpenId.setText(openid); etOpenId.setTag("openid");

    LinearLayout locRow = new LinearLayout(ctx); locRow.setOrientation(LinearLayout.HORIZONTAL);
    EditText etLon = makeCompactInput(ctx, "经度 (选填)"); etLon.setText(lon); etLon.setTag("lon");
    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, -2, 1); lp1.setMargins(0, 0, dp(ctx, 4), 0); etLon.setLayoutParams(lp1);
    
    EditText etLat = makeCompactInput(ctx, "纬度 (选填)"); etLat.setText(lat); etLat.setTag("lat");
    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, -2, 1); lp2.setMargins(dp(ctx, 4), 0, 0, 0); etLat.setLayoutParams(lp2);
    
    locRow.addView(etLon); locRow.addView(etLat);

    TextView btnDel = new TextView(ctx);
    btnDel.setText("❌ 移除此账号"); btnDel.setTextColor(COLOR_ERROR); btnDel.setTextSize(12);
    btnDel.setGravity(Gravity.RIGHT); btnDel.setPadding(0, dp(ctx, 8), 0, 0);
    
    btnDel.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            ((ViewGroup)v.getParent()).setVisibility(8); 
        }
    });

    c.addView(etUin); c.addView(etOpenId); c.addView(locRow); c.addView(btnDel);
    container.addView(c);
}

void showWindow(Activity act) {
    final Activity ctx = act; 
    Dialog d = new Dialog(ctx); d.requestWindowFeature(1); d.getWindow().setBackgroundDrawable(new ColorDrawable(0)); 
    ScrollView scroll = new ScrollView(ctx); scroll.setPadding(dp(ctx, 16), dp(ctx, 16), dp(ctx, 16), dp(ctx, 16));
    LinearLayout card = new LinearLayout(ctx); card.setOrientation(LinearLayout.VERTICAL);
    card.setBackground(makeRoundRect(COLOR_SURFACE, dp(ctx, 16))); card.setPadding(dp(ctx, 20), dp(ctx, 20), dp(ctx, 20), dp(ctx, 20));
    scroll.addView(card);

    TextView title = new TextView(ctx); title.setText("🎓 极客中控台 - 矩阵可视化"); title.setTextSize(20);
    title.getPaint().setFakeBoldText(true); title.setTextColor(COLOR_PRIMARY); title.setPadding(0, 0, 0, dp(ctx, 16)); card.addView(title);

    TextView titleId = new TextView(ctx); titleId.setText("📍 账号池配置 (防风控最多3个)"); titleId.setTextColor(COLOR_OUTLINE); titleId.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8)); card.addView(titleId);
    
    final LinearLayout accountContainer = new LinearLayout(ctx);
    accountContainer.setOrientation(LinearLayout.VERTICAL);
    card.addView(accountContainer);

    Map users = loadUsers();
    for (Object key : users.keySet()) {
        String u = (String) key;
        String[] cfg = ((String) users.get(key)).split(",");
        addAccountCard(ctx, accountContainer, u, cfg[0], cfg.length >= 2 ? cfg[1] : "", cfg.length >= 3 ? cfg[2] : "");
    }

    TextView btnAdd = makeBtn(ctx, "➕ 添加新账号", COLOR_PRIMARY, COLOR_SECONDARY_CONTAINER); card.addView(btnAdd);
    btnAdd.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            int visibleCount = 0;
            for(int i=0; i<accountContainer.getChildCount(); i++) {
                if(accountContainer.getChildAt(i).getVisibility() == 0) visibleCount++;
            }
            if(visibleCount >= 3) { Toast("系统安全熔断：最高允许维持3个并发账号"); return; }
            addAccountCard(ctx, accountContainer, "", "", "", "");
        }
    });

    TextView btnSave = makeBtn(ctx, "💾 保存配置到本地", COLOR_ON_PRIMARY, COLOR_PRIMARY); card.addView(btnSave);
    btnSave.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<accountContainer.getChildCount(); i++) {
                View child = accountContainer.getChildAt(i);
                if(child.getVisibility() == 0) { 
                    String u = ((EditText) child.findViewWithTag("uin")).getText().toString().trim();
                    String o = ((EditText) child.findViewWithTag("openid")).getText().toString().trim();
                    String ln = ((EditText) child.findViewWithTag("lon")).getText().toString().trim();
                    String lt = ((EditText) child.findViewWithTag("lat")).getText().toString().trim();
                    
                    if(!u.equals("") && !o.equals("")) {
                        if(ln.equals("")) ln = "0";
                        if(lt.equals("")) lt = "0";
                        sb.append(u).append("=").append(o).append(",").append(ln).append(",").append(lt).append("\n");
                    }
                }
            }
            write(getScriptPath() + "/" + USER_DATA_FILE, sb.toString());
            uiLog("✅ " + sb.toString().split("\n").length + " 个账号已编译进数据底层"); 
            Toast("可视化配置已落盘！");
        }
    });

    TextView titleSnipe = new TextView(ctx); titleSnipe.setText("⚡ 视距矩阵扫描舱"); titleSnipe.setTextColor(COLOR_OUTLINE); titleSnipe.setPadding(0, dp(ctx, 16), 0, dp(ctx, 8)); card.addView(titleSnipe);
    
    LinearLayout row = new LinearLayout(ctx); row.setOrientation(LinearLayout.HORIZONTAL);
    final EditText etCount = makeInput(ctx, "每号探测(次)"); etCount.setInputType(InputType.TYPE_CLASS_NUMBER); etCount.setText("20"); 
    LinearLayout.LayoutParams lpCount = new LinearLayout.LayoutParams(0, -2, 1); lpCount.setMargins(0, 0, dp(ctx, 5), dp(ctx, 10)); etCount.setLayoutParams(lpCount);
    
    final EditText etInterval = makeInput(ctx, "间隔(毫秒)"); etInterval.setInputType(InputType.TYPE_CLASS_NUMBER); etInterval.setText("300"); 
    LinearLayout.LayoutParams lpInterval = new LinearLayout.LayoutParams(0, -2, 1); lpInterval.setMargins(dp(ctx, 5), 0, 0, dp(ctx, 10)); etInterval.setLayoutParams(lpInterval);
    row.addView(etCount); row.addView(etInterval); card.addView(row);
    
    TextView btnSnipe = makeBtn(ctx, "🚀 发射多路矩阵雷达", COLOR_ON_PRIMARY, COLOR_PRIMARY); card.addView(btnSnipe);
    btnSnipe.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            int count = 20; int interval = 300;
            try { count = Integer.parseInt(etCount.getText().toString()); interval = Integer.parseInt(etInterval.getText().toString()); } catch (Exception e) {}
            if(interval < 100) { Toast("⚠️ 强制调整为300ms防拦截"); interval = 300; etInterval.setText("300"); }
            launchRadarMatrix(count, interval); 
            Toast("🚀 矩阵雷达已全部升空"); 
        }
    });

    TextView titleDaemon = new TextView(ctx); titleDaemon.setText("🛡️ 后台轮询与终端"); titleDaemon.setTextColor(COLOR_OUTLINE); titleDaemon.setPadding(0, dp(ctx, 16), 0, dp(ctx, 8)); card.addView(titleDaemon);
    final TextView btnToggle = makeBtn(ctx, isDaemonRunning ? "终止后台守护" : "激活后台守护", isDaemonRunning ? COLOR_ON_PRIMARY : COLOR_TEXT_MAIN, isDaemonRunning ? COLOR_ERROR : Color.parseColor("#E0E0E0")); card.addView(btnToggle);
    final TextView tvLog = new TextView(ctx); tvLog.setText(logBuffer.length() > 0 ? logBuffer.toString() : "System Ready..."); tvLog.setTextSize(10); tvLog.setTextColor(Color.parseColor("#49454F")); tvLog.setBackground(makeRoundRect(COLOR_SECONDARY_CONTAINER, dp(ctx, 8))); tvLog.setPadding(dp(ctx, 10), dp(ctx, 10), dp(ctx, 10), dp(ctx, 10));
    LinearLayout.LayoutParams lpLog = new LinearLayout.LayoutParams(-1, dp(ctx, 150)); lpLog.setMargins(0, dp(ctx, 12), 0, 0); tvLog.setLayoutParams(lpLog); card.addView(tvLog);

    btnToggle.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            if (isDaemonRunning) {
                stopDaemon(); btnToggle.setText("激活后台守护"); btnToggle.setBackground(makeRoundRect(Color.parseColor("#E0E0E0"), dp(ctx, 24))); btnToggle.setTextColor(COLOR_TEXT_MAIN);
            } else {
                startDaemon(); btnToggle.setText("终止后台守护"); btnToggle.setBackground(makeRoundRect(COLOR_ERROR, dp(ctx, 24))); btnToggle.setTextColor(COLOR_ON_PRIMARY);
            }
            tvLog.setText(logBuffer.toString());
        }
    });

    new Timer().schedule(new TimerTask() {
        public void run() { 
            Activity a = getThreadActivity(); 
            if (a != null) { 
                a.runOnUiThread(new Runnable() { 
                    public void run() { 
                        if (tvLog != null) tvLog.setText(logBuffer.toString()); 
                    }
                }); 
            } 
        }
    }, 1000, 2000);

    d.setContentView(scroll); d.getWindow().setLayout((int)(ctx.getResources().getDisplayMetrics().widthPixels * 0.9), -2); d.show();
}

void onUnload() {
    stopDaemon();
    info("微助教脚本容器已安全卸载");
}