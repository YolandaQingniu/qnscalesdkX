package com.qingniu.qnble.demo.util;

import android.content.Context;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.IndicateBean;
import com.yolanda.health.qnblesdk.constant.QNIndicator;
import com.yolanda.health.qnblesdk.constant.QNInfoConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ch on 2019/10/23.
 * 标准判断工具类
 */

public class IndicateUtils {


    private static double weight;

    public static IndicateBean getIndicate(Context context, int indicateType, String gender, int height, int age, Double value) {
        IndicateBean indicateBean = new IndicateBean();
        switch (indicateType) {
            case QNIndicator.TYPE_WEIGHT:
                weight = value;
                List<String> weightArray = Arrays.asList(context.getResources().getString(R.string.type_very_low),
                        context.getResources().getString(R.string.type_low), context.getResources().getString(R.string.type_standar)
                        , context.getResources().getString(R.string.type_high), context.getResources().getString(R.string.type_very_high));
                indicateBean.setIndicateDescribe(weightArray);
                double sw;
                if (gender.equals(QNInfoConst.GENDER_MAN)) {
                    sw = (height - 80) * 0.7;
                } else {
                    sw = (height * 1.37 - 110) * 0.45;
                }
                if (value <= sw * 0.8) {
                    indicateBean.setCurrentIndicate(weightArray.get(0));
                } else if (value > 0.8 * sw && value < sw * 0.9) {
                    indicateBean.setCurrentIndicate(weightArray.get(1));
                } else if (value >= 0.9 * sw && value <= sw * 1.1) {
                    indicateBean.setCurrentIndicate(weightArray.get(2));
                } else if (value > 1.1 * sw && value <= sw * 1.2) {
                    indicateBean.setCurrentIndicate(weightArray.get(3));
                } else if (value > sw * 1.2) {
                    indicateBean.setCurrentIndicate(weightArray.get(4));
                }
                break;
            case QNIndicator.TYPE_BMI:
                List<String> bmiArray = Arrays.asList(context.getResources().getString(R.string.type_low), context.getResources().getString(R.string.type_standar)
                        , context.getResources().getString(R.string.type_high));
                indicateBean.setIndicateDescribe(bmiArray);
                if (value < 18.5) {
                    indicateBean.setCurrentIndicate(bmiArray.get(0));
                } else if (value >= 18.5 && value <= 25) {
                    indicateBean.setCurrentIndicate(bmiArray.get(1));
                } else {
                    indicateBean.setCurrentIndicate(bmiArray.get(2));
                }
                break;
            case QNIndicator.TYPE_BODYFAT:
                List<String> bodyFatArray = Arrays.asList(
                        context.getResources().getString(R.string.type_low), context.getResources().getString(R.string.type_standar)
                        , context.getResources().getString(R.string.type_high), context.getResources().getString(R.string.type_very_high));
                indicateBean.setIndicateDescribe(bodyFatArray);
                if (gender.equals(QNInfoConst.GENDER_MAN)) {
                    if (value < 11) {
                        indicateBean.setCurrentIndicate(bodyFatArray.get(0));
                    } else if (value >= 11 && value <= 21) {
                        indicateBean.setCurrentIndicate(bodyFatArray.get(1));
                    } else if (value > 21 && value <= 26) {
                        indicateBean.setCurrentIndicate(bodyFatArray.get(2));
                    } else {
                        indicateBean.setCurrentIndicate(bodyFatArray.get(3));
                    }
                } else {
                    if (value < 21) {
                        indicateBean.setCurrentIndicate(bodyFatArray.get(0));
                    } else if (value >= 21 && value <= 31) {
                        indicateBean.setCurrentIndicate(bodyFatArray.get(1));
                    } else if (value > 31 && value <= 36) {
                        indicateBean.setCurrentIndicate(bodyFatArray.get(2));
                    } else {
                        indicateBean.setCurrentIndicate(bodyFatArray.get(3));
                    }
                }
                break;
            case QNIndicator.TYPE_SUBFAT:
                List<String> subFatArray = Arrays.asList(
                        context.getResources().getString(R.string.type_low), context.getResources().getString(R.string.type_standar)
                        , context.getResources().getString(R.string.type_high));
                indicateBean.setIndicateDescribe(subFatArray);
                if (gender.equals(QNInfoConst.GENDER_MAN)) {
                    if (value < 8.6) {
                        indicateBean.setCurrentIndicate(subFatArray.get(0));
                    } else if (value >= 8.6 && value <= 16.7) {
                        indicateBean.setCurrentIndicate(subFatArray.get(1));
                    } else {
                        indicateBean.setCurrentIndicate(subFatArray.get(2));
                    }
                } else {
                    if (value < 18.5) {
                        indicateBean.setCurrentIndicate(subFatArray.get(0));
                    } else if (value >= 18.5 && value <= 26.7) {
                        indicateBean.setCurrentIndicate(subFatArray.get(1));
                    } else {
                        indicateBean.setCurrentIndicate(subFatArray.get(2));
                    }
                }
                break;
            case QNIndicator.TYPE_VISFAT:
                List<String> visFatArray = Arrays.asList(context.getResources().getString(R.string.type_standar)
                        , context.getResources().getString(R.string.type_high), context.getResources().getString(R.string.type_very_high));
                indicateBean.setIndicateDescribe(visFatArray);
                if (value <= 9) {
                    indicateBean.setCurrentIndicate(visFatArray.get(0));
                } else if (value > 9 && value <= 14) {
                    indicateBean.setCurrentIndicate(visFatArray.get(1));
                } else {
                    indicateBean.setCurrentIndicate(visFatArray.get(2));
                }
                break;
            case QNIndicator.TYPE_WATER:
                List<String> waterArray = Arrays.asList(context.getResources().getString(R.string.type_sufficient)
                        , context.getResources().getString(R.string.type_standar), context.getResources().getString(R.string.type_low));
                indicateBean.setIndicateDescribe(waterArray);
                if (gender.equals(QNInfoConst.GENDER_MAN)) {
                    if (value > 65) {
                        indicateBean.setCurrentIndicate(waterArray.get(0));
                    } else if (value >= 55 && value <= 65) {
                        indicateBean.setCurrentIndicate(waterArray.get(1));
                    } else {
                        indicateBean.setCurrentIndicate(waterArray.get(2));
                    }
                } else {
                    if (value > 60) {
                        indicateBean.setCurrentIndicate(waterArray.get(0));
                    } else if (value >= 45 && value <= 60) {
                        indicateBean.setCurrentIndicate(waterArray.get(1));
                    } else {
                        indicateBean.setCurrentIndicate(waterArray.get(2));
                    }
                }
                break;
            case QNIndicator.TYPE_MUSCLE:
                List<String> muscleArray = Arrays.asList(context.getResources().getString(R.string.type_low)
                        , context.getResources().getString(R.string.type_standar), context.getResources().getString(R.string.type_high));
                indicateBean.setIndicateDescribe(muscleArray);
                if (gender.equals(QNInfoConst.GENDER_MAN)) {
                    if (value < 49) {
                        indicateBean.setCurrentIndicate(muscleArray.get(0));
                    } else if (value >= 49 && value <= 59) {
                        indicateBean.setCurrentIndicate(muscleArray.get(1));
                    } else {
                        indicateBean.setCurrentIndicate(muscleArray.get(2));
                    }
                } else {
                    if (value < 40) {
                        indicateBean.setCurrentIndicate(muscleArray.get(0));
                    } else if (value >= 40 && value <= 50) {
                        indicateBean.setCurrentIndicate(muscleArray.get(1));
                    } else {
                        indicateBean.setCurrentIndicate(muscleArray.get(2));
                    }
                }
                break;
            case QNIndicator.TYPE_BONE:
                List<String> boneArray = Arrays.asList(context.getResources().getString(R.string.type_low)
                        , context.getResources().getString(R.string.type_standar), context.getResources().getString(R.string.type_high));
                indicateBean.setIndicateDescribe(boneArray);
                if (gender.equals(QNInfoConst.GENDER_MAN)) {
                    if (weight <= 60) {
                        if (value < 2.3) {
                            indicateBean.setCurrentIndicate(boneArray.get(0));
                        } else if (value >= 2.3 && value <= 3.1) {
                            indicateBean.setCurrentIndicate(boneArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(boneArray.get(2));
                        }
                    } else if (weight > 60 && weight < 75) {
                        if (value < 2.7) {
                            indicateBean.setCurrentIndicate(boneArray.get(0));
                        } else if (value >= 2.7 && value <= 3.1) {
                            indicateBean.setCurrentIndicate(boneArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(boneArray.get(2));
                        }
                    } else {
                        if (value < 3.0) {
                            indicateBean.setCurrentIndicate(boneArray.get(0));
                        } else if (value >= 3.0 && value <= 3.4) {
                            indicateBean.setCurrentIndicate(boneArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(boneArray.get(2));
                        }
                    }
                } else {
                    if (weight <= 45) {
                        if (value < 1.6) {
                            indicateBean.setCurrentIndicate(boneArray.get(0));
                        } else if (value >= 1.6 && value <= 2.0) {
                            indicateBean.setCurrentIndicate(boneArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(boneArray.get(2));
                        }
                    } else if (weight > 45 && weight < 60) {
                        if (value < 2.0) {
                            indicateBean.setCurrentIndicate(boneArray.get(0));
                        } else if (value >= 2.0 && value <= 2.4) {
                            indicateBean.setCurrentIndicate(boneArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(boneArray.get(2));
                        }
                    } else {
                        if (value < 2.3) {
                            indicateBean.setCurrentIndicate(boneArray.get(0));
                        } else if (value >= 2.3 && value <= 2.7) {
                            indicateBean.setCurrentIndicate(boneArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(boneArray.get(2));
                        }
                    }
                }

                break;
            case QNIndicator.TYPE_BODY_SHAPE:
                List<String> bodyShapeArray = Arrays.asList(context.getResources().getString(R.string.type_shape_none),
                        context.getResources().getString(R.string.type_shape_invisible_obesity)
                        , context.getResources().getString(R.string.type_shape_hypokinesia),
                        context.getResources().getString(R.string.type_shape_lean)
                        , context.getResources().getString(R.string.type_shape_standard),
                        context.getResources().getString(R.string.type_shape_lean_and_muscular),
                        context.getResources().getString(R.string.type_shape_fueling),
                        context.getResources().getString(R.string.type_shape_full_figured)
                        , context.getResources().getString(R.string.type_shape_standard_muscle),
                        context.getResources().getString(R.string.type_shape_very_muscular));
                indicateBean.setIndicateDescribe(bodyShapeArray);
                int index = (new Double(value)).intValue();
                indicateBean.setCurrentIndicate(bodyShapeArray.get(index));
                break;
            case QNIndicator.TYPE_PROTEIN:
                List<String> proteinArray = Arrays.asList(context.getResources().getString(R.string.type_sufficient)
                        , context.getResources().getString(R.string.type_standar), context.getResources().getString(R.string.type_low));
                indicateBean.setIndicateDescribe(proteinArray);
                if (gender.equals(QNInfoConst.GENDER_MAN)) {
                    if (value > 18) {
                        indicateBean.setCurrentIndicate(proteinArray.get(0));
                    } else if (value >= 16 && value <= 18) {
                        indicateBean.setCurrentIndicate(proteinArray.get(1));
                    } else {
                        indicateBean.setCurrentIndicate(proteinArray.get(2));
                    }
                } else {
                    if (value > 16) {
                        indicateBean.setCurrentIndicate(proteinArray.get(0));
                    } else if (value >= 14 && value <= 16) {
                        indicateBean.setCurrentIndicate(proteinArray.get(1));
                    } else {
                        indicateBean.setCurrentIndicate(proteinArray.get(2));
                    }
                }
                break;
            case QNIndicator.TYPE_LBM:
                List<String> lbmArray = Arrays.asList(context.getResources().getString(R.string.type_standar));
                indicateBean.setIndicateDescribe(lbmArray);
                indicateBean.setCurrentIndicate(lbmArray.get(0));
                break;
            case QNIndicator.TYPE_BODY_AGE:
                List<String> bodyAgeArray = Arrays.asList(context.getResources().getString(R.string.type_reach_standard)
                        , context.getResources().getString(R.string.type_not_reach_standard));
                indicateBean.setIndicateDescribe(bodyAgeArray);
                if (value <= age) {
                    indicateBean.setCurrentIndicate(bodyAgeArray.get(0));
                } else {
                    indicateBean.setCurrentIndicate(bodyAgeArray.get(1));
                }
                break;
            case QNIndicator.TYPE_MUSCLE_MASS:
                List<String> muscleMassAgeArray = Arrays.asList(context.getResources().getString(R.string.type_low)
                        , context.getResources().getString(R.string.type_standar), context.getResources().getString(R.string.type_sufficient));
                indicateBean.setIndicateDescribe(muscleMassAgeArray);
                if (gender.equals(QNInfoConst.GENDER_MAN)) {
                    if (height < 160) {
                        if (value < 38.5) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(0));
                        } else if (value >= 38.5 && value <= 46.5) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(2));
                        }
                    } else if (height >= 160 && height <= 170) {
                        if (value < 44) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(0));
                        } else if (value >= 44 && value <= 52.4) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(2));
                        }
                    } else {
                        if (value < 49.4) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(0));
                        } else if (value >= 49.4 && value <= 59.4) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(2));
                        }
                    }
                } else {
                    if (height < 150) {
                        if (value < 29.1) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(0));
                        } else if (value >= 29.1 && value <= 34.7) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(2));
                        }
                    } else if (height >= 150 && height <= 160) {
                        if (value < 32.9) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(0));
                        } else if (value >= 32.9 && value <= 37.5) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(2));
                        }
                    } else {
                        if (value < 36.5) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(0));
                        } else if (value >= 36.5 && value <= 42.5) {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(1));
                        } else {
                            indicateBean.setCurrentIndicate(muscleMassAgeArray.get(2));
                        }
                    }
                }
                break;
            case QNIndicator.TYPE_FATTY_LIVER_RISK:
                List<String> fattyLiverRiskArray = Arrays.asList(context.getResources().getString(R.string.fatty_liver_risk_level0)
                        , context.getResources().getString(R.string.fatty_liver_risk_level1), context.getResources().getString(R.string.fatty_liver_risk_level2)
                        , context.getResources().getString(R.string.fatty_liver_risk_level3) , context.getResources().getString(R.string.fatty_liver_risk_level4)
                );
                indicateBean.setIndicateDescribe(fattyLiverRiskArray);
                int level = (new Double(value)).intValue();
                indicateBean.setCurrentIndicate(fattyLiverRiskArray.get(level));
                break;
            default:
                indicateBean.setIndicateDescribe(new ArrayList<String>());
                indicateBean.setCurrentIndicate(context.getResources().getString(R.string.no_standard));
                break;
        }
        return indicateBean;
    }
}
