package com.hoanglm.flutteryoutubeview

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformViewRegistry
import java.util.concurrent.atomic.AtomicReference


class FlutterYoutubeViewPlugin: FlutterPlugin, ActivityAware {
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        flutterPluginBinding = null;
    }

    override fun onDetachedFromActivity() {
        flutterPluginBinding = null;
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        val f = flutterPluginBinding;
        if (f != null) {
            clientHandler = ClientHandler(f.platformViewRegistry, binding.activity, f.binaryMessenger)
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        flutterPluginBinding = binding
    }


    companion object {
        private var clientHandler: ClientHandler? = null

        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            if (registrar.activity() == null) {
                // When a background flutter view tries to register the plugin, the registrar has no activity.
                // We stop the registration process as this plugin is foreground only.
                return
            }

            clientHandler = ClientHandler(registrar.platformViewRegistry(), registrar.activity(), registrar.messenger())
            registrar.addViewDestroyListener {
                clientHandler = null
                false
            }
            registrar.activity().application.registerActivityLifecycleCallbacks(clientHandler)
        }
    }

    private class ClientHandler(pvr: PlatformViewRegistry, activity: Activity, messenger: BinaryMessenger): Application.ActivityLifecycleCallbacks {
        var state: AtomicReference<Lifecycle.Event> = AtomicReference(Lifecycle.Event.ON_CREATE)
        private var registrarActivityHashCode: Int?  = null

        init {
            registrarActivityHashCode = activity.hashCode()
            pvr
                .registerViewFactory(
                        "plugins.hoanglm.com/youtube", YoutubeFactory(activity, messenger, state)
                )
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity.hashCode() != registrarActivityHashCode) {
                return
            }
            state.set(Lifecycle.Event.ON_CREATE)
        }

        override fun onActivityStarted(activity: Activity) {
            if (activity.hashCode() != registrarActivityHashCode) {
                return
            }
            state.set(Lifecycle.Event.ON_START)
        }

        override fun onActivityResumed(activity: Activity) {
            if (activity.hashCode() != registrarActivityHashCode) {
                return
            }
            state.set(Lifecycle.Event.ON_RESUME)
        }

        override fun onActivityPaused(activity: Activity) {
            if (activity.hashCode() != registrarActivityHashCode) {
                return
            }
            state.set(Lifecycle.Event.ON_PAUSE)
        }


        override fun onActivityStopped(activity: Activity) {
            if (activity.hashCode() != registrarActivityHashCode) {
                return
            }
            state.set(Lifecycle.Event.ON_STOP)
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity.hashCode() != registrarActivityHashCode) {
                return
            }
            state.set(Lifecycle.Event.ON_DESTROY)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
        }
    }
}
