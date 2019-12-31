package com.qingniu.qnble.demo.picker;

import android.content.Context;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.util.DateUtils;

import java.util.Calendar;
import java.util.Date;

import butterknife.OnClick;

/**
 * author: yolanda-XY
 * date: 2018/3/30
 * package_name: com.qingniu.qnble.demo.picker
 * description: ${TODO}
 */

public class DatePickerDialog extends BaseDataPickerDialog {

    DateChooseListener dateChooseListener;
    Date maxDate;
    Date minDate;

    public DatePickerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onValueChange(int index, int value) {
        if (index == 0) {
            int maxMonth = 12;
            int minMonth = 1;
            if (maxDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(maxDate);
                if (value == calendar.get(Calendar.YEAR)) {
                    //如果当前滚轮已经转到最大的限制年
                    maxMonth = calendar.get(Calendar.MONTH) + 1;
                }
            }
            if (minDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(minDate);
                if (value == calendar.get(Calendar.YEAR)) {
                    //如果当前滚轮已经转到最大的限制年
                    minMonth = calendar.get(Calendar.MONTH) + 1;
                }
            }
            setMaxValue(1, maxMonth);
            setMinValue(1, minMonth);

            int month = getValue(1);
            int year = value;
            if (month == 2) {
                int maxDay;
                if (isLeapYear(year)) {
                    maxDay = 29;
                } else {
                    maxDay = 28;
                }
                setMaxValue(2, maxDay);
            }

        } else if (index == 1) {

            int month = value;
            int year = getValue(0);
            int maxDay;

            switch (month) {
                case 2:
                    // 根据闰年来区别二月份的天数
                    if (isLeapYear(year)) {
                        maxDay = 29;
                    } else {
                        maxDay = 28;
                    }
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    maxDay = 30;
                    break;
                default:
                    maxDay = 31;
                    break;
            }
            int minDay = 1;
            if (maxDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(maxDate);
                if (getValue(0) == calendar.get(Calendar.YEAR)
                        && value == calendar.get(Calendar.MONTH) + 1) {
                    //如果当前滚轮已经转到最大的限制年和月
                    maxDay = calendar.get(Calendar.DATE);
                }
            }
            if (minDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(minDate);
                if (getValue(0) == calendar.get(Calendar.YEAR)
                        && value == calendar.get(Calendar.MONTH) + 1) {
                    //如果当前滚轮已经转到最大的限制年
                    minDay = calendar.get(Calendar.DATE);
                }
            }
            setMinValue(2, minDay);
            setMaxValue(2, maxDay);
        }
    }

    boolean isLeapYear(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 100 == 0 && year % 400 == 0;
    }

    public interface DateChooseListener {
        void onChoose(Date date);
    }

    @OnClick(R.id.confirmBtn)
    public void onConfirmClick() {
        if (dateChooseListener != null) {
            int year = getValue(0);
            int month = getValue(1);
            int day = getValue(2);
            Date date = DateUtils.getDate(year, month, day);
            dateChooseListener.onChoose(date);
        }
        dismiss();
    }


    public static class Builder extends BaseDataPickerDialog.Builder<DatePickerDialog> {
        DateChooseListener dateChooseListener;
        Date maxDate = DateUtils.getCurrentDate();
        Date minDate;
        int defaultYear = 1990;
        int defaultMonth = 1;
        int defaultDay = 1;

        public Builder defaultYear(int defaultYear) {
            this.defaultYear = defaultYear;
            return this;
        }

        public Builder defaultMonth(int defaultMonth) {
            this.defaultMonth = defaultMonth;
            return this;
        }

        public Builder defaultDay(int defaultDay) {
            this.defaultDay = defaultDay;
            return this;
        }

        public Builder dateChooseListener(DateChooseListener dateChooseListener) {
            this.dateChooseListener = dateChooseListener;
            return this;
        }

        public Builder maxDate(Date maxDate) {
            this.maxDate = maxDate;
            return this;
        }

        public Builder minDate(Date minDate) {
            this.minDate = minDate;
            return this;
        }

        public int getMaxDay(int month, int year) {
            int maxDay = 30;
            switch (month) {
                case 2:
                    // 根据闰年来区别二月份的天数
                    if (isLeapYear(year)) {
                        maxDay = 29;
                    } else {
                        maxDay = 28;
                    }
                    break;
                case 4:
                    maxDay = 30;
                    break;
                case 6:
                    maxDay = 30;
                    break;
                case 9:
                    maxDay = 30;
                    break;
                case 11:
                    maxDay = 30;
                    break;
                default:
                    maxDay = 31;
                    break;
            }

            return maxDay;
        }

        boolean isLeapYear(int year) {
            return year % 4 == 0 && year % 100 != 0 || year % 100 == 0 && year % 400 == 0;
        }

        @Override
        public DatePickerDialog build() {
            DatePickerDialog dialog = new DatePickerDialog(context);
            dialog.themeColor = themeColor;
            dialog.dateChooseListener = dateChooseListener;

            Calendar calender = Calendar.getInstance();

            PickerData yearPk = new BaseDataPickerDialog.PickerData();
            yearPk.defaultValue = defaultYear;
            yearPk.unit = context.getResources().getString(R.string.year);
            yearPk.minValue = calender.get(Calendar.YEAR) - 80;
            yearPk.maxValue = calender.get(Calendar.YEAR);

            dialog.maxDate = maxDate;
            dialog.minDate = minDate;

            PickerData monthPk = new BaseDataPickerDialog.PickerData();
            monthPk.maxValue = 12;
            monthPk.defaultValue = defaultMonth;
            monthPk.minValue = 1;
            monthPk.unit = context.getResources().getString(R.string.month);

            PickerData dayPk = new BaseDataPickerDialog.PickerData();
            dayPk.maxValue = getMaxDay(defaultMonth,defaultYear);
            dayPk.defaultValue = defaultDay;
            dayPk.minValue = 1;
            dayPk.unit = context.getResources().getString(R.string.day);

            if (minDate != null) {
                calender.setTime(minDate);
                yearPk.minValue = calender.get(Calendar.YEAR);
                if (yearPk.defaultValue < yearPk.minValue) {
                    yearPk.defaultValue = yearPk.minValue;
                }

                if (yearPk.defaultValue == yearPk.minValue) {
                    monthPk.minValue = calender.get(Calendar.MONTH) + 1;
                    if (monthPk.defaultValue < monthPk.minValue) {
                        monthPk.defaultValue = monthPk.minValue;
                    }

                    if (monthPk.defaultValue == monthPk.minValue) {
                        dayPk.minValue = calender.get(Calendar.DATE);
                        if (dayPk.defaultValue < dayPk.minValue) {
                            dayPk.defaultValue = dayPk.minValue;
                        }
                    }
                }
            }

            if (maxDate != null) {
                calender.setTime(maxDate);
                yearPk.maxValue = calender.get(Calendar.YEAR);
                if (yearPk.defaultValue > yearPk.maxValue) {
                    yearPk.defaultValue = yearPk.maxValue;
                }

                if (yearPk.defaultValue == yearPk.maxValue) {
                    monthPk.maxValue = calender.get(Calendar.MONTH) + 1;
                    if (monthPk.defaultValue < monthPk.maxValue) {
                        monthPk.defaultValue = monthPk.maxValue;
                    }

                    if (monthPk.defaultValue == monthPk.maxValue) {
                        dayPk.maxValue = calender.get(Calendar.DATE);
                        if (dayPk.defaultValue < dayPk.maxValue) {
                            dayPk.defaultValue = dayPk.maxValue;
                        }
                    }
                }
            }
            dialog.init(yearPk, monthPk, dayPk);

            return dialog;
        }
    }

}

