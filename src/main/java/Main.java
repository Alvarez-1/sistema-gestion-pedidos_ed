import bot.BotPedidos;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import servicio.ServicioAsignacion;

/**
 * Clase principal para iniciar el Sistema de Gestión de Pedidos ED.
 */
public class Main {

    public static void main(String[] args) {
        try {
            // Crear instancia única del servicio para compartir entre interfaces
            ServicioAsignacion servicioAsignacion = new ServicioAsignacion();
            
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new BotPedidos(servicioAsignacion));
            
            System.out.println("Sistema PediGo iniciado correctamente.");
            System.out.println("Bot de Telegram activo.");
            
        } catch (TelegramApiException e) {
            System.err.println("Error al iniciar PediGo: " + e.getMessage());
        }
    }
}
