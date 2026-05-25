package servicio;

import modelo.Pedido;
import modelo.Producto;
import estructura.Nodo;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServicioComprobante {

    public File generarComprobantePedidoPDF(Pedido pedido) {
        if (pedido == null) {
            System.err.println("El pedido es nulo, no se puede generar comprobante.");
            return null;
        }

        // Crear carpeta para los comprobantes
        File carpeta = new File("comprobantes");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        String nombreArchivo = "comprobantes/comprobante_pedido_" + pedido.getCodigo() + ".pdf";
        File archivoPDF = new File(nombreArchivo);

        Document documento = new Document();
        try {
            PdfWriter.getInstance(documento, new FileOutputStream(archivoPDF));
            documento.open();

            // Fuentes
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.GRAY);
            Font fuenteTextoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font fuenteTexto = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fuenteNota = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.RED);

            // Título
            Paragraph titulo = new Paragraph("COMPROBANTE DE PEDIDO", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            // Subtítulo con código y fecha
            String fechaPedido = pedido.getFecha() != null ? pedido.getFecha() : pedido.getFechaHora();
            Paragraph subtitulo = new Paragraph("Código del Pedido: " + pedido.getCodigo() + "\nFecha: " + fechaPedido, fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            documento.add(subtitulo);

            // Detalles del cliente y comercio
            documento.add(new Paragraph("INFORMACIÓN DEL CLIENTE:", fuenteSubtitulo));
            documento.add(new Paragraph("Cliente: " + (pedido.getUsuario() != null ? pedido.getUsuario().getNombre() : "N/A"), fuenteTexto));
            documento.add(new Paragraph("Dirección: " + (pedido.getUsuario() != null ? pedido.getUsuario().getDireccion() : "N/A"), fuenteTexto));
            documento.add(new Paragraph("Zona: " + (pedido.getUsuario() != null ? pedido.getUsuario().getZona() : "N/A"), fuenteTexto));
            documento.add(new Paragraph(" ", fuenteTexto));

            documento.add(new Paragraph("INFORMACIÓN DEL COMERCIO:", fuenteSubtitulo));
            documento.add(new Paragraph("Comercio: " + (pedido.getTienda() != null ? pedido.getTienda().getNombre() : "N/A"), fuenteTexto));
            documento.add(new Paragraph(" ", fuenteTexto));

            // Tabla de productos
            documento.add(new Paragraph("PRODUCTOS ADQUIRIDOS:", fuenteSubtitulo));
            PdfPTable tabla = new PdfPTable(4); // Columnas: Código, Nombre, Cantidad, Precio
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10);
            tabla.setSpacingAfter(15);

            // Encabezados de tabla
            tabla.addCell(new PdfPCell(new Paragraph("Código", fuenteTextoNegrita)));
            tabla.addCell(new PdfPCell(new Paragraph("Nombre", fuenteTextoNegrita)));
            tabla.addCell(new PdfPCell(new Paragraph("Cantidad", fuenteTextoNegrita)));
            tabla.addCell(new PdfPCell(new Paragraph("Precio", fuenteTextoNegrita)));

            // Recorrer los productos del pedido
            Nodo actual = pedido.getProductosElegidos().getPrimero();
            while (actual != null) {
                Producto p = (Producto) actual.getDato();
                tabla.addCell(new PdfPCell(new Paragraph(String.valueOf(p.getCodigoProducto()), fuenteTexto)));
                tabla.addCell(new PdfPCell(new Paragraph(p.getNombre(), fuenteTexto)));
                tabla.addCell(new PdfPCell(new Paragraph(String.valueOf(p.getCantidad()), fuenteTexto)));
                tabla.addCell(new PdfPCell(new Paragraph("$" + p.getPrecio(), fuenteTexto)));
                actual = actual.getSiguiente();
            }
            documento.add(tabla);

            // Valores financieros
            documento.add(new Paragraph("INFORMACIÓN FINANCIERA:", fuenteSubtitulo));
            documento.add(new Paragraph("Valor de los Productos: $" + pedido.getValorDeProducto(), fuenteTexto));
            documento.add(new Paragraph("Valor del Domicilio: $" + pedido.getValorDomicilio(), fuenteTexto));
            documento.add(new Paragraph("Valor Total: $" + pedido.getValorTotal(), fuenteTextoNegrita));
            documento.add(new Paragraph(" ", fuenteTexto));

            // Estado y repartidor
            documento.add(new Paragraph("ESTADO DEL PEDIDO Y LOGÍSTICA:", fuenteSubtitulo));
            documento.add(new Paragraph("Estado Actual: " + pedido.getEstadoActual(), fuenteTextoNegrita));
            if (pedido.getRappi() != null) {
                documento.add(new Paragraph("Repartidor Asignado: " + pedido.getRappi().getNombre() + " (Calificación: " + pedido.getRappi().getCalificacion() + ")", fuenteTexto));
            } else {
                documento.add(new Paragraph("Repartidor Asignado: Sin asignar", fuenteTexto));
            }
            documento.add(new Paragraph(" ", fuenteTexto));
            documento.add(new Paragraph(" ", fuenteTexto));

            // Nota obligatoria
            Paragraph nota = new Paragraph("By PediGo", fuenteNota);
            nota.setAlignment(Element.ALIGN_CENTER);
            documento.add(nota);

            System.out.println("Comprobante PDF generado exitosamente: " + nombreArchivo);
        } catch (DocumentException | IOException e) {
            System.err.println("Error al generar el PDF del pedido: " + e.getMessage());
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }

        return archivoPDF;
    }
}
