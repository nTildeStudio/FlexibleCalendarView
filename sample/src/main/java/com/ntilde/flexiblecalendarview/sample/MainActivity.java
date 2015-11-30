package com.ntilde.flexiblecalendarview.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.ntilde.flexiblecalendarview.FlexibleCalendarEvent;
import com.ntilde.flexiblecalendarview.FlexibleCalendarRange;
import com.ntilde.flexiblecalendarview.FlexibleCalendarView;
import com.ntilde.flexiblecalendarviewsample.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FlexibleCalendarView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        calendar = (FlexibleCalendarView) findViewById(R.id.calendar);

        findViewById(R.id.addEvent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(calendar.isSelectedDate()) {
                    Date selectedDate = calendar.getSelectedDate();
                    FlexibleCalendarRange evento = new FlexibleCalendarRange(selectedDate, 1, FlexibleCalendarRange.UNITS.DAYS, Color.rgb(0, 128, 0));
                    FlexibleCalendarRange rangoRojo = new FlexibleCalendarRange(14, FlexibleCalendarRange.UNITS.DAYS, Color.rgb(212, 0, 0), "No puedes donar nada");
                    FlexibleCalendarRange rangoNaranja = new FlexibleCalendarRange(1, FlexibleCalendarRange.UNITS.MONTHS, Color.rgb(255, 221, 85), "No puedes donar sangre");
                    calendar.addEvent(new FlexibleCalendarEvent("Sangre", evento, rangoNaranja, rangoRojo));
                }
                else if(calendar.isSelectedRange()){
                    Date[] selectedRange = calendar.getSelectedRange();
                    int days = (int)(Math.abs(selectedRange[0].getTime()-selectedRange[1].getTime())/(24*60*60*1000))+1;
                    Log.e("XXX", "Dias: "+days);
                    FlexibleCalendarRange evento = new FlexibleCalendarRange(selectedRange[0], days, FlexibleCalendarRange.UNITS.DAYS, Color.rgb(0, 128, 0));
                    calendar.addEvent(new FlexibleCalendarEvent("Sangre", evento));
                }
            }
        });
        ((CheckBox)findViewById(R.id.toggleMonthName)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calendar.setDisplayMonthName(isChecked);
            }
        });
        ((CheckBox)findViewById(R.id.toggleDaysName)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calendar.setDisplayDaysName(isChecked);
            }
        });
        ((CheckBox)findViewById(R.id.weekends)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calendar.setHighlightWeekend(isChecked);
            }
        });
        ((CheckBox)findViewById(R.id.multitouch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calendar.setMultitouch(isChecked);
            }
        });
        calendar.setOnSelectedDateChangeListener(new FlexibleCalendarView.OnSelectedDateChangeListener() {
            @Override
            public void OnSelectedDateChange(Date selectedDateStart, Date selectedDateEnd, List<FlexibleCalendarEvent> events, List<FlexibleCalendarRange> ranges) {
                SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
                if(selectedDateStart==null){
                    Log.e("XXX", "Nada seleccionado");
                }
                else if(selectedDateStart.getTime()==selectedDateEnd.getTime()){
                    Log.e("XXX", "Seleccionado un dia: "+format1.format(selectedDateStart));
                }
                else{
                    Log.e("XXX", "Seleccionado un rango: "+format1.format(selectedDateStart)+" - "+format1.format(selectedDateEnd));
                }
                if(events!=null){
                    for(FlexibleCalendarEvent event:events) {
                        if(event!=null) {
                            Log.e("XXX", "Evento: " + event.getEventInfo());
                        }
                    }
                }
                if(ranges!=null){
                    for(FlexibleCalendarRange range:ranges) {
                        if(range!=null) {
                            Log.e("XXX", "Rango: " + range.getMessage());
                        }
                    }
                }
            }
        });

        ((Spinner)findViewById(R.id.firstDay)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(view!=null) {
                    switch (((TextView) view).getText().toString()) {
                        case "Monday":calendar.setFirstDayOfWeek(Calendar.MONDAY);break;
                        case "Tuesday":calendar.setFirstDayOfWeek(Calendar.TUESDAY);break;
                        case "Wednesday":calendar.setFirstDayOfWeek(Calendar.WEDNESDAY);break;
                        case "Thursday":calendar.setFirstDayOfWeek(Calendar.THURSDAY);break;
                        case "Friday":calendar.setFirstDayOfWeek(Calendar.FRIDAY);break;
                        case "Saturday":calendar.setFirstDayOfWeek(Calendar.SATURDAY);break;
                        case "Sunday":calendar.setFirstDayOfWeek(Calendar.SUNDAY);break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ((Spinner)findViewById(R.id.theme)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(view!=null){
                    switch (((TextView) view).getText().toString()) {
                        case "White":
                            calendar.setBackgroundColor(Color.WHITE);
                            calendar.setMonthTextColor(Color.BLACK);
                            calendar.setMonthSelectedTextColor(Color.GRAY);
                            calendar.setDayNameTextColor(Color.GRAY);
                            calendar.setDayBoxBackgroundColor(Color.rgb(230,230,230));
                            calendar.setDaySelectedBoxBackgroundColor(Color.rgb(230,230,230));
                            calendar.setDayTextColor(Color.BLACK);
                            calendar.setDayWeekendTextColor(Color.GRAY);
                            calendar.setDayBoxBorderColor(Color.WHITE);
                            calendar.setDaySelectedCircleBackgroundColor(Color.WHITE);
                            calendar.setDayPreselectedCircleBackgroundColor(Color.rgb(245, 245, 245));
                            calendar.setDaySelectedTextColor(Color.BLACK);
                            break;
                        case "Black":
                            calendar.setBackgroundColor(Color.rgb(66,66,66));
                            calendar.setMonthTextColor(Color.WHITE);
                            calendar.setMonthSelectedTextColor(Color.LTGRAY);
                            calendar.setDayNameTextColor(Color.LTGRAY);
                            calendar.setDayBoxBackgroundColor(Color.rgb(44,44,44));
                            calendar.setDaySelectedBoxBackgroundColor(Color.rgb(44,44,44));
                            calendar.setDayTextColor(Color.WHITE);
                            calendar.setDayWeekendTextColor(Color.GRAY);
                            calendar.setDayBoxBorderColor(Color.LTGRAY);
                            calendar.setDaySelectedCircleBackgroundColor(Color.rgb(225, 225, 225));
                            calendar.setDayPreselectedCircleBackgroundColor(Color.rgb(205, 205, 205));
                            calendar.setDaySelectedTextColor(Color.BLACK);
                            break;
                        case "Red":
                            calendar.setBackgroundColor(Color.WHITE);
                            calendar.setMonthTextColor(Color.rgb(200,0,0));
                            calendar.setMonthSelectedTextColor(Color.rgb(250,50,50));
                            calendar.setDayNameTextColor(Color.rgb(200,0,0));
                            calendar.setDayBoxBackgroundColor(Color.rgb(255,0,0));
                            calendar.setDaySelectedBoxBackgroundColor(Color.rgb(255,0,0));
                            calendar.setDayTextColor(Color.WHITE);
                            calendar.setDayWeekendTextColor(Color.LTGRAY);
                            calendar.setDayBoxBorderColor(Color.WHITE);
                            calendar.setDaySelectedCircleBackgroundColor(Color.WHITE);
                            calendar.setDayPreselectedCircleBackgroundColor(Color.rgb(245, 245, 245));
                            calendar.setDaySelectedTextColor(Color.RED);
                            break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
