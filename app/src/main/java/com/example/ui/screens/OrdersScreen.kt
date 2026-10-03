package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.ui.components.OrderStatusBadge
import com.example.viewmodel.ShopViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.filteredOrders.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val currentStatusFilter by viewModel.orderStatusFilter.collectAsState()

    var showCreateOrderDialog by remember { mutableStateOf(false) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    val statuses = listOf(
        null to "Toutes",
        "EN_ATTENTE" to "En attente",
        "PAYEE" to "Payées",
        "EXPEDIEE" to "Expédiées",
        "LIVREE" to "Livrées",
        "ANNULEE" to "Annulées"
    )

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("orders_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateOrderDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_order")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle commande")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Status Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statuses) { (statusKey, statusLabel) ->
                    FilterChip(
                        selected = currentStatusFilter == statusKey,
                        onClick = { viewModel.orderStatusFilter.value = statusKey },
                        label = { Text(statusLabel) }
                    )
                }
            }

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucune commande trouvée",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderItemCard(
                            order = order,
                            currencyFormat = currencyFormat,
                            onUpdateStatus = { newStatus ->
                                viewModel.updateOrderStatus(order.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateOrderDialog) {
        CreateOrderDialog(
            customers = customers,
            products = products,
            onDismiss = { showCreateOrderDialog = false },
            onConfirm = { order, items ->
                viewModel.createOrder(order, items)
                showCreateOrderDialog = false
            }
        )
    }
}

@Composable
fun OrderItemCard(
    order: Order,
    currencyFormat: NumberFormat,
    onUpdateStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Order Number, Date, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.orderNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OrderStatusBadge(order.status)
                }

                // Status Change Action Dropdown
                Box {
                    OutlinedButton(
                        onClick = { showStatusMenu = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Changer", fontSize = 11.sp)
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Marquer 'Payée'") },
                            onClick = {
                                onUpdateStatus("PAYEE")
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Marquer 'Expédiée'") },
                            onClick = {
                                onUpdateStatus("EXPEDIEE")
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Marquer 'Livrée'") },
                            onClick = {
                                onUpdateStatus("LIVREE")
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Marquer 'En attente'") },
                            onClick = {
                                onUpdateStatus("EN_ATTENTE")
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Annuler la commande") },
                            onClick = {
                                onUpdateStatus("ANNULEE")
                                showStatusMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Client & Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = order.customerEmail.ifBlank { order.paymentMethod },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = currencyFormat.format(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Expand toggle
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (expanded) "Moins de détails" else "Détails de livraison & paiement",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Expanded content
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adresse de livraison : ${order.shippingAddress}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Moyen de paiement : ${order.paymentMethod}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (order.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notes : ${order.notes}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateOrderDialog(
    customers: List<Customer>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (Order, List<OrderItem>) -> Unit
) {
    var selectedCustomerId by remember { mutableStateOf(customers.firstOrNull()?.id ?: 1L) }
    var selectedProductId by remember { mutableStateOf(products.firstOrNull()?.id ?: 1L) }
    var quantityStr by remember { mutableStateOf("1") }
    var paymentMethod by remember { mutableStateOf("Carte Bancaire") }
    var notes by remember { mutableStateOf("") }

    val customer = customers.find { it.id == selectedCustomerId } ?: customers.firstOrNull()
    val product = products.find { it.id == selectedProductId } ?: products.firstOrNull()

    val quantity = quantityStr.toIntOrNull() ?: 1
    val unitPrice = product?.let { it.promoPrice ?: it.price } ?: 0.0
    val totalAmount = unitPrice * quantity

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle Commande Client") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Sélectionner un Client :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(customers) { c ->
                            FilterChip(
                                selected = selectedCustomerId == c.id,
                                onClick = { selectedCustomerId = c.id },
                                label = { Text(c.fullName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    Text("Sélectionner un Produit :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(products) { p ->
                            FilterChip(
                                selected = selectedProductId == p.id,
                                onClick = { selectedProductId = p.id },
                                label = { Text("${p.name} (${currencyFormat.format(p.price)})", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantité") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Moyen de paiement") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Remarques / Instructions de livraison") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total à facturer :", fontWeight = FontWeight.Bold)
                            Text(
                                text = currencyFormat.format(totalAmount),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customer != null && product != null) {
                        val orderNum = "CMD-${System.currentTimeMillis() % 100000}"
                        val newOrder = Order(
                            orderNumber = orderNum,
                            customerId = customer.id,
                            customerName = customer.fullName,
                            customerEmail = customer.email,
                            totalAmount = totalAmount,
                            status = "PAYEE",
                            paymentMethod = paymentMethod,
                            shippingAddress = "${customer.address}, ${customer.city}",
                            notes = notes
                        )
                        val orderItem = OrderItem(
                            orderId = 0,
                            productId = product.id,
                            productName = product.name,
                            quantity = quantity,
                            unitPrice = unitPrice,
                            subtotal = totalAmount
                        )
                        onConfirm(newOrder, listOf(orderItem))
                    }
                },
                modifier = Modifier.testTag("submit_create_order")
            ) {
                Text("Valider la Commande")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
