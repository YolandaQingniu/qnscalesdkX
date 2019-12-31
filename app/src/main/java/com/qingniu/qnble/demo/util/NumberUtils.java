package com.qingniu.qnble.demo.util;

/**
 * 数值工具，包括数值的格式化，取精度等
 *
 * @author hdr
 */
public class NumberUtils {
    /**
     * 格式化整数到2位，不足的补0
     *
     * @param value
     * @return
     */
    public static String formatIntegerTo2(int value) {
        if (value < 10) {
            return "0" + value;
        } else
            return String.valueOf(value);
    }


}
