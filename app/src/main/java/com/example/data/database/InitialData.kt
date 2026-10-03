package com.example.data.database

import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product

object InitialData {
    suspend fun populateDatabase(database: AppDatabase) {
        val categoryDao = database.categoryDao()
        val productDao = database.productDao()
        val customerDao = database.customerDao()
        val orderDao = database.orderDao()

        val categories = listOf(
            Category(id = 1, name = "High-Tech & Informatique", description = "Ordinateurs, smartphones et périphériques", iconName = "computer"),
            Category(id = 2, name = "Mode & Vêtements", description = "Habits, vestes et chaussures tendance", iconName = "checkroom"),
            Category(id = 3, name = "Maison & Cuisine", description = "Électroménager, déco et mobilier", iconName = "home"),
            Category(id = 4, name = "Accessoires & Gadgets", description = "Sacs, montres et équipements", iconName = "watch"),
            Category(id = 5, name = "Beauté & Soins", description = "Cosmétiques, parfums et bien-être", iconName = "spa")
        )
        categoryDao.insertAll(categories)

        val products = listOf(
            Product(
                id = 1,
                name = "PC Portable Pro Ultra 15\"",
                description = "Processeur i7, 16Go RAM, 512Go SSD NVMe, écran IPS FHD anti-reflet",
                price = 1099.00,
                promoPrice = 999.00,
                stock = 8,
                categoryId = 1,
                categoryName = "High-Tech & Informatique",
                sku = "TECH-001",
                imageUrl = "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 2,
                name = "Smartphone Apex 5G 256Go",
                description = "Écran OLED 120Hz, triple capteur photo 50MP, charge ultra-rapide 65W",
                price = 799.00,
                promoPrice = 749.00,
                stock = 14,
                categoryId = 1,
                categoryName = "High-Tech & Informatique",
                sku = "TECH-002",
                imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 3,
                name = "Écouteurs Sans Fil Pro ANC",
                description = "Réduction active du bruit, autonomie 32h avec boîtier, Bluetooth 5.3",
                price = 129.50,
                promoPrice = null,
                stock = 25,
                categoryId = 1,
                categoryName = "High-Tech & Informatique",
                sku = "TECH-003",
                imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 4,
                name = "Montre Connectée Sport GPS",
                description = "Cardiofréquencemètre optique, étanche 50m, suivi sommeil et SpO2",
                price = 189.00,
                promoPrice = 169.00,
                stock = 3, // Low stock alert
                categoryId = 4,
                categoryName = "Accessoires & Gadgets",
                sku = "ACC-001",
                imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 5,
                name = "Clavier Mécanique RGB Sans Fil",
                description = "Switches Brown tactiles et silencieux, châssis aluminium brossé",
                price = 89.90,
                promoPrice = null,
                stock = 12,
                categoryId = 1,
                categoryName = "High-Tech & Informatique",
                sku = "TECH-004",
                imageUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 6,
                name = "Veste Cuir Bomber Élégance",
                description = "Cuir d'agneau souple véritable, doublure chaude matelassée",
                price = 159.00,
                promoPrice = 139.00,
                stock = 4, // Low stock alert
                categoryId = 2,
                categoryName = "Mode & Vêtements",
                sku = "MODE-001",
                imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 7,
                name = "Sneakers Confort Running",
                description = "Amorti gel dynamique, mesh respirant, semelle haute adhérence",
                price = 79.99,
                promoPrice = null,
                stock = 19,
                categoryId = 2,
                categoryName = "Mode & Vêtements",
                sku = "MODE-002",
                imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 8,
                name = "Cafetière Espresso Pression 19 bars",
                description = "Broyeur à grains intégré, buse vapeur inox pour cappuccino crémeux",
                price = 249.00,
                promoPrice = 219.00,
                stock = 6,
                categoryId = 3,
                categoryName = "Maison & Cuisine",
                sku = "MAIS-001",
                imageUrl = "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 9,
                name = "Sac à Dos Urbain Anti-Vol",
                description = "Poche ordinateur 15.6\", port de charge USB externe, tissu déperlant",
                price = 45.00,
                promoPrice = null,
                stock = 32,
                categoryId = 4,
                categoryName = "Accessoires & Gadgets",
                sku = "ACC-002",
                imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500&q=80",
                isActive = true
            ),
            Product(
                id = 10,
                name = "Lampe Bureau Architecte LED",
                description = "3 modes d'éclairage, variateur d'intensité tactile, bras articulé",
                price = 34.90,
                promoPrice = null,
                stock = 2, // Low stock
                categoryId = 3,
                categoryName = "Maison & Cuisine",
                sku = "MAIS-002",
                imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500&q=80",
                isActive = true
            )
        )
        productDao.insertAll(products)

        val customers = listOf(
            Customer(
                id = 1,
                fullName = "Jean Dupont",
                email = "jean.dupont@orange.fr",
                phone = "+33 6 12 34 56 78",
                address = "14 Rue de la Paix",
                city = "Paris",
                totalOrders = 3,
                totalSpent = 1248.50
            ),
            Customer(
                id = 2,
                fullName = "Sophie Martin",
                email = "sophie.martin@gmail.com",
                phone = "+33 6 98 76 54 32",
                address = "27 Avenue Foch",
                city = "Lyon",
                totalOrders = 2,
                totalSpent = 318.00
            ),
            Customer(
                id = 3,
                fullName = "Marc Lefebvre",
                email = "marc.lefebvre@yahoo.com",
                phone = "+33 7 45 67 89 01",
                address = "5 Boulevard Michelet",
                city = "Marseille",
                totalOrders = 1,
                totalSpent = 1099.00
            ),
            Customer(
                id = 4,
                fullName = "Aminata Diallo",
                email = "aminata.diallo@outlook.com",
                phone = "+33 6 88 11 22 33",
                address = "8 Cours Victor Hugo",
                city = "Bordeaux",
                totalOrders = 1,
                totalSpent = 84.80
            ),
            Customer(
                id = 5,
                fullName = "Thomas Bernard",
                email = "thomas.bernard@free.fr",
                phone = "+33 7 22 33 44 55",
                address = "19 Rue Faidherbe",
                city = "Lille",
                totalOrders = 2,
                totalSpent = 253.90
            )
        )
        customerDao.insertAll(customers)

        val orders = listOf(
            Order(
                id = 1,
                orderNumber = "CMD-2026-001",
                customerId = 1,
                customerName = "Jean Dupont",
                customerEmail = "jean.dupont@orange.fr",
                totalAmount = 928.50,
                status = "PAYEE",
                paymentMethod = "Carte Bancaire",
                shippingAddress = "14 Rue de la Paix, 75002 Paris",
                createdAt = System.currentTimeMillis() - 86400000L * 2,
                notes = "Livraison express demandée"
            ),
            Order(
                id = 2,
                orderNumber = "CMD-2026-002",
                customerId = 2,
                customerName = "Sophie Martin",
                customerEmail = "sophie.martin@gmail.com",
                totalAmount = 159.00,
                status = "EXPEDIEE",
                paymentMethod = "PayPal",
                shippingAddress = "27 Avenue Foch, 69006 Lyon",
                createdAt = System.currentTimeMillis() - 86400000L * 3,
                notes = "Colissimo suivi #FR8923746"
            ),
            Order(
                id = 3,
                orderNumber = "CMD-2026-003",
                customerId = 3,
                customerName = "Marc Lefebvre",
                customerEmail = "marc.lefebvre@yahoo.com",
                totalAmount = 1099.00,
                status = "LIVREE",
                paymentMethod = "Virement Bancaire",
                shippingAddress = "5 Boulevard Michelet, 13008 Marseille",
                createdAt = System.currentTimeMillis() - 86400000L * 5,
                notes = "Colis remis en main propre"
            ),
            Order(
                id = 4,
                orderNumber = "CMD-2026-004",
                customerId = 4,
                customerName = "Aminata Diallo",
                customerEmail = "aminata.diallo@outlook.com",
                totalAmount = 84.80,
                status = "EN_ATTENTE",
                paymentMethod = "Carte Bancaire",
                shippingAddress = "8 Cours Victor Hugo, 33000 Bordeaux",
                createdAt = System.currentTimeMillis() - 3600000L * 4,
                notes = "En attente de confirmation stock"
            ),
            Order(
                id = 5,
                orderNumber = "CMD-2026-005",
                customerId = 5,
                customerName = "Thomas Bernard",
                customerEmail = "thomas.bernard@free.fr",
                totalAmount = 219.00,
                status = "PAYEE",
                paymentMethod = "Apple Pay",
                shippingAddress = "19 Rue Faidherbe, 59800 Lille",
                createdAt = System.currentTimeMillis() - 3600000L * 8,
                notes = ""
            )
        )
        orderDao.insertAll(orders)

        val orderItems = listOf(
            OrderItem(id = 1, orderId = 1, productId = 2, productName = "Smartphone Apex 5G 256Go", quantity = 1, unitPrice = 799.00, subtotal = 799.00),
            OrderItem(id = 2, orderId = 1, productId = 3, productName = "Écouteurs Sans Fil Pro ANC", quantity = 1, unitPrice = 129.50, subtotal = 129.50),
            OrderItem(id = 3, orderId = 2, productId = 6, productName = "Veste Cuir Bomber Élégance", quantity = 1, unitPrice = 159.00, subtotal = 159.00),
            OrderItem(id = 4, orderId = 3, productId = 1, productName = "PC Portable Pro Ultra 15\"", quantity = 1, unitPrice = 1099.00, subtotal = 1099.00),
            OrderItem(id = 5, orderId = 4, productId = 9, productName = "Sac à Dos Urbain Anti-Vol", quantity = 1, unitPrice = 45.00, subtotal = 45.00),
            OrderItem(id = 6, orderId = 4, productId = 10, productName = "Lampe Bureau Architecte LED", quantity = 1, unitPrice = 34.90, subtotal = 34.90),
            OrderItem(id = 7, orderId = 5, productId = 8, productName = "Cafetière Espresso Pression 19 bars", quantity = 1, unitPrice = 219.00, subtotal = 219.00)
        )
        orderDao.insertOrderItems(orderItems)
    }
}
