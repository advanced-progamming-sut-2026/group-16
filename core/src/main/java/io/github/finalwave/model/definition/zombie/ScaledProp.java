package io.github.finalwave.model.definition.zombie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ScaledProp {

    @JsonProperty("Key")
    private final String key;

    @JsonProperty("Formula")
    private final String formula;

    @JsonProperty("Arg1")
    private final Double arg1;

    @JsonProperty("Arg2")
    private final Double arg2;

    public ScaledProp(@JsonProperty("Key") String key,
                      @JsonProperty("Formula") String formula,
                      @JsonProperty("Arg1") Double arg1,
                      @JsonProperty("Arg2") Double arg2) {
        this.key = key;
        this.formula = formula;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public String getKey() {
        return key;
    }

    public String getFormula() {
        return formula;
    }

    public Double getArg1() {
        return arg1;
    }

    public Double getArg2() {
        return arg2;
    }

    public boolean isConstant() {
        return "constant".equalsIgnoreCase(formula);
    }

    @Override
    public String toString() {
        return "ScaledProp{key=" + key + ", formula=" + formula
                + ", arg1=" + arg1 + ", arg2=" + arg2 + "}";
    }
}
