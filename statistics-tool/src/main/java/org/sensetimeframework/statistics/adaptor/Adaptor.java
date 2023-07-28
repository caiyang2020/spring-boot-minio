package org.sensetimeframework.statistics.adaptor;

import org.sensetimeframework.statistics.constant.ValueConstant;
import org.sensetimeframework.statistics.entity.ConfusionMatrix;
import org.sensetimeframework.statistics.entity.EvaluationIndicator;
import org.sensetimeframework.statistics.entity.ResultSet;
import org.sensetimeframework.statistics.enumeration.EvaluationIndexEnum;
import org.sensetimeframework.statistics.enumeration.TestResultEnum;

import java.util.HashMap;
import java.util.Map;

public class Adaptor {
    public static Map<String,Object> mapKeyToUpperCase(Map<String, Object> sourceMap) {
        Map<String,Object> resMap = new HashMap<>();

        if (sourceMap == null) {
            return resMap;
        }

        for (String oldKey : sourceMap.keySet()) {
            resMap.put(oldKey.toUpperCase(), sourceMap.get(oldKey));
        }

        return resMap;
    }

    public static ConfusionMatrix mapToConfusionMatrix(Map<String, Object> sourceMap) {
        Map<String, Object> resMap = mapKeyToUpperCase(sourceMap);

        ConfusionMatrix confusionMatrix = new ConfusionMatrix();

        long TP = resMap.get(TestResultEnum.TP.name()) == null? 0L : Long.parseLong(String.valueOf(resMap.get(TestResultEnum.TP.name())));
        confusionMatrix.setTP(TP);

        long FP = resMap.get(TestResultEnum.FP.name()) == null? 0L : Long.parseLong(String.valueOf(resMap.get(TestResultEnum.FP.name())));
        confusionMatrix.setTP(FP);

        long TN = resMap.get(TestResultEnum.TN.name()) == null? 0L : Long.parseLong(String.valueOf(resMap.get(TestResultEnum.TN.name())));
        confusionMatrix.setTP(TN);

        long FN = resMap.get(TestResultEnum.FN.name()) == null? 0L : Long.parseLong(String.valueOf(resMap.get(TestResultEnum.FN.name())));
        confusionMatrix.setTP(FN);

        long INVALID = resMap.get(TestResultEnum.INVALID.name()) == null? 0L : Long.parseLong(String.valueOf(resMap.get(TestResultEnum.INVALID.name())));
        confusionMatrix.setTP(INVALID);

        long NA = resMap.get(TestResultEnum.NA.name()) == null? 0L : Long.parseLong(String.valueOf(resMap.get(TestResultEnum.NA.name())));
        confusionMatrix.setTP(NA);

        return confusionMatrix;
    }

    public static Map<String, Object> confusionMatrixToMap(ConfusionMatrix confusionMatrix) {
        Map<String,Object> resMap = new HashMap<>();

        if (confusionMatrix != null) {
            resMap.put(TestResultEnum.TP.name(), confusionMatrix.getTP());
            resMap.put(TestResultEnum.FP.name(), confusionMatrix.getFP());
            resMap.put(TestResultEnum.TN.name(), confusionMatrix.getTN());
            resMap.put(TestResultEnum.FN.name(), confusionMatrix.getFN());
            resMap.put(TestResultEnum.INVALID.name(), confusionMatrix.getINVALID());
            resMap.put(TestResultEnum.NA.name(), confusionMatrix.getNA());
        }

        return resMap;
    }

    public static Map<String, Object> evaluationIndicatorToMap(EvaluationIndicator evaluationIndicator) {
        Map<String, Object> resMap = new HashMap<>();

        if (evaluationIndicator != null) {
            resMap.put(EvaluationIndexEnum.TPR.name(), evaluationIndicator.getTPR() == null? ValueConstant.NaN : evaluationIndicator.getTPR());
            resMap.put(EvaluationIndexEnum.FPR.name(), evaluationIndicator.getFPR() == null? ValueConstant.NaN : evaluationIndicator.getFPR());
            resMap.put(EvaluationIndexEnum.PRE.name(), evaluationIndicator.getPRE() == null? ValueConstant.NaN : evaluationIndicator.getPRE());
        }

        return resMap;
    }

    public static Map<String, Object> resultSetToMap(ResultSet resultSet) {
        if (resultSet == null) {
            return new HashMap<>();
        }

        Map<String,Object> confusionMatrixMap = confusionMatrixToMap(resultSet.getConfusionMatrix());
        Map<String,Object> evaluationIndicatorMap = evaluationIndicatorToMap((resultSet.getEvaluationIndicator()));
        evaluationIndicatorMap.forEach((k, v) -> confusionMatrixMap.merge(k, v, (v1, v2) -> v2));

        return confusionMatrixMap;
    }
}
