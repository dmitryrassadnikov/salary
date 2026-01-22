package salary;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class WorkDayCalculator {

    // Храним перенесённые праздники, чтобы не пересчитывать многократно
    private static final Set<LocalDate> shiftedHolidays = new HashSet<>();

    // Метод для подсчёта рабочих дней с учётом праздников и их переносов
    public static int countWorkDays(LocalDate start, LocalDate end) {
        // Предварительно вычисляем все перенесённые праздники в диапазоне
        calculateShiftedHolidays(start, end);

        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            // Рабочий день: не выходной, не исходный праздник, не перенесённый праздник
            if (dayOfWeek != DayOfWeek.SATURDAY
                    && dayOfWeek != DayOfWeek.SUNDAY
                    && !isHoliday(current)
                    && !shiftedHolidays.contains(current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    // Проверка, является ли дата исходным государственным праздником (без учёта переносов)
    private static boolean isHoliday(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        return (month == 1 && day >= 1 && day <= 8)           // Новогодние каникулы
                || (month == 2 && day == 23)                // День защитника Отечества
                || (month == 3 && day == 8)                 // Международный женский день
                || (month == 5 && (day == 1 || day == 9))   // Праздник Весны, День Победы
                || (month == 6 && day == 12)                // День России
                || (month == 11 && day == 4);               // День народного единства
    }

    // Вычисляем все перенесённые праздники в заданном диапазоне
    private static void calculateShiftedHolidays(LocalDate start, LocalDate end) {
        shiftedHolidays.clear(); // Очищаем кэш для нового диапазона

        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (isHoliday(current)) {
                DayOfWeek dayOfWeek = current.getDayOfWeek();
                // Если праздник выпал на субботу или воскресенье — переносим
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    LocalDate shifted = findNextWorkingDay(current);
                    // Добавляем перенос, если он попадает в диапазон расчёта
                    if (!shifted.isBefore(start) && !shifted.isAfter(end)) {
                        shiftedHolidays.add(shifted);
                    }
                }
            }
            current = current.plusDays(1);
        }
    }

    // Находим следующий рабочий день (пн–пт), не являющийся праздником
    private static LocalDate findNextWorkingDay(LocalDate holiday) {
        LocalDate next = holiday.plusDays(1);
        while (true) {
            DayOfWeek dayOfWeek = next.getDayOfWeek();
            // Пропускаем выходные
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                next = next.plusDays(1);
                continue;
            }
            // Пропускаем исходные праздники (чтобы не перенести на другой праздник)
            if (isHoliday(next)) {
                next = next.plusDays(1);
                continue;
            }
            // Нашли рабочий день, не являющийся праздником
            return next;
        }
    }
}
