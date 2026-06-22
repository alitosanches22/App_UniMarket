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
import com.google.android.material.snackbar.Snackbar

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
        cargarFavoritosDesdeApi()
    }

    private fun cargarFavoritosDesdeApi() {
        val usuario = MarketplaceRepository.usuarioAutenticado ?: return
        RepositorioRemoto.cargarFavoritos(
            idUsuario = usuario.id,
            alCargar = { favoritos ->
                if (_binding == null) return@cargarFavoritos
                MarketplaceRepository.establecerFavoritosDesdeApi(favoritos)
                renderFavorites()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@cargarFavoritos
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
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
                eliminarFavorito(product, itemBinding)
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

    private fun eliminarFavorito(product: Producto, itemBinding: ItemProductoBinding) {
        val usuario = MarketplaceRepository.usuarioAutenticado
        if (usuario == null) {
            MarketplaceRepository.removeFavorite(product.id)
            renderFavorites()
            return
        }

        itemBinding.botonFavorito.isEnabled = false
        RepositorioRemoto.cambiarFavorito(
            idUsuario = usuario.id,
            idProducto = product.id,
            guardar = false,
            alCargar = {
                if (_binding == null) return@cambiarFavorito
                MarketplaceRepository.removeFavorite(product.id)
                renderFavorites()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@cambiarFavorito
                itemBinding.botonFavorito.isEnabled = true
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            cargarFavoritosDesdeApi()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
