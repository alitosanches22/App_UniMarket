package com.example.aplicacion_unimarket

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UniMarketApiService {
    @GET("api/salud")
    fun verificarSalud(): Call<RespuestaSaludApi>

    @GET("api/categorias")
    fun obtenerCategorias(): Call<List<CategoriaApi>>

    @GET("api/productos")
    fun obtenerProductos(
        @Query("busqueda") busqueda: String? = null,
        @Query("categoriaId") categoriaId: Int? = null
    ): Call<List<ProductoApi>>

    @GET("api/productos/{idProducto}")
    fun obtenerProducto(@Path("idProducto") idProducto: String): Call<ProductoApi>

    @GET("api/usuarios/{idUsuario}/productos")
    fun obtenerProductosUsuario(@Path("idUsuario") idUsuario: String): Call<List<ProductoApi>>

    @POST("api/productos")
    fun crearProducto(@Body request: ProductoRequestApi): Call<RespuestaMensajeApi>

    @PUT("api/productos/{idProducto}")
    fun actualizarProducto(
        @Path("idProducto") idProducto: String,
        @Body request: ProductoRequestApi
    ): Call<RespuestaMensajeApi>

    @PATCH("api/productos/{idProducto}/vendido")
    fun marcarProductoVendido(@Path("idProducto") idProducto: String): Call<RespuestaMensajeApi>

    @DELETE("api/productos/{idProducto}")
    fun eliminarProducto(@Path("idProducto") idProducto: String): Call<RespuestaMensajeApi>

    @POST("api/auth/login")
    fun iniciarSesion(@Body request: LoginRequestApi): Call<UsuarioApi>

    @POST("api/auth/registro")
    fun registrarUsuario(@Body request: RegistroRequestApi): Call<UsuarioApi>

    @PUT("api/usuarios/{idUsuario}")
    fun actualizarUsuario(
        @Path("idUsuario") idUsuario: String,
        @Body request: PerfilRequestApi
    ): Call<UsuarioApi>

    @GET("api/usuarios/{idUsuario}/favoritos")
    fun obtenerFavoritos(@Path("idUsuario") idUsuario: String): Call<List<ProductoApi>>

    @POST("api/usuarios/{idUsuario}/favoritos/{idProducto}")
    fun agregarFavorito(
        @Path("idUsuario") idUsuario: String,
        @Path("idProducto") idProducto: String
    ): Call<RespuestaMensajeApi>

    @DELETE("api/usuarios/{idUsuario}/favoritos/{idProducto}")
    fun eliminarFavorito(
        @Path("idUsuario") idUsuario: String,
        @Path("idProducto") idProducto: String
    ): Call<RespuestaMensajeApi>

    @POST("api/conversaciones")
    fun abrirConversacion(@Body request: ConversacionRequestApi): Call<ConversacionApi>

    @GET("api/conversaciones/{idConversacion}/mensajes")
    fun obtenerMensajes(@Path("idConversacion") idConversacion: String): Call<List<MensajeApi>>

    @POST("api/conversaciones/{idConversacion}/mensajes")
    fun enviarMensaje(
        @Path("idConversacion") idConversacion: String,
        @Body request: MensajeRequestApi
    ): Call<RespuestaMensajeApi>
}

data class RespuestaSaludApi(
    val mensaje: String,
    val baseDatos: String?
)

data class RespuestaMensajeApi(
    val id: String?,
    val mensaje: String?
)

data class CategoriaApi(
    val id: Int,
    val nombre: String
)

data class ProductoApi(
    val id: String,
    val idVendedor: String?,
    val nombreVendedor: String?,
    val carreraVendedor: String?,
    val idCategoria: Int?,
    val categoria: String?,
    val titulo: String?,
    val descripcion: String?,
    val precio: Double?,
    val estado: String?,
    val estadoPublicacion: String?,
    val cantidadImagenes: Int?,
    val tieneVideo: Boolean?
) {
    fun convertirAProducto(): Producto {
        return Producto(
            id = id,
            idVendedor = idVendedor.orEmpty(),
            titulo = titulo.orEmpty(),
            descripcion = descripcion.orEmpty(),
            precio = precio ?: 0.0,
            categoria = CategoriaProducto.desdeNombreVisible(categoria.orEmpty()),
            estado = estado.orEmpty(),
            nombreVendedor = nombreVendedor ?: "Vendedor",
            carreraVendedor = carreraVendedor ?: "Carrera no registrada",
            cantidadImagenes = (cantidadImagenes ?: 1).coerceAtLeast(1),
            tieneVideo = tieneVideo ?: false,
            vendido = estadoPublicacion == "SOLD"
        )
    }
}

data class LoginRequestApi(
    val correo: String,
    val contrasena: String
)

data class RegistroRequestApi(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val contrasena: String,
    val carrera: String,
    val telefono: String?
)

data class PerfilRequestApi(
    val nombre: String,
    val apellido: String,
    val carrera: String,
    val telefono: String?
)

data class UsuarioApi(
    val id: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val carrera: String,
    val telefono: String?
) {
    fun convertirAUsuario(): Usuario {
        return Usuario(
            id = id,
            nombre = nombre,
            apellido = apellido,
            correo = correo,
            carrera = carrera,
            telefono = telefono.orEmpty()
        )
    }
}

data class ProductoRequestApi(
    val idVendedor: String?,
    val idCategoria: Int,
    val titulo: String,
    val descripcion: String,
    val precio: Double,
    val estado: String,
    val multimedia: List<MultimediaRequestApi>
)

data class MultimediaRequestApi(
    val tipo: String,
    val url: String
)

data class ConversacionRequestApi(
    val idProducto: String,
    val idComprador: String,
    val idVendedor: String
)

data class ConversacionApi(
    val id: String,
    val idProducto: String,
    val idComprador: String,
    val idVendedor: String
)

data class MensajeRequestApi(
    val idRemitente: String,
    val contenido: String
)

data class MensajeApi(
    val id: String,
    val idRemitente: String,
    val remitente: String,
    val contenido: String,
    val creadoEn: String?
) {
    fun convertirAMensaje(idUsuarioActual: String): MensajeChat {
        return MensajeChat(
            id = id,
            idRemitente = idRemitente,
            remitente = remitente,
            contenido = contenido,
            enviadoPorUsuarioActual = idRemitente == idUsuarioActual
        )
    }
}
