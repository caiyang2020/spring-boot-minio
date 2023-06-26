package org.sensetimeframework.statistics.entity;

import lombok.Data;

@Data
public class ResultSet {
    private ConfusionMatrix confusionMatrix;
    private EvaluationIndicator evaluationIndicator;
}
