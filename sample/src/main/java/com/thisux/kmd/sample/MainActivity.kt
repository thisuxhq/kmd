package com.thisux.kmd.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.thisux.kmd.CoilKmdImageRenderer
import com.thisux.kmd.KmdKeywordHighlighter
import com.thisux.kmd.KmdMaterial3
import com.thisux.kmd.KmdOptions
import com.thisux.kmd.KmdSyntaxHighlighter
import com.thisux.kmd.LazyKmd
import com.thisux.kmd.rememberKmdState
import com.thisux.kmd.sample.theme.KmdTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KmdTheme {
                SampleApp()
            }
        }
    }
}

private enum class SampleTab(val label: String) {
    Document("Document"),
    Stream("Stream"),
    Settings("Settings"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SampleApp() {
    var selected by remember { mutableIntStateOf(0) }
    val tabs = SampleTab.entries
    val motion = remember { MotionSettings() }
    var playRequest by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("KMD") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryTabRow(selectedTabIndex = selected) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selected == index,
                        onClick = { selected = index },
                        text = { Text(tab.label) },
                    )
                }
            }
            when (tabs[selected]) {
                SampleTab.Document -> DocumentPane()
                SampleTab.Stream ->
                    StreamPane(
                        motion = motion,
                        playRequest = playRequest,
                    )
                SampleTab.Settings ->
                    SettingsPane(
                        settings = motion,
                        onPlay = {
                            playRequest += 1
                            selected = SampleTab.Stream.ordinal
                        },
                    )
            }
        }
    }
}

@Composable
private fun sampleHighlighter(): KmdSyntaxHighlighter {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        KmdKeywordHighlighter(
            keyword = scheme.primary,
            string = scheme.tertiary,
            comment = scheme.outline,
            number = scheme.secondary,
        )
    }
}

@Composable
private fun DocumentPane() {
    val context = LocalContext.current
    val highlighter = sampleHighlighter()
    SelectionContainer {
        LazyKmd(
            markdown = SampleMarkdown,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            style = KmdMaterial3.style(),
            imageRenderer = CoilKmdImageRenderer,
            syntaxHighlighter = highlighter,
            onLinkClick = { url ->
                Toast.makeText(context, url, Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
private fun StreamPane(
    motion: MotionSettings,
    playRequest: Int,
) {
    val context = LocalContext.current
    val highlighter = sampleHighlighter()
    val state = rememberKmdState()
    val scope = rememberCoroutineScope()
    var streaming by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }

    fun play() {
        job?.cancel()
        job =
            scope.launch {
                streaming = true
                state.reset()
                SampleMarkdown.chunked(4).forEach { chunk ->
                    state.append(chunk)
                    delay(motion.tokenDelayMs.toLong())
                }
                streaming = false
            }
    }

    LaunchedEffect(playRequest) {
        if (playRequest > 0) play()
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(
                onClick = { play() },
                enabled = !streaming,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(if (streaming) "Streaming…" else "Stream sample")
            }
        }
        SelectionContainer {
            LazyKmd(
                state = state,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                style = KmdMaterial3.style(),
                imageRenderer = CoilKmdImageRenderer,
                syntaxHighlighter = highlighter,
                options = KmdOptions(streaming = motion.toStreaming()),
                onLinkClick = { url ->
                    Toast.makeText(context, url, Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}
