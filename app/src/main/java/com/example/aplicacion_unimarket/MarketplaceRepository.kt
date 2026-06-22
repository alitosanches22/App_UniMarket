package com.example.aplicacion_unimarket

data class Usuario(
    val id: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val carrera: String,
    val telefono: String
) {
    val nombreCompleto: String
        get() = "$nombre $apellido"
}

enum class CategoriaProducto(val nombreVisible: String, val idApi: Int) {
    LIBROS("Libros", 1),
    ELECTRONICOS("Electronicos", 2),
    LABORATORIO("Laboratorio", 3),
    TUTORIAS("Tutorias", 4),
    OTROS("Otros", 5);

    companion object {
        fun desdeNombreVisible(valor: String): CategoriaProducto {
            return values().firstOrNull { it.nombreVisible == valor } ?: OTROS
        }
    }
}

data class Producto(
    val id: String,
    val idVendedor: String = "",
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
    val id: String = "",
    val idRemitente: String = "",
    val remitente: String,
    val contenido: String,
    val enviadoPorUsuarioActual: Boolean
)

object MarketplaceRepository {
    private val usuarioDemo = Usuario(
        id = "local-actual",
        nombre = "Alejandra",
        apellido = "Mendoza",
        correo = "alejandra.mendoza@universidad.edu",
        carrera = "Ingenieria en Sistemas",
        telefono = "099 555 2301"
    )

    private var usuarioSesion: Usuario? = null

    val currentUser: Usuario
        get() = usuarioSesion ?: usuarioDemo

    val usuarioAutenticado: Usuario?
        get() = usuarioSesion

    private val products = mutableListOf(
        Producto(
            id = "p1",
            idVendedor = "local-daniel",
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
            idVendedor = "local-mariana",
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
            idVendedor = usuarioDemo.id,
            titulo = "Kit de laboratorio basico",
            descripcion = "Gafas, bata talla M y guantes reutilizables para practicas de quimica.",
            precio = 24.99,
            categoria = CategoriaProducto.LABORATORIO,
            estado = "Nuevo",
            nombreVendedor = usuarioDemo.nombreCompleto,
            carreraVendedor = usuarioDemo.carrera,
            cantidadImagenes = 4,
            tieneVideo = true
        ),
        Producto(
            id = "p4",
            idVendedor = "local-sofia",
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
            idVendedor = "local-carlos",
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

    fun iniciarSesion(usuario: Usuario) {
        usuarioSesion = usuario
        favoriteProductIds.clear()
    }

    fun actualizarUsuario(usuario: Usuario) {
        usuarioSesion = usuario
        products.forEach { producto ->
            if (producto.idVendedor == usuario.id) {
                producto.nombreVendedor = usuario.nombreCompleto
                producto.carreraVendedor = usuario.carrera
            }
        }
    }

    fun cerrarSesion() {
        usuarioSesion = null
        favoriteProductIds.clear()
        conversations.clear()
    }

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

    fun reemplazarProductosDesdeApi(productosApi: List<Producto>) {
        products.removeAll { !it.vendido || productosApi.any { producto -> producto.id == it.id } }
        products.addAll(0, productosApi)
    }

    fun guardarProductoDesdeApi(producto: Producto) {
        products.removeAll { it.id == producto.id }
        products.add(0, producto)
    }

    fun guardarProductosDesdeApi(productosApi: List<Producto>) {
        val ids = productosApi.map { it.id }.toSet()
        products.removeAll { it.id in ids }
        products.addAll(0, productosApi)
    }

    fun reemplazarMisProductosDesdeApi(productosApi: List<Producto>) {
        val usuario = usuarioSesion ?: return
        val ids = productosApi.map { it.id }.toSet()
        products.removeAll { it.id in ids || it.idVendedor == usuario.id }
        products.addAll(0, productosApi)
    }

    fun establecerFavoritosDesdeApi(productosApi: List<Producto>) {
        favoriteProductIds.clear()
        favoriteProductIds.addAll(productosApi.map { it.id })
        guardarProductosDesdeApi(productosApi)
    }

    fun myProducts(): List<Producto> {
        val usuario = currentUser
        return products.filter {
            if (usuario.id.isNotBlank()) {
                it.idVendedor == usuario.id
            } else {
                it.nombreVendedor == usuario.nombreCompleto
            }
        }
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

    fun setFavorite(idProducto: String, esFavorito: Boolean) {
        if (esFavorito) {
            favoriteProductIds.add(idProducto)
        } else {
            favoriteProductIds.remove(idProducto)
        }
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
        val usuario = currentUser
        val product = Producto(
            id = "p${System.currentTimeMillis()}",
            idVendedor = usuario.id,
            titulo = titulo,
            descripcion = descripcion,
            precio = precio,
            categoria = categoria,
            estado = estado,
            nombreVendedor = usuario.nombreCompleto,
            carreraVendedor = usuario.carrera,
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

    fun reemplazarMensajes(idProducto: String, mensajes: List<MensajeChat>) {
        conversations[idProducto] = mensajes.toMutableList()
    }

    fun sendMessage(idProducto: String, message: String) {
        val product = findProduct(idProducto)
        val conversation = messagesFor(idProducto)
        conversation.add(
            MensajeChat(
                idRemitente = currentUser.id,
                remitente = currentUser.nombre,
                contenido = message,
                enviadoPorUsuarioActual = true
            )
        )
        conversation.add(
            MensajeChat(
                idRemitente = product?.idVendedor.orEmpty(),
                remitente = product?.nombreVendedor ?: "Vendedor",
                contenido = "Perfecto, coordinemos por este chat.",
                enviadoPorUsuarioActual = false
            )
        )
    }
}
