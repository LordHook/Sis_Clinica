const { Client } = require('pg');
const fs = require('fs');

async function importData() {
    const client = new Client({
        connectionString: 'postgresql://neondb_owner:npg_Z4IjzUTi5DCy@ep-purple-cake-atpjoyd9.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require',
    });

    await client.connect();
    console.log("Conectado a Neon PostgreSQL.");

    const rawData = fs.readFileSync('dump.json');
    const data = JSON.parse(rawData);

    // Intentar deshabilitar FK de forma global (puede fallar si no hay permisos)
    try {
        await client.query("SET session_replication_role = 'replica';");
        console.log("Replication role configurado a replica (FKs ignoradas).");
    } catch(e) {
        console.log("No se pudo deshabilitar FK (se requiere orden de inserción):", e.message);
    }

    // Orden seguro tentativo (padres primero)
    const order = [
        "especialidades",
        "usuarios",
        "consultorios",
        "horarios_medicos",
        "pacientes",
        "medicamentos",
        "citas",
        "consultas_medicas",
        "recetas",
        "bloqueos_agenda"
    ];

    // Asegurar que las tablas existan en data aunque no estén en orden
    const tables = Object.keys(data);
    const sortedTables = order.filter(t => tables.includes(t));
    const remaining = tables.filter(t => !order.includes(t));
    const finalOrder = [...sortedTables, ...remaining];

    for (const tableName of finalOrder) {
        const rows = data[tableName];
        if (!rows || rows.length === 0) continue;

        console.log(`Importando tabla: ${tableName} (${rows.length} registros)`);
        
        for (const row of rows) {
            const cols = Object.keys(row);
            const vals = Object.values(row);
            
            // Map values, handling nulls and booleans
            const mappedVals = vals.map(v => v === null ? null : v);
            
            const placeholders = cols.map((_, i) => `$${i + 1}`).join(', ');
            const query = `INSERT INTO ${tableName} (${cols.join(', ')}) VALUES (${placeholders}) ON CONFLICT DO NOTHING`;
            
            try {
                await client.query(query, mappedVals);
            } catch (err) {
                console.error(`Error insertando en ${tableName}:`, err.message);
                console.error('Row data:', row);
            }
        }

        // Si la tabla tiene una columna 'id', actualizar la secuencia
        try {
            // Verificar si hay secuencia (asumiendo convención tabla_id_seq)
            const seqName = `${tableName}_id_seq`;
            const checkSeq = await client.query(`SELECT 1 FROM pg_class WHERE relkind = 'S' AND relname = $1`, [seqName]);
            if (checkSeq.rowCount > 0) {
                await client.query(`SELECT setval($1, COALESCE((SELECT MAX(id) FROM ${tableName}), 1))`, [seqName]);
                console.log(`Secuencia ${seqName} actualizada.`);
            }
        } catch(e) {
            // Ignorar si falla
        }
    }

    try {
        await client.query("SET session_replication_role = 'origin';");
    } catch(e) {}

    console.log("Importación finalizada.");
    await client.end();
}

importData().catch(console.error);
