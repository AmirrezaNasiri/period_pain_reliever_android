package com.example.perilif

import android.view.View
import android.widget.ProgressBar
import kotlinx.coroutines.FlowPreview
import org.json.JSONObject
import android.os.Handler
import android.os.Looper
import androidx.appcompat.widget.SwitchCompat


class Commander(private var context: MainActivity) {
    var isBusy = false

    private var debouncing = false

    @FlowPreview
    fun updateCycles(cycle: Int? = null) {
        enableLoading()
        disableTurboControl()

        if (!debouncing) {
            debouncing = true
            Handler(Looper.getMainLooper()).postDelayed({
                realUpdateCycles(cycle)

                enablePadControls()
                enableTurboControl()

                context.toast.show("Cycles are updated.")
                debouncing = false
            }, 1_000)
        }
    }

    @FlowPreview
    fun enableTurbo() {
        disablePadControls()
        disableTurboControl()
        context.pads.forEach { it.value.pauseTargetCycle() }

        realUpdateCycles(100)

        Handler(Looper.getMainLooper()).postDelayed({
            uncheckTurboControl()
        }, 60_000)

        enableTurboControl()
        context.toast.show("Turbo enabled. 🔥ing up ...")
    }

    @FlowPreview
    fun disableTurbo() {
        disablePadControls()
        disableTurboControl()
        context.pads.forEach { it.value.resumeTargetCycle() }

        this.realUpdateCycles()

        enablePadControls()
        enableTurboControl()

        context.toast.show("Turbo disabled.")
    }

    fun realUpdateCycles(cycle: Int? = null) {
        val data = JSONObject()

        if (cycle == null) {
            context.pads.forEach { data.put("p${it.key}c", it.value.targetCycle) }
        } else {
            context.pads.forEach { data.put("p${it.key}c", cycle) }
        }

        sendConfig(data)
    }

    private fun sendConfig(data: JSONObject) {
        enableLoading()

        data.put("_t", "c")
        context.logger.log(">>", data.toString())
        context.bluetoothThread?.write(data.toString().toByteArray())

        disableLoading()
    }

    private fun disablePadControls() {
        context.pads.forEach { it.value.disable() }
    }

    private fun enablePadControls() {
        context.pads.forEach { it.value.enable() }
    }

    private fun enableTurboControl() {
        turboSwitch().isEnabled = true
    }

    private fun disableTurboControl() {
        turboSwitch().isEnabled = false
    }

    private fun enableLoading() {
        isBusy = true
        loadingBar().visibility = View.VISIBLE
    }

    private fun disableLoading() {
        isBusy = false
        loadingBar().visibility = View.INVISIBLE
    }

    private fun turboSwitch(): SwitchCompat {
        return context.findViewById(R.id.switch_turbo)
    }

    private fun uncheckTurboControl() {
        turboSwitch().isChecked = false
    }

    private fun loadingBar(): ProgressBar {
        return context.findViewById(R.id.command_loadingbar)
    }
}