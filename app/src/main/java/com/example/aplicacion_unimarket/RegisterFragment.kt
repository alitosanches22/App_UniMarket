package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentoRegistroBinding
import com.google.android.material.snackbar.Snackbar

class RegisterFragment : Fragment() {

    private var _binding: FragmentoRegistroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentoRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.botonCrearCuenta.setOnClickListener {
            val requiredValues = listOf(
                binding.entradaNombre.text,
                binding.entradaApellido.text,
                binding.entradaCorreo.text,
                binding.entradaContrasena.text,
                binding.entradaCarrera.text,
                binding.entradaTelefono.text
            )

            if (requiredValues.any { it.isNullOrBlank() }) {
                Snackbar.make(binding.root, "Completa todos los campos.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Cuenta creada. Ahora inicia sesion.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.accion_registro_a_inicio_sesion)
        }

        binding.botonIrInicioSesion.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
