package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentoDetalleProductoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentoDetalleProductoBinding? = null
    private val binding get() = _binding!!
    private var idProducto: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentoDetalleProductoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        idProducto = requireArguments().getString(ARG_PRODUCT_ID).orEmpty()
        renderProduct()
        cargarProductoDesdeApi()
    }

    private fun renderProduct() {
        val product = MarketplaceRepository.findProduct(idProducto)
        if (product == null) {
            Snackbar.make(binding.root, "La publicacion ya no existe.", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        binding.textoCategoriaPrincipal.text = product.categoria.nombreVisible
        binding.textoTitulo.text = product.titulo
        binding.textoPrecio.text = "$${String.format("%.2f", product.precio)}"
        binding.textoDescripcion.text = product.descripcion
        binding.textoCategoria.text = product.categoria.nombreVisible
        binding.textoCondicion.text = if (product.vendido) "Vendido" else product.estado
        binding.textoVendedor.text = "${product.nombreVendedor} - ${product.carreraVendedor}"
        binding.panelVideo.isVisible = product.tieneVideo
        binding.textoSinVideo.isVisible = !product.tieneVideo
        binding.botonFavorito.text =
            if (MarketplaceRepository.isFavorite(product.id)) "Quitar de favoritos" else "Agregar a favoritos"

        renderGallery(product.cantidadImagenes)

        binding.botonFavorito.setOnClickListener {
            cambiarFavorito(product)
        }

        val isOwner = esProductoDelUsuario(product)
        binding.botonContactarVendedor.isVisible = !isOwner
        binding.botonContactarVendedor.setOnClickListener {
            if (!isOwner) {
                findNavController().navigate(
                    R.id.accion_detalle_producto_a_chat,
                    bundleOf(ARG_PRODUCT_ID to product.id)
                )
            }
        }

        binding.grupoAccionesDueno.isVisible = isOwner
        binding.botonEditarProducto.setOnClickListener {
            findNavController().navigate(
                R.id.accion_detalle_producto_a_publicar_producto,
                bundleOf(ARG_PRODUCT_ID to product.id)
            )
        }
        binding.botonMarcarVendido.setOnClickListener {
            marcarVendido(product.id)
        }
        binding.botonEliminarProducto.setOnClickListener {
            confirmDelete(product.id)
        }
    }

    private fun cargarProductoDesdeApi() {
        RepositorioRemoto.cargarProducto(
            idProducto = idProducto,
            alCargar = { producto ->
                if (_binding == null) return@cargarProducto
                MarketplaceRepository.guardarProductoDesdeApi(producto)
                renderProduct()
            },
            alFallar = {
                // Si falla, se conserva el producto que ya estaba en cache local.
            }
        )
    }

    private fun cambiarFavorito(product: Producto) {
        val usuario = MarketplaceRepository.usuarioAutenticado
        val guardar = !MarketplaceRepository.isFavorite(product.id)

        if (usuario == null) {
            MarketplaceRepository.toggleFavorite(product.id)
            renderProduct()
            return
        }

        binding.botonFavorito.isEnabled = false
        RepositorioRemoto.cambiarFavorito(
            idUsuario = usuario.id,
            idProducto = product.id,
            guardar = guardar,
            alCargar = {
                if (_binding == null) return@cambiarFavorito
                MarketplaceRepository.setFavorite(product.id, guardar)
                renderProduct()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@cambiarFavorito
                binding.botonFavorito.isEnabled = true
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun marcarVendido(idProducto: String) {
        binding.botonMarcarVendido.isEnabled = false
        RepositorioRemoto.marcarProductoVendido(
            idProducto = idProducto,
            alCargar = {
                if (_binding == null) return@marcarProductoVendido
                MarketplaceRepository.markSold(idProducto)
                renderProduct()
                Snackbar.make(binding.root, "Producto marcado como vendido.", Snackbar.LENGTH_SHORT).show()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@marcarProductoVendido
                binding.botonMarcarVendido.isEnabled = true
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun esProductoDelUsuario(product: Producto): Boolean {
        val usuario = MarketplaceRepository.usuarioAutenticado ?: MarketplaceRepository.currentUser
        return if (product.idVendedor.isNotBlank()) {
            product.idVendedor == usuario.id
        } else {
            product.nombreVendedor == usuario.nombreCompleto
        }
    }

    private fun renderGallery(imageCount: Int) {
        binding.contenedorGaleria.removeAllViews()
        repeat(imageCount.coerceAtLeast(1)) { index ->
            val item = TextView(requireContext()).apply {
                text = "Imagen ${index + 1}"
                gravity = Gravity.CENTER
                setTextColor(resources.getColor(R.color.unimarket_ink, null))
                setBackgroundResource(R.drawable.bg_gallery_item)
            }
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.gallery_item_width),
                resources.getDimensionPixelSize(R.dimen.gallery_item_height)
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            }
            binding.contenedorGaleria.addView(item, params)
        }
    }

    private fun confirmDelete(idProducto: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar publicacion")
            .setMessage("Esta accion quitara el producto de la lista principal y favoritos.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto(idProducto)
            }
            .show()
    }

    private fun eliminarProducto(idProducto: String) {
        RepositorioRemoto.eliminarProducto(
            idProducto = idProducto,
            alCargar = {
                if (_binding == null) return@eliminarProducto
                MarketplaceRepository.deleteProduct(idProducto)
                findNavController().navigate(R.id.accion_detalle_producto_a_inicio)
            },
            alFallar = { mensaje ->
                if (_binding == null) return@eliminarProducto
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PRODUCT_ID = "idProducto"
    }
}
