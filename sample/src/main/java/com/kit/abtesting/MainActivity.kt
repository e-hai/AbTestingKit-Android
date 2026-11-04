package com.kit.abtesting

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kit.abtesting.firebase.FirebaseAbTestingProvider
import com.kit.abtesting.firebase.isFirebaseInitialized
import com.kit.abtesting.ui.theme.AbTestingKitTheme
import com.kit.abtesting.ui.theme.Danger
import com.kit.abtesting.ui.theme.DangerContainer
import com.kit.abtesting.ui.theme.Success
import com.kit.abtesting.ui.theme.SuccessContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Demo entry point.
 *
 * Without `google-services.json` + Google Services plugin, Firebase is unavailable and the
 * sample falls back to [FakeAbTestingProvider] for local API verification only.
 *
 * To use Firebase Remote Config:
 * 1. Add `google-services.json` under `sample/`
 * 2. Apply `com.google.gms.google-services` in `sample/build.gradle.kts`
 * 3. Relaunch — the sample will install [FirebaseAbTestingProvider] automatically
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbTestingKitTheme {
                AbTestingDemoApp()
            }
        }
    }
}

private val demoDefaults = mapOf(
    "feature_x" to false,
    "welcome_message" to "来自本地默认值的问候",
    "max_items" to 10L,
    "score_threshold" to 0.75,
)

private data class DemoState(
    val provider: String = "",
    val activated: Boolean = false,
    val featureX: Boolean = false,
    val welcome: String = "",
    val maxItems: Long = 0L,
    val threshold: Double = 0.0,
    val fetchCount: Int = 0,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbTestingDemoApp() {
    val appContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var booting by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(DemoState()) }
    var contentKey by remember { mutableIntStateOf(0) }

    fun readState(provider: String, activated: Boolean, fetchCount: Int): DemoState =
        DemoState(
            provider = provider,
            activated = activated,
            featureX = AbTestingClient.getBoolean("feature_x", false),
            welcome = AbTestingClient.getString("welcome_message", ""),
            maxItems = AbTestingClient.getLong("max_items", 0L),
            threshold = AbTestingClient.getDouble("score_threshold", 0.0),
            fetchCount = fetchCount,
        )

    LaunchedEffect(appContext) {
        val providerName = installProvider(appContext)
        AbTestingClient.setDefaults(demoDefaults)
        delay(350)
        val activated = AbTestingClient.fetchAndActivate()
        state = readState(providerName, activated, fetchCount = 1)
        contentKey = 1
        booting = false
    }

    fun refresh() {
        if (refreshing || booting) return
        scope.launch {
            refreshing = true
            val activated = AbTestingClient.fetchAndActivate()
            delay(280)
            state = readState(state.provider, activated, state.fetchCount + 1)
            contentKey += 1
            refreshing = false
            snackbar.showSnackbar(
                message = if (activated) "拉取并激活成功" else "拉取完成（无新配置激活）",
            )
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AbTestingKit", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "远端配置演练场",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding),
        ) {
            AnimatedContent(
                targetState = booting,
                transitionSpec = {
                    fadeIn(tween(280)) togetherWith fadeOut(tween(180))
                },
                label = "boot",
            ) { loading ->
                if (loading) {
                    BootingPane(modifier = Modifier.fillMaxSize())
                } else {
                    DemoContent(
                        state = state,
                        contentKey = contentKey,
                        refreshing = refreshing,
                        onRefresh = ::refresh,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BootingPane(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在安装 Provider…",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "设置本地默认值 → 拉取并激活",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DemoContent(
    state: DemoState,
    contentKey: Int,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProviderBanner(provider = state.provider, activated = state.activated, fetchCount = state.fetchCount)

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(320)) + slideInVertically(
                initialOffsetY = { it / 8 },
                animationSpec = tween(360, easing = FastOutSlowInEasing),
            ),
        ) {
            WelcomeHero(message = state.welcome, contentKey = contentKey)
        }

        FeatureFlagCard(enabled = state.featureX, contentKey = contentKey)

        ConfigMetrics(
            maxItems = state.maxItems,
            threshold = state.threshold,
            contentKey = contentKey,
        )

        Button(
            onClick = onRefresh,
            enabled = !refreshing,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            if (refreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("正在拉取…")
            } else {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("拉取并激活配置")
            }
        }

        Text(
            text = "配置项：feature_x · welcome_message · max_items · score_threshold",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun ProviderBanner(provider: String, activated: Boolean, fetchCount: Int) {
    val isFirebase = provider == "firebase"
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isFirebase) Icons.Rounded.Cloud else Icons.Rounded.Science,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isFirebase) "Firebase 远端配置" else "Fake 模拟 Provider (测试)",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (isFirebase) {
                        "真实后端 · 第 $fetchCount 次拉取"
                    } else {
                        "本地测试替身 · 第 $fetchCount 次拉取"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(
                label = if (activated) "已激活" else "未激活",
                container = if (activated) SuccessContainer else MaterialTheme.colorScheme.surfaceVariant,
                content = if (activated) Success else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WelcomeHero(message: String, contentKey: Int) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "欢迎文案 (welcome_message)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedContent(
                targetState = contentKey to message,
                transitionSpec = {
                    (fadeIn(tween(280)) + slideInVertically { it / 6 }) togetherWith fadeOut(tween(160))
                },
                label = "welcome",
            ) { (_, text) ->
                Text(
                    text = text.ifBlank { "—" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun FeatureFlagCard(enabled: Boolean, contentKey: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("功能开关 (feature_x)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(if (enabled) "已开启" else "已关闭", style = MaterialTheme.typography.titleMedium)
            }
            AnimatedContent(
                targetState = contentKey to enabled,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "flag",
            ) { (_, on) ->
                StatusChip(
                    label = if (on) "开启" else "关闭",
                    container = if (on) SuccessContainer else DangerContainer,
                    content = if (on) Success else Danger,
                    leading = {
                        if (on) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ConfigMetrics(maxItems: Long, threshold: Double, contentKey: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MetricTile(
            key = "最大条目 (max_items)",
            value = maxItems.toString(),
            contentKey = contentKey,
            modifier = Modifier.weight(1f),
        )
        ThresholdTile(
            threshold = threshold,
            contentKey = contentKey,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricTile(
    key: String,
    value: String,
    contentKey: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(key, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = contentKey to value,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "metric-$key",
            ) { (_, text) ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun ThresholdTile(
    threshold: Double,
    contentKey: Int,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = threshold.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "threshold",
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "阈值分数 (score_threshold)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = contentKey to threshold,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "threshold-text",
            ) { (_, value) ->
                Text(
                    text = "%.2f".format(value),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.tertiary),
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    container: Color,
    content: Color,
    leading: (@Composable () -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            leading?.invoke()
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}

private fun installProvider(appContext: Context): String {
    return if (isFirebaseInitialized(appContext)) {
        AbTestingClient.install(
            FirebaseAbTestingProvider(
                context = appContext,
                config = AbTestingConfig(minimumFetchIntervalInSeconds = 0L),
            ),
        )
        "firebase"
    } else {
        val fake = FakeAbTestingProvider().apply {
            setRemoteValues(
                mapOf(
                    "feature_x" to true,
                    "welcome_message" to "来自模拟远端配置的问候",
                    "max_items" to 42L,
                    "score_threshold" to 0.9,
                ),
            )
        }
        AbTestingClient.install(fake)
        "fake"
    }
}
