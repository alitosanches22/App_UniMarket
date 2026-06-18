package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentoPublicarProductoBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max

class PublishProductFragment : Fragment() {

    private var _binding: FragmentoPublicarProductoBinding? = null
    private val binding get() = _binding!!
    private var selectedImageCount = 1
    private var hasSelectedVideo = false
    private var editingProductId: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImageCount = max(1, uris.size)
        updateMultimediaLabels()
    }

    private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        hasSelectedVideo = uri != null
        updateMultimediaLabels()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentoPublicarProductoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingProductId = arguments?.getString(ProductDetailFragment.ARG_PRODUCT_ID)
        setupSpinners()
        setupMultimediaButtons()
        fillFormIfEditing()

        binding.botonPublicar.setOnClickListener {
            saveProduct()
        }
    }

    private fun setupSpinners() {
        val categories = CategoriaProducto.values().map { it.nombreVisible }
        binding.selectorCategoria.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        val conditions = listOf("Nuevo", "Usado - excelente", "Usado - bueno", "Usado - aceptable", "Disponible")
        binding.selectorEstado.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            conditions
        )
    }

    private fun setupMultimediaButtons() {
        binding.botonAgregarImagenes.setOnClickListener {
            imagePicker.launch("image/*")
        }
        binding.botonAgregarVideo.setOnClickListener {
            videoPicker.launch("video/*")
        }
        updateMultimediaLabels()
    }

    private fun fillFormIfEditing() {
        val product = editingProductId?.let { MarketplaceRepository.findProduct(it) } ?: return
        binding.textoTituloPantalla.text = "Editar publicacion"
        binding.botonPublicar.text = "Guardar cambios"
        binding.entradaTitulo.setText(product.titulo)
        binding.entradaDescripcion.setText(product.descripcion)
        binding.entradaPrecio.setText(String.format("%.2f", product.precio))
        selectedImageCount = product.cantidadImagenes
        hasSelectedVideo = product.tieneVideo

        val categoryPosition = CategoriaProducto.values().indexOf(product.categoria)
        if (categoryPosition >= 0) {
            binding.selectorCategoria.setSelection(categoryPosition)
        }

        val conditionAdapter = binding.selectorEstado.adapter
        for (index in 0 until conditionAdapter.count) {
            if (conditionAdapter.getItem(index) == product.estado) {
                binding.selectorEstado.setSelection(index)
                break
            }
        }

        updateMultimediaLabels()
    }

    private fun saveProduct() {
        val titulo = binding.entradaTitulo.text?.toString().orEmpty().trim()
        val descripcion = binding.entradaDescripcion.text?.toString().orEmpty().trim()
        val precio = binding.entradaPrecio.text?.toString().orEmpty().toDoubleOrNull()

        if (titulo.isBlank() || descripcion.isBlank() || precio == null) {
            Snackbar.make(binding.root, "Completa titulo, descripcion y precio valido.", Snackbar.LENGTH_SHORT).show()
            return
        }

        val categoria = CategoriaProducto.desdeNombreVisible(binding.selectorCategoria.selectedItem.toString())
        val estado = binding.selectorEstado.selectedItem.toString()

        val idProducto = editingProductId
        if (idProducto == null) {
            MarketplaceRepository.addProduct(
                titulo = titulo,
                descripcion = descripcion,
                precio = precio,
                categoria = categoria,
                estado = estado,
                cantidadImagenes = selectedImageCount,
                tieneVideo = hasSelectedVideo
            )
        } else {
            MarketplaceRepository.updateProduct(
                idProducto = idProducto,
                titulo = titulo,
                descripcion = descripcion,
                precio = precio,
                categoria = categoria,
                estado = estado,
                cantidadImagenes = selectedImageCount,
                tieneVideo = hasSelectedVideo
            )
        }

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.fragmentoInicio, false)
            .build()
        findNavController().navigate(R.id.accion_publicar_producto_a_inicio, null, navOptions)
    }

    private fun updateMultimediaLabels() {
        if (_binding == null) return
        binding.textoCantidadImagenes.text = "$selectedImageCount imagen(es) seleccionada(s)"
        binding.textoEstadoVideo.text = if (hasSelectedVideo) "Video demostrativo agregado" else "Sin video demostrativo"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
