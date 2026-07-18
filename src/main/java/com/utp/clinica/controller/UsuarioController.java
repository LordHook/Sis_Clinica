package com.utp.clinica.controller;

import com.utp.clinica.model.Especialidad;
import com.utp.clinica.model.Usuario;
import com.utp.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

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
     * Verifica si el usuario es el administrador principal (primer admin creado)
     */
    private boolean esAdminPrincipal(Integer idUsuario) {
        return usuarioService.buscarPorId(idUsuario)
                .map(u -> u.getRol() == Usuario.Rol.ADMINISTRADOR &&
                          usuarioService.listarPorRol(Usuario.Rol.ADMINISTRADOR).stream()
                                  .mapToInt(Usuario::getIdUsuario)
                                  .min().orElse(-1) == u.getIdUsuario())
                .orElse(false);
    }

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

        if (username == null || !username.contains("@")) {
            return "redirect:/usuarios?errorEmail=true";
        }

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

        Usuario guardado = usuarioService.guardar(usuario);

        // Si es nuevo usuario, asignar permisos por defecto según su rol
        if (idUsuario == null) {
            List<String> permisosDefecto = usuarioService.obtenerPermisosDefecto(guardado.getRol());
            usuarioService.guardarPermisos(guardado.getIdUsuario(), permisosDefecto);
        }

        return "redirect:/usuarios";
    }

    /**
     * Activa o desactiva la cuenta de un usuario
     * Protege al administrador principal contra desactivación
     */
    @PostMapping("/estado/{idUsuario}")
    public String cambiarEstado(@PathVariable("idUsuario") Integer idUsuario,
                                @RequestParam("estado") boolean estado) {
        if (esAdminPrincipal(idUsuario)) {
            return "redirect:/usuarios?errorAdmin=true";
        }
        usuarioService.cambiarEstado(idUsuario, estado);
        return "redirect:/usuarios";
    }

    /**
     * Elimina a un usuario
     * Protege al administrador principal contra eliminación
     */
    @PostMapping("/eliminar/{idUsuario}")
    public String eliminar(@PathVariable("idUsuario") Integer idUsuario) {
        if (esAdminPrincipal(idUsuario)) {
            return "redirect:/usuarios?errorAdmin=true";
        }
        try {
            usuarioService.eliminar(idUsuario);
            return "redirect:/usuarios";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return "redirect:/usuarios?errorInUse=true";
        }
    }

    // ========== ENDPOINTS DE PERMISOS ==========

    /**
     * Obtiene los permisos de un usuario (endpoint REST para AJAX)
     */
    @GetMapping("/permisos/{idUsuario}")
    @ResponseBody
    public ResponseEntity<?> obtenerPermisos(@PathVariable("idUsuario") Integer idUsuario) {
        List<String> permisos = usuarioService.obtenerPermisos(idUsuario);
        return ResponseEntity.ok(Map.of("permisos", permisos));
    }

    /**
     * Guarda los permisos editados de un usuario (endpoint REST para AJAX)
     */
    @PostMapping("/permisos/{idUsuario}")
    @ResponseBody
    public ResponseEntity<?> guardarPermisos(@PathVariable("idUsuario") Integer idUsuario,
                                              @RequestBody Map<String, List<String>> payload) {
        List<String> modulos = payload.get("modulos");
        if (modulos == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lista de módulos requerida"));
        }
        usuarioService.guardarPermisos(idUsuario, modulos);
        return ResponseEntity.ok(Map.of("message", "Permisos actualizados correctamente"));
    }
}
