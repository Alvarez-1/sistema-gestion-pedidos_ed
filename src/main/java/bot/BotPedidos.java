package bot;

import configuracion.ConfiguracionApp;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BotPedidos extends TelegramLongPollingBot {

    private final ConfiguracionApp configuracion;

    public BotPedidos() {
        this.configuracion = new ConfiguracionApp();
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
            String mensajeTexto = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (mensajeTexto.equals("/start")) {
                enviarMenuPrincipal(chatId);
            } else {
                enviarMensaje(chatId, "Por ahora usa /start para abrir el menú principal.");
            }
        } else if (update.hasCallbackQuery()) {
            manejarCallback(update);
        }
    }

    private void enviarMenuPrincipal(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Bienvenido a PediGo, orgullo venezolano.\n\nSelecciona una opción:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Botón Registrarme
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton btnRegistrarme = new InlineKeyboardButton();
        btnRegistrarme.setText("Registrarme");
        btnRegistrarme.setCallbackData("REGISTRARME");
        row1.add(btnRegistrarme);

        // Botón Ver comercios
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton btnVerComercios = new InlineKeyboardButton();
        btnVerComercios.setText("Ver comercios");
        btnVerComercios.setCallbackData("VER_COMERCIOS");
        row2.add(btnVerComercios);

        // Botón Consultar pedido
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton btnConsultarPedido = new InlineKeyboardButton();
        btnConsultarPedido.setText("Consultar pedido");
        btnConsultarPedido.setCallbackData("CONSULTAR_PEDIDO");
        row3.add(btnConsultarPedido);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error al enviar menú principal: " + e.getMessage());
        }
    }

    private void manejarCallback(Update update) {
        String callData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackQueryId = update.getCallbackQuery().getId();

        String respuesta;
        switch (callData) {
            case "REGISTRARME":
                respuesta = "Función en preparación: registro de cliente.";
                break;
            case "VER_COMERCIOS":
                respuesta = "Función en preparación: visualización de comercios.";
                break;
            case "CONSULTAR_PEDIDO":
                respuesta = "Función en preparación: consulta de pedido.";
                break;
            default:
                respuesta = "Opción no reconocida.";
                break;
        }

        enviarMensaje(chatId, respuesta);

        // Responder al callback para que Telegram deje de mostrar el relojito
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            System.err.println("Error al responder callback: " + e.getMessage());
        }
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
