package com.mi10tpro.aicamera

import android.Manifest
import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var preview: PreviewView
    private lateinit var status: TextView
    private lateinit var zoomSlider: SeekBar

    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null

    private var maxZoomRatio = 1f

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

        createInterface()

        permissionLauncher.launch(
            Manifest.permission.CAMERA
        )
    }

    private fun createInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF000000.toInt())
        }

        preview = PreviewView(this)

        preview.scaleType =
            PreviewView.ScaleType.FILL_CENTER

        root.addView(
            preview,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        status = TextView(this).apply {
            text = "AI Super Zoom • Starting..."
            textSize = 17f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(18, 12, 18, 8)
        }

        root.addView(status)

        zoomSlider = SeekBar(this).apply {
            max = 100
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
                capturePhoto()
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

                    if (maxZoomRatio <= 1f) {
                        return
                    }

                    val ratio =
                        1f +
                            (maxZoomRatio - 1f) *
                            progress / 100f

                    camera?.cameraControl
                        ?.setZoomRatio(ratio)

                    status.text =
                        String.format(
                            Locale.US,
                            "AI Super Zoom • %.1fx / %.1fx",
                            ratio,
                            maxZoomRatio
                        )
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
            }
        )

        setContentView(root)
    }

    private fun bindCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            try {

                val provider =
                    cameraProviderFuture.get()

                val previewUseCase =
                    Preview.Builder()
                        .build()

                previewUseCase.surfaceProvider =
                    preview.surfaceProvider

                /*
                 * MAXIMIZE_QUALITY به جای
                 * MINIMIZE_LATENCY
                 *
                 * هدف این پروژه کیفیت تصویر است،
                 * نه سرعت ثبت عکس.
                 */
                imageCapture =
                    ImageCapture.Builder()
                        .setCaptureMode(
                            ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
                        )
                        .setJpegQuality(100)
                        .build()

                provider.unbindAll()

                camera =
                    provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        previewUseCase,
                        imageCapture
                    )

                /*
                 * حداکثر زوم واقعی CameraX
                 */
                maxZoomRatio =
                    camera?.cameraInfo
                        ?.zoomState
                        ?.value
                        ?.maxZoomRatio
                        ?: 1f

                zoomSlider.progress = 0

                status.text =
                    String.format(
                        Locale.US,
                        "Camera Ready • Max Zoom %.1fx",
                        maxZoomRatio
                    )

            } catch (e: Exception) {

                status.text =
                    "Camera error: ${e.message}"
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {

        val capture = imageCapture

        if (capture == null) {

            status.text =
                "Camera is not ready"

            return
        }

        status.text =
            "Capturing high quality image..."

        val fileName =
            "AI_Camera_${System.currentTimeMillis()}.jpg"

        val contentValues =
            ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    fileName
                )

                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )

                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/Mi10TPro AI Camera"
                )
            }

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),

            object :
                ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults:
                    ImageCapture.OutputFileResults
                ) {

                    status.text =
                        "✓ High-quality photo saved"
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {

                    status.text =
                        "Capture error: ${exception.message}"
                }
            }
        )
    }
}
