package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentoPerfilBinding
import com.example.aplicacion_unimarket.databinding.ItemMiProductoBinding
import com.google.android.material.snackbar.Snackbar

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
        val user = MarketplaceRepository.currentUser
        binding.textoAvatar.text = user.nombre.first().uppercase()
        binding.textoNombre.text = user.nombreCompleto
        binding.textoCorreo.text = user.correo
        binding.textoCarrera.text = user.carrera
        binding.textoTelefono.text = user.telefono

        binding.botonFavoritos.setOnClickListener {
            findNavController().navigate(R.id.accion_perfil_a_favoritos)
        }
        binding.botonPublicar.setOnClickListener {
            findNavController().navigate(R.id.accion_perfil_a_publicar_producto)
        }
        binding.botonEditarPerfil.setOnClickListener {
            Snackbar.make(binding.root, "La edicion de perfil se conectara al backend.", Snackbar.LENGTH_SHORT).show()
        }
        binding.botonCerrarSesion.setOnClickListener {
            findNavController().navigate(R.id.accion_perfil_a_inicio_sesion)
        }

        renderStats()
        renderMyProducts()
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
                MarketplaceRepository.markSold(product.id)
                renderStats()
                renderMyProducts()
            }
            itemBinding.botonEliminar.setOnClickListener {
                MarketplaceRepository.deleteProduct(product.id)
                renderStats()
                renderMyProducts()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
