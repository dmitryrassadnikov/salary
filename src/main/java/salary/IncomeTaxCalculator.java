package salary;
public class IncomeTaxCalculator {
    public static double calculateAnnualTax(double annualIncome, double taxDeductions) {
        double taxBase = Math.max(0, annualIncome - taxDeductions);

        double annualTax = 0.0;

        if (taxBase <= 2_400_000) {
            annualTax = taxBase * 0.13;
        } else if (taxBase <= 5_000_000) {
            annualTax = 2_400_000 * 0.13 + (taxBase - 2_400_000) * 0.15;
        } else if (taxBase <= 20_000_000) {
            annualTax = 2_400_000 * 0.13 + 2_600_000 * 0.15 + (taxBase - 5_000_000) * 0.18;
        } else if (taxBase <= 50_000_000) {
            annualTax = 2_400_000 * 0.13 + 2_600_000 * 0.15 + 15_000_000 * 0.18 + (taxBase - 20_000_000) * 0.20;
        } else {
            annualTax = 2_400_000 * 0.13 + 2_600_000 * 0.15 + 15_000_000 * 0.18 + 30_000_000 * 0.20 + (taxBase - 50_000_000) * 0.22;
        }

        return annualTax;
    }
}
