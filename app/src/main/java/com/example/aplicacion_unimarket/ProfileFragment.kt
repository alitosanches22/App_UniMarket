package com.example.aplicacion_unimarket

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentoPerfilBinding
import com.example.aplicacion_unimarket.databinding.ItemMiProductoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileFragment : Fragment() {

    private var _binding: FragmentoPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentoPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderUsuario()
        configurarBotones()
        renderStats()
        renderMyProducts()
        cargarDatosPerfil()
    }

    private fun renderUsuario() {
        val user = MarketplaceRepository.currentUser
        binding.textoAvatar.text = user.nombre.first().uppercase()
        binding.textoNombre.text = user.nombreCompleto
        binding.textoCorreo.text = user.correo
        binding.textoCarrera.text = user.carrera
        binding.textoTelefono.text = user.telefono
    }

    private fun configurarBotones() {
        binding.botonFavoritos.setOnClickListener {
            findNavController().navigate(R.id.accion_perfil_a_favoritos)
        }
        binding.botonPublicar.setOnClickListener {
            findNavController().navigate(R.id.accion_perfil_a_publicar_producto)
        }
        binding.botonEditarPerfil.setOnClickListener {
            mostrarDialogoEditarPerfil()
        }
        binding.botonCerrarSesion.setOnClickListener {
            MarketplaceRepository.cerrarSesion()
            findNavController().navigate(R.id.accion_perfil_a_inicio_sesion)
        }
    }

    private fun cargarDatosPerfil() {
        val usuario = MarketplaceRepository.usuarioAutenticado ?: return
        RepositorioRemoto.cargarMisProductos(
            idUsuario = usuario.id,
            alCargar = { productos ->
                if (_binding == null) return@cargarMisProductos
                MarketplaceRepository.reemplazarMisProductosDesdeApi(productos)
                renderStats()
                renderMyProducts()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@cargarMisProductos
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )

        RepositorioRemoto.cargarFavoritos(
            idUsuario = usuario.id,
            alCargar = { favoritos ->
                if (_binding == null) return@cargarFavoritos
                MarketplaceRepository.establecerFavoritosDesdeApi(favoritos)
                renderStats()
            },
            alFallar = {
                // El perfil puede seguir mostrando los datos principales aunque favoritos tarde en cargar.
            }
        )
    }

    private fun renderStats() {
        val myProducts = MarketplaceRepository.myProducts()
        binding.textoCantidadPublicaciones.text = myProducts.size.toString()
        binding.textoCantidadVentas.text = myProducts.count { it.vendido }.toString()
        binding.textoCantidadFavoritos.text = MarketplaceRepository.favoriteProducts().size.toString()
    }

    private fun renderMyProducts() {
        binding.contenedorMisProductos.removeAllViews()
        MarketplaceRepository.myProducts().forEach { product ->
            val itemBinding = ItemMiProductoBinding.inflate(layoutInflater, binding.contenedorMisProductos, false)
            itemBinding.textoTitulo.text = product.titulo
            itemBinding.textoPrecio.text = "$${String.format("%.2f", product.precio)}"
            itemBinding.textoEstado.text = if (product.vendido) "Vendido" else product.estado
            itemBinding.botonEditar.setOnClickListener {
                findNavController().navigate(
                    R.id.accion_perfil_a_publicar_producto,
                    bundleOf(ProductDetailFragment.ARG_PRODUCT_ID to product.id)
                )
            }
            itemBinding.botonVendido.setOnClickListener {
                marcarVendido(product.id)
            }
            itemBinding.botonEliminar.setOnClickListener {
                eliminarProducto(product.id)
            }
            itemBinding.root.setOnClickListener {
                findNavController().navigate(
                    R.id.accion_perfil_a_detalle_producto,
                    bundleOf(ProductDetailFragment.ARG_PRODUCT_ID to product.id)
                )
            }
            binding.contenedorMisProductos.addView(itemBinding.root)
        }
    }

    private fun marcarVendido(idProducto: String) {
        RepositorioRemoto.marcarProductoVendido(
            idProducto = idProducto,
            alCargar = {
                if (_binding == null) return@marcarProductoVendido
                MarketplaceRepository.markSold(idProducto)
                renderStats()
                renderMyProducts()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@marcarProductoVendido
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun eliminarProducto(idProducto: String) {
        RepositorioRemoto.eliminarProducto(
            idProducto = idProducto,
            alCargar = {
                if (_binding == null) return@eliminarProducto
                MarketplaceRepository.deleteProduct(idProducto)
                renderStats()
                renderMyProducts()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@eliminarProducto
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun mostrarDialogoEditarPerfil() {
        val usuario = MarketplaceRepository.usuarioAutenticado
        if (usuario == null) {
            Snackbar.make(binding.root, "Inicia sesion para editar tu perfil.", Snackbar.LENGTH_LONG).show()
            return
        }

        val contenedor = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                resources.getDimensionPixelSize(R.dimen.spacing_lg),
                resources.getDimensionPixelSize(R.dimen.spacing_sm),
                resources.getDimensionPixelSize(R.dimen.spacing_lg),
                0
            )
        }

        val entradaNombre = agregarCampo(contenedor, "Nombre", usuario.nombre)
        val entradaApellido = agregarCampo(contenedor, "Apellido", usuario.apellido)
        val entradaCarrera = agregarCampo(contenedor, "Carrera", usuario.carrera)
        val entradaTelefono = agregarCampo(contenedor, "Telefono", usuario.telefono)

        val dialogo = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar perfil")
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .show()

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val usuarioActualizado = usuario.copy(
                nombre = entradaNombre.text?.toString().orEmpty().trim(),
                apellido = entradaApellido.text?.toString().orEmpty().trim(),
                carrera = entradaCarrera.text?.toString().orEmpty().trim(),
                telefono = entradaTelefono.text?.toString().orEmpty().trim()
            )

            if (
                usuarioActualizado.nombre.isBlank() ||
                usuarioActualizado.apellido.isBlank() ||
                usuarioActualizado.carrera.isBlank()
            ) {
                Snackbar.make(binding.root, "Nombre, apellido y carrera son obligatorios.", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            RepositorioRemoto.actualizarUsuario(
                usuario = usuarioActualizado,
                alCargar = { usuarioGuardado ->
                    if (_binding == null) return@actualizarUsuario
                    MarketplaceRepository.actualizarUsuario(usuarioGuardado)
                    renderUsuario()
                    renderStats()
                    renderMyProducts()
                    dialogo.dismiss()
                    Snackbar.make(binding.root, "Perfil actualizado.", Snackbar.LENGTH_SHORT).show()
                },
                alFallar = { mensaje ->
                    if (_binding == null) return@actualizarUsuario
                    Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun agregarCampo(
        contenedor: LinearLayout,
        etiqueta: String,
        valor: String
    ): TextInputEditText {
        val layout = TextInputLayout(requireContext()).apply {
            hint = etiqueta
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            }
        }

        val entrada = TextInputEditText(layout.context).apply {
            setText(valor)
        }

        layout.addView(entrada)
        contenedor.addView(layout)
        return entrada
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            cargarDatosPerfil()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
