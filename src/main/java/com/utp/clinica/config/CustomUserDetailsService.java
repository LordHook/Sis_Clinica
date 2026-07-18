package com.utp.clinica.config;

import com.utp.clinica.model.Usuario;
import com.utp.clinica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

/**
 * Servicio encargado de gestionar la carga de los detalles de usuario
 * para la autenticación real en Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Busca un usuario en la base de datos por su nombre de usuario
     * y genera el objeto UserDetails correspondiente con sus roles y estado
     * 
     * @param username Nombre de usuario a buscar
     * @return UserDetails con los datos cargados y encriptados
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.getEstado()) {
            throw new UsernameNotFoundException("El usuario se encuentra inactivo");
        }

        // Mapeamos el rol como un GrantedAuthority de Spring Security (ej. "ROLE_ADMINISTRADOR")
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        return new User(
                usuario.getUsuario(),
                usuario.getContrasena(),
                Collections.singletonList(authority)
        );
    }
}
