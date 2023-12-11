package org.sensetimeframework.statistics.utils;

import org.sensetimeframework.statistics.constant.ValueConstant;

import java.util.Objects;

public class Util {
    public static <T> String divide(T dividend, T divisor) {
        return divide(dividend, divisor, ValueConstant.ToFixed);
    }

    public static <T> String divide(T dividend, T divisor, Integer toFixed) {
        String[] characters = {"%", ".", String.valueOf(toFixed), "f"};
        StringBuilder sb = new StringBuilder();
        for (String character : characters) {
            sb.append(character);
        }
        String format = sb.toString();

        return divide(dividend, divisor, format);
    }

    public static <T> String divide(T dividend, T divisor, String format) {
        Double divisorDoubleType = transToDouble(divisor);
        Double dividendDoubleType = transToDouble(dividend);
        if (Math.abs(divisorDoubleType - 0.0) < Double.MIN_VALUE) {
            return ValueConstant.NaN;
        } else {
            return String.format(format, (dividendDoubleType / divisorDoubleType * 100)) + "%";
        }
    }

    /**
     * 将其他类型包括double类型数据转换为double
     *
     * @param data 待转换数据
     * @return 转换后的数据
     */
    public static <T> Double transToDouble(T data) {
        double targetDoubleValue = 0.0;
        switch (data) {
            case String s -> targetDoubleValue = Double.parseDouble(s);
            case Double d -> targetDoubleValue = d;
            case Integer i -> targetDoubleValue = i.doubleValue();
            case Long l -> targetDoubleValue = l.doubleValue();
            case Float f -> targetDoubleValue = f.doubleValue();
            case null, default -> System.out.println("Unsupported data type: " + Objects.requireNonNull(data).getClass().getSimpleName());
        }
        return targetDoubleValue;
    }
}
