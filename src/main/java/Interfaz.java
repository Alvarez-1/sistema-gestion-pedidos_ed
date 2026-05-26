import bot.BotPedidos;
import modelo.EstadoPedido;
import modelo.Pedido;
import modelo.Repartidor;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import servicio.ServicioAsignacion;
import servicio.ServicioExportacionExcel;
import estructura.Nodo;

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

/**
 * Interfaz gráfica única para el sistema PediGo usando Java Swing.
 */
public class Interfaz extends JFrame {

    private final ServicioAsignacion servicioAsignacion;
    private BotPedidos botPedidos = null;
    private boolean botIniciado = false;

    private JTextArea areaTexto;
    private JPanel panelNavegacion;
    private JPanel panelControles;
    private JTextField txtIdRepartidor;

    public Interfaz(ServicioAsignacion servicioAsignacion) {
        this.servicioAsignacion = servicioAsignacion;
        
        setTitle("PediGo - Interfaz");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        inicializarComponentes();
        mostrarVistaPrincipal();
        
        setVisible(true);
    }

    private void inicializarComponentes() {
        // Área de texto con scroll
        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(areaTexto);
        add(scroll, BorderLayout.CENTER);

        // Paneles de control
        panelNavegacion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelNavegacion, BorderLayout.NORTH);
        panelSuperior.add(panelControles, BorderLayout.SOUTH);
        
        add(panelSuperior, BorderLayout.NORTH);
    }

    // --- VISTAS ---

    private void mostrarVistaPrincipal() {
        limpiarControles();
        
        JButton btnBot = new JButton("Iniciar bot");
        btnBot.addActionListener(e -> iniciarBot());
        
        JButton btnAdmin = new JButton("Administrador");
        btnAdmin.addActionListener(e -> mostrarVistaAdministrador());
        
        JButton btnRepartidor = new JButton("Repartidor");
        btnRepartidor.addActionListener(e -> mostrarVistaRepartidor());
        
        JButton btnResumen = new JButton("Resumen");
        btnResumen.addActionListener(e -> mostrarResumen());
        
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> System.exit(0));

        panelControles.add(btnBot);
        panelControles.add(btnAdmin);
        panelControles.add(btnRepartidor);
        panelControles.add(btnResumen);
        panelControles.add(btnCerrar);
        
        actualizarUI();
        areaTexto.setText("Bienvenido al Sistema PediGo.\nSeleccione una opción para comenzar.");
    }

    private void mostrarVistaAdministrador() {
        limpiarControles();
        
        JButton btnPendientes = new JButton("Ver pendientes");
        btnPendientes.addActionListener(e -> {
            if (servicioAsignacion.getPedidosPendientes().esVacia()) {
                areaTexto.setText("No hay pedidos pendientes.");
            } else {
                areaTexto.setText("PEDIDOS PENDIENTES:\n" + servicioAsignacion.getPedidosPendientes());
            }
        });

        JButton btnSiguiente = new JButton("Procesar siguiente");
        btnSiguiente.addActionListener(e -> {
            if (servicioAsignacion.getPedidosPendientes().esVacia()) {
                areaTexto.setText("No hay pedidos pendientes para procesar.");
            } else if (servicioAsignacion.getRepartidores().esVacia()) {
                areaTexto.setText("No hay repartidores disponibles.");
            } else {
                Pedido p = servicioAsignacion.getPedidosPendientes().getPrimero().getDato();
                servicioAsignacion.asignacion();
                if (p.getEstadoActual() == EstadoPedido.REPARTIDOR_ASIGNADO) {
                    notificarCambioEstado(p);
                    areaTexto.setText("Se procesó el pedido " + p.getCodigo() + " según la cola de prioridad.");
                } else {
                    areaTexto.setText("No se pudo procesar la asignación (verifique zona o disponibilidad).");
                }
            }
        });

        JButton btnAvance = new JButton("Avance automático");
        btnAvance.addActionListener(e -> procesarAvanceAutomatico());

        JButton btnProceso = new JButton("Ver en proceso");
        btnProceso.addActionListener(e -> {
            if (servicioAsignacion.getPedidoParaEntregar().esVacia()) {
                areaTexto.setText("No hay pedidos en proceso.");
            } else {
                areaTexto.setText("PEDIDOS EN PROCESO:\n" + servicioAsignacion.getPedidoParaEntregar());
            }
        });

        JButton btnEntregados = new JButton("Ver entregados");
        btnEntregados.addActionListener(e -> areaTexto.setText("HISTORIAL ENTREGADOS:\n" + servicioAsignacion.getHistorialEntregas()));

        JButton btnCancelados = new JButton("Ver cancelados");
        btnCancelados.addActionListener(e -> areaTexto.setText("HISTORIAL CANCELADOS:\n" + servicioAsignacion.getHistorialCancelados()));

        JButton btnEstadisticas = new JButton("Ver estadísticas");
        btnEstadisticas.addActionListener(e -> {
            // Capturar la salida de consola o reconstruir
            StringBuilder sb = new StringBuilder("\n----- ESTADISTICAS -----\n");
            sb.append("Pedidos entregados: ").append(servicioAsignacion.getHistorialEntregas().getTamanio()).append("\n");
            sb.append("Pedidos cancelados: ").append(servicioAsignacion.getPedidosCancelados()).append("\n");
            sb.append("Ganancias: $").append(servicioAsignacion.getSaldo()).append("\n");
            sb.append("Zona más activa: ").append(servicioAsignacion.zonaMasPedidos());
            areaTexto.setText(sb.toString());
        });

        JButton btnExcel = new JButton("Exportar Excel");
        btnExcel.addActionListener(e -> {
            ServicioExportacionExcel excel = new ServicioExportacionExcel();
            excel.exportarEstadisticasExcel("exportaciones/estadisticas_pedigo.xlsx", servicioAsignacion);
            JOptionPane.showMessageDialog(this, "Excel generado exitosamente en: exportaciones/estadisticas_pedigo.xlsx");
        });

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> mostrarVistaPrincipal());

        panelControles.add(btnPendientes);
        panelControles.add(btnSiguiente);
        panelControles.add(btnAvance);
        panelControles.add(btnProceso);
        panelControles.add(btnEntregados);
        panelControles.add(btnCancelados);
        panelControles.add(btnEstadisticas);
        panelControles.add(btnExcel);
        panelControles.add(btnVolver);
        
        actualizarUI();
        areaTexto.setText("MODO ADMINISTRADOR ACTIVO");
    }

    private void mostrarVistaRepartidor() {
        limpiarControles();
        
        panelControles.add(new JLabel("ID Repartidor:"));
        txtIdRepartidor = new JTextField(10);
        panelControles.add(txtIdRepartidor);
        
        JButton btnVer = new JButton("Ver asignados");
        btnVer.addActionListener(e -> {
            Long id = leerIdRepartidor();
            if (id == null) return;
            Repartidor r = servicioAsignacion.getUsuariosRepartidores().get(id);
            if (r == null) {
                areaTexto.setText("Repartidor no encontrado.");
                return;
            }
            areaTexto.setText("PEDIDOS ASIGNADOS A " + r.getNombre().toUpperCase() + ":\n");
            boolean encontro = false;
            Nodo<Pedido> actual = servicioAsignacion.getPedidoParaEntregar().getPrimero();
            while (actual != null) {
                Pedido p = actual.getDato();
                if (p.getRappi() != null && p.getRappi().getId() == id) {
                    areaTexto.append(p.toString() + "\n");
                    encontro = true;
                }
                actual = actual.getSiguiente();
            }
            if (!encontro) areaTexto.append("No tiene pedidos asignados.");
        });

        JButton btnAvanzar = new JButton("Avanzar estado");
        btnAvanzar.addActionListener(e -> {
            Long id = leerIdRepartidor();
            if (id == null) return;
            String codStr = JOptionPane.showInputDialog(this, "Ingrese el código del pedido:");
            if (codStr == null) return;
            try {
                int codigo = Integer.parseInt(codStr);
                Pedido pedido = servicioAsignacion.buscarPedido(codigo);
                if (pedido == null) { areaTexto.setText("Pedido no encontrado."); return; }
                if (pedido.getRappi() == null || pedido.getRappi().getId() != id) {
                    areaTexto.setText("Este pedido no está asignado a usted.");
                    return;
                }
                if (pedido.getEstadoActual() == EstadoPedido.ENTREGADO || pedido.getEstadoActual() == EstadoPedido.CANCELADO) {
                    areaTexto.setText("El pedido ya está finalizado.");
                } else {
                    servicioAsignacion.avanzarEstado(pedido);
                    notificarCambioEstado(pedido);
                    areaTexto.setText("Estado del pedido " + codigo + " actualizado.");
                }
            } catch (NumberFormatException ex) {
                areaTexto.setText("Código inválido.");
            }
        });

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> mostrarVistaPrincipal());

        panelControles.add(btnVer);
        panelControles.add(btnAvanzar);
        panelControles.add(btnVolver);
        
        actualizarUI();
        areaTexto.setText("MODO REPARTIDOR ACTIVO\nIngrese su ID para operar.");
    }

    private void mostrarResumen() {
        StringBuilder sb = new StringBuilder("\n--- RESUMEN DEL SISTEMA ---\n");
        sb.append("Pedidos pendientes: ").append(servicioAsignacion.getPedidosPendientes().getTamanio()).append("\n");
        sb.append("Pedidos en proceso: ").append(servicioAsignacion.getPedidoParaEntregar().getTamanio()).append("\n");
        sb.append("Pedidos entregados: ").append(servicioAsignacion.getHistorialEntregas().getTamanio()).append("\n");
        sb.append("Pedidos cancelados: ").append(servicioAsignacion.getPedidosCancelados()).append("\n");
        sb.append("Repartidores registrados: ").append(servicioAsignacion.getUsuariosRepartidores().size()).append("\n");
        areaTexto.setText(sb.toString());
    }

    // --- LÓGICA DE APOYO ---

    private void iniciarBot() {
        if (botIniciado) {
            JOptionPane.showMessageDialog(this, "El bot ya está activo.");
            return;
        }
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botPedidos = new BotPedidos(servicioAsignacion);
            botsApi.registerBot(botPedidos);
            botIniciado = true;
            areaTexto.setText("Bot de Telegram PediGo iniciado correctamente.");
        } catch (TelegramApiException e) {
            areaTexto.setText("Error al iniciar bot: " + e.getMessage());
        }
    }

    private void procesarAvanceAutomatico() {
        if (servicioAsignacion.getPedidoParaEntregar().esVacia()) {
            areaTexto.setText("No hay pedidos en curso para avanzar.");
            return;
        }
        int tamanio = servicioAsignacion.getPedidoParaEntregar().getTamanio();
        Pedido[] pedidos = new Pedido[tamanio];
        Nodo<Pedido> actual = servicioAsignacion.getPedidoParaEntregar().getPrimero();
        int i = 0;
        while (actual != null && i < tamanio) {
            pedidos[i++] = actual.getDato();
            actual = actual.getSiguiente();
        }
        for (Pedido p : pedidos) {
            if (p != null) {
                servicioAsignacion.avanzarEstado(p);
                notificarCambioEstado(p);
            }
        }
        areaTexto.setText("Se avanzaron todos los pedidos en curso automáticamente.");
    }

    private void notificarCambioEstado(Pedido p) {
        if (botPedidos == null || p == null || p.getUsuario() == null) return;
        long chatId = p.getUsuario().getId();
        String msg = "";
        switch (p.getEstadoActual()) {
            case REPARTIDOR_ASIGNADO -> msg = "Tu pedido " + p.getCodigo() + " ya tiene repartidor asignado.\nRepartidor: " + p.getRappi().getNombre() + ".";
            case EN_CAMINO -> msg = "Tu pedido " + p.getCodigo() + " va en camino.\nRepartidor: " + p.getRappi().getNombre() + ".";
            case ENTREGADO -> msg = "Tu pedido " + p.getCodigo() + " fue entregado.\nGracias por comprar en PediGo.";
        }
        if (!msg.isEmpty()) botPedidos.enviarMensajeSistema(chatId, msg);
    }

    private Long leerIdRepartidor() {
        try {
            return Long.parseLong(txtIdRepartidor.getText().trim());
        } catch (NumberFormatException e) {
            areaTexto.setText("ID de repartidor inválido.");
            return null;
        }
    }

    private void limpiarControles() {
        panelControles.removeAll();
    }

    private void actualizarUI() {
        panelControles.revalidate();
        panelControles.repaint();
    }
}
