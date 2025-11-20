package salary;

import java.time.LocalDate;
import java.time.YearMonth;

import static salary.WorkDayCalculator.countWorkDays;


public class SalaryCalculator {
    private final double monthlySalary;
    private final double monthlyTax;

    // ✅ Конструктор, который принимает два параметра double
    public SalaryCalculator(double monthlySalary, double monthlyTax) {
        this.monthlySalary = monthlySalary;
        this.monthlyTax = monthlyTax;
    }

    public SalaryReport calculate(int year, int month, double cumulativeIncomeBefore) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate mid = LocalDate.of(year, month, 15);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        int workDaysFirstHalf = countWorkDays(start, mid);
        int workDaysSecondHalf = countWorkDays(mid.plusDays(1), end);
        int totalWorkDays = workDaysFirstHalf + workDaysSecondHalf;

        double dailyRate = totalWorkDays > 0 ? monthlySalary / totalWorkDays : 0;
        double grossAdvance = dailyRate * workDaysFirstHalf;
        double grossRemainder = dailyRate * workDaysSecondHalf;

        double totalGross = grossAdvance + grossRemainder;
        double taxAdvance = totalGross > 0 ? monthlyTax * (grossAdvance / totalGross) : 0;
        double taxRemainder = monthlyTax - taxAdvance;

        return new SalaryReport(
                grossAdvance, taxAdvance,
                grossRemainder, taxRemainder,
                workDaysFirstHalf, workDaysSecondHalf
        );
    }


    public static class SalaryReport {
        private final double grossAdvance;
        private final double taxAdvance;
        private final double netAdvance;
        private final double grossRemainder;
        private final double taxRemainder;
        private final double netRemainder;
        private final int workDaysAdvancePeriod;
        private final int workDaysRemainderPeriod;

        public SalaryReport(double grossAdvance, double taxAdvance,
                            double grossRemainder, double taxRemainder,
                            int workDaysAdvancePeriod, int workDaysRemainderPeriod) {
            this.grossAdvance = grossAdvance;
            this.taxAdvance = taxAdvance;
            this.netAdvance = grossAdvance - taxAdvance;
            this.grossRemainder = grossRemainder;
            this.taxRemainder = taxRemainder;
            this.netRemainder = grossRemainder - taxRemainder;
            this.workDaysAdvancePeriod = workDaysAdvancePeriod;
            this.workDaysRemainderPeriod = workDaysRemainderPeriod;
        }

        // Геттеры
        public double grossAdvance() { return grossAdvance; }
        public double taxAdvance() { return taxAdvance; }
        public double netAdvance() { return netAdvance; }
        public double grossRemainder() { return grossRemainder; }
        public double taxRemainder() { return taxRemainder; }
        public double netRemainder() { return netRemainder; }
        public double totalGross() { return grossAdvance + grossRemainder; }
        public double totalTax() { return taxAdvance + taxRemainder; }
        public double totalNet() { return netAdvance + netRemainder; }
        public int workDaysAdvancePeriod() { return workDaysAdvancePeriod; }
        public int workDaysRemainderPeriod() { return workDaysRemainderPeriod; }
        public double effectiveTaxRate() { return totalGross() > 0 ? totalTax() / totalGross() : 0; }
    }
}
