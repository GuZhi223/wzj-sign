# Tasks

- [x] Task 1: 创建底部导航资源文件
  - [x] SubTask 1.1: 创建 `res/menu/nav_bottom_menu.xml`，定义 3 个导航项（签到、日志、设置）
  - [x] SubTask 1.2: 创建矢量图标资源 `ic_home.xml`、`ic_log.xml`、`ic_settings.xml`（使用 MD3 标准图标）
  - [x] SubTask 1.3: 在 `strings.xml` 中添加导航项标签字符串

- [x] Task 2: 重写主布局 activity_main.xml
  - [x] SubTask 2.1: 替换为 CoordinatorLayout + AppBarLayout + MaterialToolbar 结构
  - [x] SubTask 2.2: 添加 FragmentContainerView 作为内容区域
  - [x] SubTask 2.3: 添加 BottomNavigationView 绑定菜单资源
  - [x] SubTask 2.4: 添加 FloatingActionButton 锚定在 BottomNav 上方
  - [x] SubTask 2.5: 配置 `fitsSystemWindows` 和 padding 处理系统栏适配

- [x] Task 3: 创建 HomeFragment（签到首页）
  - [x] SubTask 3.1: 创建 `fragment_home.xml` 布局文件（状态卡片 + 账号列表 + 空状态引导）
  - [x] SubTask 3.2: 创建 `HomeFragment.java`，初始化签到状态显示和账号列表
  - [x] SubTask 3.3: 实现账号列表 RecyclerView，使用简化版列表项（仅显示 UIN 和状态）
  - [x] SubTask 3.4: 实现空状态引导 UI（无账号时显示提示和「去添加」按钮）
  - [x] SubTask 3.5: 将签到逻辑（startSignProcess/stopSignProcess）迁移至 HomeFragment 或通过回调触发

- [x] Task 4: 创建 LogFragment（日志页面）
  - [x] SubTask 4.1: 创建 `fragment_log.xml` 布局文件（RecyclerView + 底部操作栏）
  - [x] SubTask 4.2: 创建 `log_item.xml` 列表项布局（时间戳 + 级别标签 + 消息文本）
  - [x] SubTask 4.3: 创建 `LogAdapter.java`，按日志级别着色（info/warn/error）
  - [x] SubTask 4.4: 创建 `LogFragment.java`，绑定 SignLogger 监听器实现实时更新
  - [x] SubTask 4.5: 实现清空和导出按钮功能

- [x] Task 5: 创建 SettingsFragment（设置页面）
  - [x] SubTask 5.1: 创建 `fragment_settings.xml` 布局文件（分组卡片：签到参数、GPS、守护、备份）
  - [x] SubTask 5.2: 创建 `SettingsFragment.java`，加载和保存 PreferenceManager 配置
  - [x] SubTask 5.3: 迁移签到参数配置 UI（次数、间隔）到设置页
  - [x] SubTask 5.4: 迁移 GPS 设置 UI（开关、经纬度）到设置页
  - [x] SubTask 5.5: 迁移后台守护开关 UI 到设置页
  - [x] SubTask 5.6: 迁移数据备份 UI（导出/导入按钮）到设置页

- [x] Task 6: 创建账号管理 BottomSheet
  - [x] SubTask 6.1: 创建 `dialog_account_edit.xml` 布局文件（UIN、OpenID、经纬度表单）
  - [x] SubTask 6.2: 创建 `AccountBottomSheet.java` 继承 BottomSheetDialogFragment
  - [x] SubTask 6.3: 实现新增和编辑两种模式（通过 arguments 传递账号数据）
  - [x] SubTask 6.4: 实现保存回调，通知 HomeFragment 刷新列表

- [x] Task 7: 重构 MainActivity 为导航容器
  - [x] SubTask 7.1: 移除所有直接 UI 逻辑（按钮监听、表单处理等）
  - [x] SubTask 7.2: 实现 BottomNavigationView + Fragment 切换逻辑（使用 show/hide 避免重建）
  - [x] SubTask 7.3: 管理 FAB 状态（签到中切换图标和行为）
  - [x] SubTask 7.4: 保留全局共享资源管理（executorService、signRepository 等）
  - [x] SubTask 7.5: 更新 AppBar 标题随页面切换而变化

- [x] Task 8: 集成测试和 UI 调优
  - [x] SubTask 8.1: 验证页面切换状态保持正确
  - [x] SubTask 8.2: 验证签到流程完整可用
  - [x] SubTask 8.3: 验证日志实时更新和导出功能
  - [x] SubTask 8.4: 验证设置保存和加载正确
  - [x] SubTask 8.5: 验证账号 BottomSheet 增删改流程
  - [x] SubTask 8.6: 验证深色/浅色模式适配
  - [x] SubTask 8.7: 验证 FAB 在签到运行/停止状态下的视觉切换

# Task Dependencies
- Task 1（导航资源）和 Task 2（主布局）无依赖，可并行
- Task 3（HomeFragment）、Task 4（LogFragment）、Task 5（SettingsFragment）依赖 Task 1 和 Task 2，三者之间无依赖可并行
- Task 6（BottomSheet）依赖 Task 3（需要在首页触发）
- Task 7（MainActivity 重构）依赖 Task 2、3、4、5、6 全部完成
- Task 8（集成测试）依赖 Task 7 完成
