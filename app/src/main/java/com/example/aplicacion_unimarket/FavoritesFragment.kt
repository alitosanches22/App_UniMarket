package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentoFavoritosBinding
import com.example.aplicacion_unimarket.databinding.ItemProductoBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentoFavoritosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentoFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderFavorites()
    }

    private fun renderFavorites() {
        val products = MarketplaceRepository.favoriteProducts()
        binding.contenedorFavoritos.removeAllViews()
        binding.textoEstadoVacio.isVisible = products.isEmpty()

        products.forEach { product ->
            val itemBinding = ItemProductoBinding.inflate(layoutInflater, binding.contenedorFavoritos, false)
            itemBinding.textoMiniatura.text = product.categoria.nombreVisible.take(3).uppercase()
            itemBinding.textoTitulo.text = product.titulo
            itemBinding.textoPrecio.text = "$${String.format("%.2f", product.precio)}"
            itemBinding.textoDescripcion.text = product.descripcion
            itemBinding.textoCategoria.text = product.categoria.nombreVisible
            itemBinding.textoEstado.text = product.estado
            itemBinding.textoVendedor.text = product.nombreVendedor
            itemBinding.botonFavorito.text = "Eliminar"
            itemBinding.botonFavorito.setOnClickListener {
                MarketplaceRepository.removeFavorite(product.id)
                renderFavorites()
            }
            itemBinding.root.setOnClickListener {
                findNavController().navigate(
                    R.id.accion_favoritos_a_detalle_producto,
                    bundleOf(ProductDetailFragment.ARG_PRODUCT_ID to product.id)
                )
            }
            binding.contenedorFavoritos.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
