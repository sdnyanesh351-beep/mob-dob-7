// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Block KSP's bundled IntelliJ platform from starting a real AWT EventDispatchThread
// and posting the BinaryFileTypeDecompilers.notifyDecompilerSetChange runnable
// that NPEs because ApplicationManager was never instantiated in headless CI/CLI builds.
listOf(
    "java.awt.headless" to "true",
    "awt.toolkit" to "sun.awt.HeadlessToolkit",
    "java.awt.graphicsenv" to "sun.java2d.NullSurfaceDataFactory",
    "idea.ignore.disabled.plugins" to "true",
    "idea.initially.ask.config" to "never",
    "jb.consents.confirmation.enabled" to "false",
    "idea.platform.prefix" to "Idea",
    "io.netty.leakDetection.level" to "disabled",
).forEach { (k, v) ->
    if (System.getProperty(k) == null) System.setProperty(k, v)
}
try {
    // Install a no-op default uncaught-exception handler for the AWT-EventQueue thread
    // so any late KSP/IntelliJ listener leaks do not print scary stack traces.
    val kt = Class.forName("java.awt.EventQueue")
    val push = kt.getMethod("invokeAndWait", Runnable::class.java)
    push.invoke(null, Runnable {
        Thread.currentThread().setUncaughtExceptionHandler { _, _ -> /* no-op */ }
    })
} catch (_: Throwable) {
    // Either AWT is fully disabled (good) or the reflection failed; ignore.
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}
