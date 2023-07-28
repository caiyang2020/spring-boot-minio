package org.sensetimeframework.statistics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfusionMatrix {
    private Long TP = 0L;
    private Long FP = 0L;
    private Long TN = 0L;
    private Long FN = 0L;
    private Long invalid = 0L;
    private Long NA = 0L;

    public ConfusionMatrix deepCopy() {
        return new ConfusionMatrix(TP,FP,TN,FN,invalid,NA);
    }

    public ConfusionMatrix getPositivePart() {
        return new ConfusionMatrix(TP,0L,0L,FN,0L,0L);
    }

    public ConfusionMatrix getNegativePart() {
        return new ConfusionMatrix(0L,FP,TN,0L,0L,0L);
    }

    public Long getSize() {
        return TP + FP + TN + FN + invalid + NA;
    }

    public Long getValidSize() {
        return TP + FP + TN + FN;
    }
}
