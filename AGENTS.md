# AGENTS.md

给在本仓库工作的 AI Agent 的项目约定与架构指南。人类说明见 [README.md](README.md)。

## 项目目标

封装 A/B Testing / Remote Config，解耦业务逻辑与具体供应商 SDK（Firebase、GrowthBook 或自研系统）。业务代码只依赖 `:abtesting` 的 `AbTestingClient` 单例门面；具体供应商 SDK 封装在独立实现模块（如 `:abtesting-firebase`）。

## 模块边界（必须严格遵守）

| 模块 | 可依赖 | 严禁依赖 |
| :--- | :--- | :--- |
| `:abtesting` (Core) | Kotlin 协程、AndroidX Core | Firebase 或任何具体厂商 SDK；勿把 Fake 当生产实现 |
| `:abtesting-firebase` | `:abtesting`（`api`）、Firebase BoM、`firebase-config`、`coroutines-play-services` | 改动 Core API 契约来迁就 Firebase 特性 |
| `:sample` | `:abtesting-firebase`、Jetpack Compose | 将演示业务逻辑侵入 Library 模块 |

> **新增后端规范**：新建 `abtesting-<vendor>` 模块并实现 `AbTestingProvider`，严禁在 Core 模块中引入 vendor 依赖。

## API 约定与扩展规范

- **全局门面**：`AbTestingClient`（提供 `install`、`setDefaults`、`fetchAndActivate` 与类型化读取器 `getBoolean`、`getString`、`getLong`、`getDouble`、`reset`）
- **抽象扩展点**：`AbTestingProvider`（异步 `setDefaults`、异步 `fetchAndActivate` 及同步读取操作）
- **通用配置**：`AbTestingConfig`（目前包含 `minimumFetchIntervalInSeconds`）
- **测试替身**：`FakeAbTestingProvider`（测试专用，带 `setRemoteValues` 模拟远端推送，严格模拟「默认值 -> 激活后覆盖」时序）
- **控制反转**：库内部**绝不**私自初始化 `FirebaseApp.initializeApp`，由宿主应用自行配置。使用 `isFirebaseInitialized(context)` 探测状态以支持安全降级。

## 发布与构建约定

- **GitHub Packages 坐标**：`com.kit.abtesting`（全小写，避免 GitHub Packages 大小写解析异常），发布版本管理在 [`gradle.properties`](gradle.properties)（当前版本 `1.0.0`）。
- **发布脚本与 CI/CD**：[`gradle/maven-publish.gradle.kts`](gradle/maven-publish.gradle.kts) 统一挂载至各 Library 模块，配合 [`.github/workflows/publish.yml`](.github/workflows/publish.yml) 实现 Tag 自动发布至 GitHub Packages。禁止对 `:sample` 应用 `maven-publish`。
- **AGP 9 工具链**：开启 **Built-in Kotlin** 支持。Library 模块仅应用 `com.android.library`，勿重复引入 `org.jetbrains.kotlin.android`。
- **版本目录**：统一在 [`gradle/libs.versions.toml`](gradle/libs.versions.toml) 声明版本。

## 代码与测试风格

- 包名空间：Core 模块为 `com.kit.abtesting`，Firebase 模块为 `com.kit.abtesting.firebase`。
- 状态隔离：单测前后必须通过 `AbTestingClient.reset()` 清除单例状态。
- 防御性异常：在未执行 `install()` 时调用 API 需抛出明确的异常说明。

## 常用命令

```bash
# 运行单元测试
./gradlew :abtesting:testDebugUnitTest

# 编译各模块
./gradlew :abtesting-firebase:compileDebugKotlin
./gradlew :sample:assembleDebug

# 本地发布测试
./gradlew :abtesting:publishReleasePublicationToMavenLocal \
  :abtesting-firebase:publishReleasePublicationToMavenLocal \
  --no-configuration-cache
```

## 验证清单（变更 API 或 Provider 时）

1. Core 模块不引入任何第三方 SDK 依赖。
2. `AbTestingClient` 未 `install` 时抛出有具体解决引导的异常。
3. `./gradlew :abtesting:testDebugUnitTest` 完整通过。
4. Sample 模块在缺少 `google-services.json` 时仍能自动降级运行。
