const mysql = require('mysql2/promise');
const fs = require('fs');

async function exportData() {
    const connection = await mysql.createConnection({
        uri: 'mysql://root:UTixEsLxtPxNYLRkJmnXtCjAUHfnxZKQ@acela.proxy.rlwy.net:22219/railway'
    });

    console.log("Conectado a MySQL.");

    const [tables] = await connection.query("SHOW TABLES");
    const data = {};

    for (let i = 0; i < tables.length; i++) {
        const tableName = Object.values(tables[i])[0];
        const [rows] = await connection.query(`SELECT * FROM ${tableName}`);
        data[tableName] = rows;
        console.log(`Exportados ${rows.length} registros de la tabla ${tableName}`);
    }

    fs.writeFileSync('dump.json', JSON.stringify(data, null, 2));
    console.log("Datos exportados exitosamente a dump.json");

    await connection.end();
}

exportData().catch(console.error);
