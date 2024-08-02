//package com.example.YOLOIntegration
//
//import android.os.Bundle
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.core.content.ContextCompat
//import com.example.YOLOIntegration.databinding.ActivityMainBinding
//import org.tensorflow.lite.Interpreter
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//import java.io.FileInputStream
//import java.nio.MappedByteBuffer
//import java.nio.channels.FileChannel
//import android.content.ContentValues
//import android.content.pm.PackageManager
//import android.provider.MediaStore
//import android.view.View
//import android.widget.Toast
//import android.Manifest
//import android.graphics.Bitmap
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.core.app.ActivityCompat
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.concurrent.TimeUnit
//import android.util.Size
//
//class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var cameraExecutor: ExecutorService
//    private lateinit var tflite: Interpreter
//    private var imageCapture: ImageCapture? = null
//
//    private var imageAnalyzer: ImageAnalysis? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Initialize TensorFlow Lite interpreter
//        tflite = Interpreter(loadModelFile())
//
//        // Set up the camera
//        cameraExecutor = Executors.newSingleThreadExecutor()
//        // Check permissions before setting up the camera
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        }
//
//        // Set up capture button
//        binding.captureButton.setOnClickListener { captureAndProcessImage() }
//    }
//
//    private fun captureAndProcessImage() {
//        val imageCapture = imageCapture ?: return
//
//        imageCapture.takePicture(
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageCapturedCallback() {
//                override fun onCaptureSuccess(image: ImageProxy) {
//                    processImage(image)
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
//                }
//            }
//        )
//    }
//
//    private fun processImage(image: ImageProxy) {
//        val yoloAnalyzer = YOLOv8Analyzer(tflite) { processedBitmap, results ->
//            runOnUiThread {
//                binding.imageView.setImageBitmap(processedBitmap)
//                binding.imageView.visibility = View.VISIBLE
//            }
//            saveProcessedImage(processedBitmap)
//        }
//        yoloAnalyzer.analyze(image)
//    }
//
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all { permission ->
//        ContextCompat.checkSelfPermission(baseContext, permission) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
//                }
//
//            imageCapture = ImageCapture.Builder()
//                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                .build()
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                cameraProvider.unbindAll()
//                val camera = cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, imageCapture)
//
//                // Set up autofocus
//                camera.cameraControl.cancelFocusAndMetering()
//                val factory = SurfaceOrientedMeteringPointFactory(
//                    binding.viewFinder.width.toFloat(),
//                    binding.viewFinder.height.toFloat()
//                )
//                val point = factory.createPoint(0.5f, 0.5f)
//                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
//                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
//                    .build()
//                camera.cameraControl.startFocusAndMetering(action)
//
//            } catch(exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    private fun saveProcessedImage(bitmap: Bitmap) {
//        val filename = "processed_image_${System.currentTimeMillis()}.jpg"
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YOLOApp")
//            }
//        }
//        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        uri?.let {
//            contentResolver.openOutputStream(it)?.use { outputStream ->
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            }
//            runOnUiThread {
//                Toast.makeText(this, "Processed image saved: $filename", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun takePhoto() {
//        val imageCapture = imageCapture ?: return
//
//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YOLOApp")
//            }
//        }
//
//        val outputOptions = ImageCapture.OutputFileOptions
//            .Builder(contentResolver,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues)
//            .build()
//
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                }
//
//                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                    val msg = "Photo capture succeeded: ${output.savedUri}"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, msg)
//                }
//            }
//        )
//    }
//
//
//
//    companion object {
//        private const val TAG = "YOLOv8ObjectDetection"
//        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
//        private const val REQUEST_CODE_PERMISSIONS = 10
//        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
//    }
//
//
//
//    private fun loadModelFile(): MappedByteBuffer {
//        val fileDescriptor = assets.openFd("best_float32.tflite")
//        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel = inputStream.channel
//        val startOffset = fileDescriptor.startOffset
//        val declaredLength = fileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraExecutor.shutdown()
//    }
//
////    companion object {
////        private const val TAG = "YOLOv8ObjectDetection"
////    }
//}

package com.example.YOLOIntegration

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.YOLOIntegration.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import android.Manifest
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.util.Size
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner


class MainActivity : AppCompatActivity(), LifecycleOwner {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var tflite: Interpreter
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TensorFlow Lite interpreter
        tflite = Interpreter(loadModelFile("best_float32.tflite"))

        // Set up the camera
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Check permissions before setting up the camera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up capture button
        binding.captureButton.setOnClickListener { captureAndProcessImage() }
    }

    private fun captureAndProcessImage() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    processImage(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun processImage(image: ImageProxy) {
        val yoloAnalyzer = YOLOv8Analyzer(tflite) { processedBitmap, results ->
            runOnUiThread {
                binding.imageView.setImageBitmap(processedBitmap)
                binding.imageView.visibility = View.VISIBLE
            }
            saveProcessedImage(processedBitmap)
            Log.d(TAG, "Number of detections: ${results.size}")
            results.forEach { result ->
                Log.d(TAG, "Detection: ${result.label}, Confidence: ${result.confidence}, Bounds: ${result.boundingBox}")
            }
        }
        yoloAnalyzer.analyze(image)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all { permission ->
        ContextCompat.checkSelfPermission(baseContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                Log.d(TAG, "Camera provider obtained successfully")
                bindCameraUseCases()
            } catch (exc: Exception) {
                Log.e(TAG, "Failed to get camera provider", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()

            // Bind preview use case
            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e(TAG, "Failed to bind preview use case", exc)
            }

            // Bind image capture use case
            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Failed to bind image capture use case", exc)
            }

            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            // Set up autofocus
            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            camera.cameraControl.cancelFocusAndMetering()
            val factory = SurfaceOrientedMeteringPointFactory(
                binding.viewFinder.width.toFloat(),
                binding.viewFinder.height.toFloat()
            )
            val point = factory.createPoint(0.5f, 0.5f)
            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                .build()
            camera.cameraControl.startFocusAndMetering(action)

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun saveProcessedImage(bitmap: Bitmap) {
        val filename = "processed_image_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YOLOApp")
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Log.d(TAG, "Image saved successfully: $filename")
            runOnUiThread {
                Toast.makeText(this, "Processed image saved: $filename", Toast.LENGTH_SHORT).show()
            }
        }?: Log.e(TAG, "Failed to create new MediaStore record.")
    }

//    private fun loadModelFile(): MappedByteBuffer {
//        val fileDescriptor = assets.openFd("best_float32.tflite")
//        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel = inputStream.channel
//        val startOffset = fileDescriptor.startOffset
//        val declaredLength = fileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//    }
private fun loadModelFile(filename: String): MappedByteBuffer {
    val fileDescriptor = assets.openFd(filename)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onPause() {
        super.onPause()
        cameraProvider?.unbindAll()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            Handler(Looper.getMainLooper()).postDelayed({
                startCamera()
            }, 100) // 100ms delay
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}