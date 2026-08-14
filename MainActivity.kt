package com.mi10tpro.aicamera

import android.Manifest
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

class MainActivity : ComponentActivity() {
    private lateinit var preview: PreviewView
    private lateinit var status: TextView
    private var camera: Camera? = null

    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) bindCamera() else status.text = "Camera permission required"
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(0xFF000000.toInt()) }
        preview = PreviewView(this)
        root.addView(preview, LinearLayout.LayoutParams(-1, 0, 1f))
        status = TextView(this).apply { text = "AI Super Zoom • Ready"; textSize = 17f; setTextColor(0xFFFFFFFF.toInt()); setPadding(18,12,18,8) }
        root.addView(status)
        val zoom = SeekBar(this).apply { max = 190; progress = 0 }
        root.addView(zoom)
        val mode = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Natural AI","Maximum Detail","Night AI"))
        }
        root.addView(mode)
        root.addView(Button(this).apply {
            text = "Capture with AI"
            setOnClickListener { status.text = "AI pipeline ready for native engine integration" }
        })
        zoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                val r = 1f + p / 10f
                camera?.cameraControl?.setZoomRatio(r)
                status.text = "AI Super Zoom • %.1fx".format(r)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        setContentView(root)
        permission.launch(Manifest.permission.CAMERA)
    }

    private fun bindCamera() {
        val f = ProcessCameraProvider.getInstance(this)
        f.addListener({
            val provider = f.get()
            val p = Preview.Builder().build().also { it.surfaceProvider = preview.surfaceProvider }
            provider.unbindAll()
            camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, p)
        }, mainExecutor)
    }
}
