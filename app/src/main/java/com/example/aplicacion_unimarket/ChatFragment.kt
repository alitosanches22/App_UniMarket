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
import com.google.android.material.snackbar.Snackbar

class ChatFragment : Fragment() {

    private var _binding: FragmentoChatBinding? = null
    private val binding get() = _binding!!
    private var idProducto: String = ""
    private var idConversacion: String? = null

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
        binding.botonEnviar.isEnabled = false

        binding.botonEnviar.setOnClickListener {
            val message = binding.entradaMensaje.text?.toString().orEmpty().trim()
            if (message.isBlank()) return@setOnClickListener
            enviarMensaje(message)
        }

        renderMessages()
        abrirConversacion(product)
    }

    private fun abrirConversacion(product: Producto?) {
        val usuario = MarketplaceRepository.usuarioAutenticado
        if (usuario == null || product == null || product.idVendedor.isBlank()) {
            binding.botonEnviar.isEnabled = true
            return
        }

        RepositorioRemoto.abrirConversacion(
            idProducto = product.id,
            idComprador = usuario.id,
            idVendedor = product.idVendedor,
            alCargar = { conversacion ->
                if (_binding == null) return@abrirConversacion
                idConversacion = conversacion.id
                binding.botonEnviar.isEnabled = true
                cargarMensajes()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@abrirConversacion
                binding.botonEnviar.isEnabled = true
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun cargarMensajes() {
        val usuario = MarketplaceRepository.usuarioAutenticado ?: return
        val conversacion = idConversacion ?: return

        RepositorioRemoto.cargarMensajes(
            idConversacion = conversacion,
            idUsuarioActual = usuario.id,
            alCargar = { mensajes ->
                if (_binding == null) return@cargarMensajes
                MarketplaceRepository.reemplazarMensajes(idProducto, mensajes)
                renderMessages()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@cargarMensajes
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun enviarMensaje(message: String) {
        val usuario = MarketplaceRepository.usuarioAutenticado
        val conversacion = idConversacion

        if (usuario == null || conversacion == null) {
            MarketplaceRepository.sendMessage(idProducto, message)
            binding.entradaMensaje.text?.clear()
            renderMessages()
            return
        }

        binding.botonEnviar.isEnabled = false
        RepositorioRemoto.enviarMensaje(
            idConversacion = conversacion,
            idRemitente = usuario.id,
            contenido = message,
            alCargar = {
                if (_binding == null) return@enviarMensaje
                binding.entradaMensaje.text?.clear()
                binding.botonEnviar.isEnabled = true
                cargarMensajes()
            },
            alFallar = { mensaje ->
                if (_binding == null) return@enviarMensaje
                binding.botonEnviar.isEnabled = true
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun renderMessages() {
        binding.contenedorMensajes.removeAllViews()
        val mensajes = MarketplaceRepository.messagesFor(idProducto)

        if (mensajes.isEmpty()) {
            agregarBurbuja(
                MensajeChat(
                    remitente = "UniMarket",
                    contenido = "Aun no hay mensajes. Escribe para iniciar la conversacion.",
                    enviadoPorUsuarioActual = false
                )
            )
            return
        }

        mensajes.forEach { message ->
            agregarBurbuja(message)
        }
        binding.scrollMensajes.post {
            binding.scrollMensajes.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun agregarBurbuja(message: MensajeChat) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
