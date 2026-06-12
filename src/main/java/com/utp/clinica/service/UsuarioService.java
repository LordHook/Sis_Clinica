package com.utp.clinica.service;

import com.utp.clinica.model.Usuario;
import com.utp.clinica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de gestionar la lógica de negocio para la administración de personal y usuarios
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Lista todos los usuarios registrados
     */
    public List<Usuario> listarTodos() {
        return usuarioRepo.findAll();
    }

    /**
     * Filtra la lista de usuarios por su rol
     */
    public List<Usuario> listarPorRol(Usuario.Rol rol) {
        return usuarioRepo.findByRol(rol);
    }

    /**
     * Busca un usuario por su ID primario
     */
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepo.findById(id);
    }

    /**
     * Busca un usuario por su nombre de usuario (ej. jperez)
     */
    public Optional<Usuario> buscarPorUsuario(String usuario) {
        return usuarioRepo.findByUsuario(usuario);
    }

    /**
     * Realiza búsqueda general de personal por DNI o nombres/apellidos
     */
    public List<Usuario> buscarPersonal(String query) {
        if (query == null || query.trim().isEmpty()) {
            return usuarioRepo.findAll();
        }
        return usuarioRepo.findByDniContainingOrNombresContainingOrApellidosContaining(query, query, query);
    }

    /**
     * Registra o edita un usuario, procesando su contraseña de forma segura
     */
    @Transactional
    public Usuario guardar(Usuario usuario) {
        // Si es un nuevo usuario o si se ingresó una nueva contraseña en texto plano, la encriptamos
        if (usuario.getIdUsuario() == null) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        } else {
            // Caso edición: recuperar contraseña anterior si el input no cambió
            Usuario usuarioExistente = usuarioRepo.findById(usuario.getIdUsuario()).orElse(null);
            if (usuarioExistente != null) {
                if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty() || usuario.getContrasena().equals(usuarioExistente.getContrasena())) {
                    usuario.setContrasena(usuarioExistente.getContrasena());
                } else {
                    usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
                }
            }
        }
        return usuarioRepo.save(usuario);
    }

    /**
     * Activa o desactiva la cuenta de un usuario
     */
    @Transactional
    public void cambiarEstado(Integer idUsuario, boolean estado) {
        usuarioRepo.findById(idUsuario).ifPresent(u -> {
            u.setEstado(estado);
            usuarioRepo.save(u);
        });
    }

    /**
     * Elimina físicamente o lógicamente a un usuario
     */
    @Transactional
    public void eliminar(Integer idUsuario) {
        usuarioRepo.deleteById(idUsuario);
    }
}
