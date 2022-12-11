package com.qingniu.qnble.demo.util;

import static com.qingniu.utils.NumberUtils.getOnePrecision;

import com.qingniu.utils.NumberUtils;
import com.qn.device.constant.QNDisplayModuleType;
import com.qn.device.out.QNBleApi;

/**
 * @author: hyr
 * @date: 2022/12/7 17:18
 * @desc:
 */
public class UnitConvertUtil {
    public static String getShowWeightWithUnit(QNBleApi qnBleApi, double weight, int unit, QNDisplayModuleType displayModuleType) {
        double calcWeight = qnBleApi.convertWeight(weight, unit);
        String value;
        switch (unit) {
            case 0: {
                value = calcWeight + "kg";
                break;
            }
            case 1: {
                value = calcWeight + "lb";
                break;
            }
            case 2: {
                value = calcWeight + "斤";
                break;
            }
            case 3: {
                if (calcWeight < 14) {
                    value = calcWeight + "lb";
                } else {
                    if (calcWeight % 14 == 0) {
                        value = (int) (calcWeight / 14) + "st";
                    } else {
                        if (calcWeight >= 280 && displayModuleType == QNDisplayModuleType.SIMPLE) {
                            if (calcWeight % 14 >=10){
                                value = (int)(calcWeight / 14) + "st" + Math.round(calcWeight % 14) + "lb";
                            }else {
                                value = (int)(calcWeight / 14) + "st" + getOnePrecision(calcWeight % 14) + "lb";
                            }
                        } else {
                            value = (int)(calcWeight / 14) + "st" + getOnePrecision(calcWeight % 14) + "lb";
                        }
                    }
                }
                break;
            }
            case 4: {
                value = NumberUtils.getPrecisionShow(calcWeight / 14f, 2) + "st";
                break;
            }
            default: {
                value = calcWeight + "kg";
                break;
            }
        }
        return value;
    }
}
