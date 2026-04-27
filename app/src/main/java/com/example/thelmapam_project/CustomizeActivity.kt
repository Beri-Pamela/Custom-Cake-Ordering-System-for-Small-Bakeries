package com.example.thelmapam_project

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CustomizeActivity : ComponentActivity() {

    private val viewModel: CustomizeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8F4F4) // Light cream background
                ) {
                    val design by viewModel.cakeDesign.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {
                        CustomizeScreen(
                            design = design,
                            onShapeChange = viewModel::updateShape,
                            onSizeChange = viewModel::updateSize,
                            onLayersChange = viewModel::updateLayers,
                            onMessageChange = viewModel::updateMessage,
                            onLocationChange = viewModel::updateLocation,
                            onDateChange = viewModel::updateDate,
                            onProceed = {
                                val intent = Intent(this@CustomizeActivity, OrderDetailsActivity::class.java)
                                intent.putExtra("cake_design_json", Json.encodeToString(design))
                                startActivity(intent)
                            },
                            onArPreview = {
                                startActivity(Intent(this@CustomizeActivity, ArPreviewActivity::class.java))
                            },
                            onUploadImage = {
                                startActivity(Intent(this@CustomizeActivity, ImageUploadActivity::class.java))
                            }
                        )

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFD4AF37))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeScreen(
    design: CakeDesign,
    onShapeChange: (String) -> Unit,
    onSizeChange: (String) -> Unit,
    onLayersChange: (Int) -> Unit,
    onMessageChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onProceed: () -> Unit,
    onArPreview: () -> Unit,
    onUploadImage: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            onDateChange(selectedDate.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Design Your Masterpiece", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF722F37),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section: Cake Specifications
            SectionHeader(title = "Cake Specifications")
            
            // Shape Selection
            Text(text = "Select Shape", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Round", "Square", "Heart").forEach { shape ->
                    FilterChip(
                        selected = design.shape == shape,
                        onClick = { onShapeChange(shape) },
                        label = { Text(shape) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF722F37),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Size Selection
            Text(text = "Select Size", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("8\"", "10\"", "12\"").forEach { size ->
                    FilterChip(
                        selected = design.size == size,
                        onClick = { onSizeChange(size) },
                        label = { Text(size) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF722F37),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Layers
            Text(text = "Number of Layers: ${design.layers}", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Slider(
                value = design.layers.toFloat(),
                onValueChange = { onLayersChange(it.toInt()) },
                valueRange = 1f..4f,
                steps = 2,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFD4AF37),
                    activeTrackColor = Color(0xFF722F37)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Delivery & Personalization
            SectionHeader(title = "Delivery & Personalization")

            OutlinedTextField(
                value = design.deliveryLocation,
                onValueChange = onLocationChange,
                label = { Text("Delivery Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF722F37)) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = if (design.deliveryDate > 0) dateFormatter.format(Date(design.deliveryDate)) else "",
                onValueChange = { },
                label = { Text("Delivery Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF722F37)) },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.Info, contentDescription = "Select Date", tint = Color(0xFFD4AF37))
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = design.messageText,
                onValueChange = onMessageChange,
                label = { Text("Custom Inscription") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Happy Birthday, Love...") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Price Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total Estimate", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Text(
                        text = "FCFA ${design.totalPrice}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF722F37)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onArPreview,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF722F37))
                ) {
                    Text("AR Preview")
                }
                OutlinedButton(
                    onClick = onUploadImage,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF722F37))
                ) {
                    Text("Ref Photo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF722F37)),
                enabled = design.deliveryLocation.isNotBlank() && design.deliveryDate > 0
            ) {
                Text("Confirm & Checkout", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF722F37),
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
    )
}
