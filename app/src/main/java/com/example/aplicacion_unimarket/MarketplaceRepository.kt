package com.example.aplicacion_unimarket

data class Usuario(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val carrera: String,
    val telefono: String
) {
    val nombreCompleto: String
        get() = "$nombre $apellido"
}

enum class CategoriaProducto(val nombreVisible: String) {
    LIBROS("Libros"),
    ELECTRONICOS("Electronicos"),
    LABORATORIO("Laboratorio"),
    TUTORIAS("Tutorias"),
    OTROS("Otros");

    companion object {
        fun desdeNombreVisible(valor: String): CategoriaProducto {
            return values().firstOrNull { it.nombreVisible == valor } ?: OTROS
        }
    }
}

data class Producto(
    val id: String,
    var titulo: String,
    var descripcion: String,
    var precio: Double,
    var categoria: CategoriaProducto,
    var estado: String,
    var nombreVendedor: String,
    var carreraVendedor: String,
    var cantidadImagenes: Int,
    var tieneVideo: Boolean,
    var vendido: Boolean = false
)

data class MensajeChat(
    val remitente: String,
    val contenido: String,
    val enviadoPorUsuarioActual: Boolean
)

object MarketplaceRepository {
    val currentUser = Usuario(
        nombre = "Alejandra",
        apellido = "Mendoza",
        correo = "alejandra.mendoza@universidad.edu",
        carrera = "Ingenieria en Sistemas",
        telefono = "099 555 2301"
    )

    private val products = mutableListOf(
        Producto(
            id = "p1",
            titulo = "Calculo de Stewart 8va edicion",
            descripcion = "Libro en buen estado, con ejercicios marcados y resumenes utiles para primer semestre.",
            precio = 32.00,
            categoria = CategoriaProducto.LIBROS,
            estado = "Usado - bueno",
            nombreVendedor = "Daniel Ruiz",
            carreraVendedor = "Ingenieria Civil",
            cantidadImagenes = 3,
            tieneVideo = false
        ),
        Producto(
            id = "p2",
            titulo = "Calculadora cientifica Casio fx-991",
            descripcion = "Funciona perfecto para estadistica, fisica y algebra. Incluye estuche.",
            precio = 18.50,
            categoria = CategoriaProducto.ELECTRONICOS,
            estado = "Usado - excelente",
            nombreVendedor = "Mariana Lopez",
            carreraVendedor = "Administracion",
            cantidadImagenes = 2,
            tieneVideo = true
        ),
        Producto(
            id = "p3",
            titulo = "Kit de laboratorio basico",
            descripcion = "Gafas, bata talla M y guantes reutilizables para practicas de quimica.",
            precio = 24.99,
            categoria = CategoriaProducto.LABORATORIO,
            estado = "Nuevo",
            nombreVendedor = currentUser.nombreCompleto,
            carreraVendedor = currentUser.carrera,
            cantidadImagenes = 4,
            tieneVideo = true
        ),
        Producto(
            id = "p4",
            titulo = "Tutorias de programacion Kotlin",
            descripcion = "Sesiones por hora para tareas, proyectos Android y preparacion de examenes.",
            precio = 10.00,
            categoria = CategoriaProducto.TUTORIAS,
            estado = "Disponible",
            nombreVendedor = "Sofia Andrade",
            carreraVendedor = "Software",
            cantidadImagenes = 1,
            tieneVideo = false
        ),
        Producto(
            id = "p5",
            titulo = "Mochila universitaria impermeable",
            descripcion = "Tiene compartimento para laptop de 15 pulgadas y varios bolsillos interiores.",
            precio = 21.00,
            categoria = CategoriaProducto.OTROS,
            estado = "Usado - bueno",
            nombreVendedor = "Carlos Vera",
            carreraVendedor = "Medicina",
            cantidadImagenes = 2,
            tieneVideo = false
        )
    )

    private val favoriteProductIds = mutableSetOf("p1", "p2")
    private val conversations = mutableMapOf<String, MutableList<MensajeChat>>()

    fun searchProducts(query: String = "", category: CategoriaProducto? = null): List<Producto> {
        val normalizedQuery = query.trim().lowercase()
        return products
            .filter { !it.vendido }
            .filter { product ->
                category == null || product.categoria == category
            }
            .filter { product ->
                normalizedQuery.isBlank() ||
                    product.titulo.lowercase().contains(normalizedQuery) ||
                    product.descripcion.lowercase().contains(normalizedQuery) ||
                    product.categoria.nombreVisible.lowercase().contains(normalizedQuery)
            }
    }

    fun allProducts(): List<Producto> = products.toList()

    fun myProducts(): List<Producto> {
        return products.filter { it.nombreVendedor == currentUser.nombreCompleto }
    }

    fun favoriteProducts(): List<Producto> {
        return products.filter { it.id in favoriteProductIds }
    }

    fun findProduct(idProducto: String): Producto? {
        return products.firstOrNull { it.id == idProducto }
    }

    fun isFavorite(idProducto: String): Boolean {
        return idProducto in favoriteProductIds
    }

    fun toggleFavorite(idProducto: String): Boolean {
        return if (favoriteProductIds.contains(idProducto)) {
            favoriteProductIds.remove(idProducto)
            false
        } else {
            favoriteProductIds.add(idProducto)
            true
        }
    }

    fun removeFavorite(idProducto: String) {
        favoriteProductIds.remove(idProducto)
    }

    fun addProduct(
        titulo: String,
        descripcion: String,
        precio: Double,
        categoria: CategoriaProducto,
        estado: String,
        cantidadImagenes: Int,
        tieneVideo: Boolean
    ): Producto {
        val product = Producto(
            id = "p${System.currentTimeMillis()}",
            titulo = titulo,
            descripcion = descripcion,
            precio = precio,
            categoria = categoria,
            estado = estado,
            nombreVendedor = currentUser.nombreCompleto,
            carreraVendedor = currentUser.carrera,
            cantidadImagenes = cantidadImagenes.coerceAtLeast(1),
            tieneVideo = tieneVideo
        )
        products.add(0, product)
        return product
    }

    fun updateProduct(
        idProducto: String,
        titulo: String,
        descripcion: String,
        precio: Double,
        categoria: CategoriaProducto,
        estado: String,
        cantidadImagenes: Int,
        tieneVideo: Boolean
    ) {
        findProduct(idProducto)?.apply {
            this.titulo = titulo
            this.descripcion = descripcion
            this.precio = precio
            this.categoria = categoria
            this.estado = estado
            this.cantidadImagenes = cantidadImagenes.coerceAtLeast(1)
            this.tieneVideo = tieneVideo
        }
    }

    fun markSold(idProducto: String) {
        findProduct(idProducto)?.vendido = true
        favoriteProductIds.remove(idProducto)
    }

    fun deleteProduct(idProducto: String) {
        products.removeAll { it.id == idProducto }
        favoriteProductIds.remove(idProducto)
        conversations.remove(idProducto)
    }

    fun messagesFor(idProducto: String): MutableList<MensajeChat> {
        val product = findProduct(idProducto)
        return conversations.getOrPut(idProducto) {
            mutableListOf(
                MensajeChat(
                    remitente = product?.nombreVendedor ?: "Vendedor",
                    contenido = "Hola, gracias por escribir. El producto sigue disponible.",
                    enviadoPorUsuarioActual = false
                )
            )
        }
    }

    fun sendMessage(idProducto: String, message: String) {
        val product = findProduct(idProducto)
        val conversation = messagesFor(idProducto)
        conversation.add(
            MensajeChat(
                remitente = currentUser.nombre,
                contenido = message,
                enviadoPorUsuarioActual = true
            )
        )
        conversation.add(
            MensajeChat(
                remitente = product?.nombreVendedor ?: "Vendedor",
                contenido = "Perfecto, coordinemos por este chat.",
                enviadoPorUsuarioActual = false
            )
        )
    }
}
