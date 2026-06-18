# Znz 指南针 v2.0

优化版指南针应用，基于原始 Znz.apk 重构。

## 优化内容

- **指针叠加旋转**: 自定义 Canvas 绘制罗盘面 + 平滑动画
- **传感器融合**: 加速度计 + 磁力计互补滤波
- **真北校正**: 磁偏角自动补偿
- **Material Design 3**: 深色/浅色主题自适应
- **GPS 定位**: 经纬度实时显示
- **中文方位**: 北/东北/东/东南/南/西南/西/西北
- **省电**: 传感器按生命周期管理

## 构建方法

### 方法一：本地构建（需要 JDK 17 + Android Studio）

1. 用 Android Studio 打开本项目目录
2. 等待 Gradle sync 完成
3. 点击 Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK 生成在 \pp/build/outputs/apk/debug/\

### 方法二：GitHub Actions 云端构建（无需本地环境）

1. 将本代码推送到 GitHub 仓库
2. 进入 GitHub → Actions → Build APK → Run workflow
3. 等待构建完成后下载 APK Artifact

## 系统要求

- Android 5.0 (API 21) 及以上
- 需要加速度计和磁力传感器
- 可选位置权限用于经纬度显示
