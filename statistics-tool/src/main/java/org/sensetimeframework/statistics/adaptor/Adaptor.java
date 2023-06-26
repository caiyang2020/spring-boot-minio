package org.sensetimeframework.statistics.adaptor;

import org.sensetimeframework.statistics.constant.KeyConstant;
import org.sensetimeframework.statistics.constant.ValueConstant;
import org.sensetimeframework.statistics.entity.ConfusionMatrix;
import org.sensetimeframework.statistics.entity.EvaluationIndicator;
import org.sensetimeframework.statistics.entity.ResultSet;

import java.util.HashMap;
import java.util.Map;

public class Adaptor {
    public static Map<String,Object> mapKeyToUpperCase(Map<String,Object> sourceMap){
        Map<String,Object> resMap = new HashMap<>();

        if(sourceMap == null){
            return resMap;
        }

        for(String oldKey:sourceMap.keySet()){
            resMap.put(oldKey.toUpperCase(),sourceMap.get(oldKey));
        }

        return resMap;
    }

    public static ConfusionMatrix mapToConfusionMatrix(Map<String,Object> sourceMap){
        Map<String, Object> resMap = mapKeyToUpperCase(sourceMap);

        ConfusionMatrix confusionMatrix = new ConfusionMatrix();

        long TP = resMap.get(KeyConstant.TP) == null? 0L : Long.parseLong(String.valueOf(resMap.get(KeyConstant.TP)));
        confusionMatrix.setTP(TP);

        long FP = resMap.get(KeyConstant.FP) == null? 0L : Long.parseLong(String.valueOf(resMap.get(KeyConstant.FP)));
        confusionMatrix.setTP(FP);

        long TN = resMap.get(KeyConstant.TN) == null? 0L : Long.parseLong(String.valueOf(resMap.get(KeyConstant.TN)));
        confusionMatrix.setTP(TN);

        long FN = resMap.get(KeyConstant.FN) == null? 0L : Long.parseLong(String.valueOf(resMap.get(KeyConstant.FN)));
        confusionMatrix.setTP(FN);

        long invalid = resMap.get(KeyConstant.INVALID) == null? 0L : Long.parseLong(String.valueOf(resMap.get(KeyConstant.INVALID)));
        confusionMatrix.setTP(invalid);

        long NA = resMap.get(KeyConstant.NA) == null? 0L : Long.parseLong(String.valueOf(resMap.get(KeyConstant.NA)));
        confusionMatrix.setTP(NA);

        return confusionMatrix;
    }

    public static Map<String,Object> confusionMatrixToMap(ConfusionMatrix confusionMatrix){
        Map<String,Object> resMap = new HashMap<>();

        if(confusionMatrix != null){
            resMap.put(KeyConstant.TP,confusionMatrix.getTP());
            resMap.put(KeyConstant.FP,confusionMatrix.getFP());
            resMap.put(KeyConstant.TN,confusionMatrix.getTN());
            resMap.put(KeyConstant.FN,confusionMatrix.getFN());
            resMap.put(KeyConstant.INVALID,confusionMatrix.getInvalid());
            resMap.put(KeyConstant.NA,confusionMatrix.getNA());
        }

        return resMap;
    }

    public static Map<String,Object> evaluationIndicatorToMap(EvaluationIndicator evaluationIndicator){
        Map<String,Object> resMap = new HashMap<>();

        if(evaluationIndicator != null){
            resMap.put(KeyConstant.TPR,evaluationIndicator.getTPR() == null? ValueConstant.NaN : evaluationIndicator.getTPR());
            resMap.put(KeyConstant.FPR,evaluationIndicator.getFPR() == null? ValueConstant.NaN : evaluationIndicator.getFPR());
            resMap.put(KeyConstant.PRE,evaluationIndicator.getPRE() == null? ValueConstant.NaN : evaluationIndicator.getPRE());
        }

        return resMap;
    }

    public static Map<String,Object> resultSetToMap(ResultSet resultSet){
        if(resultSet == null){
            return new HashMap<>();
        }

        Map<String,Object> confusionMatrixMap = confusionMatrixToMap(resultSet.getConfusionMatrix());
        Map<String,Object> evaluationIndicatorMap = evaluationIndicatorToMap((resultSet.getEvaluationIndicator()));
        evaluationIndicatorMap.forEach((k, v) -> confusionMatrixMap.merge(k, v, (v1, v2) -> v2));

        return confusionMatrixMap;
    }
}
