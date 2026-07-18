# 📋 Actualización del Sistema — Clínica UTP

> Documento técnico y funcional de la actualización aplicada al sistema de gestión clínica
> (Spring Boot + Thymeleaf + PostgreSQL/Neon).
> **Fecha:** 13/07/2026 · **Alcance:** 6 observaciones funcionales sobre Citas, Agenda Médica,
> Accesos, Farmacia y Horarios.

---

## 1. Resumen ejecutivo

Esta actualización reordena el flujo de trabajo del **médico**, refuerza el **control de accesos**,
mejora la **trazabilidad de despachos en Farmacia** y hace que el **agendamiento de citas respete
el horario real de cada médico** con reserva/bloqueo automático de turnos.

| # | Observación original | Estado |
|---|----------------------|--------|
| 1 | En el módulo Citas el médico solo debe ver *sus* citas, no las demás | ✅ Implementado |
| 2 | Eliminar el módulo "Mi Agenda de Hoy" (redundante con Citas) | ✅ Implementado |
| 3 | El médico debe ver sus citas de hoy, las agendadas a futuro y las atendidas anteriormente | ✅ Implementado |
| 4 | El médico solo debe tener acceso al módulo de Citas, a ningún otro | ✅ Implementado |
| 5 | Farmacia debe listar todas las atenciones/recetas emitidas + historial de despachos por día | ✅ Implementado |
| 6 | Al agendar, reservar/bloquear el horario del médico; turnos de 30 min según Gestión de Horarios | ✅ Implementado |

---

## 2. Detalle por observación

### 🟦 Observación 1 y 3 — El médico solo ve *sus* citas (pasadas atendidas, de hoy y futuras)

**Antes:** el módulo de Citas mostraba **todas** las citas de la clínica a cualquier usuario con acceso.

**Ahora:** al entrar a `/citas`, el sistema detecta el rol del usuario autenticado:

- **MÉDICO** → ve únicamente las citas donde él es el médico asignado, ordenadas cronológicamente.
  Como se listan **todas las suyas**, esto incluye automáticamente:
  - citas **atendidas** en días anteriores,
  - citas de **hoy**,
  - citas **agendadas a futuro** (p. ej. el día siguiente).
- **RECEPCIONISTA / ADMINISTRADOR** → siguen viendo todas las citas de la clínica.

**Implementación:**

- `AppViewController.citas()` — determina el usuario autenticado y decide la fuente de datos:
  ```java
  boolean esMedico = usuarioActual != null && usuarioActual.getRol() == Usuario.Rol.MEDICO;
  List<Cita> todasLasCitas = esMedico
          ? citaService.listarPorMedico(usuarioActual)   // solo las suyas
          : citaService.listarTodas();                    // todas (recepción/admin)
  ```
- `CitaService.listarPorMedico(Usuario)` — nuevo método que ordena las citas del médico por fecha/hora.
- `CitaRepository.findByMedico(Usuario)` — nueva consulta derivada.
- `AppViewController.obtenerUsuarioAutenticado()` — helper que resuelve el `Usuario` desde el contexto de seguridad.

La vista `citas.html` recibe la bandera `esVistaMedico` para adaptar el encabezado y ocultar acciones que no le corresponden (ver Observación 4).

---

### 🟦 Observación 2 — Eliminado el módulo "Mi Agenda de Hoy"

El módulo era redundante: mostraba las citas de hoy del médico, algo que ahora vive dentro del módulo de Citas.

**Cambios:**

- **Sidebar** (`fragments/sidebar.html`): se eliminó el enlace *"Mi Agenda (Hoy)"* que solo veía el rol MÉDICO.
- **Plantilla** `medico-agenda.html`: **eliminada**.
- **Ruta** `/medico/agenda` (`MedicoController.miAgenda`): se conserva pero ahora **redirige a `/citas`**
  para no romper enlaces/marcadores antiguos.
- **Redirecciones de error** dentro de `MedicoController.verConsulta(...)`: antes volvían a `/medico/agenda`;
  ahora vuelven a `/citas`.
- **Vista de consulta** (`consulta-medica.html`): el botón *"← Volver a Mi Agenda"* pasó a *"← Volver a Citas"*.
- El resaltado del menú (`paginaActiva`) al atender una consulta ahora marca **Citas**.

> El flujo de **atender consulta** (registrar diagnóstico, tratamiento y recetas) **sigue intacto**;
> solo cambió el punto de entrada: se accede desde el botón *🩺 Atender* del módulo de Citas.

---

### 🟦 Observación 4 — El médico solo accede al módulo de Citas

Se restringió el acceso del rol **MÉDICO** en **tres capas** para que sea coherente y seguro:

**a) Menú lateral (permisos de vista)**
El médico ahora solo tiene el permiso `"citas"`. Se fuerza explícitamente en:
- `AppViewController.cargarDatosUsuarioEnModelo()`
- `MedicoController.cargarDatosComunes()`
- `UsuarioService.obtenerPermisosDefecto(MEDICO)` → `["citas"]`
- `DatabaseInitializer` (siembra inicial de permisos del médico) → `["citas"]`

Al forzarlo por rol, la restricción aplica **incluso a médicos ya existentes** en la base de datos,
sin depender de los permisos que tuvieran guardados.

**b) Seguridad de rutas (`SecurityConfig`)**
Se ajustó la matriz de autorización. El médico recibe **HTTP 403** si intenta entrar por URL a otros módulos:

| Ruta | ADMIN | RECEPCIONISTA | MÉDICO | FARMACÉUTICO |
|------|:---:|:---:|:---:|:---:|
| `/citas/**`, `/api/citas/**` | ✅ | ✅ | ✅ | — |
| `/medico/**` (atender consulta) | ✅ | — | ✅ | — |
| `/api/consultas/**` (registrar acto médico) | ✅ | — | ✅ | — |
| `/horarios/**` (Gestión de Horarios) | ✅ | — | ❌ | — |
| `/pacientes/**` | ✅ | ✅ | ❌ | — |
| `/ficha-paciente/**` | ✅ | — | ❌ | — |
| `/farmacia/**`, `/api/farmacia/**` | ✅ | — | — | ✅ |
| `/usuarios/**` | ✅ | — | — | — |

> El médico conserva `/medico/**` y `/api/consultas/**` porque **atender la cita** (registrar diagnóstico,
> tratamiento y receta) es parte funcional del módulo de Citas.

**c) Redirección post-login**
Se reemplazó `defaultSuccessUrl("/dashboard")` por un `successHandler` que envía:
- **MÉDICO → `/citas`** (su único módulo),
- resto de roles → `/dashboard`.

**d) Interfaz**
En `citas.html` se oculta el botón **"+ AGENDAR NUEVA CITA"** cuando la vista es de un médico
(el médico consulta/atiende, no agenda), y el subtítulo cambia a
*"Tus citas: atendidas anteriores, de hoy y agendadas a futuro"*.

---

### 🟦 Observación 5 — Farmacia: todas las recetas + historial de despachos por día

**Hallazgo:** la base de datos **sí cuenta** con los campos necesarios en la entidad `Receta`
(`medicamento`, `cantidad`, `dosis`, `frecuencia`), y la cola de recetas emitidas ya listaba
**todas** las recetas emitidas por los médicos, con los datos del paciente (nombre, DNI), el
medicamento, la cantidad y el médico tratante. Es decir, la parte de "ver la totalidad para despachar"
ya estaba cubierta.

**Lo que faltaba:** trazabilidad de **cuándo** se despachó cada receta, para poder construir un
**historial por días**. Se añadió:

- **Nuevo campo** `Receta.fechaDespacho` (`LocalDateTime`, columna `fecha_despacho`).
  Se registra automáticamente en `FarmaciaService.despacharReceta()` en el momento de la entrega.
- **Historial agrupado por día** en `farmacia.html`:
  - Se agrupan las recetas despachadas por fecha (día), **de la más reciente a la más antigua**.
  - Cada bloque de día muestra un encabezado con la fecha en formato largo (ej. *"Lunes 12/05/2026"*)
    y el conteo de despachos de ese día.
  - Cada fila muestra hora de entrega, paciente (nombre + DNI), medicamento, cantidad y médico tratante.
  - Para recetas **antiguas** despachadas antes de esta actualización (sin `fecha_despacho`),
    se usa como referencia la fecha de la consulta, para que no queden fuera del historial.

**Implementación de la agrupación** (`AppViewController.farmacia()`):
```java
Map<LocalDate, List<Receta>> despachosPorDia = recetasDespachadas.stream()
        .sorted(Comparator.comparing(AppViewController::fechaReferenciaDespacho).reversed())
        .collect(Collectors.groupingBy(
                r -> fechaReferenciaDespacho(r).toLocalDate(),
                LinkedHashMap::new,   // preserva el orden (más reciente primero)
                Collectors.toList()));
```

La cola de **recetas pendientes (emitidas)** se mantiene igual: sigue listando la totalidad para su despacho,
con validación de stock e identidad del paciente en el modal de entrega.

---

### 🟦 Observación 6 — Reserva de horario al agendar + turnos de 30 min según Gestión de Horarios

**Antes:** el modal de agendamiento mostraba **6 horas fijas y hardcodeadas** (08:00–10:30) que no
reflejaban ni el horario real del médico ni las citas ya reservadas.

**Ahora:** los turnos se calculan **en el servidor** a partir de la configuración de **Gestión de Horarios**
del médico y de sus citas ya reservadas.

**Nuevo endpoint REST:**
```
GET /citas/horarios-disponibles?idMedico={id}&fecha={yyyy-MM-dd}
```
Respuesta:
```json
{
  "disponible": true,
  "motivo": "",
  "slots": [
    { "hora": "08:00", "estado": "LIBRE"   },
    { "hora": "08:30", "estado": "OCUPADO" },
    { "hora": "09:00", "estado": "PASADO"  }
  ]
}
```

**Lógica de cálculo** (`CitaController.horariosDisponibles`, apoyada en `HorarioService` y `CitaService`):

1. **Bloqueos:** si la fecha cae dentro de un bloqueo de agenda del médico (vacaciones, feriado, licencia),
   responde `disponible=false` con el motivo. → `HorarioService.motivoBloqueoEnFecha(...)`
2. **Horario del día:** obtiene los turnos fijos del médico para el **día de la semana** correspondiente
   (LUNES, MARTES, …). Si no atiende ese día, responde `disponible=false` con el motivo.
   → `HorarioService.obtenerHorarioPorMedicoYDia(...)`
3. **Generación de slots:** dentro de cada turno (hora inicio → hora fin) genera bloques de
   **30 minutos** (`DURACION_TURNO_MINUTOS = 30`). El último slot solo se agrega si cabe completo
   antes del fin del turno.
4. **Estado de cada slot:**
   - `OCUPADO` — existe una cita **activa** (no cancelada) del médico dentro de esa media hora.
     → `CitaService.obtenerCitasActivasDelDia(...)`
   - `PASADO` — el slot ya transcurrió (solo aplica al día de hoy).
   - `LIBRE` — disponible para reservar.

**Prevención de doble agendamiento (ya existía, reforzada en UI):**
`CitaService.agendarCita()` rechaza con excepción si el médico ya tiene una cita activa en el rango
(±29 min). Ahora la interfaz **también** refleja esos turnos como `OCUPADO`, evitando que la persona
que agenda intente reservar sobre un horario ya tomado.

**Interfaz (`citas.html`):**
- Se eliminaron las horas fijas; el contenedor de turnos se llena dinámicamente vía `fetch`.
- Los turnos se recargan al cambiar **médico** o **fecha** (`cargarTurnosDisponibles()`).
- La fecha por defecto es **hoy** y no permite fechas pasadas (`min`).
- Turnos `LIBRE` son seleccionables; `OCUPADO` / `PASADO` / `No disponible` se muestran **deshabilitados**.
- Mensajes claros cuando el médico no atiende ese día o está bloqueado.

---

## 3. Decisión de diseño pendiente de confirmación — Consultorio / Sala

En "Gestión de Horarios" cada turno del médico guarda **solo día + hora de inicio/fin**; **no** guarda
consultorio/sala. Por eso, en esta actualización:

- ✅ **Opción aplicada:** los turnos disponibles se generan desde el horario del médico y se bloquean
  los ya reservados. **El consultorio se sigue eligiendo manualmente** en un desplegable. Sin cambios de esquema.

- ⏳ **Opción alternativa (no aplicada, disponible si se solicita):** agregar un campo `consultorio` a cada
  turno de Gestión de Horarios, de modo que al elegir el turno el consultorio se **autocomplete**.
  Requiere: nueva columna en `horarios_medicos`, ajustar la pantalla de Horarios y el modal de Citas.

---

## 4. Cambios en la base de datos

| Cambio | Detalle |
|--------|---------|
| **Nueva columna** | `recetas.fecha_despacho` (`timestamp`) — fecha/hora real del despacho. |
| **Aplicación** | Automática vía Hibernate `spring.jpa.hibernate.ddl-auto=update` al arrancar. |
| **Compatibilidad** | Aditiva y no destructiva. Recetas ya despachadas quedan con `fecha_despacho = NULL` y se agrupan por la fecha de su consulta. |

SQL equivalente ejecutado por Hibernate:
```sql
alter table if exists recetas add column fecha_despacho timestamp(6);
```

---

## 5. Archivos modificados

### Java — Backend

| Archivo | Cambio |
|---------|--------|
| `model/Receta.java` | Nuevo campo `fechaDespacho` + getter/setter. |
| `repository/CitaRepository.java` | Nueva consulta `findByMedico(Usuario)`. |
| `service/CitaService.java` | `listarPorMedico(...)`, `obtenerCitasActivasDelDia(...)`. |
| `service/HorarioService.java` | `obtenerHorarioPorMedicoYDia(...)`, `motivoBloqueoEnFecha(...)`. |
| `service/FarmaciaService.java` | Registra `fechaDespacho` al despachar. |
| `service/UsuarioService.java` | Permisos por defecto del MÉDICO → `["citas"]`. |
| `controller/CitaController.java` | Endpoint `/citas/horarios-disponibles` + cálculo de turnos de 30 min. |
| `controller/AppViewController.java` | Filtrado de citas por médico; agrupación de despachos por día; permisos MÉDICO = citas. |
| `controller/MedicoController.java` | `/medico/agenda` redirige a `/citas`; permisos MÉDICO = citas; redirecciones de error a `/citas`. |
| `config/SecurityConfig.java` | Nueva matriz de accesos + `successHandler` de login por rol. |
| `config/DatabaseInitializer.java` | Siembra de permisos del médico → `["citas"]`. |

### Plantillas — Frontend (Thymeleaf)

| Archivo | Cambio |
|---------|--------|
| `templates/citas.html` | Turnos dinámicos (fetch), oculta "Agendar" al médico, subtítulo por rol. |
| `templates/farmacia.html` | Historial de despachos **agrupado por día**. |
| `templates/consulta-medica.html` | Botón "Volver" apunta a `/citas`. |
| `templates/fragments/sidebar.html` | Eliminado el enlace "Mi Agenda (Hoy)". |
| `templates/medico-agenda.html` | **Eliminado.** |

---

## 6. Verificación realizada

Todo se probó contra la aplicación en ejecución conectada a la base de datos Neon:

- ✅ **Compilación** limpia (`mvnw compile`).
- ✅ **Arranque** correcto; la migración de `fecha_despacho` se aplicó sin errores.
- ✅ `/farmacia` y `/citas` renderizan (HTTP 200, sin errores de plantilla).
- ✅ **Endpoint de turnos:** lunes devuelve slots 08:00–13:30 de 30 min; martes reporta "sin horario";
  fecha inválida responde error controlado; el día de hoy marca los slots ya transcurridos como `PASADO`.
- ✅ **Flujo de reserva:** agendar 10:00 → el slot pasa a `OCUPADO`; un duplicado es rechazado
  ("El médico ya tiene una cita agendada en ese rango horario"); al cancelar, el slot vuelve a `LIBRE`.
- ✅ **Vista del médico:** el sidebar muestra **solo Citas** (sin Mi Agenda, sin Dashboard/Pacientes/Farmacia/Horarios);
  la tabla muestra **solo sus citas**; el botón "Agendar" no aparece; el login lo lleva directo a `/citas`;
  recibe **403** en `/farmacia`, `/horarios` y `/pacientes`.

> Nota: los datos usados para verificar (una cita de prueba y una contraseña temporal de un médico)
> fueron **revertidos/eliminados** tras la prueba, dejando la base de datos como estaba (salvo la
> columna `fecha_despacho`, que es el cambio intencional).

---

## 7. Observación adicional (pre-existente, no incluida en esta actualización)

El endpoint `POST /citas/agendar` devuelve el objeto `Usuario` completo del médico en su respuesta JSON,
lo que **incluye el hash de la contraseña** (`contrasena`). No fue introducido por esta actualización,
pero se recomienda corregirlo (devolver solo `idCita`/datos mínimos, o usar un DTO). Puede abordarse
en una siguiente iteración si se desea.

---

## 8. Credenciales de prueba (entorno de demostración)

| Rol | Usuario | Notas |
|-----|---------|-------|
| Administrador | `admin@clinica.pe` | Contraseña de demo definida en `migration-script/update_admin.js`. |
| Médico | `jperez@clinica.pe` | Cuenta de demostración del Dr. Juan Carlos Pérez. |

> Las contraseñas reales dependen del estado actual de la base de datos y no se almacenan en este documento.
