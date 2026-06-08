// ==========================================
// 1. INICIALIZACIÓN
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', iniciarSesion);
    }
});

// ==========================================
// 2. LÓGICA DE LOGIN (BLINDADA Y CORREGIDA)
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

        // Verificación profesional de respuesta
        if (response.ok) {
            const data = await response.text();
            if (data === 'LOGIN_OK') {
                window.location.href = '/dashboard';
            } else {
                throw new Error('Respuesta del servidor inesperada');
            }
        } else if (response.status === 401) {
            throw new Error('Usuario o contraseña incorrectos');
        } else {
            throw new Error('Error del servidor: ' + response.status);
        }

    } catch (err) {
        alert(err.message);
        console.error('Login fallido:', err);
    }
}

// ==========================================
// 3. LÓGICA DE VISTAS Y NAVEGACIÓN
// ==========================================

function changeTab(tabId) {
    document.querySelectorAll('.view-section').forEach(el => el.classList.add('hidden-view'));
    
    const targetView = document.getElementById('view-' + tabId);
    if (targetView) targetView.classList.remove('hidden-view');

    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('nav-active');
        btn.classList.add('nav-inactive');
    });

    const targetNav = tabId === 'ficha-paciente' ? 'pacientes' : tabId;
    const activeBtn = document.getElementById('nav-' + targetNav);
    if(activeBtn) {
        activeBtn.classList.remove('nav-inactive');
        activeBtn.classList.add('nav-active');
    }

    const headerPath = document.getElementById('header-path');
    if (headerPath) headerPath.innerText = 'Clínica UTP / ' + tabId.toUpperCase();

    if (tabId === 'pacientes') {
        cargarPacientesBD();
    }
}

// ==========================================
// 4. CONEXIÓN API PACIENTES
// ==========================================

function cargarPacientesBD() {
    fetch('/api/pacientes')
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('tabla-pacientes-body');
            if (!tbody) return;
            
            tbody.innerHTML = '';
            data.forEach(paciente => {
                const fila = `
                    <tr>
                        <td class="font-mono font-medium text-slate-700">${paciente.numeroHistoriaClinica || 'N/A'}</td>
                        <td>${paciente.dni}</td>
                        <td class="font-semibold">${paciente.nombres} ${paciente.apellidos}</td>
                        <td><span class="badge badge-green">${paciente.estado}</span></td>
                        <td class="text-right space-x-2">
                            <button class="wire-btn px-3 py-1.5 text-[10px]">Editar</button>
                        </td>
                    </tr>
                `;
                tbody.innerHTML += fila;
            });
        })
        .catch(error => console.error('Error cargando pacientes:', error));
}

function cargarDashboard() {
    fetch('/api/dashboard/stats')
        .then(response => response.json())
        .then(data => {
            // Actualizar número total
            document.getElementById('total-citas').innerText = data.totalCitas;
            
            // Aquí puedes extender la lógica para llenar la tabla de médicos
            // y el gráfico de barras dinámicamente con los datos que vengan
        })
        .catch(err => console.error('Error cargando stats:', err));
}

// Función para capturar el formulario del modal
async function guardarCita() {
    const citaData = {
        paciente: { idPaciente: document.getElementById('paciente-id').value },
        medico: { idUsuario: document.getElementById('medico-select').value },
        especialidad: { idEspecialidad: document.getElementById('especialidad-select').value },
        consultorio: { idConsultorio: document.getElementById('consultorio-select').value },
        fechaHora: document.getElementById('fecha-cita').value + 'T' + document.getElementById('hora-confirmada').value
    };

    const response = await fetch('/api/citas/agendar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(citaData)
    });

    if (response.ok) {
        alert('Cita reservada con éxito');
        toggleModal('modal-nueva-cita', false); // Cerrar modal
        cargarListaCitas(); // Recargar tabla
    } else {
        alert('Error al reservar la cita');
    }
}

// Carga automática al entrar en la página de citas
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('tabla-citas-body')) {
        cargarListaCitas();
    }
});

async function cargarListaCitas() {
    try {
        const response = await fetch('/api/citas');
        const data = await response.json();
        const tbody = document.getElementById('tabla-citas-body');
        tbody.innerHTML = '';
        
        data.forEach(cita => {
            tbody.innerHTML += `
                <tr class="border-b">
                    <td class="p-4">${new Date(cita.fechaHora).toLocaleTimeString()}</td>
                    <td class="p-4">${cita.paciente.nombres}</td>
                    <td class="p-4">${cita.medico.nombres}</td>
                    <td class="p-4"><span class="px-2 py-1 bg-green-100 text-green-700 rounded text-xs">${cita.estado}</span></td>
                    <td class="p-4"><button class="text-blue-600">Editar</button></td>
                </tr>
            `;
        });
    } catch (err) {
        console.error('Error cargando citas:', err);
    }
}

// Función para abrir/cerrar cualquier modal
function toggleModal(modalId, show) {
    const modal = document.getElementById(modalId);
    if(show) modal.classList.remove('hidden-view');
    else modal.classList.add('hidden-view');
}