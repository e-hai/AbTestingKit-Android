# AGENTS.md

给在本仓库工作的 AI Agent 的项目约定。人类说明见 [README.md](README.md)。

## 项目目标

封装 A/B Testing / Remote Config，降低切换真实后端的成本。业务代码只依赖 `:abtesting` 的 `AbTestingClient`；具体后端放在独立实现模块（当前为 `:abtesting-firebase`）。

## 模块边界（必须遵守）

| 模块 | 可依赖 | 禁止 |
|------|--------|------|
| `:abtesting` | Kotlin 协程、AndroidX Core | Firebase / 任何具体 Remote Config SDK；勿把 Fake 当生产实现 |
| `:abtesting-firebase` | `:abtesting`（`api`）、Firebase Config、coroutines-play-services | 改动 core API 语义来迁就 Firebase |
| `:sample` | `:abtesting-firebase` | 把演示专用逻辑塞进 library |

新增后端（如 GrowthBook、自研）时：新建 `abtesting-<vendor>` 模块，实现 `AbTestingProvider`，不要把 vendor SDK 引进 core。

## API 约定

- 对外门面：`AbTestingClient`（`install` + `setDefaults` + `fetchAndActivate` + getters）
- 扩展点：`AbTestingProvider`
- 配置：`AbTestingConfig`（目前主要是 `minimumFetchIntervalInSeconds`）
- 本地/测试：`FakeAbTestingProvider`（测试替身，含 `setRemoteValues` 模拟远端）
- Firebase：`FirebaseAbTestingProvider`；可用性用 `isFirebaseInitialized(context)`
- **不要**在库内调用 `FirebaseApp.initializeApp`；由宿主负责

v1 不要擅自加入：variant 模型、Analytics 曝光、Flow 监听、多 Provider 路由、Compose 专用 API——除非用户明确要求。
JitPack / Maven 发布已配置：改坐标只动 [`gradle.properties`](gradle.properties) 的 `GROUP` / `VERSION_NAME` / `OWNER` 占位；脚本见 [`gradle/jitpack-publish.gradle.kts`](gradle/jitpack-publish.gradle.kts) 与 [`jitpack.yml`](jitpack.yml)。勿给 `:sample` 加 `maven-publish`。

## Gradle / 工具链

- AGP 9.x：**built-in Kotlin** 已开启。library 的 `build.gradle.kts` 只应用 `com.android.library`，**不要**再加 `org.jetbrains.kotlin.android`（会报 kotlin extension 重复注册）
- sample 可继续用 `org.jetbrains.kotlin.plugin.compose`
- 依赖与版本只改 [`gradle/libs.versions.toml`](gradle/libs.versions.toml)
- Firebase：`platform(libs.firebase.bom)` + `libs.firebase.config`（非 `firebase-config-ktx`）
- 本地构建若找不到 JRE，可用 Android Studio JBR：  
  `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`

## 代码风格

- 包名：core `com.kit.abtesting`；firebase `com.kit.abtesting.firebase`
- 保持 API 面小；新增 public 类型前先确认是否能放在现有类型上扩展
- 测试：core 行为用 `FakeAbTestingProvider` + `AbTestingClient.reset()` 隔离；Firebase 模块首版不强制仪器测试
- 改动范围只覆盖任务需要的文件；不要顺手大重构或写无关 Markdown

## 常用命令

```bash
./gradlew :abtesting:testDebugUnitTest
./gradlew :abtesting-firebase:compileDebugKotlin
./gradlew :sample:assembleDebug
./gradlew :abtesting:publishReleasePublicationToMavenLocal \
  :abtesting-firebase:publishReleasePublicationToMavenLocal \
  --no-configuration-cache
```

## 验证清单（改 API 或 Provider 时）

1. core 不出现 Firebase import
2. `AbTestingClient` 未 install 时仍抛明确错误
3. `./gradlew :abtesting:testDebugUnitTest` 通过
4. sample 在无 `google-services.json` 时仍能编译，并用 `FakeAbTestingProvider` 验证流程
