package com.utp.clinica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Clase de configuración de seguridad para proteger endpoints
 * y gestionar los roles de acceso del personal de la clínica.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define el bean del codificador de contraseñas con algoritmo BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el comportamiento del filtro de seguridad y las reglas por rol
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitamos CSRF temporalmente para facilitar llamadas REST desde frontend
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos y pantalla de login públicos
                .requestMatchers("/login", "/recuperar-password", "/css/**", "/js/**", "/images/**").permitAll()
                
                // Módulo Usuarios exclusivo de Administradores
                .requestMatchers("/usuarios/**", "/api/usuarios/**").hasRole("ADMINISTRADOR")
                
                // Módulo Citas permitido para Recepcionistas, Médicos y Administradores
                .requestMatchers("/citas/**", "/api/citas/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "MEDICO")
                
                // Módulo Horarios
                .requestMatchers("/horarios/**", "/api/horarios/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "MEDICO")

                // Módulo Pacientes
                .requestMatchers("/pacientes/**", "/api/pacientes/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "MEDICO")

                // Acto Médico y Ficha Paciente
                .requestMatchers("/ficha-paciente/**", "/api/consultas/**", "/medico/**").hasAnyRole("ADMINISTRADOR", "MEDICO")
                
                // Módulo Farmacia restringido a Farmacéuticos y Administradores
                .requestMatchers("/farmacia/**", "/api/farmacia/**").hasAnyRole("ADMINISTRADOR", "FARMACEUTICO")
                
                // Módulo Inventario restringido a Farmacéuticos y Administradores
                .requestMatchers("/inventario/**", "/api/inventario/**").hasAnyRole("ADMINISTRADOR", "FARMACEUTICO")
                // Cualquier otra solicitud requiere estar autenticado
                .anyRequest().authenticated()
            )
            // Configuración del login institucional
            .formLogin(form -> form
                .loginPage("/login")
                // El médico solo tiene el módulo de Citas: se le lleva directo allí.
                // El resto del personal aterriza en el Dashboard.
                .successHandler((request, response, authentication) -> {
                    boolean esFarmaceutico = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_FARMACEUTICO"));
                    
                    if (esFarmaceutico) {
                        response.sendRedirect(request.getContextPath() + "/farmacia");
                    } else {
                        // Todos los demás roles (incluido MÉDICO) irán al dashboard, ya que ahora tienen ese permiso.
                        response.sendRedirect(request.getContextPath() + "/dashboard");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // Configuración del cierre de sesión
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
