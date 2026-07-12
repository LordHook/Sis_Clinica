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
                
                // Módulo Usuarios y Horarios exclusivo de Administradores
                .requestMatchers("/usuarios/**", "/api/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/horarios/**", "/api/horarios/**").hasRole("ADMINISTRADOR")
                
                // Módulo Citas permitido para Recepcionistas y Administradores
                .requestMatchers("/citas/**", "/api/citas/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                
                // Módulo Pacientes permitido para Recepcionistas, Médicos y Administradores
                .requestMatchers("/pacientes/**", "/api/pacientes/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "MEDICO")
                
                // Acto Médico y Ficha Paciente restringido a Médicos y Administradores
                .requestMatchers("/ficha-paciente/**", "/api/consultas/**").hasAnyRole("ADMINISTRADOR", "MEDICO")
                
                // Módulo Farmacia restringido a Farmacéuticos y Administradores
                .requestMatchers("/farmacia/**", "/api/farmacia/**").hasAnyRole("ADMINISTRADOR", "FARMACEUTICO")
                
                // Cualquier otra solicitud requiere estar autenticado
                .anyRequest().authenticated()
            )
            // Configuración del login institucional
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
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
