// ==========================================
// 1. INICIALIZACIÓN
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    // Login inicial
    const loginForm = document.getElementById('login-form');
    if (loginForm) loginForm.addEventListener('submit', iniciarSesion);

    // Carga de tablas según la página (Evita errores si el div no existe)
    if (document.getElementById('tabla-pacientes-body')) cargarPacientesBD();
    if (document.getElementById('tabla-citas-body')) cargarListaCitas();
    if (document.getElementById('total-citas')) cargarDashboard();
    if (document.getElementById('tabla-farmacia-body')) {
        cargarFarmacia();
    }
});

// ==========================================
// 2. LÓGICA DE LOGIN
// ==========================================

async function iniciarSesion(e) {
    e.preventDefault();
    const user = document.getElementById('username').value;
    const pass = document.getElementById('password').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usuario: user, contrasena: pass })
        });

        if (response.ok) {
            const data = await response.text();
            if (data === 'LOGIN_OK') window.location.href = '/dashboard';
            else throw new Error('Respuesta inesperada');
        } else if (response.status === 401) throw new Error('Credenciales incorrectas');
        else throw new Error('Error del servidor: ' + response.status);
    } catch (err) {
        alert(err.message);
    }
}

// ==========================================
// 3. LÓGICA DE DASHBOARD
// ==========================================

function cargarDashboard() {
    fetch('/api/dashboard/stats')
        .then(res => res.json())
        .then(data => {
            if(document.getElementById('total-citas')) 
                document.getElementById('total-citas').innerText = data.totalCitas;
        });
}

// ==========================================
// 4. MÓDULO DE PACIENTES (CONECTADO A BD)
// ==========================================

function cargarPacientesBD() {
    fetch('/api/pacientes')
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('tabla-pacientes-body');
            if (!tbody) return;
            
            tbody.innerHTML = data.map(p => `
                <tr class="hover:bg-slate-50 transition-colors border-b border-slate-100">
                    <td class="p-4 font-mono font-medium text-slate-600">${p.numeroHistoriaClinica || 'N/A'}</td>
                    <td class="p-4 text-slate-600">${p.dni}</td>
                    <td class="p-4 font-bold text-slate-800">${p.nombres} ${p.apellidos}</td>
                    <td class="p-4">
                        <span class="bg-green-100 text-green-700 px-3 py-1 rounded-full text-[10px] font-black tracking-wider uppercase">${p.estado}</span>
                    </td>
                    <td class="p-4 text-right space-x-2">
                        <button type="button" class="px-3 py-1.5 border border-slate-300 text-slate-600 rounded-lg text-[10px] font-bold uppercase tracking-wider hover:bg-slate-50">Editar</button>
                        <button type="button" class="px-3 py-1.5 bg-slate-800 text-white rounded-lg text-[10px] font-bold uppercase tracking-wider hover:bg-slate-900 shadow-sm">Abrir Historia</button>
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => console.error('Error cargando pacientes:', error));
}

function toggleModalPaciente(show) {
    const modal = document.getElementById('modal-nuevo-paciente');
    if (show) {
        modal.classList.remove('hidden-view');
        // Limpiar los campos al abrir
        document.getElementById('pac-dni').value = '';
        document.getElementById('pac-nombres').value = '';
        document.getElementById('pac-apellidos').value = '';
        document.getElementById('pac-fecha').value = '';
        document.getElementById('pac-telefono').value = '';
        document.getElementById('pac-direccion').value = '';
        document.getElementById('pac-correo').value = '';
        document.getElementById('pac-alergias').value = '';
    } else {
        modal.classList.add('hidden-view');
    }
}

async function guardarPaciente() {
    const pacienteData = {
        dni: document.getElementById('pac-dni').value,
        nombres: document.getElementById('pac-nombres').value,
        apellidos: document.getElementById('pac-apellidos').value,
        fechaNacimiento: document.getElementById('pac-fecha').value,
        sexo: document.getElementById('pac-sexo').value,
        telefono: document.getElementById('pac-telefono').value,
        direccion: document.getElementById('pac-direccion').value,
        correo: document.getElementById('pac-correo').value,
        alergias: document.getElementById('pac-alergias').value
    };

    if(!pacienteData.dni || !pacienteData.nombres || !pacienteData.apellidos) {
        alert("Por favor, complete los campos obligatorios (DNI, Nombres, Apellidos).");
        return;
    }

    try {
        const response = await fetch('/api/pacientes/registrar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(pacienteData)
        });

        if (response.ok) {
            alert('Expediente guardado con éxito');
            toggleModalPaciente(false);
            cargarPacientesBD(); 
        } else {
            alert('Error al registrar. Verifique que el DNI no esté duplicado.');
        }
    } catch (error) {
        console.error("Error de red:", error);
    }
}

// ==========================================
// 5. MÓDULO DE CITAS (CONECTADO A BD REAL)
// ==========================================

async function cargarListaCitas() {
    try {
        const response = await fetch('/api/citas');
        const data = await response.json();
        const tbody = document.getElementById('tabla-citas-body');
        if (!tbody) return;
        
        tbody.innerHTML = data.map(c => `
            <tr class="border-b">
                <td class="p-4">${new Date(c.fechaHora).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</td>
                <td class="p-4">${c.paciente.nombres} ${c.paciente.apellidos}</td>
                <td class="p-4">${c.medico.nombres}</td>
                <td class="p-4">${c.consultorio.nombreNumero}</td>
                <td class="p-4"><span class="px-2 py-1 bg-green-100 text-green-700 rounded text-xs">${c.estado}</span></td>
                <td class="p-4 text-right"><button type="button" class="text-blue-600">Editar</button></td>
            </tr>
        `).join('');
    } catch (err) { console.error('Error cargando citas:', err); }
}

async function toggleModalCita(show) {
    const modal = document.getElementById('modal-nueva-cita');
    if (show) {
        modal.classList.remove('hidden-view');
        await cargarDatosModal(); 
    } else {
        modal.classList.add('hidden-view');
    }
}

async function cargarDatosModal() {
    const res = await fetch('/api/citas/opciones');
    const data = await res.json();

    document.getElementById('especialidad-select').innerHTML = data.especialidades.map(e => `<option value="${e.idEspecialidad}">${e.nombre}</option>`).join('');
    document.getElementById('medico-select').innerHTML = data.medicos.map(m => `<option value="${m.idUsuario}">Dr. ${m.apellidos}</option>`).join('');
    document.getElementById('consultorio-select').innerHTML = data.consultorios.map(c => `<option value="${c.idConsultorio}">${c.nombreNumero}</option>`).join('');
    
    const pacienteSelect = document.getElementById('paciente-id');
    if(pacienteSelect.tagName === 'SELECT') {
        pacienteSelect.innerHTML = data.pacientes.map(p => `<option value="${p.idPaciente}">${p.nombres} ${p.apellidos}</option>`).join('');
    }
}

async function guardarCita() {
    const citaData = {
        paciente: { idPaciente: document.getElementById('paciente-id').value },
        medico: { idUsuario: document.getElementById('medico-select').value },
        especialidad: { idEspecialidad: document.getElementById('especialidad-select').value },
        consultorio: { idConsultorio: document.getElementById('consultorio-select').value },
        fechaHora: document.getElementById('fecha-cita').value + 'T' + document.getElementById('hora-confirmada-valor').value
    };

    const response = await fetch('/api/citas/agendar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(citaData)
    });

    if (response.ok) {
        alert('Cita reservada con éxito');
        toggleModalCita(false);
        cargarListaCitas();
    } else {
        alert('Error al reservar la cita');
    }
}

function seleccionarTurno(boton, hora) {
    document.querySelectorAll('.turno-btn').forEach(btn => {
        btn.className = 'turno-btn border border-green-300 bg-white text-green-600 py-3 rounded-xl flex flex-col items-center justify-center';
        btn.querySelector('span:nth-child(2)').innerText = 'Libre';
    });
    boton.className = 'turno-btn border-2 border-green-500 bg-green-50 text-green-700 py-3 rounded-xl flex flex-col items-center justify-center shadow-sm';
    boton.querySelector('span:nth-child(2)').innerText = 'Seleccionado';
    document.getElementById('hora-confirmada-texto').innerText = hora;
    document.getElementById('hora-confirmada-valor').value = hora + ':00';
}

async function cargarFarmacia() {
    try {
        const estado = document.getElementById('select-estado')?.value || 'EMITIDA';
        const busqueda = document.getElementById('input-busqueda')?.value || '';
        
        const res = await fetch(`/api/farmacia/listar?estado=${estado}&busqueda=${busqueda}`);
        if (!res.ok) throw new Error('Error al conectar con el servidor');
        
        const data = await res.json();
        const tbody = document.getElementById('tabla-farmacia-body');
        
        if (!tbody) return;

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="p-8 text-center text-slate-400 italic">No hay recetas.</td></tr>';
            return;
        }

        // CORRECCIÓN: Usamos ?. para evitar el error de 'undefined'
        tbody.innerHTML = data.map(r => {
            const paciente = r.consulta?.cita?.paciente || { nombres: 'Sin', apellidos: 'Asignar', dni: 'N/A' };
            const medico = r.consulta?.cita?.medico || { apellidos: 'Sin asignar' };

            return `
            <tr class="border-b border-slate-100 hover:bg-slate-50 transition-colors">
                <td class="p-4 text-xs text-slate-500 font-mono">${new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</td>
                <td class="p-4">
                    <div class="font-bold text-slate-800">${paciente.nombres} ${paciente.apellidos}</div>
                    <div class="text-xs text-slate-400">DNI: ${paciente.dni}</div>
                </td>
                <td class="p-4 text-sm text-slate-600">Dr. ${medico.apellidos}</td>
                <td class="p-4">
                    <span class="badge ${r.estado === 'EMITIDA' ? 'badge-blue' : 'badge-green'}">${r.estado}</span>
                </td>
                <td class="p-4 text-right">
                    ${r.estado === 'EMITIDA' ? 
                    `<button type="button" onclick="abrirDespacho(${r.idReceta})" class="wire-btn-dark">Ver y Despachar</button>` : 
                    `<span class="text-xs text-slate-400 font-bold uppercase">Entregado</span>`}
                </td>
            </tr>
            `;
        }).join('');

    } catch (error) {
        console.error('Error al cargar farmacia:', error);
        alert('Hubo un problema al cargar los datos de farmacia.');
    }
}

function abrirDespacho(id) {
    // Aquí haces un fetch para buscar el detalle de la receta específica
    // Y rellenas los elementos del modal (nombre-paciente, detalle-receta, etc.)
    toggleModal('modal-despacho', true);
}
