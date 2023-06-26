package org.sensetimeframework.statistics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationIndicator {
    private String TPR;
    private String FPR;
    private String PRE;
}
