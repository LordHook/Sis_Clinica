package com.utp.clinica.config;

import com.utp.clinica.model.*;
import com.utp.clinica.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Inicializador de Base de Datos
 * Carga catálogos básicos y usuarios de demostración con contraseñas encriptadas en el primer arranque.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired private EspecialidadRepository especialidadRepo;
    @Autowired private ConsultorioRepository consultorioRepo;
    @Autowired private MedicamentoRepository medicamentoRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private PacienteRepository pacienteRepo;
    @Autowired private CitaRepository citaRepo;
    @Autowired private HorarioMedicoRepository horarioRepo;
    @Autowired private PermisoUsuarioRepository permisoRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== INICIANDO CONFIGURACIÓN Y SIEMBRA DE BASE DE DATOS ======");

        // 0. Encriptar contraseñas en texto plano de usuarios existentes en la base de datos
        try {
            List<Usuario> todosLosUsuarios = usuarioRepo.findAll();
            for (Usuario u : todosLosUsuarios) {
                String contrasena = u.getContrasena();
                if (contrasena != null && !contrasena.startsWith("$2a$") && !contrasena.startsWith("$2b$") && !contrasena.startsWith("$2y$")) {
                    System.out.println("[DB MIGRATION] Encriptando contraseña en texto plano para: " + u.getUsuario());
                    u.setContrasena(passwordEncoder.encode(contrasena));
                    usuarioRepo.save(u);
                }
            }
        } catch (Exception e) {
            System.err.println("[DB MIGRATION ERROR] Error al encriptar contraseñas: " + e.getMessage());
        }

        // 1. Sembrar Especialidades
        if (especialidadRepo.count() == 0) {
            Especialidad medGen = new Especialidad(); medGen.setNombre("Medicina General");
            Especialidad pediatria = new Especialidad(); pediatria.setNombre("Pediatría");
            Especialidad cardiologia = new Especialidad(); cardiologia.setNombre("Cardiología");
            Especialidad derma = new Especialidad(); derma.setNombre("Dermatología");
            especialidadRepo.saveAll(Arrays.asList(medGen, pediatria, cardiologia, derma));
            System.out.println("[DB INIT] Especialidades creadas.");
        }

        // 2. Sembrar Consultorios
        if (consultorioRepo.count() == 0) {
            Consultorio c101 = new Consultorio(); c101.setNombreNumero("Cons. 101"); c101.setPiso("1");
            Consultorio c102 = new Consultorio(); c102.setNombreNumero("Cons. 102"); c102.setPiso("1");
            Consultorio c204 = new Consultorio(); c204.setNombreNumero("Cons. 204"); c204.setPiso("2");
            Consultorio c302 = new Consultorio(); c302.setNombreNumero("Cons. 302"); c302.setPiso("3");
            consultorioRepo.saveAll(Arrays.asList(c101, c102, c204, c302));
            System.out.println("[DB INIT] Consultorios creados.");
        }

        // 3. Sembrar Medicamentos
        if (medicamentoRepo.count() == 0) {
            Medicamento m1 = new Medicamento(); m1.setNombre("Paracetamol 500mg"); m1.setStock(450); m1.setPrecio(new BigDecimal("1.50"));
            Medicamento m2 = new Medicamento(); m2.setNombre("Losartán 50mg"); m2.setStock(200); m2.setPrecio(new BigDecimal("4.50"));
            Medicamento m3 = new Medicamento(); m3.setNombre("Amoxicilina 500mg"); m3.setStock(80); m3.setPrecio(new BigDecimal("3.00"));
            Medicamento m4 = new Medicamento(); m4.setNombre("Ibuprofeno 400mg"); m4.setStock(150); m4.setPrecio(new BigDecimal("2.00"));
            medicamentoRepo.saveAll(Arrays.asList(m1, m2, m3, m4));
            System.out.println("[DB INIT] Medicamentos en stock creados.");
        }

        // Recuperar especialidad para médico
        List<Especialidad> especialidades = especialidadRepo.findAll();
        Especialidad general = especialidades.stream()
                .filter(e -> e.getNombre().equals("Medicina General"))
                .findFirst().orElse(null);

        // 4. Sembrar Usuarios
        if (usuarioRepo.count() == 0) {
            // Administrador
            Usuario admin = new Usuario();
            admin.setDni("10000001");
            admin.setNombres("Admin");
            admin.setApellidos("General");
            admin.setUsuario("admin@clinica.pe");
            admin.setContrasena(passwordEncoder.encode("admin"));
            admin.setRol(Usuario.Rol.ADMINISTRADOR);
            admin.setEstado(true);

            // Recepcionista
            Usuario recep = new Usuario();
            recep.setDni("20000002");
            recep.setNombres("Rosa");
            recep.setApellidos("Pérez");
            recep.setUsuario("recep@clinica.pe");
            recep.setContrasena(passwordEncoder.encode("recep"));
            recep.setRol(Usuario.Rol.RECEPCIONISTA);
            recep.setEstado(true);

            // Médico
            Usuario medico = new Usuario();
            medico.setDni("10293847");
            medico.setNombres("Juan Carlos");
            medico.setApellidos("Pérez");
            medico.setUsuario("jperez@clinica.pe");
            medico.setContrasena(passwordEncoder.encode("medico"));
            medico.setRol(Usuario.Rol.MEDICO);
            medico.setEstado(true);
            medico.setEspecialidad(general);

            // Farmacéutico
            Usuario farma = new Usuario();
            farma.setDni("40000004");
            farma.setNombres("Felipe");
            farma.setApellidos("López");
            farma.setUsuario("farma@clinica.pe");
            farma.setContrasena(passwordEncoder.encode("farma"));
            farma.setRol(Usuario.Rol.FARMACEUTICO);
            farma.setEstado(true);

            usuarioRepo.saveAll(Arrays.asList(admin, recep, medico, farma));
            System.out.println("[DB INIT] Usuarios (admin, recep, jperez, farma) creados.");

            // Sembrar permisos por defecto para cada usuario
            sembrarPermisosDefecto(admin, Arrays.asList("dashboard", "citas", "pacientes", "farmacia", "horarios", "usuarios"));
            sembrarPermisosDefecto(recep, Arrays.asList("dashboard", "citas", "pacientes"));
<<<<<<< HEAD
            sembrarPermisosDefecto(medico, Arrays.asList("citas"));
=======
            sembrarPermisosDefecto(medico, Arrays.asList("dashboard", "citas", "pacientes", "horarios"));
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
            sembrarPermisosDefecto(farma, Arrays.asList("dashboard", "farmacia"));
            System.out.println("[DB INIT] Permisos por defecto asignados.");
        }

        // Recuperar médico para horario y citas
        Usuario drPerez = usuarioRepo.findByUsuario("jperez@clinica.pe").orElse(null);

        // 5. Sembrar Horario del médico
        if (horarioRepo.count() == 0 && drPerez != null) {
            HorarioMedico h1 = new HorarioMedico();
            h1.setMedico(drPerez);
            h1.setDiaSemana("LUNES");
            h1.setHoraInicio(LocalTime.of(8, 0));
            h1.setHoraFin(LocalTime.of(14, 0));

            HorarioMedico h2 = new HorarioMedico();
            h2.setMedico(drPerez);
            h2.setDiaSemana("MIERCOLES");
            h2.setHoraInicio(LocalTime.of(8, 0));
            h2.setHoraFin(LocalTime.of(14, 0));

            HorarioMedico h3 = new HorarioMedico();
            h3.setMedico(drPerez);
            h3.setDiaSemana("VIERNES");
            h3.setHoraInicio(LocalTime.of(14, 0));
            h3.setHoraFin(LocalTime.of(18, 0));

            horarioRepo.saveAll(Arrays.asList(h1, h2, h3));
            System.out.println("[DB INIT] Horarios para el Dr. Pérez sembrados.");
        }

        // 6. Sembrar Pacientes
        if (pacienteRepo.count() == 0) {
            Paciente p1 = new Paciente();
            p1.setDni("74839201");
            p1.setNumeroHistoriaClinica("HC-004521");
            p1.setNombres("Roberto");
            p1.setApellidos("Díaz Suárez");
            p1.setFechaNacimiento(LocalDate.of(1981, 5, 12));
            p1.setSexo(Paciente.Sexo.M);
            p1.setTelefono("987654321");
            p1.setDireccion("Av. Arequipa 1230, Lima");
            p1.setCorreo("roberto.diaz@gmail.com");
            p1.setAlergias("PENICILINA");
            p1.setEstado(Paciente.EstadoPaciente.ACTIVO);

            Paciente p2 = new Paciente();
            p2.setDni("45829103");
            p2.setNumeroHistoriaClinica("HC-008932");
            p2.setNombres("María Elena");
            p2.setApellidos("Gonzales");
            p2.setFechaNacimiento(LocalDate.of(1990, 8, 22));
            p2.setSexo(Paciente.Sexo.F);
            p2.setTelefono("999888777");
            p2.setDireccion("Calle Las Flores 450, Lince");
            p2.setCorreo("maria.gonzales@gmail.com");
            p2.setAlergias("Ninguna");
            p2.setEstado(Paciente.EstadoPaciente.ACTIVO);

            Paciente p3 = new Paciente();
            p3.setDni("74839202");
            p3.setNumeroHistoriaClinica("HC-000001");
            p3.setNombres("Carlos");
            p3.setApellidos("Mendoza");
            p3.setFechaNacimiento(LocalDate.of(1985, 3, 10));
            p3.setSexo(Paciente.Sexo.M);
            p3.setTelefono("911222333");
            p3.setDireccion("Av. Javier Prado 2050, San Isidro");
            p3.setAlergias("Ninguna");
            p3.setEstado(Paciente.EstadoPaciente.ACTIVO);

            pacienteRepo.saveAll(Arrays.asList(p1, p2, p3));
            System.out.println("[DB INIT] Pacientes de demostración creados.");
        }

        // Sembrar citas de demostración
        if (citaRepo.count() == 0 && drPerez != null) {
            List<Paciente> pacientes = pacienteRepo.findAll();
            List<Consultorio> consultorios = consultorioRepo.findAll();
            if (!pacientes.isEmpty() && !consultorios.isEmpty()) {
                Cita cita1 = new Cita();
                cita1.setPaciente(pacientes.get(0)); // Roberto
                cita1.setMedico(drPerez);
                cita1.setEspecialidad(general);
                cita1.setConsultorio(consultorios.get(0)); // Cons 101
                cita1.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
                cita1.setEstado(Cita.EstadoCita.PROGRAMADA);

                Cita cita2 = new Cita();
                cita2.setPaciente(pacientes.get(1)); // María Elena
                cita2.setMedico(drPerez);
                cita2.setEspecialidad(general);
                cita2.setConsultorio(consultorios.get(0));
                cita2.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30)));
                cita2.setEstado(Cita.EstadoCita.CONFIRMADA);

                Cita cita3 = new Cita();
                cita3.setPaciente(pacientes.get(2)); // Carlos Mendoza
                cita3.setMedico(drPerez);
                cita3.setEspecialidad(general);
                cita3.setConsultorio(consultorios.get(2)); // Cons 204
                cita3.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
                cita3.setEstado(Cita.EstadoCita.ATENDIDA);

                citaRepo.saveAll(Arrays.asList(cita1, cita2, cita3));
                System.out.println("[DB INIT] Citas de demostración sembradas.");
            }
        }

        System.out.println("====== PROCESO DE INICIALIZACIÓN DE DB COMPLETADO CON ÉXITO ======");
    }

    /**
     * Helper para sembrar permisos por defecto de un usuario
     */
    private void sembrarPermisosDefecto(Usuario usuario, List<String> modulos) {
        for (String modulo : modulos) {
            PermisoUsuario permiso = new PermisoUsuario();
            permiso.setUsuario(usuario);
            permiso.setModulo(modulo);
            permisoRepo.save(permiso);
        }
    }
}
