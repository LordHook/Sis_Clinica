const bcrypt = require('bcryptjs');
const { Client } = require('pg');

async function updateAdmin() {
    const pgClient = new Client({
        connectionString: 'postgresql://neondb_owner:npg_Z4IjzUTi5DCy@ep-purple-cake-atpjoyd9.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require',
    });

    await pgClient.connect();

    const newPassword = 'Admin123*';
    const salt = bcrypt.genSaltSync(10);
    const hash = bcrypt.hashSync(newPassword, salt);

    try {
        const res = await pgClient.query(
            "UPDATE usuarios SET usuario = 'admin@clinica.pe', contrasena = $1 WHERE usuario = 'admin'",
            [hash]
        );
        console.log(`Usuario actualizado. Filas afectadas: ${res.rowCount}`);
    } catch(e) {
        console.error("Error actualizando admin:", e);
    }
    
    // Y si queremos actualizar el resto para que no se rompan (opcional, los dejaremos como están para que los arreglen o prueben con el nuevo admin)
    
    await pgClient.end();
}

updateAdmin();
