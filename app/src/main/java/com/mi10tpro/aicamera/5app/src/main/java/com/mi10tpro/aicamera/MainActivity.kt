package com.mi10tpro.aicamera

import android.Manifest
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

class MainActivity : ComponentActivity() {

    private lateinit var preview: PreviewView
    private lateinit var status: TextView

    private var camera: Camera? = null

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                bindCamera()
            } else {
                status.text = "Camera permission required"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF000000.toInt())
        }

        preview = PreviewView(this)

        root.addView(
            preview,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        status = TextView(this).apply {
            text = "AI Super Zoom • Ready"
            textSize = 17f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(18, 12, 18, 8)
        }

        root.addView(status)

        val zoomSlider = SeekBar(this).apply {
            max = 190
            progress = 0
        }

        root.addView(zoomSlider)

        val modeSpinner = Spinner(this)

        modeSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf(
                    "Natural AI",
                    "Maximum Detail",
                    "Night AI"
                )
            )

        root.addView(modeSpinner)

        val captureButton = Button(this).apply {
            text = "Capture with AI"

            setOnClickListener {
                status.text =
                    "AI pipeline ready"
            }
        }

        root.addView(captureButton)

        zoomSlider.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val ratio =
                        1f + progress / 10f

                    camera?.cameraControl
                        ?.setZoomRatio(ratio)

                    status.text =
                        "AI Super Zoom • %.1fx"
                            .format(ratio)
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        setContentView(root)

        permissionLauncher.launch(
            Manifest.permission.CAMERA
        )
    }

    private fun bindCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(this)

        cameraProviderFuture.addListener({

            val provider =
                cameraProviderFuture.get()

            val previewUseCase =
                Preview.Builder()
                    .build()

            previewUseCase.surfaceProvider =
                preview.surfaceProvider

            provider.unbindAll()

            camera =
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    previewUseCase
                )

        }, mainExecutor)
    }
}
