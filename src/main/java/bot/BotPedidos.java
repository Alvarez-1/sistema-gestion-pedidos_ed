package bot;

import bot.estado.EstadoConversacionBot;
import bot.sesion.ClienteRegistroTemporal;
import bot.sesion.SesionPedidoTemporal;
import configuracion.ConfiguracionApp;
import estructura.ListaSimple;
import estructura.Nodo;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import modelo.Cliente;
import modelo.Comercio;
import modelo.EstadoPedido;
import modelo.Pedido;
import modelo.Producto;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import servicio.ServicioAsignacion;
import servicio.ServicioComprobante;
import servicio.ServicioDatosBD;

public class BotPedidos extends TelegramLongPollingBot {

    private final ConfiguracionApp configuracion;
    private final ServicioDatosBD servicioDatosBD;
    private final ServicioAsignacion servicioAsignacion;
    private final ServicioComprobante servicioComprobante;
    
    private final HashMap<Long, EstadoConversacionBot> estadosPorChat;
    private final HashMap<Long, Cliente> clientesTelegram;
    private final HashMap<Long, ClienteRegistroTemporal> registrosTemporales;
    private final HashMap<Long, SesionPedidoTemporal> pedidosTemporales;

    private int contadorPedidosTelegram = 1000;

    public BotPedidos() {
        this.configuracion = new ConfiguracionApp();
        this.servicioDatosBD = new ServicioDatosBD();
        this.servicioAsignacion = new ServicioAsignacion();
        this.servicioComprobante = new ServicioComprobante();
        
        this.estadosPorChat = new HashMap<>();
        this.clientesTelegram = new HashMap<>();
        this.registrosTemporales = new HashMap<>();
        this.pedidosTemporales = new HashMap<>();

        // Carga inicial de repartidores para que el servicio tenga datos
        this.servicioAsignacion.setRepartidores(servicioDatosBD.cargarRepartidores());
    }

    @Override
    public String getBotUsername() {
        return configuracion.getTelegramBotUsername();
    }

    @Override
    public String getBotToken() {
        return configuracion.getTelegramBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            manejarMensajeTexto(update);
        } else if (update.hasCallbackQuery()) {
            manejarCallback(update);
        }
    }

    private void manejarMensajeTexto(Update update) {
        String mensajeTexto = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        if (mensajeTexto.equalsIgnoreCase("cancelar")) {
            cancelarOperacion(chatId);
            return;
        }

        if (mensajeTexto.equals("/start")) {
            enviarMenuPrincipal(chatId);
            return;
        }

        EstadoConversacionBot estado = estadosPorChat.getOrDefault(chatId, EstadoConversacionBot.INICIO);

        switch (estado) {
            case REGISTRO_NOMBRE:
                procesarRegistroNombre(chatId, mensajeTexto);
                break;
            case REGISTRO_TELEFONO:
                procesarRegistroTelefono(chatId, mensajeTexto);
                break;
            case REGISTRO_DIRECCION:
                procesarRegistroDireccion(chatId, mensajeTexto);
                break;
            case REGISTRO_ZONA:
                procesarRegistroZona(chatId, mensajeTexto);
                break;
            case CONSULTA_PEDIDO_CODIGO:
                procesarConsultaPedido(chatId, mensajeTexto);
                break;
            case CREAR_PEDIDO_SELECCION_COMERCIO:
                procesarSeleccionComercio(chatId, mensajeTexto);
                break;
            case CREAR_PEDIDO_SELECCION_PRODUCTO:
                procesarSeleccionProducto(chatId, mensajeTexto);
                break;
            case CREAR_PEDIDO_CANTIDAD:
                procesarCantidadProducto(chatId, mensajeTexto);
                break;
            default:
                enviarMensaje(chatId, "Usa /start para abrir el menú principal.");
                break;
        }
    }

    private void enviarMenuPrincipal(long chatId) {
        estadosPorChat.put(chatId, EstadoConversacionBot.MENU_PRINCIPAL);
        
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Bienvenido a PediGo.\nGestiona tus domicilios de forma rápida desde aquí.\n\n¿Qué quieres hacer?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(crearFilaBoton("Registrarme", "REGISTRARME"));
        rowsInline.add(crearFilaBoton("Ver comercios", "VER_COMERCIOS"));
        rowsInline.add(crearFilaBoton("Crear pedido", "CREAR_PEDIDO"));
        rowsInline.add(crearFilaBoton("Consultar pedido", "CONSULTAR_PEDIDO"));

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error al enviar menú principal: " + e.getMessage());
        }
    }

    private List<InlineKeyboardButton> crearFilaBoton(String texto, String callbackData) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton boton = new InlineKeyboardButton();
        boton.setText(texto);
        boton.setCallbackData(callbackData);
        row.add(boton);
        return row;
    }

    private void manejarCallback(Update update) {
        String callData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackQueryId = update.getCallbackQuery().getId();

        if (callData.startsWith("CANCELAR_PEDIDO_")) {
            procesarCancelacionReal(chatId, callData.replace("CANCELAR_PEDIDO_", ""));
        } else {
            switch (callData) {
                case "REGISTRARME":
                    iniciarRegistro(chatId);
                    break;
                case "VER_COMERCIOS":
                    mostrarComercios(chatId);
                    break;
                case "CREAR_PEDIDO":
                    iniciarCreacionPedido(chatId);
                    break;
                case "CONSULTAR_PEDIDO":
                    iniciarConsultaPedido(chatId);
                    break;
                case "AGREGAR_OTRO_PRODUCTO":
                    volverASeleccionProducto(chatId);
                    break;
                case "CONFIRMAR_PEDIDO":
                    confirmarPedido(chatId);
                    break;
                default:
                    enviarMensaje(chatId, "Opción no reconocida.");
                    break;
            }
        }

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            System.err.println("Error al responder callback: " + e.getMessage());
        }
    }

    // --- FLUJO DE REGISTRO ---

    private void iniciarRegistro(long chatId) {
        estadosPorChat.put(chatId, EstadoConversacionBot.REGISTRO_NOMBRE);
        registrosTemporales.put(chatId, new ClienteRegistroTemporal());
        enviarMensaje(chatId, "Ingresa tu nombre:");
    }

    private void procesarRegistroNombre(long chatId, String nombre) {
        registrosTemporales.get(chatId).setNombre(nombre);
        estadosPorChat.put(chatId, EstadoConversacionBot.REGISTRO_TELEFONO);
        enviarMensaje(chatId, "Ingresa tu número de teléfono:");
    }

    private void procesarRegistroTelefono(long chatId, String telefonoStr) {
        try {
            long telefono = Long.parseLong(telefonoStr);
            registrosTemporales.get(chatId).setTelefono(telefono);
            estadosPorChat.put(chatId, EstadoConversacionBot.REGISTRO_DIRECCION);
            enviarMensaje(chatId, "Ingresa tu dirección:");
        } catch (NumberFormatException e) {
            enviarMensaje(chatId, "El número de teléfono debe ser numérico. Inténtalo nuevamente:");
        }
    }

    private void procesarRegistroDireccion(long chatId, String direccion) {
        registrosTemporales.get(chatId).setDireccion(direccion);
        estadosPorChat.put(chatId, EstadoConversacionBot.REGISTRO_ZONA);
        enviarMensaje(chatId, "Ingresa tu zona: Norte, Sur, Este u Oeste.");
    }

    private void procesarRegistroZona(long chatId, String zona) {
        String zonaNormalizada = normalizarZona(zona);
        if (zonaNormalizada == null) {
            enviarMensaje(chatId, "Zona no válida. Ingresa una de estas opciones: Norte, Sur, Este u Oeste.");
            return;
        }

        ClienteRegistroTemporal temp = registrosTemporales.get(chatId);
        Cliente nuevoCliente = new Cliente(temp.getDireccion(), false, chatId, temp.getNombre(), temp.getTelefono(), zonaNormalizada);
        
        clientesTelegram.put(chatId, nuevoCliente);
        registrosTemporales.remove(chatId);
        
        enviarMensaje(chatId, "Registro completado correctamente.");
        enviarMenuPrincipal(chatId);
    }

    private String normalizarZona(String zona) {
        String z = zona.trim().toLowerCase();
        if (z.equals("norte")) return "Norte";
        if (z.equals("sur")) return "Sur";
        if (z.equals("este")) return "Este";
        if (z.equals("oeste")) return "Oeste";
        return null;
    }

    // --- VER COMERCIOS ---

    private void mostrarComercios(long chatId) {
        ListaSimple<Comercio> comercios = servicioDatosBD.cargarComercios();
        if (comercios == null || comercios.esVacia()) {
            enviarMensaje(chatId, "No hay comercios disponibles por el momento.");
        } else {
            enviarMensaje(chatId, construirTextoComercios(comercios));
        }
        enviarMenuPrincipal(chatId);
    }

    private String construirTextoComercios(ListaSimple<Comercio> lista) {
        StringBuilder sb = new StringBuilder("Comercios disponibles:\n");
        Nodo<Comercio> actual = lista.getPrimero();
        int i = 1;
        while (actual != null) {
            sb.append(i).append(". ").append(actual.getDato().getNombre()).append("\n");
            actual = actual.getSiguiente();
            i++;
        }
        return sb.toString();
    }

    // --- FLUJO CREAR PEDIDO ---

    private void iniciarCreacionPedido(long chatId) {
        Cliente cliente = clientesTelegram.get(chatId);
        if (cliente == null) {
            enviarMensaje(chatId, "Primero debes registrarte para crear un pedido.");
            enviarMenuPrincipal(chatId);
            return;
        }

        ListaSimple<Comercio> comercios = servicioDatosBD.cargarComercios();
        if (comercios == null || comercios.esVacia()) {
            enviarMensaje(chatId, "Lo sentimos, no hay comercios disponibles para realizar pedidos.");
            enviarMenuPrincipal(chatId);
            return;
        }

        SesionPedidoTemporal sesion = new SesionPedidoTemporal();
        sesion.setComerciosDisponibles(comercios);
        pedidosTemporales.put(chatId, sesion);
        
        estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_SELECCION_COMERCIO);
        enviarMensaje(chatId, construirTextoComercios(comercios) + "\nEscribe el número del comercio donde quieres pedir:");
    }

    private void procesarSeleccionComercio(long chatId, String indexStr) {
        try {
            int indice = Integer.parseInt(indexStr);
            SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
            Comercio comercio = obtenerComercioPorIndice(sesion.getComerciosDisponibles(), indice);
            
            if (comercio == null) {
                enviarMensaje(chatId, "Número de comercio no válido. Inténtalo de nuevo:");
                return;
            }

            sesion.setComercioSeleccionado(comercio);
            Pedido nuevoPedido = new Pedido(contadorPedidosTelegram++, clientesTelegram.get(chatId), comercio);
            sesion.setPedidoTemporal(nuevoPedido);
            
            // Cargar productos
            ListaSimple<Producto> productos = comercio.getProductos();
            sesion.setProductosDisponibles(productos);
            
            estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_SELECCION_PRODUCTO);
            enviarMensaje(chatId, construirTextoProductos(productos) + "\nEscribe el número del producto que deseas agregar:");
            
        } catch (NumberFormatException e) {
            enviarMensaje(chatId, "Debes ingresar un número válido:");
        }
    }

    private String construirTextoProductos(ListaSimple<Producto> lista) {
        StringBuilder sb = new StringBuilder("Productos disponibles:\n");
        Nodo<Producto> actual = lista.getPrimero();
        int i = 1;
        while (actual != null) {
            Producto p = actual.getDato();
            sb.append(i).append(". ").append(p.getNombre())
              .append(" ($").append(p.getPrecio()).append(") ")
              .append("[Disp: ").append(p.getCantidad()).append("]\n");
            actual = actual.getSiguiente();
            i++;
        }
        return sb.toString();
    }

    private void procesarSeleccionProducto(long chatId, String indexStr) {
        try {
            int indice = Integer.parseInt(indexStr);
            SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
            Producto producto = obtenerProductoPorIndice(sesion.getProductosDisponibles(), indice);
            
            if (producto == null) {
                enviarMensaje(chatId, "Número de producto no válido. Inténtalo de nuevo:");
                return;
            }

            if (producto.getCantidad() <= 0) {
                enviarMensaje(chatId, "Lo sentimos, este producto no tiene stock. Selecciona otro:");
                return;
            }

            sesion.setProductoSeleccionado(producto);
            estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_CANTIDAD);
            enviarMensaje(chatId, "Ingresa la cantidad:");
            
        } catch (NumberFormatException e) {
            enviarMensaje(chatId, "Debes ingresar un número válido:");
        }
    }

    private void procesarCantidadProducto(long chatId, String cantStr) {
        try {
            int cantidad = Integer.parseInt(cantStr);
            if (cantidad <= 0) {
                enviarMensaje(chatId, "La cantidad debe ser mayor a 0. Inténtalo de nuevo:");
                return;
            }

            SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
            Producto pOriginal = sesion.getProductoSeleccionado();
            
            if (cantidad > pOriginal.getCantidad()) {
                enviarMensaje(chatId, "No hay suficiente stock. Disponible: " + pOriginal.getCantidad() + ". Ingresa una cantidad menor:");
                return;
            }

            sesion.getPedidoTemporal().agregarProducto(pOriginal.getCodigoProducto(), cantidad);
            
            // Mostrar opciones
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Producto agregado. ¿Qué deseas hacer ahora?");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            rows.add(crearFilaBoton("Agregar otro producto", "AGREGAR_OTRO_PRODUCTO"));
            rows.add(crearFilaBoton("Confirmar pedido", "CONFIRMAR_PEDIDO"));
            markup.setKeyboard(rows);
            message.setReplyMarkup(markup);

            execute(message);
            estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_CONFIRMACION);

        } catch (NumberFormatException | TelegramApiException e) {
            enviarMensaje(chatId, "Ingresa un número entero válido:");
        }
    }

    private void volverASeleccionProducto(long chatId) {
        SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
        estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_SELECCION_PRODUCTO);
        enviarMensaje(chatId, construirTextoProductos(sesion.getProductosDisponibles()) + "\nEscribe el número del producto que deseas agregar:");
    }

    private void confirmarPedido(long chatId) {
        SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
        Pedido pedido = sesion.getPedidoTemporal();

        if (pedido == null || !pedido.tieneProductos()) {
            enviarMensaje(chatId, "No se puede confirmar un pedido sin productos.");
            cancelarOperacion(chatId);
            return;
        }

        // 1. Encolar en pedidos pendientes
        Cliente cliente = clientesTelegram.get(chatId);
        servicioAsignacion.getPedidosPendientes().encolar(pedido, cliente.isVip() ? 1 : 0);

        // 2. Registrar en el mapa
        servicioAsignacion.registrarPedidoEnMapa(pedido);

        // 3. Generar PDF
        File pdf = servicioComprobante.generarComprobantePedidoPDF(pedido);

        // 4. Enviar PDF
        if (pdf != null && pdf.exists()) {
            SendDocument doc = new SendDocument();
            doc.setChatId(String.valueOf(chatId));
            doc.setDocument(new InputFile(pdf));
            doc.setCaption("Aquí tienes tu comprobante de pedido.");
            try {
                execute(doc);
            } catch (TelegramApiException e) {
                enviarMensaje(chatId, "El pedido fue creado, pero no se pudo enviar el comprobante PDF.");
            }
        } else {
            enviarMensaje(chatId, "El pedido fue creado, pero no se pudo generar el comprobante PDF.");
        }

        enviarMensaje(chatId, "Pedido creado correctamente. Código del pedido: " + pedido.getCodigo());
        
        pedidosTemporales.remove(chatId);
        enviarMenuPrincipal(chatId);
    }

    // --- CONSULTAR Y CANCELAR ---

    private void iniciarConsultaPedido(long chatId) {
        estadosPorChat.put(chatId, EstadoConversacionBot.CONSULTA_PEDIDO_CODIGO);
        enviarMensaje(chatId, "Ingresa el código del pedido que deseas consultar:");
    }

    private void procesarConsultaPedido(long chatId, String codigoStr) {
        try {
            int codigo = Integer.parseInt(codigoStr);
            Pedido pedido = servicioAsignacion.buscarPedido(codigo);

            if (pedido == null) {
                enviarMensaje(chatId, "No se encontró ningún pedido con el código: " + codigo);
                enviarMenuPrincipal(chatId);
                return;
            }

            String resumen = "Resumen del pedido " + codigo + ":\n" +
                             "Estado: " + pedido.getEstadoActual() + "\n" +
                             "Comercio: " + pedido.getTienda().getNombre() + "\n" +
                             "Total: $" + pedido.getValorTotal();
            
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(resumen);

            // Si no está ENTREGADO ni CANCELADO, mostrar botón cancelar
            if (pedido.getEstadoActual() != EstadoPedido.ENTREGADO && 
                pedido.getEstadoActual() != EstadoPedido.CANCELADO) {
                
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                rows.add(crearFilaBoton("Cancelar pedido", "CANCELAR_PEDIDO_" + codigo));
                markup.setKeyboard(rows);
                message.setReplyMarkup(markup);
            }

            execute(message);
            enviarMenuPrincipal(chatId);

        } catch (NumberFormatException | TelegramApiException e) {
            enviarMensaje(chatId, "El código del pedido debe ser numérico. Inténtalo nuevamente.");
        }
    }

    private void procesarCancelacionReal(long chatId, String codigoStr) {
        try {
            int codigo = Integer.parseInt(codigoStr);
            Pedido pedido = servicioAsignacion.buscarPedido(codigo);

            if (pedido != null) {
                servicioAsignacion.cancelarPedido(pedido);
                enviarMensaje(chatId, "Pedido " + codigo + " cancelado correctamente.");
            } else {
                enviarMensaje(chatId, "No se encontró el pedido para cancelar.");
            }
        } catch (NumberFormatException e) {
            enviarMensaje(chatId, "Error al procesar el código del pedido.");
        }
        enviarMenuPrincipal(chatId);
    }

    // --- UTILIDADES ---

    private Comercio obtenerComercioPorIndice(ListaSimple<Comercio> lista, int indice) {
        if (indice <= 0 || lista == null) return null;
        Nodo<Comercio> actual = lista.getPrimero();
        int i = 1;
        while (actual != null) {
            if (i == indice) return actual.getDato();
            actual = actual.getSiguiente();
            i++;
        }
        return null;
    }

    private Producto obtenerProductoPorIndice(ListaSimple<Producto> lista, int indice) {
        if (indice <= 0 || lista == null) return null;
        Nodo<Producto> actual = lista.getPrimero();
        int i = 1;
        while (actual != null) {
            if (i == indice) return actual.getDato();
            actual = actual.getSiguiente();
            i++;
        }
        return null;
    }

    private void cancelarOperacion(long chatId) {
        estadosPorChat.remove(chatId);
        registrosTemporales.remove(chatId);
        pedidosTemporales.remove(chatId);
        enviarMensaje(chatId, "Operación cancelada.");
        enviarMenuPrincipal(chatId);
    }

    private void enviarMensaje(long chatId, String texto) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(texto);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error al enviar mensaje: " + e.getMessage());
        }
    }
}
