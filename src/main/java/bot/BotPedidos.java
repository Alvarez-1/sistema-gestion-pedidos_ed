package bot;

import bot.estado.EstadoConversacionBot;
import bot.sesion.ClienteRegistroTemporal;
import bot.sesion.SesionPedidoTemporal;
import configuracion.ConfiguracionApp;
import estructura.ListaSimple;
import estructura.Nodo;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
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
import servicio.ServicioClienteBD;
import servicio.ServicioComprobante;
import servicio.ServicioDatosBD;

public class BotPedidos extends TelegramLongPollingBot {

    private final ConfiguracionApp configuracion;
    private final ServicioDatosBD servicioDatosBD;
    private final ServicioAsignacion servicioAsignacion;
    private final ServicioComprobante servicioComprobante;
    private final ServicioClienteBD servicioClienteBD;
    
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
        this.servicioClienteBD = new ServicioClienteBD();
        
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
            volverAlMenu(chatId, true);
            return;
        }

        if (mensajeTexto.equals("/start")) {
            procesarComandoStart(chatId);
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

    private void procesarComandoStart(long chatId) {
        estadosPorChat.remove(chatId);
        registrosTemporales.remove(chatId);
        pedidosTemporales.remove(chatId);

        Cliente cliente = servicioClienteBD.buscarClientePorChatId(chatId);
        if (cliente != null) {
            clientesTelegram.put(chatId, cliente);
            enviarMenuPrincipal(chatId);
        } else {
            estadosPorChat.put(chatId, EstadoConversacionBot.ESPERANDO_ACEPTACION_DATOS);
            enviarMensajeConBotones(chatId, getMensajeTratamientoDatos(), crearBotonesAceptarDatos());
        }
    }

    private String getMensajeTratamientoDatos() {
        return "Para continuar usando PediGo, primero debes aceptar los Términos y condiciones, " +
                "autorizar el tratamiento de tus datos personales y la Política de Acceso a Canales de Atención.\n\n" +
                "1️⃣ Ver Términos y condiciones de Telegram para el acceso a sus canales en: https://telegram.org/tos\n\n" +
                "2️⃣ Ver Política de tratamiento de información personal segun la Ley 1581 de 2012 en: " +
                "https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=49981\n\n" +
                "¿Aceptas y te acoges a las políticas y procedimientos anteriores? (Presione SI para autorizar, o NO para no continuar)";
    }

    private InlineKeyboardMarkup crearBotonesAceptarDatos() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(crearFilaBoton("Sí, acepto", "ACEPTAR_DATOS"));
        rows.add(crearFilaBoton("No acepto", "RECHAZAR_DATOS"));
        markup.setKeyboard(rows);
        return markup;
    }

    private void enviarMenuPrincipal(long chatId) {
        estadosPorChat.put(chatId, EstadoConversacionBot.MENU_PRINCIPAL);
        
        String texto = "Bienvenido a PediGo.\nUn sistema de domicilios y el orgullo venezolano.\n\n¿Qué quieres hacer chamo?";
        
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        if (clientesTelegram.containsKey(chatId)) {
            rowsInline.add(crearFilaBoton("Crear pedido", "CREAR_PEDIDO"));
            rowsInline.add(crearFilaBoton("Consultar pedido", "CONSULTAR_PEDIDO"));
            rowsInline.add(crearFilaBoton("Actualizar datos", "ACTUALIZAR_DATOS"));
        } else {
            rowsInline.add(crearFilaBoton("Registrarme", "REGISTRARME"));
        }

        markupInline.setKeyboard(rowsInline);
        enviarMensajeConBotones(chatId, texto, markupInline);
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
        } else if (callData.startsWith("GENERAR_COMPROBANTE_")) {
            procesarGenerarComprobante(chatId, callData.replace("GENERAR_COMPROBANTE_", ""));
        } else if (callData.startsWith("NO_COMPROBANTE_")) {
            procesarNoComprobante(chatId, callData.replace("NO_COMPROBANTE_", ""));
        } else {
            switch (callData) {
                case "ACEPTAR_DATOS":
                    iniciarRegistro(chatId);
                    break;
                case "RECHAZAR_DATOS":
                    enviarMensaje(chatId, "Naguara, no puedes continuar sin aceptar el tratamiento de datos. Si cambias de opinión, usa /start.");
                    break;
                case "REGISTRARME":
                    verificarRegistroExistente(chatId);
                    break;
                case "ACTUALIZAR_DATOS":
                    iniciarRegistro(chatId);
                    break;
                case "CREAR_PEDIDO":
                    if (verificarSesion(chatId)) iniciarCreacionPedido(chatId);
                    break;
                case "ENTREGA_NORMAL":
                    procesarTipoEntrega(chatId, false);
                    break;
                case "ENTREGA_PRIORITARIA":
                    procesarTipoEntrega(chatId, true);
                    break;
                case "CONSULTAR_PEDIDO":
                    if (verificarSesion(chatId)) iniciarConsultaPedido(chatId);
                    break;
                case "AGREGAR_OTRO_PRODUCTO":
                    volverASeleccionProducto(chatId);
                    break;
                case "CONFIRMAR_PEDIDO":
                    confirmarPedido(chatId);
                    break;
                case "VOLVER_MENU":
                    volverAlMenu(chatId, false);
                    break;
                case "CANCELAR_PROCESO":
                    volverAlMenu(chatId, true);
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

    private boolean verificarSesion(long chatId) {
        if (clientesTelegram.containsKey(chatId)) return true;
        
        Cliente cliente = servicioClienteBD.buscarClientePorChatId(chatId);
        if (cliente != null) {
            clientesTelegram.put(chatId, cliente);
            return true;
        }
        
        enviarMensajeConBotones(chatId, "Primero debes registrarte para usar esta opción.", 
                new InlineKeyboardMarkup(List.of(crearFilaBoton("Registrarme", "REGISTRARME"))));
        return false;
    }

    private void verificarRegistroExistente(long chatId) {
        if (verificarSesion(chatId)) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            rows.add(crearFilaBoton("Actualizar datos", "ACTUALIZAR_DATOS"));
            rows.add(crearFilaBoton("Volver al menú", "VOLVER_MENU"));
            markup.setKeyboard(rows);
            enviarMensajeConBotones(chatId, "Ya estás registrado en PediGo.\n\n¿Quieres actualizar tus datos chamo?", markup);
        } else {
            iniciarRegistro(chatId);
        }
    }

    // --- FLUJO DE REGISTRO ---

    private void iniciarRegistro(long chatId) {
        estadosPorChat.put(chatId, EstadoConversacionBot.REGISTRO_NOMBRE);
        registrosTemporales.put(chatId, new ClienteRegistroTemporal());
        enviarMensajeConBotones(chatId, "Ingresa tu nombre completo:", crearBotonCancelar());
    }

    private void procesarRegistroNombre(long chatId, String nombre) {
        if (!esNombreValido(nombre)) {
            enviarMensajeConBotones(chatId, "El nombre no es válido.", crearBotonCancelar());
            return;
        }
        registrosTemporales.get(chatId).setNombre(nombre);
        estadosPorChat.put(chatId, EstadoConversacionBot.REGISTRO_TELEFONO);
        enviarMensajeConBotones(chatId, "Ingresa tu número de teléfono.", crearBotonCancelar());
    }

    private boolean esNombreValido(String nombre) {
        if (nombre == null || nombre.trim().length() < 3 || nombre.trim().length() > 60) return false;
        return Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", nombre);
    }

    private void procesarRegistroTelefono(long chatId, String telefonoStr) {
        if (!esCelularColombianoValido(telefonoStr)) {
            enviarMensajeConBotones(chatId, "El número debe tener 10 dígitos,", crearBotonCancelar());
            return;
        }
        registrosTemporales.get(chatId).setTelefono(Long.parseLong(telefonoStr));
        estadosPorChat.put(chatId, EstadoConversacionBot.REGISTRO_DIRECCION);
        enviarMensajeConBotones(chatId, "Ingresa tu dirección:", crearBotonCancelar());
    }

    private boolean esCelularColombianoValido(String telefono) {
        if (telefono == null) return false;
        return Pattern.matches("^3\\d{9}$", telefono);
    }

    private void procesarRegistroDireccion(long chatId, String direccion) {
        registrosTemporales.get(chatId).setDireccion(direccion);
        estadosPorChat.put(chatId, EstadoConversacionBot.REGISTRO_ZONA);
        enviarMensajeConBotones(chatId, "Ingresa tu zona: Norte, Sur, Este u Oeste.", crearBotonCancelar());
    }

    private void procesarRegistroZona(long chatId, String zona) {
        String zonaNormalizada = normalizarZona(zona);
        if (zonaNormalizada == null) {
            enviarMensajeConBotones(chatId, "Zona no válida. Ingresa una de estas opciones: Norte, Sur, Este u Oeste.", crearBotonCancelar());
            return;
        }

        ClienteRegistroTemporal temp = registrosTemporales.get(chatId);
        Cliente nuevoCliente = new Cliente(temp.getDireccion(), false, chatId, temp.getNombre(), temp.getTelefono(), zonaNormalizada);
        
        servicioClienteBD.guardarOActualizarCliente(nuevoCliente, chatId);
        clientesTelegram.put(chatId, nuevoCliente);
        registrosTemporales.remove(chatId);
        
        enviarMensaje(chatId, "Registro completado correctamente. Tus datos fueron guardados de forma segura.");
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

    // --- FLUJO CREAR PEDIDO ---

    private void iniciarCreacionPedido(long chatId) {
        estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_TIPO_ENTREGA);
        
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(crearFilaBoton("Entrega normal", "ENTREGA_NORMAL"));
        rows.add(crearFilaBoton("Entrega prioritaria", "ENTREGA_PRIORITARIA"));
        rows.add(crearFilaBoton("Cancelar proceso", "CANCELAR_PROCESO"));
        markup.setKeyboard(rows);

        enviarMensajeConBotones(chatId, "Selecciona el tipo de entrega:", markup);
    }

    private void procesarTipoEntrega(long chatId, boolean prioritaria) {
        SesionPedidoTemporal sesion = new SesionPedidoTemporal();
        sesion.setEntregaPrioritaria(prioritaria);
        
        ListaSimple<Comercio> comercios = servicioDatosBD.cargarComercios();
        if (comercios == null || comercios.esVacia()) {
            enviarMensaje(chatId, "Lo sentimos, no hay comercios disponibles.");
            enviarMenuPrincipal(chatId);
            return;
        }

        sesion.setComerciosDisponibles(comercios);
        pedidosTemporales.put(chatId, sesion);
        
        estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_SELECCION_COMERCIO);
        enviarMensajeConBotones(chatId, construirTextoComercios(comercios) + "\nEscribe el número del comercio:", crearBotonCancelar());
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

    private void procesarSeleccionComercio(long chatId, String indexStr) {
        try {
            int indice = Integer.parseInt(indexStr);
            SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
            Comercio comercio = obtenerComercioPorIndice(sesion.getComerciosDisponibles(), indice);
            
            if (comercio == null) {
                enviarMensajeConBotones(chatId, "Número no válido. Inténtalo de nuevo:", crearBotonCancelar());
                return;
            }

            sesion.setComercioSeleccionado(comercio);
            Pedido nuevoPedido = new Pedido(generarCodigoPedido(), clientesTelegram.get(chatId), comercio);
            sesion.setPedidoTemporal(nuevoPedido);
            
            ListaSimple<Producto> productos = comercio.getProductos();
            sesion.setProductosDisponibles(productos);
            
            estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_SELECCION_PRODUCTO);
            enviarMensajeConBotones(chatId, construirTextoProductos(productos) + "\nEscribe el número del producto:", crearBotonCancelar());
            
        } catch (NumberFormatException e) {
            enviarMensajeConBotones(chatId, "Ingresa un número válido:", crearBotonCancelar());
        }
    }

    private int generarCodigoPedido() {
        return contadorPedidosTelegram++;
    }

    private String construirTextoProductos(ListaSimple<Producto> lista) {
        StringBuilder sb = new StringBuilder("Productos disponibles:\n");
        Nodo<Producto> actual = lista.getPrimero();
        int i = 1;
        while (actual != null) {
            Producto p = actual.getDato();
            sb.append(i).append(". ").append(p.getNombre())
              .append(" (").append(formatearMoneda(p.getPrecio())).append(") ")
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
                enviarMensajeConBotones(chatId, "Número no válido. Inténtalo de nuevo:", crearBotonCancelar());
                return;
            }

            if (producto.getCantidad() <= 0) {
                enviarMensajeConBotones(chatId, "Sin stock. Selecciona otro:", crearBotonCancelar());
                return;
            }

            sesion.setProductoSeleccionado(producto);
            estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_CANTIDAD);
            enviarMensajeConBotones(chatId, "Ingresa la cantidad:", crearBotonCancelar());
            
        } catch (NumberFormatException e) {
            enviarMensajeConBotones(chatId, "Ingresa un número válido:", crearBotonCancelar());
        }
    }

    private void procesarCantidadProducto(long chatId, String cantStr) {
        try {
            int cantidad = Integer.parseInt(cantStr);
            if (cantidad <= 0) {
                enviarMensajeConBotones(chatId, "Debe ser mayor a 0. Inténtalo de nuevo:", crearBotonCancelar());
                return;
            }

            SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
            Producto pOriginal = sesion.getProductoSeleccionado();
            
            if (cantidad > pOriginal.getCantidad()) {
                enviarMensajeConBotones(chatId, "Stock insuficiente (Disp: " + pOriginal.getCantidad() + "). Reintenta:", crearBotonCancelar());
                return;
            }

            sesion.getPedidoTemporal().agregarProducto(pOriginal.getCodigoProducto(), cantidad);
            
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            rows.add(crearFilaBoton("Agregar otro producto", "AGREGAR_OTRO_PRODUCTO"));
            rows.add(crearFilaBoton("Confirmar pedido", "CONFIRMAR_PEDIDO"));
            rows.add(crearFilaBoton("Cancelar pedido", "CANCELAR_PROCESO"));
            markup.setKeyboard(rows);

            enviarMensajeConBotones(chatId, "Producto agregado. ¿Qué deseas hacer?", markup);
            estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_CONFIRMACION);

        } catch (NumberFormatException e) {
            enviarMensajeConBotones(chatId, "Ingresa un número entero válido:", crearBotonCancelar());
        }
    }

    private void volverASeleccionProducto(long chatId) {
        SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
        estadosPorChat.put(chatId, EstadoConversacionBot.CREAR_PEDIDO_SELECCION_PRODUCTO);
        enviarMensajeConBotones(chatId, construirTextoProductos(sesion.getProductosDisponibles()) + "\nEscribe el número del producto:", crearBotonCancelar());
    }

    private void confirmarPedido(long chatId) {
        SesionPedidoTemporal sesion = pedidosTemporales.get(chatId);
        Pedido pedido = sesion.getPedidoTemporal();

        if (pedido == null || !pedido.tieneProductos()) {
            enviarMensaje(chatId, "No hay productos en el pedido.");
            volverAlMenu(chatId, true);
            return;
        }

        int prioridad = sesion.isEntregaPrioritaria() ? 1 : 0;
        servicioAsignacion.getPedidosPendientes().encolar(pedido, prioridad);
        servicioAsignacion.registrarPedidoEnMapa(pedido);

        // Enviar resumen del pedido
        String resumen = construirMensajePedidoCreado(pedido, sesion.isEntregaPrioritaria());
        
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(crearFilaBoton("Sí, generar comprobante", "GENERAR_COMPROBANTE_" + pedido.getCodigo()));
        rows.add(crearFilaBoton("No, gracias", "NO_COMPROBANTE_" + pedido.getCodigo()));
        markup.setKeyboard(rows);

        enviarMensajeConBotones(chatId, resumen + "\n\n¿Deseas recibir el comprobante del pedido?", markup);
        pedidosTemporales.remove(chatId);
    }

    private String construirMensajePedidoCreado(Pedido pedido, boolean entregaPrioritaria) {
        String tipoEntrega = entregaPrioritaria ? "prioritaria" : "normal";
        String prioridadTexto = entregaPrioritaria ? "alta" : "normal";
        String tiempoEstimado = entregaPrioritaria ? "30 segundos" : "1 minuto";

        return "Pedido creado correctamente.\n\n" +
               "Tu pedido fue agregado a la cola de pedidos pendientes.\n" +
               "Tipo de entrega: " + tipoEntrega + ".\n" +
               "Prioridad asignada: " + prioridadTexto + ".\n" +
               "Código del pedido: " + pedido.getCodigo() + ".\n" +
               "Estado: " + pedido.getEstadoActual().toString().toLowerCase().replace("_", " ") + ".\n\n" +
               "Estamos preparando tu pedido.\n" +
               "Tiempo estimado: " + tiempoEstimado + ".";
    }

    // --- CONSULTAR Y CANCELAR ---

    private void iniciarConsultaPedido(long chatId) {
        estadosPorChat.put(chatId, EstadoConversacionBot.CONSULTA_PEDIDO_CODIGO);
        enviarMensajeConBotones(chatId, "Ingresa el código del pedido:", crearBotonCancelar());
    }

    private void procesarConsultaPedido(long chatId, String codigoStr) {
        try {
            int codigo = Integer.parseInt(codigoStr);
            Pedido pedido = servicioAsignacion.buscarPedido(codigo);

            if (pedido == null) {
                enviarMensajeConBotones(chatId, "No se encontró el pedido: " + codigo, crearBotonVolver());
                return;
            }

            String resumen = "Resumen del pedido " + codigo + ":\n" +
                             "Estado: " + pedido.getEstadoActual() + "\n" +
                             "Comercio: " + pedido.getTienda().getNombre() + "\n" +
                             "Total: " + formatearMoneda(pedido.getValorTotal());
            
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            if (pedido.getEstadoActual() != EstadoPedido.ENTREGADO && pedido.getEstadoActual() != EstadoPedido.CANCELADO) {
                rows.add(crearFilaBoton("Cancelar pedido", "CANCELAR_PEDIDO_" + codigo));
            }
            rows.add(crearFilaBoton("Generar comprobante", "GENERAR_COMPROBANTE_" + codigo));
            rows.add(crearFilaBoton("Volver al menú", "VOLVER_MENU"));
            markup.setKeyboard(rows);

            enviarMensajeConBotones(chatId, resumen, markup);

        } catch (NumberFormatException e) {
            enviarMensajeConBotones(chatId, "El código debe ser numérico.", crearBotonCancelar());
        }
    }

    private void procesarCancelacionReal(long chatId, String codigoStr) {
        int codigo = Integer.parseInt(codigoStr);
        Pedido pedido = servicioAsignacion.buscarPedido(codigo);
        if (pedido != null) {
            servicioAsignacion.cancelarPedido(pedido);
            enviarMensaje(chatId, "Pedido " + codigo + " cancelado.");
        }
        enviarMenuPrincipal(chatId);
    }

    private void procesarGenerarComprobante(long chatId, String codigoStr) {
        Pedido pedido = servicioAsignacion.buscarPedido(Integer.parseInt(codigoStr));
        if (pedido != null) {
            File pdf = servicioComprobante.generarComprobantePedidoPDF(pedido);
            if (pdf != null && pdf.exists()) {
                SendDocument doc = new SendDocument();
                doc.setChatId(String.valueOf(chatId));
                doc.setDocument(new InputFile(pdf));
                doc.setCaption("Comprobante del pedido " + codigoStr);
                try {
                    execute(doc);
                    enviarMensaje(chatId, "Comprobante enviado correctamente. Tu pedido sigue en proceso. Puedes consultarlo con el código " + codigoStr + ".");
                    enviarMensajeConBotones(chatId, "¿Qué deseas hacer ahora?", crearBotonVolver());
                } catch (TelegramApiException e) {
                    enviarMensajeConBotones(chatId, "Error al enviar el PDF.", crearBotonVolver());
                }
            } else {
                enviarMensajeConBotones(chatId, "No se pudo generar el comprobante.", crearBotonVolver());
            }
        }
    }

    private void procesarNoComprobante(long chatId, String codigoStr) {
        enviarMensaje(chatId, "Listo. Tu pedido sigue en proceso. Puedes consultarlo con el código " + codigoStr + ".");
        volverAlMenu(chatId, false);
    }

    // --- UTILIDADES ---

    private String formatearMoneda(double valor) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        nf.setMaximumFractionDigits(0);
        return nf.format(valor);
    }

    private InlineKeyboardMarkup crearBotonCancelar() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(crearFilaBoton("Cancelar proceso", "CANCELAR_PROCESO")));
        return markup;
    }

    private InlineKeyboardMarkup crearBotonVolver() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(crearFilaBoton("Volver al menú", "VOLVER_MENU")));
        return markup;
    }

    private Comercio obtenerComercioPorIndice(ListaSimple<Comercio> lista, int indice) {
        if (indice <= 0 || lista == null) return null;
        int i = 1;
        Nodo<Comercio> actual = lista.getPrimero();
        while (actual != null) {
            if (i == indice) return actual.getDato();
            actual = actual.getSiguiente();
            i++;
        }
        return null;
    }

    private Producto obtenerProductoPorIndice(ListaSimple<Producto> lista, int indice) {
        if (indice <= 0 || lista == null) return null;
        int i = 1;
        Nodo<Producto> actual = lista.getPrimero();
        while (actual != null) {
            if (i == indice) return actual.getDato();
            actual = actual.getSiguiente();
            i++;
        }
        return null;
    }

    private void volverAlMenu(long chatId, boolean mostrarMensaje) {
        estadosPorChat.remove(chatId);
        registrosTemporales.remove(chatId);
        pedidosTemporales.remove(chatId);
        if (mostrarMensaje) {
            enviarMensaje(chatId, "Proceso cancelado.");
        }
        enviarMenuPrincipal(chatId);
    }

    private void enviarMensaje(long chatId, String texto) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(texto);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void enviarMensajeConBotones(long chatId, String texto, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(texto);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
