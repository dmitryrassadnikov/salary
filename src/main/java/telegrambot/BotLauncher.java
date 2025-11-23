package telegrambot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class BotLauncher {
    public static void main(String[] args) {
        try {
            SalaryTelegramBot bot = new SalaryTelegramBot();
            bot.getBotUsername();
            bot.getBotToken();
            // Запуск бота через TelegramBotsApi
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (Exception e) {
            System.err.println("Ошибка запуска бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @GetMapping("/health")
public String healthCheck() {
    return "OK";
}
    
}
