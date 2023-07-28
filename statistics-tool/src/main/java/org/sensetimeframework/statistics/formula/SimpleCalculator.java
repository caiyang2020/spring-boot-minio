package org.sensetimeframework.statistics.formula;

import org.sensetimeframework.statistics.constant.ValueConstant;
import org.sensetimeframework.statistics.entity.ConfusionMatrix;
import org.sensetimeframework.statistics.entity.EvaluationIndicator;
import org.sensetimeframework.statistics.utils.Util;

public class SimpleCalculator {
    /**
     * 将两个混淆矩阵相加
     *
     * @param augend 第一个混淆矩阵
     * @param addend 第二个混淆矩阵
     * @return 相加后的混淆矩阵
     */
    public static ConfusionMatrix add(ConfusionMatrix augend, ConfusionMatrix addend) {
        long TP = augend.getTP() + addend.getTP();
        long FP = augend.getFP() + addend.getFP();
        long TN = augend.getTN() + addend.getTN();
        long FN = augend.getFN() + addend.getFN();
        long invalid = augend.getInvalid() + addend.getInvalid();
        long NA = augend.getNA() + addend.getNA();
        return new ConfusionMatrix(TP, FP, TN, FN, invalid, NA);
    }

    /**
     * 将混淆矩阵扩大相应的倍数
     *
     * @param confusionMatrix 目标混淆矩阵
     * @param multiple 扩大倍数
     * @return 扩大后的混淆矩阵
     */
    public static ConfusionMatrix multiply(ConfusionMatrix confusionMatrix, Long multiple) {
        long TP = confusionMatrix.getTP() * multiple;
        long FP = confusionMatrix.getFP() * multiple;
        long TN = confusionMatrix.getTN() * multiple;
        long FN = confusionMatrix.getFN() * multiple;
        long invalid = confusionMatrix.getInvalid() * multiple;
        long NA = confusionMatrix.getNA() * multiple;
        return new ConfusionMatrix(TP, FP, TN, FN, invalid, NA);
    }

    /**
     * 根据混淆矩阵计算评价指标
     *
     * @param confusionMatrix 目标混淆矩阵
     * @return 评价指标
     */
    public static EvaluationIndicator evaluate(ConfusionMatrix confusionMatrix) {
        return evaluate(confusionMatrix, ValueConstant.ToFixed);
    }

    /**
     * 根据混淆矩阵计算评价指标并指定保留几位小数
     *
     * @param confusionMatrix 目标混淆矩阵
     * @param toFixed 保留位数
     * @return 评价指标
     */
    public static EvaluationIndicator evaluate(ConfusionMatrix confusionMatrix, Integer toFixed) {
        String TPR = evaluateTPR(confusionMatrix, toFixed);
        String FPR = evaluateFPR(confusionMatrix, toFixed);
        String PRE = evaluatePRE(confusionMatrix, toFixed);

        return new EvaluationIndicator(TPR, FPR, PRE);
    }

    /**
     * 根据混淆矩阵计算评价指标之TPR
     *
     * @param confusionMatrix 目标混淆矩阵
     * @return 评价指标之TPR
     */
    public static String evaluateTPR(ConfusionMatrix confusionMatrix) {
        return evaluateTPR(confusionMatrix, ValueConstant.ToFixed);
    }

    /**
     * 根据混淆矩阵计算评价指标之FPR
     *
     * @param confusionMatrix 目标混淆矩阵
     * @return 评价指标之FPR
     */
    public static String evaluateFPR(ConfusionMatrix confusionMatrix) {
        return evaluateFPR(confusionMatrix, ValueConstant.ToFixed);
    }

    /**
     * 根据混淆矩阵计算评价指标之PRE
     *
     * @param confusionMatrix 目标混淆矩阵
     * @return 评价指标之PRE
     */
    public static String evaluatePRE(ConfusionMatrix confusionMatrix) {
        return evaluatePRE(confusionMatrix, ValueConstant.ToFixed);
    }

    /**
     * 根据混淆矩阵计算评价指标之TPR(指定保留几位小数)
     *
     * @param confusionMatrix 目标混淆矩阵
     * @param toFixed 保留位数
     * @return 评价指标之TPR
     */
    public static String evaluateTPR(ConfusionMatrix confusionMatrix, Integer toFixed) {
        return Util.divide(confusionMatrix.getTP(), confusionMatrix.getTP() + confusionMatrix.getFN(), toFixed);
    }

    /**
     * 根据混淆矩阵计算评价指标之FPR(指定保留几位小数)
     *
     * @param confusionMatrix 目标混淆矩阵
     * @param toFixed 保留位数
     * @return 评价指标之FPR
     */
    public static String evaluateFPR(ConfusionMatrix confusionMatrix, Integer toFixed) {
        return Util.divide(confusionMatrix.getFP(), confusionMatrix.getFP() + confusionMatrix.getTN(), toFixed);
    }

    /**
     * 根据混淆矩阵计算评价指标之PRE(指定保留几位小数)
     *
     * @param confusionMatrix 目标混淆矩阵
     * @param toFixed 保留位数
     * @return 评价指标之PRE
     */
    public static String evaluatePRE(ConfusionMatrix confusionMatrix, Integer toFixed) {
        return Util.divide(confusionMatrix.getTP(), confusionMatrix.getTP() + confusionMatrix.getFP(), toFixed);
    }
}
