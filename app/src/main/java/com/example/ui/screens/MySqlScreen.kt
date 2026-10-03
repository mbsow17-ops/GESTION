package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.MySqlConnectionConfig
import com.example.ui.components.CodeBlockViewer
import com.example.ui.components.SqlResultTableView
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MySqlScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Console SQL", "Tables & Schéma", "Connexion MySQL", "Export Script DDL")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("mysql_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        when (selectedTab) {
            0 -> SqlConsoleTab(viewModel)
            1 -> DatabaseSchemaTab(viewModel)
            2 -> MySqlConnectionTab(viewModel)
            3 -> SqlExportTab(viewModel)
        }
    }
}

@Composable
fun SqlConsoleTab(viewModel: ShopViewModel) {
    var queryText by remember { mutableStateOf(viewModel.sqlQueryInput.value) }
    val sqlResult by viewModel.sqlQueryResult.collectAsState()
    val isExecuting by viewModel.isExecutingSql.collectAsState()

    val presets = listOf(
        "Produits en stock" to "SELECT id, name, price, stock, sku FROM produits WHERE stock > 0 ORDER BY stock DESC;",
        "Alertes rupture" to "SELECT id, name, price, stock, sku FROM produits WHERE stock <= 5;",
        "Moyenne par catégorie" to "SELECT categoryName, COUNT(*) as nb_articles, ROUND(AVG(price), 2) as prix_moyen FROM produits GROUP BY categoryName;",
        "Commandes récentes" to "SELECT orderNumber, customerName, totalAmount, status FROM commandes ORDER BY id DESC;",
        "Top clients dépensiers" to "SELECT fullName, email, totalOrders, totalSpent FROM clients ORDER BY totalSpent DESC;"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Console Interactive MySQL",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Exécutez vos requêtes SQL réelles sur les tables de la boutique.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Query presets
        item {
            Text(
                text = "Requêtes rapides :",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets) { (title, sql) ->
                    FilterChip(
                        selected = queryText == sql,
                        onClick = {
                            queryText = sql
                            viewModel.executeSql(sql)
                        },
                        label = { Text(title, fontSize = 11.sp) }
                    )
                }
            }
        }

        // SQL Query Input Box
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Éditeur SQL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { queryText = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Effacer", modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sql_query_input"),
                        minLines = 3,
                        maxLines = 6,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.executeSql(queryText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_sql_query"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = queryText.isNotBlank() && !isExecuting
                    ) {
                        if (isExecuting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exécution en cours...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exécuter la Requête SQL")
                        }
                    }
                }
            }
        }

        // Results Section
        item {
            Text(
                text = "Résultats de la requête :",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (sqlResult != null) {
                SqlResultTableView(result = sqlResult!!)
            } else {
                Text(
                    text = "Aucune requête exécutée.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DatabaseSchemaTab(viewModel: ShopViewModel) {
    val products by viewModel.allProducts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    val tables = listOf(
        TableSchema(
            name = "produits",
            rowCount = products.size,
            description = "Catalogue de vente (prix, stock, sku, liaisons)",
            columns = listOf(
                ColumnDef("id", "BIGINT AUTO_INCREMENT", isPrimary = true),
                ColumnDef("name", "VARCHAR(200) NOT NULL"),
                ColumnDef("description", "TEXT"),
                ColumnDef("price", "DECIMAL(10,2) NOT NULL"),
                ColumnDef("promo_price", "DECIMAL(10,2) NULL"),
                ColumnDef("stock", "INT DEFAULT 0"),
                ColumnDef("category_id", "BIGINT (FK categories.id)", isForeignKey = true),
                ColumnDef("sku", "VARCHAR(60) UNIQUE"),
                ColumnDef("is_active", "BOOLEAN DEFAULT TRUE"),
                ColumnDef("created_at", "TIMESTAMP")
            )
        ),
        TableSchema(
            name = "categories",
            rowCount = categories.size,
            description = "Catégories de classement pour la boutique",
            columns = listOf(
                ColumnDef("id", "BIGINT AUTO_INCREMENT", isPrimary = true),
                ColumnDef("name", "VARCHAR(150) NOT NULL"),
                ColumnDef("description", "TEXT"),
                ColumnDef("icon_name", "VARCHAR(50)")
            )
        ),
        TableSchema(
            name = "commandes",
            rowCount = orders.size,
            description = "Commandes passées par les acheteurs",
            columns = listOf(
                ColumnDef("id", "BIGINT AUTO_INCREMENT", isPrimary = true),
                ColumnDef("order_number", "VARCHAR(50) UNIQUE"),
                ColumnDef("customer_id", "BIGINT (FK clients.id)", isForeignKey = true),
                ColumnDef("customer_name", "VARCHAR(150) NOT NULL"),
                ColumnDef("total_amount", "DECIMAL(12,2) NOT NULL"),
                ColumnDef("status", "ENUM('EN_ATTENTE','PAYEE',...)"),
                ColumnDef("payment_method", "VARCHAR(80)"),
                ColumnDef("shipping_address", "TEXT"),
                ColumnDef("created_at", "TIMESTAMP")
            )
        ),
        TableSchema(
            name = "clients",
            rowCount = customers.size,
            description = "Base de données clientèle & historique d'achats",
            columns = listOf(
                ColumnDef("id", "BIGINT AUTO_INCREMENT", isPrimary = true),
                ColumnDef("full_name", "VARCHAR(150) NOT NULL"),
                ColumnDef("email", "VARCHAR(150) UNIQUE NOT NULL"),
                ColumnDef("phone", "VARCHAR(30)"),
                ColumnDef("address", "VARCHAR(255)"),
                ColumnDef("city", "VARCHAR(100)"),
                ColumnDef("total_spent", "DECIMAL(12,2) DEFAULT 0.00")
            )
        ),
        TableSchema(
            name = "lignes_commande",
            rowCount = 7,
            description = "Détail des articles associés à chaque commande",
            columns = listOf(
                ColumnDef("id", "BIGINT AUTO_INCREMENT", isPrimary = true),
                ColumnDef("order_id", "BIGINT (FK commandes.id)", isForeignKey = true),
                ColumnDef("product_id", "BIGINT (FK produits.id)", isForeignKey = true),
                ColumnDef("product_name", "VARCHAR(200) NOT NULL"),
                ColumnDef("quantity", "INT NOT NULL"),
                ColumnDef("unit_price", "DECIMAL(10,2) NOT NULL"),
                ColumnDef("subtotal", "DECIMAL(12,2) NOT NULL")
            )
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Structure Relationnelle MySQL (InnoDB)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Schéma officiel normalisé avec clés primaires et étrangères pour l'e-commerce.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(tables) { table ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(table.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${table.rowCount} ligne(s)",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(table.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        table.columns.forEach { col ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (col.isPrimary) {
                                        Icon(Icons.Default.Key, contentDescription = "PK", tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = col.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (col.isPrimary) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = col.type,
                                    fontSize = 11.sp,
                                    color = if (col.isForeignKey) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MySqlConnectionTab(viewModel: ShopViewModel) {
    val currentConfig by viewModel.mySqlConfig.collectAsState()
    val isTesting by viewModel.isTestingConnection.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    var host by remember { mutableStateOf(currentConfig.host) }
    var port by remember { mutableStateOf(currentConfig.port.toString()) }
    var database by remember { mutableStateOf(currentConfig.database) }
    var user by remember { mutableStateOf(currentConfig.user) }
    var password by remember { mutableStateOf(currentConfig.password) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Paramètres du Serveur MySQL",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configurez la chaîne de connexion JDBC pour l'application JSF et le serveur distant.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Connection status card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (connectionStatus?.first == true) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (connectionStatus?.first == true) Color(0xFF10B981) else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (connectionStatus?.first == true) Icons.Default.CheckCircle else Icons.Default.Storage,
                        contentDescription = null,
                        tint = if (connectionStatus?.first == true) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (connectionStatus?.first == true) "Connexion MySQL Active" else "Prêt à se connecter",
                            fontWeight = FontWeight.Bold,
                            color = if (connectionStatus?.first == true) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = connectionStatus?.second ?: "jdbc:mysql://$host:${port.ifBlank { "3306" }}/$database",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (connectionStatus?.first == true) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Hôte MySQL (IP ou Domaine)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = database,
                    onValueChange = { database = it },
                    label = { Text("Nom de la Base") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Utilisateur") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            Button(
                onClick = {
                    val config = MySqlConnectionConfig(
                        host = host,
                        port = port.toIntOrNull() ?: 3306,
                        database = database,
                        user = user,
                        password = password
                    )
                    viewModel.testMySqlConnection(config)
                },
                modifier = Modifier.fillMaxWidth().testTag("test_mysql_conn_button"),
                shape = RoundedCornerShape(10.dp),
                enabled = !isTesting
            ) {
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test de connexion en cours...")
                } else {
                    Icon(Icons.Default.CloudDone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tester la Connexion MySQL")
                }
            }
        }
    }
}

@Composable
fun SqlExportTab(viewModel: ShopViewModel) {
    val sqlScript = remember { viewModel.getMySqlDdlScript() }
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Script SQL Complet (DDL & DML)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Importable directement dans phpMyAdmin ou MySQL Workbench.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(sqlScript))
                        copied = true
                        scope.launch {
                            delay(2500)
                            copied = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("copy_sql_script_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (copied) "Copié !" else "Copier SQL", fontSize = 12.sp)
                }
            }
        }

        item {
            CodeBlockViewer(
                filename = "ecommerce_boutique.sql",
                code = sqlScript,
                language = "sql"
            )
        }
    }
}

data class ColumnDef(val name: String, val type: String, val isPrimary: Boolean = false, val isForeignKey: Boolean = false)
data class TableSchema(val name: String, val rowCount: Int, val description: String, val columns: List<ColumnDef>)
