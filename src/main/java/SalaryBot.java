//import salary.SalaryCalculator;
//
//import java.util.Scanner;
//
//public class SalaryBot {
//    private final Scanner scanner = new Scanner(System.in);
//    private double annualTaxDeductions = 0.0;
//
//    // Границы ступеней налогообложения (в рублях)
//    private static final double THRESHOLD_1 = 2_400_000;  // до 2,4 млн
//    private static final double THRESHOLD_2 = 5_000_000;  // до 5 млн
//    private static final double THRESHOLD_3 = 20_000_000; // до 20 млн
//    private static final double THRESHOLD_4 = 50_000_000; // до 50 млн
//
//    // Ставки НДФЛ по ступеням
//    private static final double RATE_1 = 0.13;  // 13%
//    private static final double RATE_2 = 0.15;  // 15%
//    private static final double RATE_3 = 0.18;  // 18%
//    private static final double RATE_4 = 0.20;  // 20%
//    private static final double RATE_5 = 0.22;  // 22%
//
//
//    /**
//     * Расчёт НДФЛ по прогрессивной шкале с учётом нарастающего дохода и вычетов
//     */
//    private double calculateProgressiveTax(double cumulativeIncome) {
//        double taxBase = Math.max(0, cumulativeIncome - annualTaxDeductions);
//        double tax = 0.0;
//
//        if (taxBase <= THRESHOLD_1) {
//            tax = taxBase * RATE_1;
//        } else if (taxBase <= THRESHOLD_2) {
//            tax = THRESHOLD_1 * RATE_1 + (taxBase - THRESHOLD_1) * RATE_2;
//        } else if (taxBase <= THRESHOLD_3) {
//            tax = THRESHOLD_1 * RATE_1
//                  + (THRESHOLD_2 - THRESHOLD_1) * RATE_2
//                  + (taxBase - THRESHOLD_2) * RATE_3;
//        } else if (taxBase <= THRESHOLD_4) {
//            tax = THRESHOLD_1 * RATE_1
//                  + (THRESHOLD_2 - THRESHOLD_1) * RATE_2
//                  + (THRESHOLD_3 - THRESHOLD_2) * RATE_3
//                  + (taxBase - THRESHOLD_3) * RATE_4;
//        } else {
//            tax = THRESHOLD_1 * RATE_1
//                  + (THRESHOLD_2 - THRESHOLD_1) * RATE_2
//                  + (THRESHOLD_3 - THRESHOLD_2) * RATE_3
//                  + (THRESHOLD_4 - THRESHOLD_3) * RATE_4
//                  + (taxBase - THRESHOLD_4) * RATE_5;
//        }
//
//        return tax;
//    }
//
//    public void run() {
//        System.out.println("Калькулятор зарплаты с прогрессивным НДФЛ (13%/15%/18%/20%/22%) и нарастающим итогом");
//        System.out.println("Аванс: 20 числа (за отработанные дни с 1 по 15 число)");
//        System.out.println("Оклад: 5 числа (остаток за месяц)");
//        System.out.println("-".repeat(60));
//
//
//        try {
//            System.out.print("Суммарные налоговые вычеты за год (руб.): ");
//            annualTaxDeductions = scanner.nextDouble();
//            if (annualTaxDeductions < 0) {
//                System.out.println("Вычеты не могут быть отрицательными. Установлено 0 руб.");
//                annualTaxDeductions = 0.0;
//            }
//            System.out.println();
//        } catch (Exception e) {
//            System.out.println("Ошибка ввода вычетов. Установлено 0 руб.");
//            annualTaxDeductions = 0.0;
//            scanner.nextLine(); // очистка буфера
//        }
//
//        while (true) {
//            try {
//                System.out.print("Оклад (руб.) [введите 'exit' для выхода]: ");
//                String input = scanner.next().trim();
//
//
//                if (input.equalsIgnoreCase("exit")) {
//                    System.out.println("Работа калькулятора завершена.");
//                    break;
//                }
//
//                double salary = Double.parseDouble(input);
//                if (salary <= 0) {
//                    System.out.println("Оклад должен быть больше нуля.");
//                    continue;
//                }
//
//                System.out.print("Год: ");
//                int year = scanner.nextInt();
//
//                System.out.print("Месяц (1–12): ");
//                int month = scanner.nextInt();
//                if (month < 1 || month > 12) {
//                    System.out.println("Ошибка: месяц должен быть от 1 до 12.");
//                    continue;
//                }
//
//                // Нарастающий доход до текущего месяца
//                double cumulativeIncomeBefore = salary * (month - 1);
//                // Совокупный доход с текущим месяцем
//                double totalCumulativeIncome = cumulativeIncomeBefore + salary;
//
//                // Расчёт НДФЛ: за предыдущие месяцы и за текущий месяц
//                double prevMonthTax = calculateProgressiveTax(cumulativeIncomeBefore);
//                double currentMonthTax = calculateProgressiveTax(totalCumulativeIncome) - prevMonthTax;
//
//                // Создаём калькулятор (передаём НДФЛ за текущий месяц)
//
//                SalaryCalculator calculator = new SalaryCalculator(salary, currentMonthTax);
//
//                // Выполняем расчёт
//                SalaryCalculator.SalaryReport report = calculator.calculate(
//                        year, month, cumulativeIncomeBefore
//                );
//
//                // Вывод результата
//                System.out.println("\n!" + "=".repeat(65));
//                System.out.printf("Расчёт для %02d.%d%n", month, year);
//                System.out.printf("Нарастающий доход (до текущего месяца): %,12.2f руб.%n", cumulativeIncomeBefore);
//                System.out.printf("Совокупный доход за год (с текущим месяцем): %,12.2f руб.%n",
//                        totalCumulativeIncome);
//                System.out.printf("Налоговые вычеты за год: %,12.2f руб.%n", annualTaxDeductions);
//                System.out.println("=".repeat(65));
//
//                System.out.printf("Аванс (20 числа, за %d рабочих дней с 01 по 15 %02d.%d):%n",
//                        report.workDaysAdvancePeriod(), month, year);
//                System.out.printf("  Начислено:   %,12.2f руб.%n", report.grossAdvance());
//                System.out.printf("  НДФЛ:        %,12.2f руб. (%4.1f%%)%n",
//                        report.taxAdvance(), report.effectiveTaxRate() * 100);
//                System.out.printf("  К выплате:   %,12.2f руб.%n", report.netAdvance());
//
//
//                System.out.printf("%nОклад (5 числа, остаток за месяц):%n");
//                System.out.printf("  Начислено:   %,12.2f руб.%n", report.grossRemainder());
//                System.out.printf("  НДФЛ:        %,12.2f руб. (%4.1f%%)%n",
//                        report.taxRemainder(), report.effectiveTaxRate() * 100);
//                System.out.printf("  К выплате:   %,12.2f руб.%n", report.netRemainder());
//
//                System.out.println("-".repeat(65));
//                System.out.printf("Итого за месяц:%n");
//                System.out.printf("  Начислено:   %,12.2f руб.%n", report.totalGross());
//                System.out.printf("  НДФЛ:        %,12.2f руб.%n", report.totalTax());
//                System.out.printf("  К выплате:   %,12.2f руб.%n", report.totalNet());
//                System.out.printf("Нарастающий доход на конец месяца: %,12.2f руб.%n",
//                        totalCumulativeIncome);
//
//
//                // Дополнительно: вывод по ступеням налогообложения
//                System.out.println("\n!" + "-".repeat(65));
//                System.out.println("Детализация по ступеням налогообложения:");
//                double taxBase = Math.max(0, totalCumulativeIncome - annualTaxDeductions);
//                System.out.printf("Налогооблагаемая база (с учётом вычетов): %,12.2f руб.%n", taxBase);
//
//
//                if (taxBase <= THRESHOLD_1) {
//                    System.out.printf("→ До 2,4 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            taxBase, RATE_1 * 100, taxBase * RATE_1);
//                } else if (taxBase <= THRESHOLD_2) {
//                    double part1 = THRESHOLD_1;
//                    double part2 = taxBase - THRESHOLD_1;
//                    System.out.printf("→ До 2,4 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part1, RATE_1 * 100, part1 * RATE_1);
//                    System.out.printf("→ Свыше 2,4 до 5 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part2, RATE_2 * 100, part2 * RATE_2);
//                } else if (taxBase <= THRESHOLD_3) {
//                    double part1 = THRESHOLD_1;
//                    double part2 = THRESHOLD_2 - THRESHOLD_1;
//                    double part3 = taxBase - THRESHOLD_2;
//                    System.out.printf("→ До 2,4 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part1, RATE_1 * 100, part1 * RATE_1);
//                    System.out.printf("→ Свыше 2,4 до 5 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part2, RATE_2 * 100, part2 * RATE_2);
//                    System.out.printf("→ Свыше 5 до 20 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part3, RATE_3 * 100, part3 * RATE_3);
//                } else if (taxBase <= THRESHOLD_4) {
//                    double part1 = THRESHOLD_1;
//                    double part2 = THRESHOLD_2 - THRESHOLD_1;
//                    double part3 = THRESHOLD_3 - THRESHOLD_2;
//                    double part4 = taxBase - THRESHOLD_3;
//                    System.out.printf("→ До 2,4 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part1, RATE_1 * 100, part1 * RATE_1);
//                    System.out.printf("→ Свыше 2,4 до 5 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part2, RATE_2 * 100, part2 * RATE_2);
//                    System.out.printf("→ Свыше 5 до 20 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part3, RATE_3 * 100, part3 * RATE_3);
//                    System.out.printf("→ Свыше 20 до 50 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part4, RATE_4 * 100, part4 * RATE_4);
//                } else {
//                    double part1 = THRESHOLD_1;
//                    double part2 = THRESHOLD_2 - THRESHOLD_1;
//                    double part3 = THRESHOLD_3 - THRESHOLD_2;
//                    double part4 = THRESHOLD_4 - THRESHOLD_3;
//                    double part5 = taxBase - THRESHOLD_4;
//                    System.out.printf("→ До 2,4 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part1, RATE_1 * 100, part1 * RATE_1);
//                    System.out.printf("→ Свыше 2,4 до 5 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part2, RATE_2 * 100, part2 * RATE_2);
//                    System.out.printf("→ Свыше 5 до 20 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part3, RATE_3 * 100, part3 * RATE_3);
//                    System.out.printf("→ Свыше 20 до 50 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part4, RATE_4 * 100, part4 * RATE_4);
//                    System.out.printf("→ Свыше 50 млн руб.: %,12.2f × %4.1f%% = %,12.2f руб.%n",
//                            part5, RATE_5 * 100, part5 * RATE_5);
//                }
//
//            } catch (NumberFormatException e) {
//                System.out.println("Ошибка: введите числовое значение.");
//            } catch (Exception e) {
//                System.out.println("Произошла ошибка: " + e.getMessage());
//            }
//            System.out.println(); // Пустая строка между расчётами
//        }
//
//        scanner.close();
//    }
//
//    public static void main(String[] args) {
//        new SalaryBot().run();
//    }
//}
