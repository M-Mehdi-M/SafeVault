package project.safevault.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import project.safevault.R
import project.safevault.databinding.FragmentScannerBinding
import project.safevault.mlkit.DocumentProcessor
import project.safevault.mlkit.TextRecognitionHelper
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private var lastCapturedBitmap: Bitmap? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(requireContext(), R.string.scanner_camera_permission, Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        binding.btnCapture.setOnClickListener { captureImage() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun captureImage() {
        val capture = imageCapture ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCapture.isEnabled = false
        capture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val bitmap = imageProxyToBitmap(imageProxy, rotation)
                    imageProxy.close()
                    bitmap?.let { processImage(it) }
                        ?: run {
                            binding.progressBar.visibility = View.GONE
                            binding.btnCapture.isEnabled = true
                            Toast.makeText(requireContext(), R.string.general_error, Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true
                    Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy, rotationDegrees: Int): Bitmap? {
        val buffer: ByteBuffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun processImage(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val rawText = withContext(Dispatchers.IO) {
                    TextRecognitionHelper.recognizeText(bitmap)
                }
                binding.progressBar.visibility = View.GONE
                binding.btnCapture.isEnabled = true
                if (rawText.isBlank()) {
                    Toast.makeText(requireContext(), R.string.scanner_no_text, Toast.LENGTH_SHORT).show()
                    return@launch
                }
                lastCapturedBitmap = bitmap
                val document = DocumentProcessor.process(rawText)
                val resultText = buildString {
                    if (document.extractedFields.isNotEmpty()) {
                        append("── Extracted Fields ──\n")
                        document.extractedFields.forEach { (key, value) ->
                            append("$key: $value\n")
                        }
                        append("\n── Raw Text ──\n")
                    }
                    append(document.rawText)
                }
                showScanResult(resultText)
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnCapture.isEnabled = true
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToInternal(bitmap: Bitmap): String? {
        return try {
            val dir = File(requireContext().filesDir, "scanned_images")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "scan_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun showScanResult(text: String) {
        val bottomSheet = ScanResultBottomSheet.newInstance(text)
        bottomSheet.onSaveClicked = { scannedText ->
            val imagePath = lastCapturedBitmap?.let { saveBitmapToInternal(it) }
            val bundle = Bundle().apply {
                putString("scannedText", scannedText)
                if (imagePath != null) putString("scannedImagePath", imagePath)
            }
            findNavController().navigate(R.id.action_scanner_to_addItem, bundle)
        }
        bottomSheet.show(childFragmentManager, "scan_result")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

