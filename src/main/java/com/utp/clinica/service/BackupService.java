package com.utp.clinica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

@Service
public class BackupService {

    @Autowired
    private DataSource dataSource;

    // Ejecutar cada 10 minutos
    @Scheduled(cron = "0 0/10 * * * ?")
    public void realizarBackup() {
        System.out.println("Iniciando tarea de respaldo automático de BD...");
        
        File backupDir = new File("backups");
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File backupFile = new File(backupDir, "backup_" + timestamp + ".sql");

        try (Connection conn = dataSource.getConnection();
             PrintWriter writer = new PrintWriter(new FileWriter(backupFile))) {
             
            DatabaseMetaData metaData = conn.getMetaData();
            String[] types = {"TABLE"};
            ResultSet tables = metaData.getTables(null, "public", "%", types);
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                writer.println("-- Backup tabla: " + tableName);
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
                     
                    int columnCount = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
                        for (int i = 1; i <= columnCount; i++) {
                            Object obj = rs.getObject(i);
                            if (obj == null) {
                                sql.append("NULL");
                            } else if (obj instanceof Number) {
                                sql.append(obj);
                            } else {
                                sql.append("'").append(obj.toString().replace("'", "''")).append("'");
                            }
                            if (i < columnCount) sql.append(", ");
                        }
                        sql.append(");");
                        writer.println(sql.toString());
                    }
                } catch (Exception e) {
                    writer.println("-- Error al exportar tabla " + tableName + ": " + e.getMessage());
                }
                writer.println();
            }
            System.out.println("Respaldo guardado exitosamente en: " + backupFile.getAbsolutePath());
            
            limpiarBackupsAntiguos(backupDir);

        } catch (Exception e) {
            System.err.println("Error durante el proceso de respaldo: " + e.getMessage());
        }
    }

    private void limpiarBackupsAntiguos(File backupDir) {
        // Mantiene solo los últimos 24 archivos (equivalente a las últimas 4 horas si es cada 10 min)
        File[] files = backupDir.listFiles((d, name) -> name.startsWith("backup_") && name.endsWith(".sql"));
        if (files != null && files.length > 24) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            for (int i = 0; i < files.length - 24; i++) {
                if (files[i].delete()) {
                    System.out.println("Backup antiguo eliminado por rotación: " + files[i].getName());
                }
            }
        }
    }
}
