package com.utp.clinica.controller;

import com.utp.clinica.model.Especialidad;
import com.utp.clinica.model.Usuario;
import com.utp.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador encargado de procesar peticiones relacionadas a los Usuarios del personal
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private com.utp.clinica.repository.EspecialidadRepository especialidadRepo;

    /**
     * Guarda o edita un usuario
     */
    @PostMapping("/guardar")
    public String guardar(@RequestParam(value = "idUsuario", required = false) Integer idUsuario,
                          @RequestParam("dni") String dni,
                          @RequestParam("nombres") String nombres,
                          @RequestParam("apellidos") String apellidos,
                          @RequestParam("usuario") String username,
                          @RequestParam("contrasena") String password,
                          @RequestParam("rol") String rol,
                          @RequestParam(value = "idEspecialidad", required = false) Integer idEspecialidad) {

        Usuario usuario = new Usuario();
        if (idUsuario != null) {
            usuario.setIdUsuario(idUsuario);
        }
        usuario.setDni(dni);
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setUsuario(username);
        usuario.setContrasena(password);
        usuario.setRol(Usuario.Rol.valueOf(rol));
        usuario.setEstado(true);

        if (Usuario.Rol.MEDICO.name().equals(rol) && idEspecialidad != null) {
            Especialidad esp = especialidadRepo.findById(idEspecialidad).orElse(null);
            usuario.setEspecialidad(esp);
        }

        usuarioService.guardar(usuario);
        return "redirect:/usuarios";
    }

    /**
     * Activa o desactiva la cuenta de un usuario
     */
    @PostMapping("/estado/{idUsuario}")
    public String cambiarEstado(@PathVariable("idUsuario") Integer idUsuario,
                                @RequestParam("estado") boolean estado) {
        usuarioService.cambiarEstado(idUsuario, estado);
        return "redirect:/usuarios";
    }

    /**
     * Elimina a un usuario
     */
    @PostMapping("/eliminar/{idUsuario}")
    public String eliminar(@PathVariable("idUsuario") Integer idUsuario) {
        usuarioService.eliminar(idUsuario);
        return "redirect:/usuarios";
    }
}
