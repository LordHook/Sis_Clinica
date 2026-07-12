const mysql = require('mysql2/promise');
const { Client } = require('pg');

async function validate() {
    const mysqlConn = await mysql.createConnection({
        uri: 'mysql://root:UTixEsLxtPxNYLRkJmnXtCjAUHfnxZKQ@acela.proxy.rlwy.net:22219/railway'
    });

    const pgClient = new Client({
        connectionString: 'postgresql://neondb_owner:npg_Z4IjzUTi5DCy@ep-purple-cake-atpjoyd9.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require',
    });

    await pgClient.connect();

    const [tables] = await mysqlConn.query("SHOW TABLES");
    console.log("=== INFORME DE VALIDACIÓN DE MIGRACIÓN ===");
    console.log("Tabla | MySQL | PostgreSQL | Estado");
    console.log("-------------------------------------------------");

    let totalMySql = 0;
    let totalPg = 0;

    for (let i = 0; i < tables.length; i++) {
        const tableName = Object.values(tables[i])[0];
        
        const [mysqlRows] = await mysqlConn.query(`SELECT count(*) as cnt FROM ${tableName}`);
        const countMySql = mysqlRows[0].cnt;
        totalMySql += countMySql;

        let countPg = 0;
        try {
            const pgRes = await pgClient.query(`SELECT count(*) as cnt FROM ${tableName}`);
            countPg = parseInt(pgRes.rows[0].cnt);
        } catch(e) {
            countPg = 'ERROR';
        }

        totalPg += countPg;

        const diff = countPg >= countMySql ? 'OK' : 'FALTAN DATOS';
        console.log(`${tableName.padEnd(20)} | ${countMySql.toString().padEnd(5)} | ${countPg.toString().padEnd(10)} | ${diff}`);
    }

    console.log("-------------------------------------------------");
    console.log(`TOTAL REGISTROS      | ${totalMySql.toString().padEnd(5)} | ${totalPg.toString().padEnd(10)} | ${totalPg >= totalMySql ? 'EXITO' : 'FALLA'}`);

    // Hibernate Seeded check
    console.log("\nNota: PostgreSQL puede tener más registros si Hibernate ejecutó data.sql o inicialización automática.");

    await mysqlConn.end();
    await pgClient.end();
}

validate().catch(console.error);
