package com.example.thelmapam_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        // Load Popular This Week images
        val popular1Image = view.findViewById<ImageView>(R.id.popular1_image)
        val popular2Image = view.findViewById<ImageView>(R.id.popular2_image)

        Glide.with(this)
            .load("https://images.unsplash.com/photo-1535141192574-5d4897c12636?w=800&q=80")
            .centerCrop()
            .into(popular1Image)

        Glide.with(this)
            .load("https://images.unsplash.com/photo-1562440499-64c9a111f713?w=800&q=80")
            .centerCrop()
            .into(popular2Image)

        // Load images for templates
        val template1Image = view.findViewById<ImageView>(R.id.template1_image)
        val template2Image = view.findViewById<ImageView>(R.id.template2_image)

        Glide.with(this)
            .load("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&q=80")
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .centerCrop()
            .into(template1Image)

        Glide.with(this)
            .load("https://images.unsplash.com/photo-1606312619070-d48b4c652a52?w=500&q=80")
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .centerCrop()
            .into(template2Image)

        view.findViewById<android.view.View>(R.id.fab_start_new_cake)?.setOnClickListener {
            startActivity(Intent(requireContext(), CustomizeActivity::class.java))
        }
        
        // Setup click listeners for templates to go to Detail screen
        view.findViewById<View>(R.id.card_template_1)?.setOnClickListener {
             navigateToDetail(0)
        }
        view.findViewById<View>(R.id.card_template_2)?.setOnClickListener {
             navigateToDetail(1)
        }
        
        // Popular items clicks
        popular1Image.setOnClickListener { navigateToDetail(0) }
        popular2Image.setOnClickListener { navigateToDetail(1) }

        return view
    }

    private fun navigateToDetail(index: Int) {
        val templates = (viewModel.templates.value)
        if (templates.isNotEmpty() && index < templates.size) {
            val intent = Intent(requireContext(), CakeDetailActivity::class.java)
            intent.putExtra("template_json", Json.encodeToString(templates[index]))
            startActivity(intent)
        } else {
             // Fallback for demo if repo isn't ready
             startActivity(Intent(requireContext(), CustomizeActivity::class.java))
        }
    }
}

@AndroidEntryPoint
class OrdersFragment : Fragment() {
    private val viewModel: OrdersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F4F4)) {
                        OrdersScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersScreen(viewModel: OrdersViewModel) {
    val orders by viewModel.orders.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "My Orders",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF722F37)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders placed yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(orders) { order ->
                    OrderCard(order)
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=200",
                contentDescription = null,
                modifier = Modifier.size(64.dp).background(Color(0xFFF8F4F4), RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Order #${order.orderId.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                Text(text = "${order.cakeDesign.shape} Cake • ${order.cakeDesign.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = order.status, color = Color(0xFFD4AF37), fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }
            Text(text = "FCFA ${order.cakeDesign.totalPrice}", fontWeight = FontWeight.Bold, color = Color(0xFF722F37))
        }
    }
}

@AndroidEntryPoint
class ChatFragment : Fragment() {
    
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        ChatScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("SweetCreations Assistant", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF722F37), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(messages) { msg ->
                val alignment = if (msg.isUser) Arrangement.End else Arrangement.Start
                val color = if (msg.isUser) Color(0xFF722F37) else Color(0xFFF8F4F4)
                val textColor = if (msg.isUser) Color.White else Color.Black
                
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = alignment) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = color),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = msg.text, modifier = Modifier.padding(12.dp), color = textColor)
                    }
                }
            }
        }
        
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = Color(0xFFD4AF37))
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about cake designs...") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                enabled = !isLoading && inputText.isNotBlank()
            ) {
                Icon(painter = painterResource(android.R.drawable.ic_menu_send), contentDescription = null, tint = Color(0xFF722F37))
            }
        }
    }
}

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                        ProfileScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            color = Color(0xFFF8F4F4)
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_myplaces),
                contentDescription = null,
                modifier = Modifier.padding(24.dp),
                tint = Color(0xFF722F37)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "John Doe", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "johndoe@example.com", color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ProfileMenuItem("My Account")
        ProfileMenuItem("Delivery Addresses")
        ProfileMenuItem("Payment Methods")
        ProfileMenuItem("Settings")
        
        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(onClick = { /* Logout */ }) {
            Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileMenuItem(title: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 16.sp)
            Icon(painter = painterResource(android.R.drawable.ic_media_play), contentDescription = null, modifier = Modifier.size(16.dp))
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))
    }
}
