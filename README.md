# 一键调试 / OneKey USB Debug (免 Root APK 版本)

这是一个方便实用的 Android 轻量级工具，旨在通过免 Root 的方式（仅需一次 ADB 授权），为 Android 开发者和高级用户提供瞬间开启或关闭 **USB 调试**、**开发者选项**，以及自动绕过定制系统（如小米 HyperOS/MIUI、OPPO ColorOS 等）安全限制的能力。

---

## ✨ 核心特性 | Features

- **🛡️ 免 Root 运行**：无需解锁 Bootloader 或获取 Root 权限，仅需在电脑端使用 ADB 授予一次权限即可永久生效。
- **⚡ 下拉栏快捷磁贴 (Quick Settings Tile)**：支持将“一键调试”磁贴添加到系统下拉通知栏，无需打开应用，随时随地秒级切换。
- **🤖 自动化广播支持 (Broadcast Receiver)**：支持 Tasker、MacroDroid 等自动化软件通过发送系统广播来触发控制：
  *   **广播 Action**: `com.lovelyterry.onekey.usbdebug.action.TOGGLE`
  *   **广播参数 (可选)**: 携带 Boolean 类型的 `enable` 参数，指定是开启 (`true`) 还是关闭 (`false`)，若不携带参数则在当前状态之间轮摆切换。
- **🔓 自动解除系统限制**：一键开启时，会自动配置并锁定以下高级安全设置，省去手动确认和插卡等繁琐限制：
  *   允许通过 ADB 进行模拟点击与输入（解除 MIUI/HyperOS 安全调试限制）
  *   禁用 ADB 应用安装的扫描和人工弹窗确认
  *   关闭各种权限监控（如 OPPO Permission Monitor 等）

---

## 📥 安装与授权指南 | Setup Guide

### 第一步：安装 APK
1. 使用电脑编译此项目生成 APK，或直接下载打包好的 APK 安装包。
2. 安装到手机中。

### 第二步：进行一次性 ADB 授权
为了让应用有权修改系统调试状态，需要通过电脑授权 `WRITE_SECURE_SETTINGS` 权限（该权限仅能通过 ADB 赋予，但授予后可跨重启永久有效）：

1. 手机打开系统的 **开发者选项**，开启 **USB 调试**（如果是小米/红米等定制系统，还需开启 **“USB 调试（安全设置）”**，即允许通过 ADB 修改系统设置）。
2. 将手机通过 USB 连接至电脑。
3. 在电脑终端（Windows 使用 PowerShell / CMD，Mac / Linux 使用 Terminal）中执行以下命令：
   ```bash
   adb shell pm grant com.onekey.usbdebug android.permission.WRITE_SECURE_SETTINGS
   ```
4. 授权成功后，打开手机上的“一键调试” App，此时会显示控制面板。

---

## 🛠️ 使用方法 | Usage

### 方式一：下拉快捷磁贴（最推荐）
1. 下拉手机通知栏，点击“编辑磁贴/排序”按钮。
2. 找到名为 **“一键USB调试”** 的磁贴，将其拖动到常用磁贴区域。
3. 此后，无论在任何界面，直接下拉状态栏点一下磁贴即可随时一键强开/强关调试及开发者选项。

### 方式二：应用内一键切换
1. 打开“一键调试” App。
2. 点击底部的 **“一键开启所有”** 或 **“一键关闭所有”** 大按钮。
3. 也可以单独控制“开发者选项”、“USB调试”以及“定制系统限制”的细分开关。

### 方式三：配合自动化工具
在 Tasker 或 MacroDroid 中，配置事件触发时发送如下意图（Intent）：
- **Action**: `com.onekey.usbdebug.action.TOGGLE`
- **Target**: `Broadcast Receiver`
- **Extras**: `enable:true` (开启) 或 `enable:false` (关闭)

---

## 📂 项目目录结构 | Folder Structure

*   `app/`：免 Root APK 项目的核心源代码与配置。
*   `settings.gradle` & `build.gradle`：标准的 Android Gradle 构建配置。

---

## 📄 许可证 | License

[MIT License](./LICENSE)
