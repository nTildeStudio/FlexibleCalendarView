package com.ntilde.flexiblecalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class FlexibleCalendarView extends View{

    private OnSelectedDateChangeListener mOnSelectedDateChangeListener;

    private static final int MONTH_VIEW=1;
    private static final int YEAR_VIEW=2;
    private static final int YEARS_VIEW=3;

    private int actualView=MONTH_VIEW;

    private Canvas c;

    private TreeMap<Long,FlexibleCalendarEvent> mEvents;
    private HashMap<Long,FlexibleCalendarRange> mEventAssociated;

    private Calendar cal, calDaysName;
    private int mWeekCount;
    private int mActualMonth;
    private String mMonthName, mYearName;

    private GestureDetector mDetector = new GestureDetector(FlexibleCalendarView.this.getContext(), new mListener());

    private boolean mHighlightToday; //TODO use it and highlight today when needed, we also need a style to hightlight today
    private boolean mDisplayMonthName;
    private boolean mDisplayDaysName;
    private int mDayBoxStrokeWidth;
    private int mFirstDayOfWeekReaded, mFirstDayOfWeek, mBlankDays;

    private Paint mBackground;
    private Paint mMonthText;
    private Paint mSelectedMonthText;
    private Paint mDaysNameText;
    private Paint mEnabledDayText;
    private Paint mEnabledSelectedDayText;
    private Paint mEnabledDayWeekendText;
    private Paint mEnabledDayBoxFill;
    private Paint mEnabledDayBoxStroke;
    private Paint mSelectedDayBoxFill;
    private Paint mSelectedDayBoxStroke;
    private Paint mSelectedDayBoxCircle;
    private Paint mPreselectedDayBoxCircle;
    private Paint mEventBoxFill;

    private int mBackgroundColor;
    private int mMonthTextColor;
    private int mMonthSelectedTextColor;
    private int mDayNameTextColor;
    private int mDayBoxBackgroundColor;
    private int mDayTextColor;
    private int mDayWeekendTextColor;
    private int mDayBoxBorderColor;
    private int mDaySelectedBoxBackgroundColor;
    private int mDaySelectedCircleBackgroundColor;
    private int mDayPreselectedCircleBackgroundColor;
    private int mDaySelectedTextColor;

    private boolean mHighlightWeekend;

    private Path triangleLeft, triangleRight;

    private int mDayWidth, mMonthWidth;
    private int mDayHeight, mMonthHeight;

    private int mSelectedDay=-1;
    private int mSelectedDayMulti1=-1;
    private int mSelectedDayMulti2=-1;
    private Date mSelectedDate=null;
    private Date mSelectedDateMulti1=null;
    private Date mSelectedDateMulti2=null;
    private int mPreselectedDay=-1;
    private int mPreselectedDayMulti1=-1;
    private int mPreselectedDayMulti2=-1;
    private boolean mPreselectedTitle =false;
    private boolean mPreselectedLeft=false;
    private boolean mPreselectedRight=false;

    private long multiuptime;
    private int multiuptemp2;
    private static final long multiupdifftime=1000;
    private boolean multiEnabled=false;

    public FlexibleCalendarView(Context context) {
        super(context);

        init(true);
    }

    public FlexibleCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlexibleCalendarView, 0, 0);

        try{
            mHighlightToday = a.getBoolean(R.styleable.FlexibleCalendarView_highlightToday, false);
            mDisplayMonthName = a.getBoolean(R.styleable.FlexibleCalendarView_displayMonthName, true);
            mDisplayDaysName = a.getBoolean(R.styleable.FlexibleCalendarView_displayDaysName, true);
            mFirstDayOfWeekReaded = a.getInt(R.styleable.FlexibleCalendarView_firstDayOfWeek, 1);
            multiEnabled = a.getBoolean(R.styleable.FlexibleCalendarView_multitouch, false);
            mDayBoxStrokeWidth=1;

            mBackgroundColor = a.getColor(R.styleable.FlexibleCalendarView_backgroundColor, Color.WHITE);
            mMonthTextColor = a.getColor(R.styleable.FlexibleCalendarView_monthTextColor, Color.BLACK);
            mDayNameTextColor = a.getColor(R.styleable.FlexibleCalendarView_dayNameTextColor, Color.GRAY);
            mMonthSelectedTextColor = a.getColor(R.styleable.FlexibleCalendarView_monthSelectedTextColor, mDayNameTextColor);
            mDayBoxBackgroundColor = a.getColor(R.styleable.FlexibleCalendarView_dayBoxBackgroundColor, Color.rgb(230,230,230));
            mDayTextColor = a.getColor(R.styleable.FlexibleCalendarView_dayTextColor, Color.BLACK);
            mDayWeekendTextColor = a.getColor(R.styleable.FlexibleCalendarView_dayWeekendTextColor, mDayTextColor);
            mDayBoxBorderColor = a.getColor(R.styleable.FlexibleCalendarView_dayBoxBorderColor, Color.WHITE);
            mDaySelectedBoxBackgroundColor = a.getColor(R.styleable.FlexibleCalendarView_daySelectedBoxBackgroundColor, Color.rgb(230, 230, 230));
            mDaySelectedCircleBackgroundColor = a.getColor(R.styleable.FlexibleCalendarView_daySelectedCircleBackgroundColor, Color.WHITE);
            mDayPreselectedCircleBackgroundColor = a.getColor(R.styleable.FlexibleCalendarView_dayPreselectedCircleBackgroundColor, Color.rgb(245, 245, 245));
            mDaySelectedTextColor = a.getColor(R.styleable.FlexibleCalendarView_daySelectedTextColor, mDayTextColor);

            mHighlightWeekend = a.getBoolean(R.styleable.FlexibleCalendarView_highlightWeekend, true);
        }
        finally {
            a.recycle();
        }

        init(true);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putSerializable("mEvents", mEvents);
        bundle.putSerializable("mEventAssociated", mEventAssociated);
        bundle.putSerializable("cal", cal);
        bundle.putInt("mSelectedDay", mSelectedDay);
        bundle.putInt("mSelectedDayMulti1", mSelectedDayMulti1);
        bundle.putInt("mSelectedDayMulti2", mSelectedDayMulti2);
        bundle.putSerializable("mSelectedDate", mSelectedDate);
        bundle.putSerializable("mSelectedDateMulti1", mSelectedDateMulti1);
        bundle.putSerializable("mSelectedDateMulti2", mSelectedDateMulti2);
        bundle.putBoolean("mDisplayDaysName", mDisplayDaysName);
        bundle.putBoolean("mDisplayMonthName", mDisplayMonthName);
        bundle.putInt("actualView", actualView);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mEvents = (TreeMap<Long,FlexibleCalendarEvent>)bundle.getSerializable("mEvents");
            mEventAssociated = (HashMap<Long, FlexibleCalendarRange>)bundle.getSerializable("mEventAssociated");
            cal = (Calendar)bundle.getSerializable("cal");
            mSelectedDay = bundle.getInt("mSelectedDay");
            mSelectedDayMulti1 = bundle.getInt("mSelectedDayMulti1");
            mSelectedDayMulti2 = bundle.getInt("mSelectedDayMulti2");
            mSelectedDate = (Date) bundle.getSerializable("mSelectedDate");
            mSelectedDateMulti1 = (Date) bundle.getSerializable("mSelectedDateMulti1");
            mSelectedDateMulti2 = (Date) bundle.getSerializable("mSelectedDateMulti2");
            mDisplayDaysName = bundle.getBoolean("mDisplayDaysName");
            mDisplayMonthName = bundle.getBoolean("mDisplayMonthName");
            actualView = bundle.getInt("actualView");
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
        init(true);
    }

    private  void init(boolean firstInit){
        if(firstInit) {

            if (mEvents == null){
                mEvents = new TreeMap<>();
            }
            if(mEventAssociated==null) {
                mEventAssociated = new HashMap<>();
            }

            switch (mFirstDayOfWeekReaded) {
                case 1:mFirstDayOfWeek=Calendar.MONDAY;break;
                case 2:mFirstDayOfWeek=Calendar.TUESDAY;break;
                case 3:mFirstDayOfWeek=Calendar.WEDNESDAY;break;
                case 4:mFirstDayOfWeek=Calendar.THURSDAY;break;
                case 5:mFirstDayOfWeek=Calendar.FRIDAY;break;
                case 6:mFirstDayOfWeek=Calendar.SATURDAY;break;
                case 7:mFirstDayOfWeek=Calendar.SUNDAY;break;
            }

            if(cal==null) {
                cal = Calendar.getInstance();
            }

            mWeekCount = cal.getActualMaximum(Calendar.WEEK_OF_MONTH);
            calDaysName = Calendar.getInstance();
            calDaysName.set(Calendar.DAY_OF_WEEK, mFirstDayOfWeek);

            mBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBackground.setStyle(Paint.Style.FILL);
            mBackground.setColor(mBackgroundColor);

            mMonthText = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMonthText.setColor(mMonthTextColor);

            mSelectedMonthText = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSelectedMonthText.setColor(mMonthSelectedTextColor);

            mDaysNameText = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDaysNameText.setColor(mDayNameTextColor);

            mEnabledDayText = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEnabledDayText.setColor(mDayTextColor);

            mEnabledSelectedDayText = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEnabledSelectedDayText.setColor(mDaySelectedTextColor);

            mEnabledDayWeekendText = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEnabledDayWeekendText.setColor(mDayWeekendTextColor);

            mEnabledDayBoxFill = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEnabledDayBoxFill.setStyle(Paint.Style.FILL);
            mEnabledDayBoxFill.setColor(mDayBoxBackgroundColor);

            mEnabledDayBoxStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEnabledDayBoxStroke.setStyle(Paint.Style.STROKE);
            mEnabledDayBoxStroke.setColor(mDayBoxBorderColor);
            mEnabledDayBoxStroke.setStrokeWidth(mDayBoxStrokeWidth);

            mSelectedDayBoxFill = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSelectedDayBoxFill.setStyle(Paint.Style.FILL);
            mSelectedDayBoxFill.setColor(mDaySelectedBoxBackgroundColor);

            mSelectedDayBoxStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSelectedDayBoxStroke.setStyle(Paint.Style.STROKE);
            mSelectedDayBoxStroke.setColor(Color.WHITE);
            mSelectedDayBoxStroke.setStrokeWidth(mDayBoxStrokeWidth);

            mSelectedDayBoxCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSelectedDayBoxCircle.setStyle(Paint.Style.FILL);
            mSelectedDayBoxCircle.setColor(mDaySelectedCircleBackgroundColor);

            mPreselectedDayBoxCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPreselectedDayBoxCircle.setStyle(Paint.Style.FILL);
            mPreselectedDayBoxCircle.setColor(mDayPreselectedCircleBackgroundColor);

            mEventBoxFill = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEventBoxFill.setStyle(Paint.Style.FILL);
        }
        mActualMonth = cal.get(Calendar.MONTH);
        String monthName = String.format(Locale.getDefault(),"%tB",cal);
        mMonthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);
        mYearName = cal.get(Calendar.YEAR)+"";
        cal.setFirstDayOfWeek(mFirstDayOfWeek);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void initEventsAssociated(){
        mEventAssociated = new HashMap<>();
        for(FlexibleCalendarEvent event:mEvents.values()) {
            ArrayList<FlexibleCalendarRange> eventAssociated = event.getRanges();
            if (eventAssociated != null) {
                for (FlexibleCalendarRange range : eventAssociated) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(event.getEvent().getDate());
                    c.add(Calendar.DAY_OF_YEAR, 1);
                    Calendar c2 = Calendar.getInstance();
                    c2.setTime(event.getEvent().getDate());
                    //noinspection ResourceType
                    c2.add(range.getUnit(), range.getRange());
                    if(c2.getTimeInMillis()-c.getTimeInMillis()>=24*60*60*1000){
                        do{
                            mEventAssociated.put(c.getTimeInMillis(), range);
                            c.add(Calendar.DAY_OF_YEAR, 1);
                        }while(c.getTimeInMillis()<=c2.getTimeInMillis());
                    }
                }
            }
        }
        invalidate();
    }

    private void drawCenter(Canvas canvas, Paint paint, String text, Rect area) {
        Rect r = new Rect();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = area.left + area.width() / 2f - r.width() / 2f - r.left;
        float y = area.top + area.height() / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        c=canvas;
        switch (actualView){
            case MONTH_VIEW:
                drawMonth(canvas);
                break;
            case YEAR_VIEW:
                drawYear(canvas);
                break;
            case YEARS_VIEW:
                break;
        }

    }

    private void drawYear(Canvas canvas){

        canvas.drawRect(canvas.getClipBounds(), mBackground);

        cal.set(Calendar.MONTH, 0);

        int left, top=0, right, bottom=mMonthHeight;

        if(mDisplayMonthName){
            Rect area=new Rect(0, top, canvas.getClipBounds().width(), mDayHeight);
            mMonthText.setTextSize(Math.min(mDayWidth, mDayHeight)/2.4f);
            mSelectedMonthText.setTextSize(Math.min(mDayWidth,mDayHeight) / 2.4f);
            drawCenter(canvas, mPreselectedTitle ? mSelectedMonthText : mMonthText, mYearName, area);
            top+=mDayHeight;
            bottom+=mMonthHeight;

            int diff = Math.abs(mDayWidth-mDayHeight);
            triangleLeft = new Path();
            triangleLeft.moveTo(mDayWidth * 0.3f + diff*0.3f, mDayHeight / 2);
            triangleLeft.lineTo(mDayWidth * 0.6f, mDayHeight * 0.3f);
            triangleLeft.lineTo(mDayWidth * 0.6f, mDayHeight * 0.7f);
            triangleLeft.close();
            triangleRight = new Path();
            triangleRight.moveTo(canvas.getClipBounds().width() - mDayWidth * 0.3f - diff * 0.3f, mDayHeight / 2);
            triangleRight.lineTo(canvas.getClipBounds().width() - mDayWidth * 0.6f, mDayHeight * 0.3f);
            triangleRight.lineTo(canvas.getClipBounds().width() - mDayWidth * 0.6f, mDayHeight * 0.7f);
            triangleRight.close();

            canvas.drawPath(triangleLeft, mPreselectedLeft?mSelectedMonthText:mMonthText);
            canvas.drawPath(triangleRight, mPreselectedRight?mSelectedMonthText:mMonthText);
        }

        for(int monthBlock=0;monthBlock<3;monthBlock++) {
            left = 0;
            right = mMonthWidth;
            for (int month = 0; month < 4; month++) {

                canvas.drawRect(left, top, right, bottom, mEnabledDayBoxFill);
                canvas.drawRect(left, top, right, bottom, mEnabledDayBoxStroke);

                Rect area=new Rect(mMonthWidth*month, top, mMonthWidth*(month+1), top+mDayHeight/2);
                String monthName=String.format(Locale.getDefault(), "%tB", cal).substring(0,3).toUpperCase();
                mEnabledDayText.setTextSize(Math.min(mDayWidth, mDayHeight)/4.2f);
                drawCenter(canvas, mEnabledDayText, monthName, area);

                cal.set(Calendar.DAY_OF_MONTH, 1);
                int actualMonth=cal.get(Calendar.MONTH);
                cal.getTime();
                cal.set(Calendar.DAY_OF_WEEK, mFirstDayOfWeek);
                int monthWeeks=cal.getActualMaximum(Calendar.WEEK_OF_MONTH);
                for(int week=0;week<=monthWeeks;week++){
                    for(int day=0;day<7;day++){
                        if(cal.get(Calendar.MONTH)==actualMonth) {
                            int l=left+mMonthWidth/7*day;
                            int t=top+mDayHeight/2+(mMonthHeight-mDayHeight/2)/6*week;
                            int r=(left+(mMonthWidth)/7*(day+1));
                            int b=top+mDayHeight/2+(mMonthHeight-mDayHeight/2)/6*(week+1);
                            Rect dayArea=new Rect(l, t, r, b);
                            mMonthText.setTextSize(Math.min(mDayWidth, mDayHeight)/6.7f);
                            mEnabledDayText.setTextSize(Math.min(mDayWidth, mDayHeight)/6.7f);
                            mEnabledDayWeekendText.setTextSize(Math.min(mDayWidth, mDayHeight)/6.7f);
                            if(mHighlightWeekend&&(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY)) {
                                drawCenter(canvas, mEnabledDayWeekendText, cal.get(Calendar.DAY_OF_MONTH) + "", dayArea);
                            }
                            else {
                                drawCenter(canvas, mEnabledDayText, cal.get(Calendar.DAY_OF_MONTH) + "", dayArea);
                            }
                            if(mEvents.keySet().contains(cal.getTimeInMillis())){
                                FlexibleCalendarEvent event=mEvents.get(cal.getTimeInMillis());
                                mEventBoxFill.setColor(event.getEvent().getColor());
                                canvas.drawRect(l, t + (int) ((mMonthHeight - mDayHeight / 2) / 6 * 0.90), r, b, mEventBoxFill);
                            }
                            else if(mEventAssociated.keySet().contains(cal.getTimeInMillis())){
                                mEventBoxFill.setColor(mEventAssociated.get(cal.getTimeInMillis()).getColor());
                                canvas.drawRect(l, t + (int) ((mMonthHeight-mDayHeight/2)/6 * 0.90), r, b, mEventBoxFill);
                            }
                        }
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }

                left+=mMonthWidth+mDayBoxStrokeWidth;
                right+=mMonthWidth+mDayBoxStrokeWidth;
            }
            top+=mMonthHeight+mDayBoxStrokeWidth;
            bottom+=mMonthHeight+mDayBoxStrokeWidth;
        }
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.MONTH, 0);
    }

    private void drawMonth(Canvas canvas){

        canvas.drawRect(canvas.getClipBounds(), mBackground);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.getTime();
        cal.set(Calendar.DAY_OF_WEEK, mFirstDayOfWeek);

        int left, top=0, right, bottom=mDayHeight;

        if(mDisplayMonthName){
            Rect area=new Rect(0, top, canvas.getClipBounds().width(), mDayHeight);
            mMonthText.setTextSize(Math.min(mDayWidth,mDayHeight)/2.4f);
            mSelectedMonthText.setTextSize(Math.min(mDayWidth,mDayHeight / 2.4f));
            drawCenter(canvas, mPreselectedTitle ? mSelectedMonthText : mMonthText, mMonthName, area);
            top+=mDayHeight;
            bottom+=mDayHeight;

            int diff = Math.abs(mDayWidth-mDayHeight);
            triangleLeft = new Path();
            triangleLeft.moveTo(mDayWidth * 0.3f + diff*0.3f, mDayHeight / 2);
            triangleLeft.lineTo(mDayWidth * 0.6f, mDayHeight * 0.3f);
            triangleLeft.lineTo(mDayWidth * 0.6f, mDayHeight * 0.7f);
            triangleLeft.close();
            triangleRight = new Path();
            triangleRight.moveTo(canvas.getClipBounds().width() - mDayWidth * 0.3f - diff*0.3f, mDayHeight / 2);
            triangleRight.lineTo(canvas.getClipBounds().width() - mDayWidth * 0.6f, mDayHeight * 0.3f);
            triangleRight.lineTo(canvas.getClipBounds().width() - mDayWidth * 0.6f, mDayHeight * 0.7f);
            triangleRight.close();

            canvas.drawPath(triangleLeft, mPreselectedLeft?mSelectedMonthText:mMonthText);
            canvas.drawPath(triangleRight, mPreselectedRight?mSelectedMonthText:mMonthText);
        }

        if(mDisplayDaysName){
            Rect area=new Rect(0, top, mDayWidth+mDayBoxStrokeWidth,top+mDayHeight/2);
            for(int dia=0;dia<7;dia++){
                String titulo=String.format(Locale.getDefault(),"%ta",calDaysName).substring(0,2);
                titulo=Character.toUpperCase(titulo.charAt(0)) + titulo.substring(1);
                mDaysNameText.setTextSize(Math.min(mDayWidth, mDayHeight)/3.14f);
                drawCenter(canvas, mDaysNameText, titulo, area);
                area.left+=mDayWidth+mDayBoxStrokeWidth;
                area.right+=mDayWidth+mDayBoxStrokeWidth;
                calDaysName.add(Calendar.DAY_OF_MONTH, 1);
            }
            top+=mDayHeight/2;
            bottom+=mDayHeight/2;
        }

        int radius = Math.min(mDayWidth,mDayHeight) / 2;
        int margin=radius-(int)(radius*.8);
        mEnabledDayText.setTextSize(Math.min(mDayWidth,mDayHeight)/2.8f);
        mEnabledDayWeekendText.setTextSize(Math.min(mDayWidth,mDayHeight)/2.8f);
        mEnabledSelectedDayText.setTextSize(Math.min(mDayWidth,mDayHeight)/2.8f);
        mBlankDays=0;
        boolean multiPre=false;
        boolean multiSel=false;
        boolean selectedDate=false;
        for(int week=0;week<=mWeekCount;week++){
            left=0;
            right=mDayWidth;
            for(int day=0;day<7;day++){
                if(cal.get(Calendar.MONTH)==mActualMonth) {
                    if (mSelectedDay != -1 && week * 7 + day == mSelectedDay) {
                        selectedDate=true;
                        mSelectedDate=cal.getTime();
                        canvas.drawRect(left, top, right, bottom, mSelectedDayBoxFill);
                        canvas.drawRect(left, top, right, bottom, mSelectedDayBoxStroke);
                        canvas.drawCircle(left + mDayWidth/2, top + mDayHeight/2, (int) (radius * .8), mSelectedDayBoxCircle);
                        if(mSelectedDay>mPreselectedDayMulti1 && mSelectedDay<mPreselectedDayMulti2){
                            canvas.drawRect(left, top+margin, right, bottom-margin, mSelectedDayBoxCircle);
                        }
                        else if(mSelectedDay==mPreselectedDayMulti1&&mSelectedDay!=mPreselectedDayMulti2){
                            canvas.drawRect(left+mDayWidth/2, top+margin, right, bottom-margin, mSelectedDayBoxCircle);
                        }
                        else if(mSelectedDay!=mPreselectedDayMulti1&&mSelectedDay==mPreselectedDayMulti2){
                            canvas.drawRect(left, top+margin, right-mDayWidth/2, bottom-margin, mSelectedDayBoxCircle);
                        }
                        if (mPreselectedDayMulti1 != -1 && week * 7 + day == mPreselectedDayMulti1) {
                            mSelectedDateMulti1 = cal.getTime();
                            multiPre = true;
                        }
                        else if (mPreselectedDayMulti2 != -1 && week * 7 + day == mPreselectedDayMulti2) {
                            mSelectedDateMulti2 = cal.getTime();
                            multiPre = false;
                        }
                    }
                    else if (!multiSel) {
                        canvas.drawRect(left, top, right, bottom, mEnabledDayBoxFill);
                        canvas.drawRect(left, top, right, bottom, mEnabledDayBoxStroke);
                        if (mPreselectedDay != -1 && week * 7 + day == mPreselectedDay) {
                            selectedDate=true;
                            canvas.drawCircle(left + mDayWidth / 2, top + mDayHeight / 2, (int) (radius * .8), mPreselectedDayBoxCircle);
                        }
                        if (mPreselectedDayMulti1 != -1 && week * 7 + day == mPreselectedDayMulti1) {
                            selectedDate=true;
                            canvas.drawRect(left+mDayWidth/2, top+margin, right, bottom-margin, mPreselectedDayBoxCircle);
                            canvas.drawCircle(left + mDayWidth / 2, top + mDayHeight / 2, (int) (radius * .8), mPreselectedDayBoxCircle);
                            multiPre = true;
                        }
                        else if (mPreselectedDayMulti2 != -1 && week * 7 + day == mPreselectedDayMulti2) {
                            selectedDate=true;
                            canvas.drawRect(left, top+margin, right-mDayWidth/2, bottom-margin, mPreselectedDayBoxCircle);
                            canvas.drawCircle(left + mDayWidth / 2, top + mDayHeight / 2, (int) (radius * .8), mPreselectedDayBoxCircle);
                            multiPre = false;
                        }
                        else if (multiPre) {
                            selectedDate=true;
                            canvas.drawRect(left, top+margin, right, bottom-margin, mPreselectedDayBoxCircle);
                        }
                    }
                    if(multiSel){
                        selectedDate=true;
                        canvas.drawRect(left, top, right, bottom, mSelectedDayBoxFill);
                        canvas.drawRect(left, top, right, bottom, mSelectedDayBoxStroke);
                        canvas.drawRect(left, top+margin, right, bottom-margin, mSelectedDayBoxCircle);
                        if (mPreselectedDayMulti1 != -1 && week * 7 + day == mPreselectedDayMulti1) {
                            multiPre = true;
                        }
                        else if (mPreselectedDayMulti2 != -1 && week * 7 + day == mPreselectedDayMulti2) {
                            multiPre = false;
                        }
                    }
                    if(mSelectedDayMulti1 != -1 && week * 7 + day == mSelectedDayMulti1){
                        selectedDate=true;
                        canvas.drawRect(left, top, right, bottom, mSelectedDayBoxFill);
                        canvas.drawRect(left, top, right, bottom, mSelectedDayBoxStroke);
                        canvas.drawRect(left+mDayWidth/2, top+margin, right, bottom-margin, mSelectedDayBoxCircle);
                        if(mSelectedDayMulti1==mPreselectedDayMulti2){
                            canvas.drawRect(left, top+margin, right-mDayWidth/2, bottom-margin, mSelectedDayBoxCircle);
                        }
                        else if(mSelectedDayMulti1>mPreselectedDayMulti1 && mSelectedDayMulti1<mPreselectedDayMulti2){
                            canvas.drawRect(left, top+margin, right, bottom-margin, mSelectedDayBoxCircle);
                        }
                        canvas.drawCircle(left + mDayWidth/2, top + mDayHeight/2, (int) (radius * .8), mSelectedDayBoxCircle);
                        multiSel=true;
                    }
                    if(mSelectedDayMulti2 != -1 && week * 7 + day == mSelectedDayMulti2){
                        canvas.drawRect(left, top, right, bottom, mSelectedDayBoxFill);
                        canvas.drawRect(left, top, right, bottom, mSelectedDayBoxStroke);
                        canvas.drawRect(left, top+margin, right-mDayWidth/2, bottom-margin, mSelectedDayBoxCircle);
                        if(mSelectedDayMulti2==mPreselectedDayMulti1){
                            canvas.drawRect(left+mDayWidth/2, top+margin, right, bottom-margin, mSelectedDayBoxCircle);
                        }
                        else if(mSelectedDayMulti2>mPreselectedDayMulti1 && mSelectedDayMulti2<mPreselectedDayMulti2){
                            canvas.drawRect(left, top+margin, right, bottom-margin, mSelectedDayBoxCircle);
                        }
                        canvas.drawCircle(left + mDayWidth/2, top + mDayHeight/2, (int) (radius * .8), mSelectedDayBoxCircle);
                        multiSel=false;
                    }
                    if(selectedDate){
                        drawCenter(canvas, mEnabledSelectedDayText, "" + cal.get(Calendar.DAY_OF_MONTH), new Rect(left, top, right, bottom));
                        selectedDate=false;
                    }
                    else {
                        if(mHighlightWeekend&&(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY)) {
                            drawCenter(canvas, mEnabledDayWeekendText, "" + cal.get(Calendar.DAY_OF_MONTH), new Rect(left, top, right, bottom));
                        }
                        else {
                            drawCenter(canvas, mEnabledDayText, "" + cal.get(Calendar.DAY_OF_MONTH), new Rect(left, top, right, bottom));
                        }
                    }
                    if(mEvents.keySet().contains(cal.getTimeInMillis())){
                        FlexibleCalendarEvent event=mEvents.get(cal.getTimeInMillis());
                        mEventBoxFill.setColor(event.getEvent().getColor());
                        canvas.drawRect(left, top+(int)(mDayHeight*0.95), right, bottom, mEventBoxFill);
                    }
                    else if(mEventAssociated.keySet().contains(cal.getTimeInMillis())){
                        mEventBoxFill.setColor(mEventAssociated.get(cal.getTimeInMillis()).getColor());
                        canvas.drawRect(left, top + (int) (mDayHeight * 0.95), right, bottom, mEventBoxFill);
                    }
                }
                else if(week==0){
                    mBlankDays++;
                }
                left+=mDayWidth+mDayBoxStrokeWidth;
                right+=mDayWidth+mDayBoxStrokeWidth;
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            top+=mDayHeight+mDayBoxStrokeWidth;
            bottom+=mDayHeight+mDayBoxStrokeWidth;
        }
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mDetector.onTouchEvent(event);
        if (!result){
            if(actualView==MONTH_VIEW) {
                mSelectedDate=null;
                mSelectedDateMulti1=null;
                mSelectedDateMulti2=null;
                if(multiEnabled && event.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
                    if(event.getPointerCount()==2) {
                        MotionEvent.PointerCoords point2 = new MotionEvent.PointerCoords();
                        event.getPointerCoords(event.getActionIndex(), point2);
                        multiuptime = System.currentTimeMillis();
                        multiuptemp2 = touchedDay(point2.x, point2.y);
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(multiEnabled && multiuptemp2!=-1 && System.currentTimeMillis()-multiuptime<=multiupdifftime){
                        int selectedDay1=touchedDay(event.getX(), event.getY());
                        int selectedDay2=multiuptemp2;
                        if(selectedDay1!=-1&&selectedDay2!=-1) {
                            mSelectedDayMulti1=Math.min(selectedDay1, selectedDay2);
                            mSelectedDayMulti2=Math.max(selectedDay1, selectedDay2);
                            mSelectedDay = -1;
                        }
                        else{
                            mSelectedDayMulti1=-1;
                            mSelectedDayMulti2=-1;
                        }
                        mPreselectedDayMulti1 = -1;
                        mPreselectedDayMulti2 = -2;
                        mPreselectedTitle = false;
                        mPreselectedLeft = false;
                        mPreselectedRight = false;

                        if(mSelectedDayMulti1!=-1&&mSelectedDayMulti2!=-1){
                            if (mOnSelectedDateChangeListener != null) {
                                Calendar c1 = (Calendar) cal.clone();
                                c1.set(Calendar.DAY_OF_MONTH, mSelectedDayMulti1 - mBlankDays + 1);
                                Calendar ci = (Calendar) cal.clone();
                                ci.set(Calendar.DAY_OF_MONTH, mSelectedDayMulti1 - mBlankDays + 1);
                                Calendar c2 = (Calendar) cal.clone();
                                c2.set(Calendar.DAY_OF_MONTH, mSelectedDayMulti2 - mBlankDays + 1);
                                ArrayList<FlexibleCalendarEvent> events = new ArrayList<>();
                                ArrayList<FlexibleCalendarRange> ranges = new ArrayList<>();
                                do{
                                    events.add(getEvent(ci.getTime()));
                                    ranges.add(mEventAssociated.get(ci.getTimeInMillis()));
                                    ci.add(Calendar.DAY_OF_YEAR, 1);
                                }while(ci.getTimeInMillis()<=c2.getTimeInMillis());
                                mOnSelectedDateChangeListener.OnSelectedDateChange(
                                        c1.getTime(),
                                        c2.getTime(),
                                        events,
                                        ranges
                                );
                            }
                        }
                    }
                    else {
                        int selectedDay = touchedDay(event.getX(), event.getY());
                        if (selectedDay != -1) {
                            mSelectedDay = mSelectedDay == selectedDay ? -1 : selectedDay;
                            if (mSelectedDay != -1) {
                                if (mOnSelectedDateChangeListener != null) {
                                    Calendar calClone = (Calendar) cal.clone();
                                    calClone.set(Calendar.DAY_OF_MONTH, mSelectedDay - mBlankDays + 1);
                                    ArrayList<FlexibleCalendarEvent> events = new ArrayList<>();
                                    events.add(getEvent(calClone.getTime()));
                                    ArrayList<FlexibleCalendarRange> ranges = new ArrayList<>();
                                    ranges.add(mEventAssociated.get(calClone.getTimeInMillis()));
                                    mOnSelectedDateChangeListener.OnSelectedDateChange(
                                            calClone.getTime(),
                                            calClone.getTime(),
                                            events,
                                            ranges
                                    );
                                }
                            } else {
                                if (mOnSelectedDateChangeListener != null) {
                                    mOnSelectedDateChangeListener.OnSelectedDateChange(null, null, null, null);
                                }
                            }
                        } else if (touchedTitle(event.getX(), event.getY())) {
                            actualView = YEAR_VIEW;
                            init(false);
                        } else if (touchedLeft(event.getX(), event.getY())) {
                            cal.add(Calendar.MONTH, -1);
                            mSelectedDay = -1;
                            mSelectedDate = null;
                            mSelectedDateMulti1 = null;
                            mSelectedDateMulti2 = null;
                            init(false);
                        } else if (touchedRight(event.getX(), event.getY())) {
                            cal.add(Calendar.MONTH, 1);
                            mSelectedDay = -1;
                            mSelectedDate = null;
                            mSelectedDateMulti1 = null;
                            mSelectedDateMulti2 = null;
                            init(false);
                        }
                        mPreselectedDay = -1;
                        mPreselectedDayMulti1 = -1;
                        mPreselectedDayMulti2 = -2;
                        mPreselectedTitle = false;
                        mPreselectedLeft = false;
                        mPreselectedRight = false;
                        mSelectedDayMulti1 = -1;
                        mSelectedDayMulti2 = -2;
                    }
                    multiuptime=-1;
                    multiuptemp2 = -1;
                    invalidate();
                    result = true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if(multiEnabled && event.getPointerCount()==2){
                        MotionEvent.PointerCoords point1=new MotionEvent.PointerCoords(), point2=new MotionEvent.PointerCoords();
                        event.getPointerCoords(0, point1);
                        event.getPointerCoords(1, point2);
                        int selectedDay1=touchedDay(point1.x, point1.y);
                        int selectedDay2=touchedDay(point2.x, point2.y);
                        if(selectedDay1!=-1&&selectedDay2!=-1) {
                            mPreselectedDayMulti1=Math.min(selectedDay1, selectedDay2);
                            mPreselectedDayMulti2=Math.max(selectedDay1, selectedDay2);
                        }
                        else{
                            mPreselectedDayMulti1=-1;
                            mPreselectedDayMulti2=-1;
                        }
                        mPreselectedDay = -1;
                        mPreselectedTitle = false;
                        mPreselectedLeft = false;
                        mPreselectedRight = false;
                    }
                    else {
                        mPreselectedDay = touchedDay(event.getX(), event.getY());
                        mPreselectedDayMulti1=-1;
                        mPreselectedDayMulti2=-1;
                        mPreselectedTitle = touchedTitle(event.getX(), event.getY());
                        mPreselectedLeft = touchedLeft(event.getX(), event.getY());
                        mPreselectedRight = touchedRight(event.getX(), event.getY());
                    }
                    invalidate();
                }
            }
            else if(actualView==YEAR_VIEW){
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int selectedMonth = touchedMonth(event.getX(), event.getY());
                    if (selectedMonth != -1) {
                        cal.set(Calendar.MONTH, selectedMonth);
                        actualView=MONTH_VIEW;
                        mSelectedDay=-1;
                        mSelectedDate=null;
                        mSelectedDateMulti1 = null;
                        mSelectedDateMulti2 = null;
                        init(false);
                    } else if (touchedTitle(event.getX(), event.getY())) {

                    } else if (touchedLeft(event.getX(), event.getY())) {
                        cal.add(Calendar.YEAR, -1);
                        init(false);
                    } else if (touchedRight(event.getX(), event.getY())) {
                        cal.add(Calendar.YEAR, 1);
                        init(false);
                    }
                    mPreselectedDay = -1;
                    mPreselectedTitle = false;
                    mPreselectedLeft = false;
                    mPreselectedRight = false;
                    invalidate();
                    result = true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    mPreselectedDay = touchedDay(event.getX(), event.getY());
                    mPreselectedTitle = touchedTitle(event.getX(), event.getY());
                    mPreselectedLeft = touchedLeft(event.getX(), event.getY());
                    mPreselectedRight = touchedRight(event.getX(), event.getY());
                    invalidate();
                }
            }
        }
        return result;
    }

    private int touchedDay(float x, float y){
        int day=-1;
        if(mDisplayMonthName&&y<=mDayHeight||
                !mDisplayMonthName&&mDisplayDaysName&&y<=mDayHeight/2||
                mDisplayMonthName&&mDisplayDaysName&&y<=mDayHeight*1.5){
            return day;
        }
        if(mDisplayMonthName){
            y-=mDayHeight;
        }
        if(mDisplayDaysName){
            y-=mDayHeight/2;
        }
        day=(int)(y/(mDayHeight+mDayBoxStrokeWidth*2))*7;
        day+=(int)(x/(mDayWidth+mDayBoxStrokeWidth*2));
        return day;
    }

    private int touchedMonth(float x, float y) {
        int month=-1;

        if(y<=mDayHeight){
            return month;
        }
        y-=mDayHeight;
        month=(int)(y/(mMonthHeight+mDayBoxStrokeWidth*2))*4;
        month+=(int)(x/(mMonthWidth+mDayBoxStrokeWidth*2));
        return month;
    }

    private boolean touchedLeft(float x, float y){
        return x<=mDayWidth&&y<=mDayHeight;
    }

    private boolean touchedRight(float x, float y){
        return x>=c.getClipBounds().width()-mDayWidth&&y<=mDayHeight;
    }

    private boolean touchedTitle(float x, float y){
        return actualView==MONTH_VIEW&&x>=mDayWidth&&x<=c.getClipBounds().width()-mDayWidth&&y<=mDayHeight;
    }

    class mListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mDayWidth=w/7;
        mDayHeight=(h-(mDisplayMonthName?mDayWidth:0)-(mDisplayDaysName?mDayWidth/2:0))/6;
        mMonthWidth=w/4-mDayBoxStrokeWidth;
        mMonthHeight=(h-mDayHeight)/3-mDayBoxStrokeWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float  heightInWeeks=6;
        heightInWeeks+=mDisplayMonthName?1:0;
        heightInWeeks+=mDisplayDaysName?0.5:0;

        int desiredWidth = (int)((heightSize+mDayBoxStrokeWidth+1)/heightInWeeks*7);
        int desiredHeight = (int)((widthSize+mDayBoxStrokeWidth+1)/7*heightInWeeks)+mDayBoxStrokeWidth*5;

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    //GETTERS AND SETTERS

    public int getFirstDayOfWeek(){
        return mFirstDayOfWeek;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek){
        mFirstDayOfWeekReaded=firstDayOfWeek==1?7:firstDayOfWeek-1;
        init(true);
        invalidate();
        requestLayout();
    }

    public void setMultitouch(boolean multitouch){
        multiEnabled = multitouch;
    }

    public boolean isMultitouch(){
        return multiEnabled;
    }

    public boolean isSelectedDate(){
        return mSelectedDay!=-1;
    }

    public boolean isSelectedRange(){
        return mSelectedDayMulti1!=-1&&mSelectedDayMulti2!=-1;
    }

    public void setOnSelectedDateChangeListener(OnSelectedDateChangeListener onSelectedDateChangeListener){
        mOnSelectedDateChangeListener = onSelectedDateChangeListener;
    }

    public List<FlexibleCalendarEvent> getSelectedEvent(){
        List<FlexibleCalendarEvent> events = events = new ArrayList<>();;
        if(mSelectedDate!=null) {
            if(mEvents.containsKey(mSelectedDate.getTime())){
                events.add(mEvents.get(mSelectedDate.getTime()));
            }
        }
        else if(mSelectedDayMulti1!=-1 && mSelectedDayMulti2!=-1) {
            Calendar c1 = (Calendar) cal.clone();
            c1.set(Calendar.DAY_OF_MONTH, mSelectedDayMulti1 - mBlankDays + 1);
            Calendar ci = (Calendar) cal.clone();
            ci.set(Calendar.DAY_OF_MONTH, mSelectedDayMulti1 - mBlankDays + 1);
            Calendar c2 = (Calendar) cal.clone();
            c2.set(Calendar.DAY_OF_MONTH, mSelectedDayMulti2 - mBlankDays + 1);
            do{
                if(getEvent(ci.getTime())!=null) {
                    events.add(getEvent(ci.getTime()));
                }
                ci.add(Calendar.DAY_OF_YEAR, 1);
            }while(ci.getTimeInMillis()<=c2.getTimeInMillis());
        }
        return events;
    }

    public FlexibleCalendarEvent getEvent(Date date){
        if(date!=null) {
            return mEvents.get(date.getTime());
        }
        else{
            return null;
        }
    }

    public boolean isHighlightToday(){
        return mHighlightToday;
    }

    public void setHighlightToday(boolean highlightToday){
        mHighlightToday = highlightToday;
        invalidate();
        requestLayout();
    }

    public boolean isDisplayMonthName() {
        return mDisplayMonthName;
    }

    public void setDisplayMonthName(boolean displayMonthName){
        mDisplayMonthName = displayMonthName;
        invalidate();
        requestLayout();
    }

    public boolean isDisplayDaysName() {
        return mDisplayDaysName;
    }

    public void setDisplayDaysName(boolean displayDaysName){
        mDisplayDaysName = displayDaysName;
        invalidate();
        requestLayout();
    }

    public Date getSelectedDate(){
        return mSelectedDate;
    }

    public Date[] getSelectedRange(){
        Calendar c1 = (Calendar) cal.clone();
        c1.set(Calendar.DAY_OF_MONTH, mSelectedDayMulti1 - mBlankDays + 1);
        Calendar c2 = (Calendar) cal.clone();
        c2.set(Calendar.DAY_OF_MONTH, mSelectedDayMulti2 - mBlankDays + 1);
        return new Date[]{c1.getTime(), c2.getTime()};
    }

    public Map<Long,FlexibleCalendarEvent> getEvents(){
        return mEvents;
    }

    public void setEvents(TreeMap<Long,FlexibleCalendarEvent> events){
        mEvents=events;
        initEventsAssociated();
    }

    public void addEvent(FlexibleCalendarEvent event){
        if(mEvents==null){
            mEvents = new TreeMap<>();
        }
        if(event.getEvent().getDate()!=null) {
            Calendar c1=Calendar.getInstance();
            c1.setTimeInMillis(event.getEvent().getDate().getTime());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Calendar c2=Calendar.getInstance();
            c2.setTimeInMillis(c1.getTimeInMillis());
            //noinspection ResourceType
            c2.add(event.getEvent().getUnit(), event.getEvent().getRange());
            if(c2.getTimeInMillis()-c1.getTimeInMillis()>=24*60*60*1000){
                do{
                    mEvents.put(c1.getTimeInMillis(), event);
                    c1.add(Calendar.DAY_OF_YEAR, 1);
                }while(c1.getTimeInMillis()<c2.getTimeInMillis());
            }
            initEventsAssociated();
        }
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        init(true);
        invalidate();
    }

    public void setMonthTextColor(int monthTextColor) {
        mMonthTextColor = monthTextColor;
        init(true);
        invalidate();
    }

    public void setMonthSelectedTextColor(int monthSelectedTextColor) {
        mMonthSelectedTextColor = monthSelectedTextColor;
        init(true);
        invalidate();
    }

    public void setDayNameTextColor(int dayNameTextColor) {
        mDayNameTextColor = dayNameTextColor;
        init(true);
        invalidate();
    }

    public void setDayBoxBackgroundColor(int dayBoxBackgroundColor) {
        mDayBoxBackgroundColor = dayBoxBackgroundColor;
        init(true);
        invalidate();
    }

    public void setDayTextColor(int dayTextColor) {
        mDayTextColor = dayTextColor;
        init(true);
        invalidate();
    }

    public void setDayWeekendTextColor(int dayWeekendTextColor) {
        mDayWeekendTextColor = dayWeekendTextColor;
        init(true);
        invalidate();
    }

    public void setDayBoxBorderColor(int dayBoxBorderColor) {
        mDayBoxBorderColor = dayBoxBorderColor;
        init(true);
        invalidate();
    }

    public void setDaySelectedBoxBackgroundColor(int daySelectedBoxBackgroundColor) {
        mDaySelectedBoxBackgroundColor = daySelectedBoxBackgroundColor;
        init(true);
        invalidate();
    }

    public void setDaySelectedCircleBackgroundColor(int daySelectedCircleBackgroundColor) {
        mDaySelectedCircleBackgroundColor = daySelectedCircleBackgroundColor;
        init(true);
        invalidate();
    }

    public void setDayPreselectedCircleBackgroundColor(int dayPreselectedCircleBackgroundColor) {
        mDayPreselectedCircleBackgroundColor = dayPreselectedCircleBackgroundColor;
        init(true);
        invalidate();
    }

    public void setDaySelectedTextColor(int daySelectedTextColor) {
        mDaySelectedTextColor = daySelectedTextColor;
        init(true);
        invalidate();
    }

    public void setHighlightWeekend(boolean highlightWeekend) {
        mHighlightWeekend = highlightWeekend;
        init(true);
        invalidate();
    }

    public boolean isHighlightWeekend(){
        return mHighlightWeekend;
    }

    public interface OnSelectedDateChangeListener{
        void OnSelectedDateChange(Date selectedDateStart, Date selectedDateEnd, List<FlexibleCalendarEvent> events, List<FlexibleCalendarRange> ranges);
    }
}
