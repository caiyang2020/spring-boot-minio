package org.sensetimeframework.statistics.formula;

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
        double targetDoubleValue = transToDouble(target);
        double sum = dataList.stream().mapToDouble(o -> transToDouble(o) - targetDoubleValue).sum();
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
        double targetDoubleValue = transToDouble(target);
        double sum = dataList.stream().mapToDouble(o -> Math.abs(transToDouble(o) - targetDoubleValue)).sum();
        return sum / dataList.size();
    }

    /**
     * 统计一批数据的平均值
     *
     * @param dataList 待统计数据
     * @return 平均值
     */
    public static <T> Double calcMean(List<T> dataList) {
        return dataList.stream().mapToDouble(ErrorCalculator::transToDouble).sum() / dataList.size();
    }

    /**
     * 统计一批数据的方差
     *
     * @param dataList 待统计数据
     * @return 方差
     */
    public static <T> Double calcPopVariance(List<T> dataList) {
        double mean = calcMean(dataList);
        return dataList.stream().mapToDouble(o -> Math.pow((transToDouble(o) - mean), 2)).sum() / dataList.size();
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

    /**
     * 将其他类型包括double类型数据转换为double
     *
     * @param data 待转换数据
     * @return 转换后的数据
     */
    public static <T> Double transToDouble(T data) {
        double targetDoubleValue = 0.0;
        if (data instanceof String) {
            targetDoubleValue = Double.parseDouble((String) data);
        } else if (data instanceof Double) {
            targetDoubleValue = (Double) data;
        } else if (data instanceof Integer) {
            targetDoubleValue = ((Integer) data).doubleValue();
        } else if (data instanceof Long) {
            targetDoubleValue = ((Long) data).doubleValue();
        } else if (data instanceof Float) {
            targetDoubleValue = ((Float) data).doubleValue();
        } else {
            System.out.println("Unsupported data type: " + data.getClass().getSimpleName());
        }
        return targetDoubleValue;
    }
}
