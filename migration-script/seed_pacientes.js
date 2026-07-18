const { Client } = require('pg');

// ---------- Datos base para generar pacientes realistas ----------
const NOMBRES_M = [
    'Carlos', 'Luis', 'Jose', 'Juan', 'Miguel', 'Jorge', 'Pedro', 'Ricardo',
    'Fernando', 'Diego', 'Alberto', 'Andres', 'Manuel', 'Raul', 'Victor',
    'Cesar', 'Gonzalo', 'Sergio', 'Martin', 'Enrique', 'Alexander', 'Renzo',
    'Piero', 'Franco', 'Gabriel'
];

const NOMBRES_F = [
    'Maria', 'Ana', 'Rosa', 'Carmen', 'Patricia', 'Laura', 'Sofia', 'Lucia',
    'Gabriela', 'Andrea', 'Fiorella', 'Karina', 'Milagros', 'Diana', 'Vanessa',
    'Ximena', 'Alejandra', 'Cecilia', 'Silvia', 'Pilar', 'Yolanda', 'Vera',
    'Estefany', 'Melissa', 'Camila'
];

const APELLIDOS = [
    'Garcia', 'Rodriguez', 'Gonzalez', 'Fernandez', 'Lopez', 'Martinez',
    'Sanchez', 'Perez', 'Gomez', 'Diaz', 'Torres', 'Ramirez', 'Flores',
    'Rojas', 'Vargas', 'Castillo', 'Chavez', 'Mendoza', 'Vega', 'Reyes',
    'Cruz', 'Salazar', 'Quispe', 'Huaman', 'Aguilar', 'Paredes', 'Rios',
    'Silva', 'Medina', 'Guerrero'
];

const TIPOS_VIA = ['Av.', 'Jr.', 'Calle', 'Psje.'];
const NOMBRES_CALLE = [
    'Los Alisos', 'Las Flores', 'San Martin', 'Grau', 'Los Pinos', 'Union',
    'Las Begonias', 'Los Cedros', 'Arequipa', 'Tacna', 'Los Olivos',
    'Las Palmeras', 'Bolivar', 'Los Jazmines', 'La Marina'
];
const DISTRITOS = [
    'Miraflores', 'San Isidro', 'Surco', 'La Molina', 'San Borja',
    'Los Olivos', 'San Miguel', 'Comas', 'Ate', 'Chorrillos',
    'Barranco', 'Jesus Maria', 'Lince', 'Independencia', 'Villa El Salvador'
];
const ALERGIAS_POSIBLES = [
    'Penicilina', 'Polen', 'Mariscos', 'Polvo', 'Aspirina', 'Lactosa',
    'Ibuprofeno', 'Sulfamidas', 'Nueces', 'Latex'
];

// ---------- Utilidades ----------
function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
function pick(arr) {
    return arr[randomInt(0, arr.length - 1)];
}
function quitarTildes(s) {
    return s.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

// DNI: 8 dígitos, único (respeta la nueva regla de 8-9 dígitos)
function generarDniUnico(existentes) {
    let dni;
    do {
        dni = String(randomInt(10000000, 99999999));
    } while (existentes.has(dni));
    existentes.add(dni);
    return dni;
}

// Teléfono móvil peruano: 9 dígitos, empieza en 9 (regla: exactamente 9 dígitos)
function generarTelefonoUnico(existentes) {
    let tel;
    do {
        tel = '9' + String(randomInt(0, 99999999)).padStart(8, '0');
    } while (existentes.has(tel));
    existentes.add(tel);
    return tel;
}

function generarFechaNacimiento() {
    const year = randomInt(1945, 2015);
    const month = randomInt(1, 12);
    const day = randomInt(1, 28);
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

async function seed() {
    const client = new Client({
        connectionString: 'postgresql://neondb_owner:npg_Z4IjzUTi5DCy@ep-purple-cake-atpjoyd9.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require',
    });

    await client.connect();
    console.log('Conectado a Neon PostgreSQL.');

    // Cargar datos existentes para evitar cualquier duplicado
    const dniRes = await client.query('SELECT dni FROM pacientes');
    const telRes = await client.query('SELECT telefono FROM pacientes WHERE telefono IS NOT NULL');
    const hcRes = await client.query('SELECT numero_historia_clinica FROM pacientes');

    const dniSet = new Set(dniRes.rows.map(r => r.dni));
    const telSet = new Set(telRes.rows.map(r => r.telefono).filter(Boolean));

    let maxHc = 0;
    hcRes.rows.forEach(r => {
        const match = (r.numero_historia_clinica || '').match(/(\d+)$/);
        if (match) maxHc = Math.max(maxHc, parseInt(match[1], 10));
    });

    const TOTAL = 50;
    let insertados = 0;

    for (let i = 0; i < TOTAL; i++) {
        const sexo = Math.random() < 0.5 ? 'M' : 'F';
        const nombres = sexo === 'M' ? pick(NOMBRES_M) : pick(NOMBRES_F);
        const apellidos = `${pick(APELLIDOS)} ${pick(APELLIDOS)}`;

        const dni = generarDniUnico(dniSet);
        const telefono = generarTelefonoUnico(telSet);
        const fechaNacimiento = generarFechaNacimiento();
        const direccion = `${pick(TIPOS_VIA)} ${pick(NOMBRES_CALLE)} ${randomInt(100, 999)}, ${pick(DISTRITOS)}`;

        const correoBase = quitarTildes(`${nombres}.${apellidos.split(' ')[0]}`).toLowerCase().replace(/\s+/g, '');
        const correo = `${correoBase}${randomInt(1, 999)}@gmail.com`;

        // ~35% de probabilidad de tener una alergia registrada
        const alergias = Math.random() < 0.35 ? pick(ALERGIAS_POSIBLES) : 'Ninguna conocida';

        maxHc++;
        const numeroHistoriaClinica = `HC-${String(maxHc).padStart(6, '0')}`;

        try {
            await client.query(
                `INSERT INTO pacientes
                    (numero_historia_clinica, dni, nombres, apellidos, fecha_nacimiento, sexo, telefono, direccion, correo, alergias, estado, fecha_registro)
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 'ACTIVO', NOW())`,
                [numeroHistoriaClinica, dni, nombres, apellidos, fechaNacimiento, sexo, telefono, direccion, correo, alergias]
            );
            insertados++;
            console.log(`(${insertados}/${TOTAL}) ${numeroHistoriaClinica} - ${nombres} ${apellidos} - DNI ${dni} - Tel ${telefono}`);
        } catch (err) {
            console.error(`Error insertando paciente DNI ${dni}:`, err.message);
        }
    }

    console.log(`\nListo. ${insertados} pacientes insertados de ${TOTAL} solicitados.`);
    await client.end();
}

seed().catch(err => {
    console.error('Error general del script:', err);
    process.exit(1);
});
