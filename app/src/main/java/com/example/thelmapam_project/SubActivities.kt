package com.example.thelmapam_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ImageUploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_upload)
    }
}

class ArPreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_preview)
        findViewById<Button>(R.id.fab_place_cake)?.setOnClickListener {
            startActivity(Intent(this, OrderDetailsActivity::class.java))
            finish()
        }
    }
}

@AndroidEntryPoint
class OrderDetailsActivity : AppCompatActivity() {
    
    @Inject lateinit var repository: CakeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val designJson = intent.getStringExtra("cake_design_json")
        val design = if (designJson != null) {
            Json.decodeFromString<CakeDesign>(designJson)
        } else {
            CakeDesign()
        }

        setContent {
            MaterialTheme {
                OrderSummaryScreen(
                    design = design,
                    onConfirm = { processOrder(design) },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun processOrder(design: CakeDesign) {
        val order = Order(
            orderId = UUID.randomUUID().toString(),
            cakeDesign = design,
            deliveryType = "Home Delivery",
            date = design.deliveryDate,
            address = design.deliveryLocation,
            status = "Paid & Pending",
            createdAt = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            repository.saveOrder(order)
            
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:mengnjohpamela@gmail.com") 
                putExtra(Intent.EXTRA_SUBJECT, "URGENT: New Cake Order Received!")
                val body = """
                    Hello Pamela,
                    
                    A new order has been placed via the App.
                    
                    Order Details:
                    - ID: ${order.orderId.takeLast(6).uppercase()}
                    - Design: ${design.shape} Cake (${design.size})
                    - Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(design.deliveryDate))}
                    - Location: ${design.deliveryLocation}
                    - Message: ${design.messageText}
                    - Total: FCFA ${design.totalPrice}
                    
                    Check your Firestore dashboard for more specs.
                """.trimIndent()
                putExtra(Intent.EXTRA_TEXT, body)
            }
            
            val intent = Intent(this@OrderDetailsActivity, SuccessActivity::class.java)
            intent.putExtra("email_intent", emailIntent)
            startActivity(intent)
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryScreen(design: CakeDesign, onConfirm: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Final Review", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
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
                .background(Color(0xFFF8F4F4))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                    startY = 100f
                                )
                            )
                    )
                    Text(
                        text = "${design.shape} Masterpiece",
                        modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SummarySection("Configuration") {
                SummaryDetailRow(Icons.Default.Info, "Size & Layers", "${design.size} • ${design.layers} Layers")
                SummaryDetailRow(Icons.Default.Star, "Shape", design.shape)
            }

            Spacer(modifier = Modifier.height(16.dp))

            SummarySection("Delivery Details") {
                SummaryDetailRow(Icons.Default.LocationOn, "Address", design.deliveryLocation)
                SummaryDetailRow(Icons.Default.DateRange, "Schedule", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(design.deliveryDate)))
            }

            if (design.messageText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SummarySection("Personalization") {
                    SummaryDetailRow(Icons.Default.Edit, "Inscription", "\"${design.messageText}\"")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total Amount", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Text(
                        text = "FCFA ${design.totalPrice}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF722F37))
            ) {
                Text("Place Order & Notify Admin", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SummarySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF722F37),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SummaryDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFFD4AF37)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = "$label: ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

class SuccessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val emailIntent = intent.getParcelableExtra<Intent>("email_intent")
        
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.padding(24.dp).size(72.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "Order Confirmed!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF722F37)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Your order is now being processed. Pamela (Admin) has been notified. You can now relax while we bake your dream!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Button(
                            onClick = {
                                if (emailIntent != null) {
                                    startActivity(Intent.createChooser(emailIntent, "Send Confirmation Email"))
                                }
                                val mainIntent = Intent(this@SuccessActivity, MainActivity::class.java)
                                mainIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(mainIntent)
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF722F37))
                        ) {
                            Text("Back to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@AndroidEntryPoint
class CakeDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val templateJson = intent.getStringExtra("template_json")
        val template = if (templateJson != null) {
            Json.decodeFromString<CakeTemplate>(templateJson)
        } else null

        setContent {
            MaterialTheme {
                if (template != null) {
                    CakeDetailScreen(
                        template = template,
                        onCustomize = {
                            val intent = Intent(this, CustomizeActivity::class.java)
                            startActivity(intent)
                        },
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CakeDetailScreen(template: CakeTemplate, onCustomize: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Starting from", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                        Text(text = "FCFA ${template.basePrice}", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF722F37))
                    }
                    Button(
                        onClick = onCustomize,
                        modifier = Modifier.height(56.dp).padding(start = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF722F37))
                    ) {
                        Text("Customize Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F4F4))
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                AsyncImage(
                    model = template.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).statusBarsPadding().background(Color.White.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF722F37)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = Color(0xFFFFF9E6),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⭐ Top Rated Cake",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A luxury creation from our expert bakers. This ${template.name} is designed to elevate your celebration with sophisticated taste and artistic design. We use only the finest premium ingredients.",
                    color = Color.Gray,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailHighlight(Icons.Default.Face, "Premium")
                    DetailHighlight(Icons.Default.Favorite, "Artisanal")
                    DetailHighlight(Icons.Default.Star, "Luxury")
                }
            }
        }
    }
}

@Composable
fun DetailHighlight(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = Color.White
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = Color(0xFFD4AF37)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
