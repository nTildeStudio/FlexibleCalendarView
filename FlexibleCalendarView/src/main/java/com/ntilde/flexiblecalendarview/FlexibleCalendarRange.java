package com.ntilde.flexiblecalendarview;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class FlexibleCalendarRange implements Serializable{

    public enum UNITS{
        DAYS, MONTHS
    }

    private Date mDate;
    private int mRange;
    private int mUnit;
    private int mColor;
    private String mMessage;

    public FlexibleCalendarRange(int range, UNITS unit, int color, String message) {
        mRange = range;
        switch (unit){
            case DAYS:
                mUnit = Calendar.DAY_OF_YEAR;
                break;
            case MONTHS:
                mUnit = Calendar.MONTH;
                break;
        }
        mColor = color;
        mMessage = message;
    }

    public FlexibleCalendarRange(Date date, int range, UNITS unit, int color) {
        mDate = date;
        mRange = range;
        switch (unit){
            case DAYS:
                mUnit = Calendar.DAY_OF_YEAR;
                break;
            case MONTHS:
                mUnit = Calendar.MONTH;
                break;
        }
        mColor = color;
    }

    public Date getDate() {
        return mDate;
    }

    public int getRange() {
        return mRange;
    }

    public int getUnit() {
        return mUnit;
    }

    public int getColor() {
        return mColor;
    }

    public String getMessage() {
        return mMessage;
    }
}
