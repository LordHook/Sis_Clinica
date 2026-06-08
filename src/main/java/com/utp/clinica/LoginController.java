package com.utp.clinica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credenciales) {
        // Esto responde a tu archivo app.js
        return usuarioRepo.findByUsuarioAndContrasena(credenciales.get("usuario"), credenciales.get("contrasena"))
                .map(u -> ResponseEntity.ok("LOGIN_OK"))
                .orElse(ResponseEntity.status(401).body("LOGIN_ERROR"));
    }
}