package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentoInicioSesionBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentoInicioSesionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentoInicioSesionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.entradaCorreo.setText(MarketplaceRepository.currentUser.correo)

        binding.botonIniciarSesion.setOnClickListener {
            val email = binding.entradaCorreo.text?.toString().orEmpty().trim()
            val password = binding.entradaContrasena.text?.toString().orEmpty()

            if (email.isBlank() || password.isBlank()) {
                Snackbar.make(binding.root, "Ingresa correo y contrasena.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.fragmentoInicioSesion, true)
                .build()
            findNavController().navigate(R.id.accion_inicio_sesion_a_inicio, null, navOptions)
        }

        binding.botonRegistrarse.setOnClickListener {
            findNavController().navigate(R.id.accion_inicio_sesion_a_registro)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
