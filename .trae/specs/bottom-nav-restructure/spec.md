# 底部导航多页面重构规范

## Why
当前应用将所有功能（签到状态、账号管理、签到控制、日志查看、后台守护、数据备份）全部堆叠在单个 ScrollView 页面中，导致信息层级扁平、操作路径不清晰、认知负荷过高。核心签到操作被埋在第三张卡片里，用户需要来回滚动才能完成一次签到流程。需要重构为 Material Design 3 底部导航架构，将功能合理分配到多个页面中。

## What Changes
- 将单页面架构重构为 BottomNavigationView + Fragment 多页面架构
- 新增 3 个 Fragment：签到页（HomeFragment）、日志页（LogFragment）、设置页（SettingsFragment）
- 签到页作为默认首页，聚焦核心操作：账号列表展示、签到状态、签到按钮
- 日志页独立展示完整日志，提供清空和导出功能
- 设置页整合签到参数配置、GPS 设置、后台守护、数据备份等低频操作
- 新建主布局使用 MD3 Scaffold 模式（AppBar + Content + BottomNav + FAB）
- 为账号添加操作引入 BottomSheetDialogFragment 提升输入体验
- **BREAKING** `activity_main.xml` 将被完全重写
- **BREAKING** `MainActivity.java` 中的 UI 逻辑将被拆分到各 Fragment

## Impact
- Affected specs: 用户界面、交互体验、MD3 UI 重新设计
- Affected code:
  - `activity_main.xml` — 完全重写为 Scaffold + FragmentContainerView
  - `MainActivity.java` — 精简为宿主 Activity，仅管理导航和 Fragment 切换
  - 新增 `HomeFragment.java` / `LogFragment.java` / `SettingsFragment.java`
  - 新增对应 3 个 Fragment 布局文件
  - 新增 `nav_bottom_menu.xml` 底部导航菜单资源
  - 新增 `ic_home.xml` / `ic_log.xml` / `ic_settings.xml` 矢量图标
  - 可选新增 `AccountBottomSheet.java` 及其布局（账号添加/编辑）

## ADDED Requirements

### Requirement: 底部导航架构
系统 SHALL 提供 MD3 BottomNavigationView 底部导航：
- 包含 3 个导航项：签到（首页）、日志、设置
- 使用 Material Design 3 图标和标签
- 支持页面切换时保持各页面状态
- 底部导航栏使用 `colorSurfaceContainer` 背景

#### Scenario: 页面切换
- **WHEN** 用户点击底部导航项
- **THEN** 切换到对应 Fragment 页面，当前项高亮显示

### Requirement: 签到首页（HomeFragment）
系统 SHALL 提供聚焦核心操作的签到首页：
- 顶部显示签到状态卡片（就绪/运行中/已完成）
- 中部显示已配置账号列表（仅展示 UIN 和状态，不显示完整表单）
- 底部使用 FAB（FloatingActionButton）作为主签到操作入口
- 签到运行中时 FAB 转为停止按钮
- 账号为空时显示引导提示

#### Scenario: 执行签到
- **WHEN** 用户点击 FAB 按钮
- **THEN** 开始签到流程，FAB 变为停止状态，状态卡片实时更新进度

#### Scenario: 账号为空引导
- **WHEN** 没有已配置的账号
- **THEN** 显示空状态引导，提示用户前往设置页添加账号

### Requirement: 日志页面（LogFragment）
系统 SHALL 提供独立的日志查看页面：
- 使用 RecyclerView 展示结构化日志列表（而非单一 TextView）
- 日志条目按级别使用不同颜色标识（info/warn/error）
- 支持自动滚动到最新日志
- 底部提供清空和导出操作按钮
- 日志内容可水平滚动以支持长消息

#### Scenario: 日志实时更新
- **WHEN** 签到流程产生新日志
- **THEN** 日志页自动追加并滚动到最新条目

### Requirement: 设置页面（SettingsFragment）
系统 SHALL 提供整合的设置页面，包含以下区域：
- **签到参数**：签到次数、间隔时间配置
- **模拟定位**：GPS 开关、经纬度坐标输入
- **后台守护**：前台服务开关及状态显示
- **数据管理**：导出/导入数据备份按钮
- 各设置区域使用 MD3 Preference 风格或卡片分组

#### Scenario: 修改签到参数
- **WHEN** 用户修改签到次数或间隔
- **THEN** 保存到 SharedPreferences，下次签到自动使用新参数

### Requirement: 账号管理 BottomSheet
系统 SHALL 使用 BottomSheetDialogFragment 管理账号：
- 首页账号列表点击「添加」按钮时弹出
- 包含 UIN、OpenID、经纬度输入表单
- 支持编辑已有账号
- 保存后关闭 BottomSheet 并刷新列表
- 已有 3 个账号时禁用添加并提示

#### Scenario: 添加新账号
- **WHEN** 用户点击首页的添加账号按钮
- **THEN** 弹出底部弹窗表单，填写完成后保存并刷新列表

### Requirement: MD3 Scaffold 布局
系统 SHALL 使用 MD3 推荐的 Scaffold 布局模式：
- CoordinatorLayout + AppBarLayout + MaterialToolbar 作为顶部
- FragmentContainerView 作为内容区域
- BottomNavigationView 固定在底部
- FAB 锚定在 BottomNav 上方右侧
- 使用 `fitsSystemWindows` 处理系统栏适配

#### Scenario: 布局适配
- **WHEN** 在不同屏幕尺寸上显示
- **THEN** BottomNav 和 FAB 正确适配，内容区域不被遮挡

## MODIFIED Requirements

### Requirement: MainActivity 职责
MainActivity 需要从「全功能宿主」精简为「导航容器」：
- 移除所有直接 UI 逻辑（按钮点击、表单处理等）
- 仅负责初始化 BottomNavigation + Fragment 切换
- 管理全局状态（isSignRunning）和共享资源（executorService、signRepository 等）
- 通过接口或 ViewModel 向 Fragment 提供共享数据

### Requirement: 账号数据展示
账号列表展示方式需要调整：
- 首页列表项仅显示 UIN（QQ号）和签到状态，不再显示完整表单
- 完整的账号编辑功能移至 BottomSheet 或设置页

## REMOVED Requirements

### Requirement: 单页面 ScrollView 布局
**Reason**: 被 Bottom Navigation 多页面架构替代
**Migration**: 所有功能分配到 HomeFragment、LogFragment、SettingsFragment 三个页面

### Requirement: 内嵌日志 TextView
**Reason**: 改为 LogFragment 中的 RecyclerView 结构化展示
**Migration**: 日志展示逻辑从 MainActivity 移至 LogFragment
