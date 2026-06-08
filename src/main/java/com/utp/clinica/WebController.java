package com.utp.clinica;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // Ruta de inicio
    @GetMapping({"/", "/login"})
    public String login() {
        return "login"; // Carga login.html
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("menu", "dashboard");
        return "dashboard"; // Carga dashboard.html que usa el layout
    }

    @GetMapping("/pacientes")
    public String pacientes(Model model) {
        model.addAttribute("menu", "pacientes");
        return "pacientes"; // Carga pacientes.html que usa el layout
    }
    @GetMapping("/citas")
public String citas(Model model) {
    model.addAttribute("paginaActiva", "citas");
    return "citas"; // Asegúrate de tener citas.html en templates/
}
@GetMapping("/farmacia")
public String farmacia(Model model) {
    model.addAttribute("active", "farmacia");
    return "farmacia"; // Nombre del archivo HTML
}
}