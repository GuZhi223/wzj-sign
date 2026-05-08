# 微助教自动签到

一款基于微助教微信 API 的 Android 原生自动签到工具，支持普通签到和 GPS 模拟定位签到。

## 开发背景

我们的历史老师规定：每次签到前三名完成签到的同学可以加 0.1 平时分。为了不错过任何一次签到机会，开发了这款 Android 原生应用，自动检测签到任务并第一时间完成签到。

本项目基于以下上游仓库演进：
- [zn-cn/wzj-sign-in-weixin](https://github.com/zn-cn/wzj-sign-in-weixin)（Golang + 微信公众号版本）
- [IntZhx/wzj_signin](https://github.com/IntZhx/wzj_signin)（Go + Web 页面版本）

## 免责声明

- 本项目与"微助教 / teachermate"无任何关联。
- 本应用仅供学习交流使用，请勿用于任何违反学校规定或法律法规的用途。
- 使用本应用所产生的一切后果由用户自行承担。

## 功能

- **多账号管理**：支持同时配置最多 3 个账号，数据持久化存储于本地数据库
- **自动轮询签到**：前台服务每 5 秒轮询一次可签到列表，检测到签到任务后自动提交
- **普通签到**：自动完成普通签到
- **GPS 模拟定位签到**：支持自定义经纬度，签到时自动对坐标进行随机偏移处理，防止坐标完全一致
- **签到通知**：签到成功后通过系统通知提醒
- **签到日志**：实时记录签到过程，方便排查问题
- **数据备份与恢复**：支持将账号数据导出为 JSON 文件，也可从备份文件导入
- **MIUI 风格界面**：采用 [Miuix](https://github.com/compose-miuix-ui/miuix) Compose UI 库，MIUI/HyperOS 设计风格，支持深色模式

## 技术栈

| 类别 | 技术 |
|---|---|
| 语言 | Kotlin + Java (业务逻辑层) |
| 最低版本 | Android 7.0 (API 24) |
| UI 框架 | Jetpack Compose + [Miuix](https://github.com/compose-miuix-ui/miuix) |
| 网络请求 | Retrofit2 + OkHttp3 |
| 数据解析 | Gson |
| 本地存储 | Room 数据库 + SharedPreferences |
| 后台服务 | 前台服务 (Foreground Service) + WakeLock |

## 运行前准备

- Android Studio Ladybug (2024.2) 或更高版本
- JDK 17+
- Android SDK 37
- 一台 Android 7.0+ 的设备或模拟器

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/GuZhi223/wzj-sign.git
cd wzj-sign
```

### 2. 使用 Android Studio 打开项目

用 Android Studio 打开项目根目录，等待 Gradle 同步完成。

### 3. 获取 OpenID

OpenID 是调用微助教 API 的必要凭证，有两种获取方式：

**方式一：手速复制网址（推荐）**

1. 在微信中打开微助教，进入课程页面
2. 当老师发起签到时，微助教会跳转到签到页面
3. 趁微信还没完全加载页面的时候，快速点击右上角菜单 → 复制链接
4. 得到类似这样的网址：`https://v18.teachermate.cn/wechat-pro-ssr/?openid=03612c0f398dbc98485de4e5574d74ef&from=wzj`
5. 直接将完整网址粘贴到应用中，会自动提取 OpenID

**方式二：抓包获取**

1. 使用抓包工具（如 HttpCanary、Charles 等）捕获网络请求
2. 在请求 Header 中找到 `Openid` 字段，复制其值

> **注意**：OpenID 有时效性，过期后需要重新获取。复制网址时手速要快，微信加载完成后地址栏会隐藏。

### 4. 运行

连接设备或启动模拟器，点击 Android Studio 的 Run 按钮即可。

## 使用指南

### 添加账号

1. 打开应用，进入"签到"页面
2. 点击"添加"按钮
3. 输入备注（用于标识账号）和 OpenID
4. OpenID 支持直接粘贴完整网址，应用会自动提取其中的 OpenID
5. 点击保存

### 配置 GPS 坐标（可选）

1. 进入"设置"页面
2. 开启"模拟定位"开关
3. 输入目标地点的经纬度，可通过以下坐标拾取工具获取：
   - [高德坐标拾取器](https://lbs.amap.com/console/show/picker)（推荐，国内常用）
   - [腾讯地图坐标拾取器](https://lbs.qq.com/getPoint/)

   > **注意**：请确认拾取到的坐标与微助教使用的坐标系一致（通常为 GCJ-02），否则定位可能有偏差。

### 开始自动签到

1. 在"签到"页面确认账号已添加
2. 点击右下角的播放按钮
3. 应用将持续监控签到任务
4. 签到成功后会收到系统通知

### 后台守护

1. 进入"设置"页面
2. 开启"后台守护"开关
3. 应用将以前台服务形式运行，即使关闭应用界面也能持续监控签到

### 查看日志

进入"日志"页面可查看所有签到记录，包括签到成功、失败、错误等信息。

### 数据备份

进入"设置"页面：
- 点击"导出数据"将账号信息备份到 `Documents/WzjSignBackup/` 目录
- 点击"导入数据"从最近的备份文件恢复账号信息

## 签到原理

1. 应用通过微助教微信 API（`https://v18.teachermate.cn/wechat-api/`）轮询当前是否有活跃的签到任务
2. 检测到签到任务后，根据签到类型（普通/GPS）组装请求数据
3. GPS 签到时，会对用户设置的坐标进行随机偏移（±0.000020），并截断为 5 位小数
4. 向签到接口提交请求，完成自动签到

## 项目结构

```
app/src/main/java/com/wzj/sign/
├── data/                          # 数据层 (Java)
│   ├── dao/AccountDao.java        # Room DAO
│   ├── entity/AccountEntity.java  # 数据库实体
│   ├── AccountRepository.java     # 数据仓库
│   ├── AppDatabase.java           # Room 数据库
│   ├── BackupManager.java         # 数据备份管理
│   ├── DataConverter.java         # 数据转换工具
│   └── PreferenceManager.java     # 偏好设置管理
├── log/                           # 日志模块 (Java)
│   ├── LogEntry.java              # 日志条目
│   └── SignLogger.java            # 日志记录器
├── network/                       # 网络层 (Java)
│   ├── model/                     # 请求/响应模型
│   ├── NetworkUtils.java          # 网络工具
│   ├── RetrofitClient.java        # Retrofit 客户端
│   ├── SignRepository.java        # 签到业务仓库
│   └── TeachermateApi.java        # API 接口定义
├── service/                       # 后台服务 (Java)
│   ├── NotificationHelper.java    # 通知管理
│   ├── ServiceManager.java        # 服务管理
│   └── SignForegroundService.java # 前台签到服务
├── ui/                            # UI 层 (Kotlin + Compose)
│   ├── home/
│   │   ├── HomeScreen.kt          # 首页（签到管理）
│   │   └── AccountCard.kt         # 账号卡片组件
│   ├── log/
│   │   └── LogScreen.kt           # 日志页面
│   ├── settings/
│   │   └── SettingsScreen.kt      # 设置页面
│   ├── about/
│   │   └── AboutScreen.kt         # 关于页面
│   └── components/
│       └── AccountBottomSheet.kt  # 账号编辑弹窗
├── MainActivity.kt                # 入口 Activity + 状态管理
├── Account.java                   # 账号模型
└── MyApplication.java             # Application
```

## 常见问题

### OpenID 过期

OpenID 有时效性，过期后签到会失败。请重新抓包获取新的 OpenID 并更新账号信息。

### 签到失败

1. 检查 OpenID 是否有效
2. 检查网络连接是否正常
3. 查看日志页面获取详细错误信息

### 后台服务被系统杀死

部分 Android 系统会对后台应用进行限制。建议：
1. 在系统设置中将本应用加入电池优化白名单
2. 开启自启动权限
3. 锁定应用在最近任务列表中

## 相关项目

- [zn-cn/wzj-sign-in-weixin](https://github.com/zn-cn/wzj-sign-in-weixin) - Golang + 微信公众号版本，通过微信公众号交互实现自动签到
- [IntZhx/wzj_signin](https://github.com/IntZhx/wzj_signin) - Go + Web 页面版本，通过浏览器管理签到任务
- [Azuka753/wzj_sign_public](https://github.com/Azuka753/wzj_sign_public) - 上游项目
- [compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix) - MIUI/HyperOS 风格的 Compose UI 库

## 开源协议

本项目基于 [MIT License](LICENSE) 开源，与上游项目保持一致。
