package demo;

import bot.BotPedidos;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PruebaBotTelegram {

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new BotPedidos());
            System.out.println("Bot PediGo iniciado correctamente.");
        } catch (TelegramApiException e) {
            System.err.println("Error al iniciar el bot de Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
