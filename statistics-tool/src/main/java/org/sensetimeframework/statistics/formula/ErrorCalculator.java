package org.sensetimeframework.statistics.formula;

import org.sensetimeframework.statistics.utils.Util;

import java.util.List;

public class ErrorCalculator {
    /**
     * 统计一批数据与某个标定值之间的平均误差
     *
     * @param dataList 待统计数据
     * @param target 标定值
     * @return 平均误差
     */
    public static <U,V> Double calcAverageError(List<U> dataList, V target){
        double targetDoubleValue = Util.transToDouble(target);
        double sum = dataList.stream().mapToDouble(o -> Util.transToDouble(o) - targetDoubleValue).sum();
        return sum / dataList.size();
    }

    /**
     * 统计一批数据与某个标定值之间的平均绝对误差
     *
     * @param dataList 待统计数据
     * @param target 标定值
     * @return 平均绝对误差
     */
    public static <U,V> Double calcAbsAverageError(List<U> dataList, V target){
        double targetDoubleValue = Util.transToDouble(target);
        double sum = dataList.stream().mapToDouble(o -> Math.abs(Util.transToDouble(o) - targetDoubleValue)).sum();
        return sum / dataList.size();
    }

    /**
     * 统计一批数据的平均值
     *
     * @param dataList 待统计数据
     * @return 平均值
     */
    public static <T> Double calcMean(List<T> dataList) {
        return dataList.stream().mapToDouble(Util::transToDouble).sum() / dataList.size();
    }

    /**
     * 统计一批数据的方差
     *
     * @param dataList 待统计数据
     * @return 方差
     */
    public static <T> Double calcPopVariance(List<T> dataList) {
        double mean = calcMean(dataList);
        return dataList.stream().mapToDouble(o -> Math.pow((Util.transToDouble(o) - mean), 2)).sum() / dataList.size();
    }

    /**
     * 统计一批数据的标准差
     *
     * @param dataList 待统计数据
     * @return 标准差
     */
    public static <T> Double calcPopStdDev(List<T> dataList) {
        return Math.sqrt(calcPopVariance(dataList));
    }
}
