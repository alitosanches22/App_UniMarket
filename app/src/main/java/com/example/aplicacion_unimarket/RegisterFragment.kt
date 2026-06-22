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
            val nombre = binding.entradaNombre.text?.toString().orEmpty().trim()
            val apellido = binding.entradaApellido.text?.toString().orEmpty().trim()
            val correo = binding.entradaCorreo.text?.toString().orEmpty().trim()
            val contrasena = binding.entradaContrasena.text?.toString().orEmpty()
            val carrera = binding.entradaCarrera.text?.toString().orEmpty().trim()
            val telefono = binding.entradaTelefono.text?.toString().orEmpty().trim()

            if (listOf(nombre, apellido, correo, contrasena, carrera, telefono).any { it.isBlank() }) {
                Snackbar.make(binding.root, "Completa todos los campos.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cambiarCarga(true)
            RepositorioRemoto.registrarUsuario(
                nombre = nombre,
                apellido = apellido,
                correo = correo,
                contrasena = contrasena,
                carrera = carrera,
                telefono = telefono,
                alCargar = {
                    if (_binding == null) return@registrarUsuario
                    cambiarCarga(false)
                    Toast.makeText(requireContext(), "Cuenta creada. Ahora inicia sesion.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.accion_registro_a_inicio_sesion)
                },
                alFallar = { mensaje ->
                    if (_binding == null) return@registrarUsuario
                    cambiarCarga(false)
                    Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
                }
            )
        }

        binding.botonIrInicioSesion.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun cambiarCarga(cargando: Boolean) {
        binding.botonCrearCuenta.isEnabled = !cargando
        binding.botonIrInicioSesion.isEnabled = !cargando
        binding.botonCrearCuenta.text = if (cargando) "Creando..." else "Registrarse"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
