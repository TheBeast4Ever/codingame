package rules;

public enum EfficiencyRate {
    USELESS(0), WEAK(10), AVERAGE(50), HIGH(90), MAXIMUM(100);
    private final Integer efficiency;

    private EfficiencyRate(Integer efficiency) {
        this.efficiency = efficiency;
    }

    public Integer getValue() {
        return efficiency;
    }
}
