# Checklist

## 底部导航
- [x] BottomNavigationView 正确显示 3 个导航项（签到、日志、设置）
- [x] 每个导航项有正确的 MD3 矢量图标
- [x] 点击导航项可正确切换 Fragment
- [x] 当前选中项正确高亮
- [x] 底部导航栏使用 `colorSurfaceContainer` 背景色

## 签到首页（HomeFragment）
- [x] 签到状态卡片正确显示当前状态
- [x] 账号列表仅展示 UIN 和状态信息（非完整表单）
- [x] 无账号时显示空状态引导提示
- [x] 空状态引导包含跳转到设置页的按钮
- [x] FAB 按钮正确锚定在 BottomNav 上方
- [x] 签到前 FAB 显示开始图标，签到中显示停止图标
- [x] 点击 FAB 可触发签到/停止操作
- [x] 签到进度实时更新状态卡片

## 日志页面（LogFragment）
- [x] 日志使用 RecyclerView 结构化展示
- [x] 日志条目包含时间戳、级别标签、消息内容
- [x] 不同级别日志使用不同颜色标识（info/warn/error）
- [x] 新日志自动追加并滚动到最新
- [x] 清空按钮功能正常
- [x] 导出按钮功能正常

## 设置页面（SettingsFragment）
- [x] 签到参数（次数、间隔）正确加载和保存
- [x] GPS 开关及经纬度坐标正确加载和保存
- [x] 后台守护开关正确显示服务状态
- [x] 后台守护开关切换功能正常
- [x] 数据导出按钮功能正常
- [x] 数据导入按钮功能正常
- [x] 各设置区域使用卡片分组，视觉层级清晰

## 账号管理 BottomSheet
- [x] 点击添加按钮弹出 BottomSheet
- [x] BottomSheet 包含 UIN、OpenID、经纬度输入表单
- [x] 保存后 BottomSheet 关闭并刷新列表
- [x] 编辑已有账号时表单回填正确数据
- [x] 已有 3 个账号时禁用添加并显示提示

## MainActivity 重构
- [x] MainActivity 不再包含直接 UI 交互逻辑
- [x] 全局共享资源（executorService、signRepository 等）正确管理
- [x] AppBar 标题随页面切换正确更新
- [x] FAB 状态随签到运行状态正确切换
- [x] 生命周期管理正确（onDestroy 释放资源）

## MD3 规范
- [x] 整体布局使用 MD3 Scaffold 模式
- [x] BottomNav 使用 MD3 标准样式
- [x] FAB 使用 MD3 标准样式
- [x] 页面间距和留白符合 MD3 4dp 网格规范
- [x] 浅色/深色模式下色彩显示正确
- [x] 组件使用 MD3 主题色彩（Primary、Secondary、Surface 等）

## 兼容性
- [x] 页面切换时各 Fragment 状态保持正确
- [x] 横竖屏切换不丢失状态
- [x] 系统返回键行为正确（BottomSheet 先关闭，再退出应用）
