package telegrambot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import salary.SalaryCalculator;

import java.time.LocalDate;
import java.time.YearMonth;

import static salary.WorkDayCalculator.countWorkDays;

public class SalaryTelegramBot extends TelegramLongPollingBot {

    private double annualTaxDeductions = 0.0;

    @Override
    public String getBotUsername() {
        return "SalaryCalcProgresTaxBot";
    }

    @Override
    public String getBotToken() {
        return "8221171893:AAFl1uG8PDfWlIue-7Mq3XrqcMDmkbLq-ZY";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            try {
                handleMessage(chatId, messageText);
            } catch (Exception e) {
                sendMessage(chatId, "Произошла ошибка: " + e.getMessage());
            }
        }
    }

    private void handleMessage(long chatId, String message) {
        if (message.equals("/start")) {
            sendMessage(chatId,
                    "Калькулятор зарплаты с прогрессивным НДФЛ (13–22%)\n" +
                    "Отправьте: оклад год месяц (например: 50000 2025 3)\n" +
                    "Или /help для справки");
        } else if (message.equals("/help")) {
            sendMessage(chatId,
                    "Формат ввода: оклад год месяц\n" +
                    "Пример: 50000 2025 3\n" +
                    "Для установки вычетов: /deductions 10000");
        } else if (message.startsWith("/deductions")) {
            try {
                String[] parts = message.split(" ");
                annualTaxDeductions = Double.parseDouble(parts[1]);
                if (annualTaxDeductions < 0) {
                    sendMessage(chatId, "Вычеты не могут быть отрицательными. Установлено 0 руб.");
                    annualTaxDeductions = 0.0;
                } else {
                    sendMessage(chatId, "Налоговые вычеты установлены: " + annualTaxDeductions + " руб.");
                }
            } catch (Exception e) {
                sendMessage(chatId, "Ошибка ввода вычетов. Используйте: /deductions 10000");
            }
        } else {
            processSalaryRequest(chatId, message);
        }
    }

    private void processSalaryRequest(long chatId, String input) {
        try {
            String[] parts = input.split(" ");
            if (parts.length != 3) {
                sendMessage(chatId, "Неверный формат. Пример: 50000 2025 3");
                return;
            }

            double salary = Double.parseDouble(parts[0]);
            int year = Integer.parseInt(parts[1]);
            int month = Integer.parseInt(parts[2]);

            if (salary <= 0) {
                sendMessage(chatId, "Оклад должен быть больше нуля.");
                return;
            }
            if (month < 1 || month > 12) {
                sendMessage(chatId, "Месяц должен быть от 1 до 12.");
                return;
            }
            if (year < 2025 || year > 2030) {
                sendMessage(chatId, "Данные доступны за 2025–2030 гг.");
                return;
            }

            // Определяем количество рабочих дней
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate mid = LocalDate.of(year, month, 15);
            LocalDate end = YearMonth.of(year, month).atEndOfMonth();

            int workDaysFirstHalf = countWorkDays(start, mid);
            int workDaysSecondHalf = countWorkDays(mid.plusDays(1), end);
            int totalWorkDays = workDaysFirstHalf + workDaysSecondHalf  ;

            if (totalWorkDays == 0) {
                sendMessage(chatId, "В этом месяце нет рабочих дней.");
                return;
            }

            // Расчет пропорционального оклада
            double dailyRate = salary / totalWorkDays;
            double advanceSalary = dailyRate * workDaysFirstHalf;
            double remainderSalary = dailyRate * workDaysSecondHalf;

            double cumulativeIncomeBefore = salary * (month - 1);
            double totalCumulativeIncome = cumulativeIncomeBefore + salary;

            double prevMonthTax = calculateProgressiveTax(cumulativeIncomeBefore);
            double currentMonthTax = calculateProgressiveTax(totalCumulativeIncome) - prevMonthTax;


            SalaryCalculator calculator = new SalaryCalculator(salary, currentMonthTax);
            SalaryCalculator.SalaryReport report = calculator.calculate(year, month, cumulativeIncomeBefore);

            // Формирование ответа
            StringBuilder response = new StringBuilder();
            response.append("📊 Расчёт для ").append(month).append(".").append(year).append("\n");
            response.append("Нарастающий доход: ").append(String.format("%,.2f", cumulativeIncomeBefore)).append(" руб.\n");
            response.append("Совокупный доход: ").append(String.format("%,.2f", totalCumulativeIncome)).append(" руб.\n");
            response.append("Вычеты: ").append(String.format("%,.2f", annualTaxDeductions)).append(" руб.\n\n");

            // Эффективная ставка НДФЛ
            double effectiveTaxRate = report.totalTax() > 0 ? report.totalTax() / (advanceSalary + remainderSalary) : 0;
            response.append("📈 Эффективная ставка НДФЛ: ").append(String.format("%.2f%%", effectiveTaxRate * 100)).append("\n\n");

            response.append("💵 Аванс (20 числа) — за 01–15 число:\n");
            response.append("  Дней:         ").append(workDaysFirstHalf).append(" раб. дн.\n");
            response.append("  Начислено: ").append(String.format("%,.2f", report.grossAdvance())).append(" руб.\n");
            response.append("  НДФЛ: ").append(String.format("%,.2f", report.taxAdvance())).append(" руб.");
            if (report.grossAdvance() > 0) {
                double advanceRate = report.taxAdvance() / report.grossAdvance();
                response.append(" (").append(String.format("%.1f%%", advanceRate * 100)).append(")\n");
            } else {
                response.append(" (0.0%)\n");
            }
            response.append("  К выплате: ").append(String.format("%,.2f", report.netAdvance())).append(" руб.\n\n");

            response.append("💵 Оклад (5 числа) — за 16–").append(end.getDayOfMonth()).append(" число:\n");
            response.append("  Дней:         ").append(workDaysSecondHalf).append(" раб. дн.\n");
            response.append("  Начислено: ").append(String.format("%,.2f", report.grossRemainder())).append(" руб.\n");
            response.append("  НДФЛ: ").append(String.format("%,.2f", report.taxRemainder())).append(" руб.");
            if (report.grossRemainder() > 0) {
                double remainderRate = report.taxRemainder() / report.grossRemainder();
                response.append(" (").append(String.format("%.1f%%", remainderRate * 100)).append(")\n");
            } else {
                response.append(" (0.0%)\n");
            }
            response.append("  К выплате: ").append(String.format("%,.2f", report.netRemainder())).append(" руб.\n\n");

            response.append("💰 Итого за месяц:\n");
            response.append("  Начислено: ").append(String.format("%,.2f", report.totalGross())).append(" руб.\n");
            response.append("  НДФЛ: ").append(String.format("%,.2f", report.totalTax())).append(" руб.");
            if (report.totalGross() > 0) {
                response.append(" (").append(String.format("%.1f%%", effectiveTaxRate * 100)).append(")\n");
            } else {
                response.append(" (0.0%)\n");
            }
            response.append("  К выплате: ").append(String.format("%,.2f", report.totalNet())).append(" руб.\n");

            sendMessage(chatId, response.toString());

        } catch (NumberFormatException e) {
            sendMessage(chatId, "Ошибка: введите числа в формате оклад год месяц.");
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка расчёта: " + e.getMessage());
        }
    }



    private double calculateProgressiveTax(double cumulativeIncome) {
        double taxBase = Math.max(0, cumulativeIncome - annualTaxDeductions);
        double tax = 0.0;

        final double THRESHOLD_1 = 2_400_000;
        final double THRESHOLD_2 = 5_000_000;
        final double THRESHOLD_3 = 20_000_000;
        final double THRESHOLD_4 = 50_000_000;

        final double RATE_1 = 0.13;
        final double RATE_2 = 0.15;
        final double RATE_3 = 0.18;
        final double RATE_4 = 0.20;
        final double RATE_5 = 0.22;

        if (taxBase <= THRESHOLD_1) {
            tax = taxBase * RATE_1;
        } else if (taxBase <= THRESHOLD_2) {
            tax = THRESHOLD_1 * RATE_1 + (taxBase - THRESHOLD_1) * RATE_2;
        } else if (taxBase <= THRESHOLD_3) {
            tax = THRESHOLD_1 * RATE_1
                  + (THRESHOLD_2 - THRESHOLD_1) * RATE_2
                  + (taxBase - THRESHOLD_2) * RATE_3;
        } else if (taxBase <= THRESHOLD_4) {
            tax = THRESHOLD_1 * RATE_1
                  + (THRESHOLD_2 - THRESHOLD_1) * RATE_2
                  + (THRESHOLD_3 - THRESHOLD_2) * RATE_3
                  + (taxBase - THRESHOLD_3) * RATE_4;
        } else {
            tax = THRESHOLD_1 * RATE_1
                  + (THRESHOLD_2 - THRESHOLD_1) * RATE_2
                  + (THRESHOLD_3 - THRESHOLD_2) * RATE_3
                  + (THRESHOLD_4 - THRESHOLD_3) * RATE_4
                  + (taxBase - THRESHOLD_4) * RATE_5;
        }
        return tax;
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
