package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.ProductEntity
import com.example.util.Localization
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    settings: AppSettingsEntity,
    lang: String,
    onBack: () -> Unit,
    onProductFound: (String, (ProductEntity) -> Unit) -> Unit,
    onViewProduct: (Long) -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onSellNow: (ProductEntity) -> Unit,
    onAddStock: (ProductEntity) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraProviderRef?.unbindAll()
            } catch (_: Exception) {}
            cameraExecutor.shutdown()
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var manualCodeInput by remember { mutableStateOf("") }
    var scannedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var isScanningActive by remember { mutableStateOf(true) }

    fun handleCodeScanned(code: String) {
        if (!isScanningActive && scannedProduct != null) return
        isScanningActive = false
        onProductFound(code) { found ->
            scannedProduct = found
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.tr("scan_qr", lang)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("qr_scanner_screen")
        ) {
            if (hasCameraPermission) {
                // Live CameraX view using TextureView (COMPATIBLE mode) to avoid SurfaceView BufferQueue issues in Compose
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                cameraProviderRef = cameraProvider
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                val reader = MultiFormatReader()

                                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    val buffer = imageProxy.planes[0].buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    val width = imageProxy.width
                                    val height = imageProxy.height

                                    try {
                                        val source = PlanarYUVLuminanceSource(
                                            bytes, width, height, 0, 0, width, height, false
                                        )
                                        val bitmap = BinaryBitmap(HybridBinarizer(source))
                                        val result = reader.decodeWithState(bitmap)
                                        if (result != null && result.text.isNotBlank()) {
                                            previewView.post {
                                                handleCodeScanned(result.text)
                                            }
                                        }
                                    } catch (_: Exception) {
                                        // Frame had no barcode
                                    } finally {
                                        imageProxy.close()
                                    }
                                }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (_: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    onRelease = {
                        try {
                            cameraProviderRef?.unbindAll()
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Scanner overlay box with glowing corners
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .align(Alignment.Center)
                        .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                )
            } else {
                // Permission not granted state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enable camera access to scan shoe barcodes and QR labels instantly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Camera Permission")
                    }
                }
            }

            // Bottom Manual & Quick-Test Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scan or Enter Barcode / Product ID",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualCodeInput,
                            onValueChange = { manualCodeInput = it },
                            placeholder = { Text("e.g. TS-0001") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scanner_manual_input")
                        )
                        Button(
                            onClick = {
                                if (manualCodeInput.isNotBlank()) {
                                    handleCodeScanned(manualCodeInput.trim())
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("scanner_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Instant Test QR Simulators:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { handleCodeScanned("TS-0001") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).testTag("test_qr_ts0001")
                        ) {
                            Text("TS-0001", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { handleCodeScanned("TS-0002") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("TS-0002", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { handleCodeScanned("TS-0003") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("TS-0003", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    // Exact Product Record Popup (Section 4 Requirement)
    if (scannedProduct != null) {
        val prod = scannedProduct!!
        AlertDialog(
            onDismissRequest = {
                scannedProduct = null
                isScanningActive = true
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(prod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = prod.productId,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Size:", color = Color.Gray)
                        Text(prod.size, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Color:", color = Color.Gray)
                        Text(prod.color, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Stock:", color = Color.Gray)
                        Text(
                            text = "${prod.quantity} pairs",
                            fontWeight = FontWeight.Bold,
                            color = if (prod.quantity <= prod.minStockLevel) Color(0xFFBA1A1A) else Color(0xFF1B873F)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Retail Selling Price:", color = Color.Gray)
                        Text("${settings.currency} ${"%,.0f".format(prod.retailPrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Wholesale Dealer Price:", color = Color.Gray)
                        Text("${settings.currency} ${"%,.0f".format(prod.wholesalePrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons: VIEW PRODUCT, EDIT PRODUCT, SELL NOW, ADD STOCK
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val target = prod
                                scannedProduct = null
                                onSellNow(target)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("scanned_sell_now"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SELL NOW", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val target = prod
                                    scannedProduct = null
                                    onViewProduct(target.id)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("VIEW")
                            }

                            FilledTonalButton(
                                onClick = {
                                    val target = prod
                                    scannedProduct = null
                                    onEditProduct(target)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("EDIT")
                            }

                            FilledTonalButton(
                                onClick = {
                                    val target = prod
                                    scannedProduct = null
                                    onAddStock(target)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ STOCK")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        scannedProduct = null
                        isScanningActive = true
                    }
                ) {
                    Text("Close Scanner")
                }
            }
        )
    }
}
