package salary;
import java.time.DayOfWeek;
import java.time.LocalDate;


public class WorkDayCalculator {
    // Метод для подсчёта рабочих дней с учётом праздников
    public static int countWorkDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            // Считаем рабочим днём, если это не выходной и не праздник
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY && !isHoliday(current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    // Проверка, является ли дата праздником
    private static boolean isHoliday(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // Основные государственные праздники РФ
        return (month == 1 && day >= 1 && day <= 8)                      // Новогодние каникулы
               || (month == 2 && day == 23)                                 // День защитника Отечества
               || (month == 3 && day == 8)                                  // Международный женский день
               || (month == 5 && (day == 1 || day == 9))                    // Праздник Весны, День Победы
               || (month == 6 && day == 12)                                 // День России
               || (month == 11 && day == 4);                                // День народного единства
    }
}
