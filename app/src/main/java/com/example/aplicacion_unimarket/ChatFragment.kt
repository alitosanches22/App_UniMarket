package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.aplicacion_unimarket.databinding.FragmentoChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentoChatBinding? = null
    private val binding get() = _binding!!
    private var idProducto: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentoChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        idProducto = requireArguments().getString(ProductDetailFragment.ARG_PRODUCT_ID).orEmpty()

        val product = MarketplaceRepository.findProduct(idProducto)
        binding.textoTituloChat.text = product?.nombreVendedor ?: "Vendedor"
        binding.textoContextoProducto.text = product?.titulo ?: "Producto"

        binding.botonEnviar.setOnClickListener {
            val message = binding.entradaMensaje.text?.toString().orEmpty().trim()
            if (message.isBlank()) return@setOnClickListener
            MarketplaceRepository.sendMessage(idProducto, message)
            binding.entradaMensaje.text?.clear()
            renderMessages()
        }

        renderMessages()
    }

    private fun renderMessages() {
        binding.contenedorMensajes.removeAllViews()
        MarketplaceRepository.messagesFor(idProducto).forEach { message ->
            val messageView = TextView(requireContext()).apply {
                text = "${message.remitente}\n${message.contenido}"
                setTextColor(resources.getColor(R.color.unimarket_ink, null))
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.spacing_md),
                    resources.getDimensionPixelSize(R.dimen.spacing_sm),
                    resources.getDimensionPixelSize(R.dimen.spacing_md),
                    resources.getDimensionPixelSize(R.dimen.spacing_sm)
                )
                setBackgroundResource(
                    if (message.enviadoPorUsuarioActual) R.drawable.bg_message_me else R.drawable.bg_message_other
                )
            }
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.chat_bubble_max_width),
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = if (message.enviadoPorUsuarioActual) Gravity.END else Gravity.START
                bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            }
            binding.contenedorMensajes.addView(messageView, params)
        }
        binding.scrollMensajes.post {
            binding.scrollMensajes.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
