package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.FinancialRatioDTO;
import com.cbo.credit_scoring.models.BalanceSheet;
import com.cbo.credit_scoring.models.IncomeStatement;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FinancialRatioCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal THREE_SIXTY_FIVE = new BigDecimal("365");
    private static final int DEFAULT_SCALE = 4;
    private static final int PERCENTAGE_SCALE = 2;

    // ============= Efficiency Ratios =============

    /**
     * Activity Ratio (Sales Growth) - Year-over-year sales growth rate
     */
    public BigDecimal calculateActivityRatio(IncomeStatement current, IncomeStatement previous) {
        if (previous == null || previous.getSales() == null || previous.getSales().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.getSales()
                .subtract(previous.getSales())
                .divide(previous.getSales(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    /**
     * Inventory Turnover - How many times inventory is sold and replaced
     * Formula: Inventory / Cost of Goods Sold
     */
    public BigDecimal calculateInventoryTurnover(BalanceSheet balance, IncomeStatement income) {
        if (income.getCostOfGoodsSold() == null || income.getCostOfGoodsSold().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return balance.getStocks()
                .divide(income.getCostOfGoodsSold(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Collection Period (days) - Average days to collect receivables
     * Formula: (Trade Receivables / Sales) * 365
     */
    public BigDecimal calculateCollectionPeriod(BalanceSheet balance, IncomeStatement income) {
        if (income.getSales() == null || income.getSales().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return balance.getTradeOtherReceivables()
                .divide(income.getSales(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(THREE_SIXTY_FIVE);
    }

    /**
     * Working Capital to Sales - Working capital efficiency
     * Formula: Net Working Capital / Sales
     */
    public BigDecimal calculateWorkingCapitalToSales(BalanceSheet balance, IncomeStatement income) {
        if (income.getSales() == null || income.getSales().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return balance.getNetWorkingCapital()
                .divide(income.getSales(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    // ============= Financial Strength Ratios =============

    /**
     * Current Ratio - Ability to pay short-term obligations
     * Formula: Current Assets / Current Liabilities
     */
    public BigDecimal calculateCurrentRatio(BalanceSheet balance) {
        if (balance.getTotalCurrentLiabilities().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return balance.getTotalCurrentAssets()
                .divide(balance.getTotalCurrentLiabilities(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Acid Test Ratio (Quick Ratio) - Ability to pay immediate obligations
     * Formula: (Current Assets - Inventory) / Current Liabilities
     */
    public BigDecimal calculateAcidTestRatio(BalanceSheet balance) {
        if (balance.getTotalCurrentLiabilities().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal quickAssets = balance.getTotalCurrentAssets()
                .subtract(balance.getStocks() != null ? balance.getStocks() : BigDecimal.ZERO);
        return quickAssets.divide(balance.getTotalCurrentLiabilities(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Equity to Total Asset (%) - Financial leverage
     * Formula: (Total Equity / Total Assets) * 100
     */
    public BigDecimal calculateEquityToTotalAsset(BalanceSheet balance) {
        if (balance.getTotalAssets().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return balance.getTotalCapital()
                .divide(balance.getTotalAssets(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    /**
     * Debt to Total Asset (%) - Percentage of assets financed by debt
     * Formula: (Total Liabilities / Total Assets) * 100
     */
    public BigDecimal calculateDebtToTotalAsset(BalanceSheet balance) {
        if (balance.getTotalAssets().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return balance.getTotalLiabilities()
                .divide(balance.getTotalAssets(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    /**
     * Debt to Net Worth - Leverage ratio
     * Formula: Total Liabilities / Tangible Net Worth
     */
    public BigDecimal calculateDebtToNetWorth(BalanceSheet balance) {
        if (balance.getTangibleNetWorth().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return balance.getTotalLiabilities()
                .divide(balance.getTangibleNetWorth(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    // ============= Profitability Ratios =============

    /**
     * Return on Assets (ROA) (%) - How profitable a company is relative to its total assets
     * Formula: (Net Income / Total Assets) * 100
     */
    public BigDecimal calculateROA(BalanceSheet balance, IncomeStatement income) {
        if (balance.getTotalAssets().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getNetIncome()
                .divide(balance.getTotalAssets(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    /**
     * Return on Equity (ROE) (%) - How much profit a company generates with shareholder funds
     * Formula: (Net Income / Total Equity) * 100
     */
    public BigDecimal calculateROE(BalanceSheet balance, IncomeStatement income) {
        if (balance.getTotalCapital().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getNetIncome()
                .divide(balance.getTotalCapital(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    /**
     * Debt Service Coverage Ratio - Ability to pay interest
     * Formula: Profit Before Interest and Tax / Interest Expense
     */
    public BigDecimal calculateDebtServiceCoverageRatio(IncomeStatement income) {
        if (income.getInterestExpense() == null || income.getInterestExpense().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getProfitBeforeInterestAndTax()
                .divide(income.getInterestExpense(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Operating Profit Margin (%) - Operating profitability
     * Formula: (Operating Profit / Sales) * 100
     */
    public BigDecimal calculateOperatingProfitMargin(IncomeStatement income) {
        if (income.getSales() == null || income.getSales().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getProfitBeforeInterestTaxDepreciation()
                .divide(income.getSales(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    /**
     * Gross Profit Margin (%) - Gross profitability
     * Formula: (Gross Profit / Sales) * 100
     */
    public BigDecimal calculateGrossProfitMargin(IncomeStatement income) {
        if (income.getSales() == null || income.getSales().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getGrossProfit()
                .divide(income.getSales(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    /**
     * Net Profit Margin (%) - Net profitability
     * Formula: (Net Income / Sales) * 100
     */
    public BigDecimal calculateNetProfitMargin(IncomeStatement income) {
        if (income.getSales() == null || income.getSales().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getNetIncome()
                .divide(income.getSales(), DEFAULT_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    // ============= Additional Ratios =============

    /**
     * Asset Turnover - Efficiency of asset use
     * Formula: Sales / Total Assets
     */
    public BigDecimal calculateAssetTurnover(BalanceSheet balance, IncomeStatement income) {
        if (balance.getTotalAssets().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getSales()
                .divide(balance.getTotalAssets(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Fixed Asset Turnover - Efficiency of fixed asset use
     * Formula: Sales / Fixed Assets
     */
    public BigDecimal calculateFixedAssetTurnover(BalanceSheet balance, IncomeStatement income) {
        if (balance.getTotalNonCurrentAssets().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getSales()
                .divide(balance.getTotalNonCurrentAssets(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Interest Coverage Ratio - Ability to pay interest
     * Formula: Operating Profit / Interest Expense
     */
    public BigDecimal calculateInterestCoverageRatio(IncomeStatement income) {
        if (income.getInterestExpense() == null || income.getInterestExpense().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.getProfitBeforeInterestAndTax()
                .divide(income.getInterestExpense(), DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculate all ratios for a given period
     */
    public FinancialRatioDTO calculateAllRatios(BalanceSheet balance, IncomeStatement income, IncomeStatement previousIncome) {
        return FinancialRatioDTO.builder()
                .activityRatio(calculateActivityRatio(income, previousIncome))
                .inventoryTurnover(calculateInventoryTurnover(balance, income))
                .collectionPeriod(calculateCollectionPeriod(balance, income))
                .workingCapitalToSales(calculateWorkingCapitalToSales(balance, income))
                .currentRatio(calculateCurrentRatio(balance))
                .acidTestRatio(calculateAcidTestRatio(balance))
                .equityToTotalAsset(calculateEquityToTotalAsset(balance))
                .debtToTotalAsset(calculateDebtToTotalAsset(balance))
                .debtToNetWorth(calculateDebtToNetWorth(balance))
                .roa(calculateROA(balance, income))
                .roe(calculateROE(balance, income))
                .debtServiceCoverageRatio(calculateDebtServiceCoverageRatio(income))
                .operatingProfitMargin(calculateOperatingProfitMargin(income))
                .grossProfitMargin(calculateGrossProfitMargin(income))
                .netProfitMargin(calculateNetProfitMargin(income))
                .assetTurnover(calculateAssetTurnover(balance, income))
                .fixedAssetTurnover(calculateFixedAssetTurnover(balance, income))
                .interestCoverageRatio(calculateInterestCoverageRatio(income))
                .build();
    }

    // ============= Helper Methods =============

    /**
     * Format a ratio to a specific scale
     */
    public BigDecimal formatRatio(BigDecimal ratio, int scale) {
        if (ratio == null) {
            return BigDecimal.ZERO;
        }
        return ratio.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Format a percentage ratio
     */
    public BigDecimal formatPercentage(BigDecimal ratio) {
        return formatRatio(ratio, PERCENTAGE_SCALE);
    }

    /**
     * Check if a ratio is healthy based on typical benchmarks
     */
    public boolean isHealthyRatio(String ratioName, BigDecimal value) {
        if (value == null) return false;

        switch (ratioName) {
            case "currentRatio":
                return value.compareTo(new BigDecimal("1.5")) >= 0;
            case "acidTestRatio":
                return value.compareTo(new BigDecimal("1")) >= 0;
            case "debtToEquity":
                return value.compareTo(new BigDecimal("2")) <= 0;
            case "roa":
                return value.compareTo(new BigDecimal("5")) >= 0;
            case "roe":
                return value.compareTo(new BigDecimal("10")) >= 0;
            case "profitMargin":
                return value.compareTo(BigDecimal.ZERO) > 0;
            default:
                return true;
        }
    }

    /**
     * Get trend direction (improving, declining, stable)
     */
    public String getTrendDirection(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null) return "UNKNOWN";

        int comparison = current.compareTo(previous);
        if (comparison > 0) return "IMPROVING";
        if (comparison < 0) return "DECLINING";
        return "STABLE";
    }
}