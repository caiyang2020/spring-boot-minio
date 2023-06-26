package org.sensetimeframework.statistics.utils;

import org.sensetimeframework.statistics.constant.ValueConstant;

public class Util {
    public static String divide(Long dividend,Long divisor){
        return divide(dividend,divisor,ValueConstant.ToFixed);
    }

    public static String divide(Long dividend,Long divisor,Integer toFixed){
        String[] characters = {"%", ".", String.valueOf(toFixed), "f"};
        StringBuilder sb = new StringBuilder();
        for (String character : characters) {
            sb.append(character);
        }
        String format = sb.toString();

        return divide(dividend,divisor,format);
    }

    public static String divide(Long dividend,Long divisor,String format){
        if(divisor == 0L){
            return ValueConstant.NaN;
        }else{
            return String.format(format, (dividend.doubleValue() / divisor.doubleValue() * 100)) + "%";
        }
    }
}
