package com.example.YOLOIntegration

import android.graphics.*
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import com.example.YOLOIntegration.YOLOv8Analyzer.DetectionResult
import kotlin.math.pow
import kotlin.math.sqrt


//class YOLOv8Analyzer(
//    private val tflite: Interpreter,
//    private val onAnalysisResult: (Bitmap, List<DetectionResult>) -> Unit
//) : ImageAnalysis.Analyzer {
//
//
//    companion object {
//        private const val TAG = "YOLOv8Analyzer"
//        private const val INPUT_SIZE = 800
//        private const val NUM_BOXES = 8400
//        private const val NUM_CLASSES = 1  // Assuming only one class (fingerprint)
//        private const val CONFIDENCE_THRESHOLD = 0.1f
//        private const val IOU_THRESHOLD = 0.1f
//    }
//
////    override fun analyze(image: ImageProxy) {
////        val bitmap = image.toBitmap()
////        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
////
////        val inputBuffer = preprocess(resizedBitmap)
////        val outputBuffer = Array(1) { Array(8) { FloatArray(13125) } }
////
////        tflite.run(inputBuffer, outputBuffer)
////
////        val results = interpretResults(outputBuffer[0])
////        val processedBitmap = drawDetectionResult(resizedBitmap, results)
////
////        // Update UI with the processed bitmap
////        onAnalysisResult(processedBitmap)
////
////        image.close()
////    }
//    override fun analyze(image: ImageProxy) {
//        try {
//            Log.d("YOLOv8Analyzer", "Starting image analysis. Image format: ${image.format}")
//            val bitmap = image.toBitmap()
//            Log.d("YOLOv8Analyzer", "Bitmap created successfully. Size: ${bitmap.width}x${bitmap.height}")
//
//            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
//            Log.d("YOLOv8Analyzer", "Bitmap resized to ${INPUT_SIZE}x${INPUT_SIZE}")
//
//            val inputBuffer = preprocess(resizedBitmap)
//            Log.d("YOLOv8Analyzer", "Preprocessing completed")
//
//            val outputBuffer = Array(1) { Array(8) { FloatArray(13125) } }
//            tflite.run(inputBuffer, outputBuffer)
//            Log.d("YOLOv8Analyzer", "TFLite model execution completed")
//
//            val results = interpretResults(outputBuffer[0])
//            Log.d("YOLOv8Analyzer", "Results interpreted. Number of detections: ${results.size}")
//
//            val processedBitmap = drawDetectionResult(resizedBitmap, results)
//            Log.d("YOLOv8Analyzer", "Detection results drawn on bitmap")
//
//            onAnalysisResult(processedBitmap, results)
//            Log.d("YOLOv8Analyzer", "Analysis result callback invoked")
//        } catch (e: Exception) {
//            Log.e("YOLOv8Analyzer", "Error processing image", e)
//        } finally {
//            image.close()
//        }
//    }
//
//
//    private fun preprocess(bitmap: Bitmap): ByteBuffer {
//        val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
//        inputBuffer.order(ByteOrder.nativeOrder())
//
//        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
//        bitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
//
//        for (pixelValue in intValues) {
//            // Normalize to [-1, 1] or [0, 1] based on your model's requirements
//            inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f * 2 - 1))
//            inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f * 2 - 1))
//            inputBuffer.putFloat(((pixelValue and 0xFF) / 255.0f * 2 - 1))
//        }
//
//        return inputBuffer
//    }
//
//    private fun interpretResults(output: Array<FloatArray>): List<DetectionResult> {
//        val results = mutableListOf<DetectionResult>()
//        for (i in 0 until NUM_BOXES) {
//            val confidence = output[4][i]
//            //Log.d(TAG, "Box $i: confidence = $confidence")
//            Log.d(TAG, "Box $i: x=${output[0][i]}, y=${output[1][i]}, w=${output[2][i]}, h=${output[3][i]}, conf=$confidence")
//            if (confidence > CONFIDENCE_THRESHOLD) {
//                val x = output[0][i]
//                val y = output[1][i]
//                val w = output[2][i]
//                val h = output[3][i]
//
//                val left = (x - w / 2) * INPUT_SIZE
//                val top = (y - h / 2) * INPUT_SIZE
//                val right = (x + w / 2) * INPUT_SIZE
//                val bottom = (y + h / 2) * INPUT_SIZE
//
//                results.add(
//                    DetectionResult(
//                        RectF(left, top, right, bottom),
//                        "Fingerprint",
//                        confidence
//                    )
//                )
//            }
//        }
//        Log.d("YOLOv8Analyzer", "Total detections: ${results.size}")
//        return results
//    }
//
//    private fun nonMaxSuppression(boxes: Array<FloatArray>, scores: FloatArray): List<Int> {
//        val indices = scores.indices.sortedByDescending { scores[it] }
//        val selectedIndices = mutableListOf<Int>()
//
//        for (i in indices) {
//            var keep = true
//            for (j in selectedIndices) {
//                if (computeIoU(boxes[i], boxes[j]) > IOU_THRESHOLD) {
//                    keep = false
//                    break
//                }
//            }
//            if (keep) selectedIndices.add(i)
//        }
//
//        return selectedIndices
//    }
//
//    private fun computeIoU(boxA: FloatArray, boxB: FloatArray): Float {
//        val xA = maxOf(boxA[0], boxB[0])
//        val yA = maxOf(boxA[1], boxB[1])
//        val xB = minOf(boxA[2], boxB[2])
//        val yB = minOf(boxA[3], boxB[3])
//
//        val interArea = maxOf(0f, xB - xA) * maxOf(0f, yB - yA)
//        val boxAArea = (boxA[2] - boxA[0]) * (boxA[3] - boxA[1])
//        val boxBArea = (boxB[2] - boxB[0]) * (boxB[3] - boxB[1])
//
//        return interArea / (boxAArea + boxBArea - interArea)
//    }
//
//    private fun drawDetectionResult(bitmap: Bitmap, results: List<DetectionResult>): Bitmap {
//        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//        val canvas = Canvas(outputBitmap)
//        val paint = Paint()
//
//        for (result in results) {
//            // Draw bounding box
//            paint.color = Color.RED
//            paint.style = Paint.Style.STROKE
//            paint.strokeWidth = 4f
//            canvas.drawRect(result.boundingBox, paint)
//
//            // Draw label and confidence
//            paint.color = Color.WHITE
//            paint.style = Paint.Style.FILL
//            paint.textSize = 36f
//            val label = "${result.label} ${String.format("%.2f", result.confidence)}"
//            canvas.drawText(label, result.boundingBox.left, result.boundingBox.top - 10f, paint)
//        }
//
//        return outputBitmap
//    }
//
//    private fun updateUI(bitmap: Bitmap) {
//        // This method should be implemented in your Activity/Fragment
//        // to update the UI with the processed bitmap
//    }
//
//    data class DetectionResult(val boundingBox: RectF, val label: String, val confidence: Float)
//
////    private fun ImageProxy.toBitmap(): Bitmap {
////        val yBuffer = planes[0].buffer
////        val uBuffer = planes[1].buffer
////        val vBuffer = planes[2].buffer
////
////        val ySize = yBuffer.remaining()
////        val uSize = uBuffer.remaining()
////        val vSize = vBuffer.remaining()
////
////        val nv21 = ByteArray(ySize + uSize + vSize)
////
////        yBuffer.get(nv21, 0, ySize)
////        vBuffer.get(nv21, ySize, vSize)
////        uBuffer.get(nv21, ySize + vSize, uSize)
////
////        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
////        val out = ByteArrayOutputStream()
////        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
////        val imageBytes = out.toByteArray()
////        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
////    }
////    private fun ImageProxy.toBitmap(): Bitmap {
////        val yBuffer = planes[0].buffer // Y
////        val uBuffer = planes[1].buffer // U
////        val vBuffer = planes[2].buffer // V
////
////        val ySize = yBuffer.remaining()
////        val uSize = uBuffer.remaining()
////        val vSize = vBuffer.remaining()
////
////        val nv21 = ByteArray(ySize + uSize + vSize)
////
////        // U and V are swapped
////        yBuffer.get(nv21, 0, ySize)
////        vBuffer.get(nv21, ySize, vSize)
////        uBuffer.get(nv21, ySize + vSize, uSize)
////
////        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
////        val out = ByteArrayOutputStream()
////        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
////        val imageBytes = out.toByteArray()
////        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
////    }
//    private fun ImageProxy.toBitmap(): Bitmap {
//        val buffer = planes[0].buffer
//        val data = ByteArray(buffer.remaining())
//        buffer.get(data)
//
//        val source = ImageDecoder.createSource(ByteBuffer.wrap(data))
//        return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
//            decoder.isMutableRequired = true
//        }
//    }
//}

class YOLOv8Analyzer(
    private val tflite: Interpreter,
    private val onAnalysisResult: (Bitmap, List<DetectionResult>) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "YOLOv8Analyzer"
        private const val INPUT_SIZE = 800
        private const val NUM_BOXES = 13125  // Changed to match model output
        private const val NUM_CLASSES = 1  // Assuming only one class (fingerprint)
        private const val CONFIDENCE_THRESHOLD = 0.7f  // Set this very low for testing
    }

    override fun analyze(image: ImageProxy) {
        Log.d(TAG, "Starting image analysis. Image format: ${image.format}")
        val bitmap = image.toBitmap()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        val inputBuffer = preprocess(resizedBitmap)
        val outputBuffer = Array(1) { Array(8) { FloatArray(13125) } }  // Changed to match model output

        tflite.run(inputBuffer, outputBuffer)
        Log.d(TAG, "Model output shape: ${outputBuffer.size} x ${outputBuffer[0].size} x ${outputBuffer[0][0].size}")

        val results = interpretResults(outputBuffer[0])
        val processedBitmap = drawDetectionResult(resizedBitmap, results)

        onAnalysisResult(processedBitmap, results)
        image.close()
    }


    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(bytes)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

//        val mean_r = 0.485f
//        val mean_g = 0.456f
//        val mean_b = 0.406f
//        val std_r = 0.229f
//        val std_g = 0.224f
//        val std_b = 0.225f

        for (pixelValue in intValues) {
            val r = (pixelValue shr 16 and 0xFF) / 255.0f
            val g = (pixelValue shr 8 and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f

//            inputBuffer.putFloat((r - mean_r) / std_r)
//            inputBuffer.putFloat((g - mean_g) / std_g)
//            inputBuffer.putFloat((b - mean_b) / std_b)
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        return inputBuffer
    }

//    private fun interpretResults(output: Array<FloatArray>): List<DetectionResult> {
//
//        val results = mutableListOf<DetectionResult>()
//        val numClasses = 1
//        val numBoxes = output[0].size / (numClasses + 5)
//
//        var maxConfidence = Float.MIN_VALUE
//        var minConfidence = Float.MAX_VALUE
//
//        Log.d(TAG, "Raw output size: ${output.size} x ${output[0].size}")
//        Log.d(TAG, "First few elements: ${output[0].take(10)}")
//
//        for (i in 0 until numBoxes) {
//            val offset = i * (numClasses + 5)
//            val confidence = output[4][offset + 4]
//            maxConfidence = maxOf(maxConfidence, confidence)
//            minConfidence = minOf(minConfidence, confidence)
//            Log.d(TAG, "Box $i: x=${output[0][i]}, y=${output[1][i]}, w=${output[2][i]}, h=${output[3][i]}, conf=$confidence")
//            if (confidence > CONFIDENCE_THRESHOLD) {
//                val x = output[0][offset]
//                val y = output[1][offset]
//                val w = output[2][offset]
//                val h = output[3][offset]
//                results.add(
//                    DetectionResult(
//                        RectF(
//                            (x - w/2) * INPUT_SIZE,
//                            (y - h/2) * INPUT_SIZE,
//                            (x + w/2) * INPUT_SIZE,
//                            (y + h/2) * INPUT_SIZE
//                        ),
//                        "Fingerprint",
//                        confidence
//                    )
//                )
//            }
//        }
//
//        Log.d(TAG, "Max confidence: $maxConfidence")
//        Log.d(TAG, "Min confidence: $minConfidence")
//        Log.d(TAG, "Total detections before NMS: ${results.size}")
//        val nmsResults = nms(results, 0.5f)
//        Log.d(TAG, "Total detections after NMS: ${nmsResults.size}")
//        return nmsResults
//    }

    private fun interpretResults(output: Array<FloatArray>): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()
        Log.d(TAG, "Output size: ${output.size}")
        Log.d(TAG, "Rows: ${output[0].size}")
        val num_boxes = output[0].size
        // val rows = output[0].size / (NUM_CLASSES + 7)  // 5 for x, y, w, h, confidence
        // Log.d(TAG, "Rows: ${rows}")
        // Chose the value 7 everywhere because NUM_CLASSES here is 1 and total dimensions per box in the model output is 8
        // So to loop over all outputs, we need to divide it in rows of 8, i.e. NUM_CLASSES + 7

        for (i in 0 until num_boxes) {
            val confidence = output[6][i] //val confidence = output[6][i * (NUM_CLASSES + 7) + 6]
            if (confidence > CONFIDENCE_THRESHOLD) {
//                val x = output[0][i * (NUM_CLASSES + 5) + 0]
//                val y = output[0][i * (NUM_CLASSES + 5) + 1]
//                val w = output[0][i * (NUM_CLASSES + 5) + 2]
//                val h = output[0][i * (NUM_CLASSES + 5) + 3]
                val x = output[0][i]
                val y = output[1][i]
                val w = output[2][i]
                val h = output[3][i]

                val left = (x - w / 2) * INPUT_SIZE
                val top = (y - h / 2) * INPUT_SIZE
                val right = (x + w / 2) * INPUT_SIZE
                val bottom = (y + h / 2) * INPUT_SIZE

                results.add(
                    DetectionResult(
                        RectF(left, top, right, bottom),
                        "Fingerprint",
                        confidence
                    )
                )
            }
        }

        return nms(results, 0.3f) // iouThreshold
    }

    private fun sigmoid(x: Float): Float {
        return 1.0f / (1.0f + exp(-x))
    }

    private fun nms(boxes: List<DetectionResult>, iouThreshold: Float): List<DetectionResult> {
        val sortedBoxes = boxes.sortedByDescending { it.confidence }
        val selectedBoxes = mutableListOf<DetectionResult>()

        for (box in sortedBoxes) {
            var shouldSelect = true
            for (selectedBox in selectedBoxes) {
                if (calculateIoU(box.boundingBox, selectedBox.boundingBox) > iouThreshold) {
                    shouldSelect = false
                    break
                }
            }
            if (shouldSelect) selectedBoxes.add(box)
        }

        return selectedBoxes
    }

    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val intersectionArea = RectF().apply {
            setIntersect(box1, box2)
        }.let { if (it.isEmpty) 0f else it.width() * it.height() }

        val box1Area = box1.width() * box1.height()
        val box2Area = box2.width() * box2.height()
        val unionArea = box1Area + box2Area - intersectionArea

        return intersectionArea / unionArea
    }

    private fun drawDetectionResult(bitmap: Bitmap, results: List<DetectionResult>): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val paint = Paint()

        for (result in results) {
            paint.color = Color.RED
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 8f
            canvas.drawRect(result.boundingBox, paint)

            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            paint.textSize = 48f
            val label = "${result.label} ${String.format("%.2f", result.confidence)}"
            canvas.drawText(label, result.boundingBox.left, result.boundingBox.top - 10f, paint)
        }

        return outputBitmap
    }


    data class DetectionResult(val boundingBox: RectF, val label: String, val confidence: Float)
}
