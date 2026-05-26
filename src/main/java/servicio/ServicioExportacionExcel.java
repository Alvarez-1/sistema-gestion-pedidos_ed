package servicio;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

/**
 * Servicio quirúrgico para exportar estadísticas a formato Excel (.xlsx).
 */
public class ServicioExportacionExcel {

    public void exportarEstadisticasExcel(String rutaArchivo, ServicioAsignacion servicio) {
        File archivo = new File(rutaArchivo);
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Estadísticas PediGo");

            // Crear estilos básicos
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Encabezados
            Row headerRow = sheet.createRow(0);
            Cell cell0 = headerRow.createCell(0);
            cell0.setCellValue("Métrica");
            cell0.setCellStyle(headerStyle);
            
            Cell cell1 = headerRow.createCell(1);
            cell1.setCellValue("Valor");
            cell1.setCellStyle(headerStyle);

            // Datos
            int rowNum = 1;
            agregarFila(sheet, rowNum++, "Pedidos pendientes", String.valueOf(servicio.getPedidosPendientes().getTamanio()));
            agregarFila(sheet, rowNum++, "Pedidos en proceso", String.valueOf(servicio.getPedidoParaEntregar().getTamanio()));
            agregarFila(sheet, rowNum++, "Pedidos entregados", String.valueOf(servicio.getHistorialEntregas().getTamanio()));
            agregarFila(sheet, rowNum++, "Pedidos cancelados", String.valueOf(servicio.getPedidosCancelados()));
            agregarFila(sheet, rowNum++, "Repartidores registrados", String.valueOf(servicio.getUsuariosRepartidores().size()));
            agregarFila(sheet, rowNum++, "Ganancias", "$" + servicio.getSaldo());
            agregarFila(sheet, rowNum++, "Zona más activa", servicio.zonaMasPedidos());

            // Ajustar ancho de columnas
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (FileOutputStream fileOut = new FileOutputStream(archivo)) {
                workbook.write(fileOut);
                System.out.println("Excel generado exitosamente en: " + rutaArchivo);
            }
        } catch (IOException e) {
            System.err.println("Error al exportar Excel: " + e.getMessage());
        }
    }

    private void agregarFila(Sheet sheet, int rowNum, String metrica, String valor) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(metrica);
        row.createCell(1).setCellValue(valor);
    }
}
