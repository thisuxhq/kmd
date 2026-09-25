package com.thisux.kmd.benchmark

import com.thisux.kmd.KmdEngine
import org.junit.Test
import java.io.File
import java.lang.management.ManagementFactory
import kotlin.system.measureNanoTime

// The measured half: reports timings and allocations, asserts nothing,
// so a slow CI machine cannot fail the build. Read the report instead.
class KmdEngineBenchmark {
    private val threads = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean

    @Test
    fun report() {
        warmUp()
        val lines = mutableListOf<String>()
        lines += "# kmd-benchmark"
        lines += ""
        lines += "Host JVM, `KmdEngine` only. Medians unless noted."
        lines += ""
        lines += "## Static parse"
        lines += ""
        lines += "| size | blocks | parse ms |"
        lines += "|---|---|---|"
        for (kb in listOf(1, 10, 50, 100)) {
            val markdown = Corpus.ofSize(kb * 1_000)
            val blocks = KmdEngine().parse(markdown).blocks.size
            val ms = median(runs = 15, warmup = 5) { KmdEngine().parse(markdown) } / 1e6
            lines += "| $kb KB | $blocks | ${fmt(ms)} |"
        }
        lines += ""
        lines += "## Streaming append"
        lines += ""
        lines += "4-character tokens. Budget is the time one token may take at that rate."
        lines += ""
        lines += "| document | tokens | p50 µs | p99 µs | max µs | alloc / append |"
        lines += "|---|---|---|---|---|---|"
        for (kb in listOf(1, 10, 50)) {
            val tokens = Corpus.tokens(Corpus.ofSize(kb * 1_000))
            repeat(3) { stream(tokens) }
            val (times, bytes) = stream(tokens)
            times.sort()
            lines += "| $kb KB | ${tokens.size} | ${fmt(pct(times, 50) / 1e3)} | ${fmt(pct(times, 99) / 1e3)} | " +
                "${fmt(times.last() / 1e3)} | ${bytes / tokens.size} B |"
        }
        lines += ""
        lines += "| rate | budget per token |"
        lines += "|---|---|"
        for (rate in listOf(10, 50, 100)) {
            lines += "| $rate tokens/sec | ${1_000 / rate} ms |"
        }
        val report = lines.joinToString("\n")
        println(report)
        File("build/reports/kmd-benchmark").apply { mkdirs() }.resolve("report.md").writeText(report + "\n")
    }

    // Let the JIT settle so the first size measured is not penalized.
    private fun warmUp() {
        val markdown = Corpus.ofSize(10_000)
        repeat(20) { KmdEngine().parse(markdown) }
        repeat(3) { stream(Corpus.tokens(markdown)) }
    }

    private fun stream(tokens: List<String>): Pair<LongArray, Long> {
        val engine = KmdEngine()
        val times = LongArray(tokens.size)
        val thread = Thread.currentThread().id
        val before = threads.getThreadAllocatedBytes(thread)
        tokens.forEachIndexed { index, token ->
            times[index] = measureNanoTime { engine.append(token) }
        }
        return times to threads.getThreadAllocatedBytes(thread) - before
    }

    private inline fun median(
        runs: Int,
        warmup: Int,
        block: () -> Unit,
    ): Double {
        repeat(warmup) { block() }
        val times = LongArray(runs) { measureNanoTime(block) }
        times.sort()
        return times[runs / 2].toDouble()
    }

    private fun pct(
        sorted: LongArray,
        p: Int,
    ): Double = sorted[((sorted.size - 1) * p / 100)].toDouble()

    private fun fmt(value: Double): String = "%.2f".format(value)
}
