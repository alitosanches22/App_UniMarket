package com.example.aplicacion_unimarket

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RepositorioRemoto {
    fun iniciarSesion(
        correo: String,
        contrasena: String,
        alCargar: (Usuario) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio ->
                servicio.iniciarSesion(LoginRequestApi(correo = correo, contrasena = contrasena))
            },
            alCargar = { usuarioApi -> alCargar(usuarioApi.convertirAUsuario()) },
            alFallar = alFallar
        )
    }

    fun registrarUsuario(
        nombre: String,
        apellido: String,
        correo: String,
        contrasena: String,
        carrera: String,
        telefono: String,
        alCargar: (Usuario) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio ->
                servicio.registrarUsuario(
                    RegistroRequestApi(
                        nombre = nombre,
                        apellido = apellido,
                        correo = correo,
                        contrasena = contrasena,
                        carrera = carrera,
                        telefono = telefono.ifBlank { null }
                    )
                )
            },
            alCargar = { usuarioApi -> alCargar(usuarioApi.convertirAUsuario()) },
            alFallar = alFallar
        )
    }

    fun actualizarUsuario(
        usuario: Usuario,
        alCargar: (Usuario) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio ->
                servicio.actualizarUsuario(
                    usuario.id,
                    PerfilRequestApi(
                        nombre = usuario.nombre,
                        apellido = usuario.apellido,
                        carrera = usuario.carrera,
                        telefono = usuario.telefono.ifBlank { null }
                    )
                )
            },
            alCargar = { usuarioApi -> alCargar(usuarioApi.convertirAUsuario()) },
            alFallar = alFallar
        )
    }

    fun cargarProductos(
        busqueda: String = "",
        categoria: CategoriaProducto? = null,
        alCargar: (List<Producto>) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio ->
                servicio.obtenerProductos(
                    busqueda = busqueda.ifBlank { null },
                    categoriaId = categoria?.idApi
                )
            },
            alCargar = { productosApi ->
                alCargar(productosApi.map { it.convertirAProducto() })
            },
            alFallar = alFallar
        )
    }

    fun cargarProducto(
        idProducto: String,
        alCargar: (Producto) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio -> servicio.obtenerProducto(idProducto) },
            alCargar = { productoApi -> alCargar(productoApi.convertirAProducto()) },
            alFallar = alFallar
        )
    }

    fun cargarMisProductos(
        idUsuario: String,
        alCargar: (List<Producto>) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio -> servicio.obtenerProductosUsuario(idUsuario) },
            alCargar = { productosApi ->
                alCargar(productosApi.map { it.convertirAProducto() })
            },
            alFallar = alFallar
        )
    }

    fun guardarProducto(
        idProducto: String?,
        idVendedor: String,
        titulo: String,
        descripcion: String,
        precio: Double,
        categoria: CategoriaProducto,
        estado: String,
        cantidadImagenes: Int,
        tieneVideo: Boolean,
        alCargar: () -> Unit,
        alFallar: (String) -> Unit
    ) {
        val request = ProductoRequestApi(
            idVendedor = idVendedor,
            idCategoria = categoria.idApi,
            titulo = titulo,
            descripcion = descripcion,
            precio = precio,
            estado = estado,
            multimedia = crearMultimedia(cantidadImagenes, tieneVideo)
        )

        ejecutar(
            crearLlamada = { servicio ->
                if (idProducto == null) {
                    servicio.crearProducto(request)
                } else {
                    servicio.actualizarProducto(idProducto, request)
                }
            },
            alCargar = { alCargar() },
            alFallar = alFallar
        )
    }

    fun marcarProductoVendido(
        idProducto: String,
        alCargar: () -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio -> servicio.marcarProductoVendido(idProducto) },
            alCargar = { alCargar() },
            alFallar = alFallar
        )
    }

    fun eliminarProducto(
        idProducto: String,
        alCargar: () -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio -> servicio.eliminarProducto(idProducto) },
            alCargar = { alCargar() },
            alFallar = alFallar
        )
    }

    fun cargarFavoritos(
        idUsuario: String,
        alCargar: (List<Producto>) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio -> servicio.obtenerFavoritos(idUsuario) },
            alCargar = { productosApi ->
                alCargar(productosApi.map { it.convertirAProducto() })
            },
            alFallar = alFallar
        )
    }

    fun cambiarFavorito(
        idUsuario: String,
        idProducto: String,
        guardar: Boolean,
        alCargar: () -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio ->
                if (guardar) {
                    servicio.agregarFavorito(idUsuario, idProducto)
                } else {
                    servicio.eliminarFavorito(idUsuario, idProducto)
                }
            },
            alCargar = { alCargar() },
            alFallar = alFallar
        )
    }

    fun abrirConversacion(
        idProducto: String,
        idComprador: String,
        idVendedor: String,
        alCargar: (ConversacionApi) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio ->
                servicio.abrirConversacion(
                    ConversacionRequestApi(
                        idProducto = idProducto,
                        idComprador = idComprador,
                        idVendedor = idVendedor
                    )
                )
            },
            alCargar = alCargar,
            alFallar = alFallar
        )
    }

    fun cargarMensajes(
        idConversacion: String,
        idUsuarioActual: String,
        alCargar: (List<MensajeChat>) -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio -> servicio.obtenerMensajes(idConversacion) },
            alCargar = { mensajesApi ->
                alCargar(mensajesApi.map { it.convertirAMensaje(idUsuarioActual) })
            },
            alFallar = alFallar
        )
    }

    fun enviarMensaje(
        idConversacion: String,
        idRemitente: String,
        contenido: String,
        alCargar: () -> Unit,
        alFallar: (String) -> Unit
    ) {
        ejecutar(
            crearLlamada = { servicio ->
                servicio.enviarMensaje(
                    idConversacion,
                    MensajeRequestApi(idRemitente = idRemitente, contenido = contenido)
                )
            },
            alCargar = { alCargar() },
            alFallar = alFallar
        )
    }

    private fun crearMultimedia(
        cantidadImagenes: Int,
        tieneVideo: Boolean
    ): List<MultimediaRequestApi> {
        val imagenes = List(cantidadImagenes.coerceAtLeast(1)) { indice ->
            MultimediaRequestApi(
                tipo = "IMAGE",
                url = "app://imagen-${indice + 1}"
            )
        }
        val video = if (tieneVideo) {
            listOf(MultimediaRequestApi(tipo = "VIDEO", url = "app://video-demostrativo"))
        } else {
            emptyList()
        }
        return imagenes + video
    }

    private fun <T> ejecutar(
        crearLlamada: (UniMarketApiService) -> Call<T>,
        alCargar: (T) -> Unit,
        alFallar: (String) -> Unit
    ) {
        val servicios = ApiConfig.servicios

        fun intentar(indice: Int) {
            if (indice >= servicios.size) {
                alFallar("No se pudo conectar con la API.")
                return
            }

            crearLlamada(servicios[indice]).enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            alCargar(body)
                        } else {
                            alFallar("La API respondio sin datos.")
                        }
                    } else {
                        alFallar(extraerMensajeError(response))
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    if (indice < servicios.lastIndex) {
                        intentar(indice + 1)
                    } else {
                        alFallar(t.message ?: "No se pudo conectar con la API.")
                    }
                }
            })
        }

        intentar(0)
    }

    private fun <T> extraerMensajeError(response: Response<T>): String {
        return response.errorBody()?.string()
            ?.takeIf { it.isNotBlank() }
            ?: "La API respondio con error ${response.code()}."
    }
}
