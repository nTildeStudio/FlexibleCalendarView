package com.ntilde.flexiblecalendarview;

import java.io.Serializable;
import java.util.ArrayList;

public class FlexibleCalendarEvent implements Serializable{

    private Object mEventInfo;
    private FlexibleCalendarRange mEvent;
    private ArrayList<FlexibleCalendarRange> mRange;

    public FlexibleCalendarEvent(Object eventInfo, FlexibleCalendarRange event){
        this(eventInfo, event, new FlexibleCalendarRange[]{});
    }

    public FlexibleCalendarEvent(Object eventInfo, FlexibleCalendarRange event, FlexibleCalendarRange... ranges){
        mEventInfo = eventInfo;
        mEvent = event;
        mRange=new ArrayList<>();
        if(ranges != null) {
            for (FlexibleCalendarRange range : ranges) {
                mRange.add(range);
            }
        }
    }

    public FlexibleCalendarRange getEvent() {
        return mEvent;
    }

    public void setEvent(FlexibleCalendarRange event) {
        mEvent = event;
    }

    public ArrayList<FlexibleCalendarRange> getRanges() {
        return mRange;
    }

    public void setRanges(ArrayList<FlexibleCalendarRange> ranges) {
        mRange = ranges;
    }

    public void addRange(FlexibleCalendarRange range){
        if(mRange ==null){
            mRange =new ArrayList<>();
        }
        mRange.add(range);
    }

    public Object getEventInfo() {
        return mEventInfo;
    }

    public void setEventInfo(Object eventInfo) {
        mEventInfo = eventInfo;
    }
}
