# TCIC Android Simple Demo

这是一个基于WebView的Android原生应用，用于展示Web与TCIC SDK的交互功能。

## 功能特性

- 使用WebView加载网页内容
- JavaScript与原生代码交互
- 监听网页中的 `gotoHomePage` 方法调用
- 支持网络访问权限

## 运行方法

### 前提条件

- Android Studio 最新版本
- JDK 11或更高版本
- Android SDK 34

### 编译运行

1. 打开Android Studio
2. 选择 "Open an Existing Project"
3. 选择本项目根目录
4. 等待Gradle同步完成
5. 连接Android设备或启动模拟器
6. 点击运行按钮（▶️）或使用快捷键 `Ctrl + R`

### 命令行运行

```bash
# 授予gradlew执行权限（首次运行）
chmod +x gradlew

# 编译并安装到设备
./gradlew installDebug

# 清理构建
./gradlew clean

# 构建Release版本
./gradlew assembleRelease
```

## WebView功能说明

### JavaScript接口

应用提供了以下JavaScript接口：

```javascript
// 在网页中调用原生方法
Android.gotoHomePage();
```

### 配置说明

1. **修改加载URL**：在 `MainActivity.kt` 中修改 `webView.loadUrl("https://example.com")` 为您的实际URL
2. **自定义处理逻辑**：在 `WebAppInterface.gotoHomePage()` 方法中添加您的业务逻辑

### 权限配置

应用已配置网络访问权限：
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 项目结构

```
app/
├── src/main/
│   ├── java/com/example/tcic_android_simple_demo/
│   │   └── MainActivity.kt    # 主Activity，包含WebView实现
│   ├── res/                   # 资源文件
│   └── AndroidManifest.xml    # 应用配置清单
```

## 技术支持

如有问题，请检查：
1. 网络连接是否正常
2. 加载的URL是否可访问
3. Android设备是否开启USB调试模式

## 版本信息

- 编译版本：Android SDK 34
- 最低支持版本：Android 8.0 (API 24)
- 开发语言：Kotlin