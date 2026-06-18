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
            MarketplaceRepository.toggleFavorite(product.id)
            renderProduct()
        }

        binding.botonContactarVendedor.setOnClickListener {
            findNavController().navigate(
                R.id.accion_detalle_producto_a_chat,
                bundleOf(ARG_PRODUCT_ID to product.id)
            )
        }

        val isOwner = product.nombreVendedor == MarketplaceRepository.currentUser.nombreCompleto
        binding.grupoAccionesDueno.isVisible = isOwner
        binding.botonEditarProducto.setOnClickListener {
            findNavController().navigate(
                R.id.accion_detalle_producto_a_publicar_producto,
                bundleOf(ARG_PRODUCT_ID to product.id)
            )
        }
        binding.botonMarcarVendido.setOnClickListener {
            MarketplaceRepository.markSold(product.id)
            renderProduct()
        }
        binding.botonEliminarProducto.setOnClickListener {
            confirmDelete(product.id)
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
                MarketplaceRepository.deleteProduct(idProducto)
                findNavController().navigate(R.id.accion_detalle_producto_a_inicio)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PRODUCT_ID = "idProducto"
    }
}
