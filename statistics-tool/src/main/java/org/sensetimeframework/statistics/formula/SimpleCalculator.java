package org.sensetimeframework.statistics.formula;

import org.sensetimeframework.statistics.constant.ValueConstant;
import org.sensetimeframework.statistics.entity.ConfusionMatrix;
import org.sensetimeframework.statistics.entity.EvaluationIndicator;
import org.sensetimeframework.statistics.utils.Util;

public class SimpleCalculator {
    public static ConfusionMatrix add(ConfusionMatrix augend,ConfusionMatrix addend){
        long TP = augend.getTP() + addend.getTP();
        long FP = augend.getFP() + addend.getFP();
        long TN = augend.getTN() + addend.getTN();
        long FN = augend.getFN() + addend.getFN();
        long invalid = augend.getInvalid() + addend.getInvalid();
        long NA = augend.getNA() + addend.getNA();
        return new ConfusionMatrix(TP,FP,TN,FN,invalid,NA);
    }

    public static ConfusionMatrix multiply(ConfusionMatrix confusionMatrix,Long multiple){
        long TP = confusionMatrix.getTP() * multiple;
        long FP = confusionMatrix.getFP() * multiple;
        long TN = confusionMatrix.getTN() * multiple;
        long FN = confusionMatrix.getFN() * multiple;
        long invalid = confusionMatrix.getInvalid() * multiple;
        long NA = confusionMatrix.getNA() * multiple;
        return new ConfusionMatrix(TP,FP,TN,FN,invalid,NA);
    }

    public static EvaluationIndicator evaluate(ConfusionMatrix confusionMatrix){
        return evaluate(confusionMatrix,ValueConstant.ToFixed);
    }

    public static EvaluationIndicator evaluate(ConfusionMatrix confusionMatrix,Integer toFixed){
        String TPR = evaluateTPR(confusionMatrix,toFixed);
        String FPR = evaluateFPR(confusionMatrix,toFixed);
        String PRE = evaluatePRE(confusionMatrix,toFixed);

        return new EvaluationIndicator(TPR,FPR,PRE);
    }

    public static String evaluateTPR(ConfusionMatrix confusionMatrix){
        return evaluateTPR(confusionMatrix,ValueConstant.ToFixed);
    }

    public static String evaluateFPR(ConfusionMatrix confusionMatrix){
        return evaluateFPR(confusionMatrix,ValueConstant.ToFixed);
    }

    public static String evaluatePRE(ConfusionMatrix confusionMatrix){
        return evaluatePRE(confusionMatrix,ValueConstant.ToFixed);
    }

    public static String evaluateTPR(ConfusionMatrix confusionMatrix,Integer toFixed){
        return Util.divide(confusionMatrix.getTP(), confusionMatrix.getTP() + confusionMatrix.getFN(),toFixed);
    }

    public static String evaluateFPR(ConfusionMatrix confusionMatrix,Integer toFixed){
        return Util.divide(confusionMatrix.getFP(), confusionMatrix.getFP() + confusionMatrix.getTN(),toFixed);
    }

    public static String evaluatePRE(ConfusionMatrix confusionMatrix,Integer toFixed){
        return Util.divide(confusionMatrix.getTP(), confusionMatrix.getTP() + confusionMatrix.getFP(),toFixed);
    }
}
