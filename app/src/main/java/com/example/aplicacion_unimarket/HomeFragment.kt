package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentoInicioBinding
import com.example.aplicacion_unimarket.databinding.ItemProductoBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentoInicioBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: CategoriaProducto? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentoInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategory(binding.chipTodo, null)
        setupCategory(binding.chipLibros, CategoriaProducto.LIBROS)
        setupCategory(binding.chipElectronicos, CategoriaProducto.ELECTRONICOS)
        setupCategory(binding.chipLaboratorio, CategoriaProducto.LABORATORIO)
        setupCategory(binding.chipTutorias, CategoriaProducto.TUTORIAS)
        setupCategory(binding.chipOtros, CategoriaProducto.OTROS)

        binding.entradaBusqueda.doOnTextChanged { _, _, _, _ ->
            renderProducts()
            cargarProductosDesdeApi()
        }
        binding.botonPublicarProducto.setOnClickListener {
            findNavController().navigate(R.id.accion_inicio_a_publicar_producto)
        }
        binding.botonFavoritos.setOnClickListener {
            findNavController().navigate(R.id.accion_inicio_a_favoritos)
        }
        binding.botonPerfil.setOnClickListener {
            findNavController().navigate(R.id.accion_inicio_a_perfil)
        }

        renderProducts()
        cargarProductosDesdeApi()
        cargarFavoritosDesdeApi()
    }

    private fun setupCategory(chip: Chip, category: CategoriaProducto?) {
        chip.setOnClickListener {
            selectedCategory = category
            renderProducts()
            cargarProductosDesdeApi()
        }
    }

    private fun renderProducts() {
        val query = binding.entradaBusqueda.text?.toString().orEmpty()
        val products = MarketplaceRepository.searchProducts(query, selectedCategory)
        binding.contenedorProductos.removeAllViews()
        binding.textoEstadoVacio.isVisible = products.isEmpty()

        products.forEach { product ->
            val itemBinding = ItemProductoBinding.inflate(layoutInflater, binding.contenedorProductos, false)
            bindProduct(itemBinding, product)
            binding.contenedorProductos.addView(itemBinding.root)
        }
    }

    private fun cargarProductosDesdeApi() {
        val query = binding.entradaBusqueda.text?.toString().orEmpty()
        RepositorioRemoto.cargarProductos(
            busqueda = query,
            categoria = selectedCategory,
            alCargar = { productos ->
                if (_binding == null) return@cargarProductos
                MarketplaceRepository.reemplazarProductosDesdeApi(productos)
                renderProducts()
                cargarFavoritosDesdeApi()
            },
            alFallar = {
                // Si el backend no esta encendido, la app conserva los datos locales de prueba.
            }
        )
    }

    private fun cargarFavoritosDesdeApi() {
        val usuario = MarketplaceRepository.usuarioAutenticado ?: return
        RepositorioRemoto.cargarFavoritos(
            idUsuario = usuario.id,
            alCargar = { favoritos ->
                if (_binding == null) return@cargarFavoritos
                MarketplaceRepository.establecerFavoritosDesdeApi(favoritos)
                renderProducts()
            },
            alFallar = {
                // La lista principal puede funcionar aunque favoritos no se carguen todavia.
            }
        )
    }

    private fun bindProduct(itemBinding: ItemProductoBinding, product: Producto) {
        itemBinding.textoMiniatura.text = product.categoria.nombreVisible.take(3).uppercase()
        itemBinding.textoTitulo.text = product.titulo
        itemBinding.textoPrecio.text = "$${String.format("%.2f", product.precio)}"
        itemBinding.textoDescripcion.text = product.descripcion
        itemBinding.textoCategoria.text = product.categoria.nombreVisible
        itemBinding.textoEstado.text = if (product.vendido) "Vendido" else product.estado
        itemBinding.textoVendedor.text = product.nombreVendedor
        itemBinding.botonFavorito.text =
            if (MarketplaceRepository.isFavorite(product.id)) "Guardado" else "Guardar"

        itemBinding.root.setOnClickListener {
            openDetail(product.id)
        }
        itemBinding.botonFavorito.setOnClickListener {
            cambiarFavorito(product, itemBinding)
        }
    }

    private fun cambiarFavorito(product: Producto, itemBinding: ItemProductoBinding) {
        val usuario = MarketplaceRepository.usuarioAutenticado
        val guardar = !MarketplaceRepository.isFavorite(product.id)

        if (usuario == null) {
            MarketplaceRepository.toggleFavorite(product.id)
            renderProducts()
            return
        }

        itemBinding.botonFavorito.isEnabled = false
        RepositorioRemoto.cambiarFavorito(
            idUsuario = usuario.id,
            idProducto = product.id,
            guardar = guardar,
            alCargar = {
                if (_binding == null) return@cambiarFavorito
                MarketplaceRepository.setFavorite(product.id, guardar)
                renderProducts()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@cambiarFavorito
                itemBinding.botonFavorito.isEnabled = true
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun openDetail(idProducto: String) {
        findNavController().navigate(
            R.id.accion_inicio_a_detalle_producto,
            bundleOf(ProductDetailFragment.ARG_PRODUCT_ID to idProducto)
        )
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            renderProducts()
            cargarProductosDesdeApi()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
