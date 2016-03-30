package neos.planner.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import neos.planner.R;
import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 23.03.16.
 * Project: Planner
 *
 * Активити для редактирования пользовательского события
 */

public class PlannerEditEventActivity extends AppCompatActivity {

    //Параметры переданные из главной активити
    private Bundle extras;

    /*Переменные для работы с элементами активити*/
    private TextView mEventDate;
    private TextView mEventTime;
    private Spinner mRemindMeParam;
    private EditText mEventBody;

    /*Переменные для хранения пользовательских данных*/
    private DbEvent event;
    private int day;
    private int month;
    private int year;
    private int hours;
    private int minutes;

    /*Переменные хранящие объекты для доступа к БД*/
    private ORMLiteOpenHelper helper;
    private Dao<DbEvent, Long> eventDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        extras = getIntent().getExtras();

        Toolbar toolbar = (Toolbar) findViewById(R.id.barNoteDetails);
        toolbar.setTitle(R.string.edit_event_header);
        setSupportActionBar(toolbar);

        try {
            helper = OpenHelperManager.getHelper(this, ORMLiteOpenHelper.class);
            eventDAO = helper.getEventsDao();
            Long id = extras.getLong("ID");
            event = eventDAO.queryForId(id);
            getDate(event.getDate());
            getTime(event.getTime());
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        mEventDate = (TextView) findViewById(R.id.mEventDate);
        mEventDate.setText(event.getDate());
        mEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog =
                        new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                monthOfYear++;
                                if (monthOfYear < 10) {
                                    mEventDate.setText(dayOfMonth + ".0" + monthOfYear + "." + year);
                                } else {
                                    mEventDate.setText(dayOfMonth + "." + monthOfYear + "." + year);
                                }
                                ;
                            }
                        }, year, month, day);
                dialog.show();
            }
        });

        mEventTime = (TextView) findViewById(R.id.mEventTime);
        mEventTime.setText(event.getTime());
        mEventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (minute < 10) {
                            mEventTime.setText(hourOfDay + ":0" + minute);
                        } else {
                            mEventTime.setText(hourOfDay + ":" + minute);
                        }
                    }
                }, hours, minutes, true);
                dialog.show();
            }
        });

        mRemindMeParam = (Spinner) findViewById(R.id.mRemindMeParam);
        String[] reminds = {
                getBaseContext().getString(R.string.event_remind_15_min),
                getBaseContext().getString(R.string.event_remind_30_min),
                getBaseContext().getString(R.string.event_remind_1_hour),
                getBaseContext().getString(R.string.event_remind_3_hour),
                getBaseContext().getString(R.string.event_remind_6_hour)
        };
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, reminds);
        mRemindMeParam.setAdapter(adapter);
        setRemindOption(event.getRemind());

        mEventBody = (EditText) findViewById(R.id.mEventBody);
        mEventBody.setText(event.getEvent());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddEvent);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    event.setEvent(mEventBody.getText().toString());
                    event.setDate(mEventDate.getText().toString());
                    event.setTime(mEventTime.getText().toString());
                    event.setRemind(parseRemindOption(mRemindMeParam.getSelectedItem().toString()));
                    eventDAO.update(event);
                    setResult(RESULT_OK, new Intent());
                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*Метод разбивающий строковую дату по отдельным целочисленным значениям
    * @param date - Параметр перадающий дату*/
    private void getDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date currDate = sdf.parse(date);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currDate);

            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /*Метод разбивающий строковую дату по отдельным целочисленным значениям
    * @param time - Параметр перадающий время*/
    private void getTime(String time) {
        Scanner scanner = new Scanner(time);
        scanner.useDelimiter("\\:");
        hours = scanner.nextInt();
        minutes = scanner.nextInt();
    }

    /*Временное решение для удобства сохранения времени до события в нужном формате*/
    private String parseRemindOption(String remind) {
        if (remind.equals(getBaseContext().getString(R.string.event_remind_15_min))) {
            return "00:15";
        } else if (remind.equals(getBaseContext().getString(R.string.event_remind_30_min))) {
            return "00:30";
        } else if (remind.equals(getBaseContext().getString(R.string.event_remind_1_hour))) {
            return "01:00";
        } else if (remind.equals(getBaseContext().getString(R.string.event_remind_3_hour))) {
            return "03:00";
        } else {
            return "06:00";
        }
    }

    /*Временное решение для удобства сохранения времени до события в нужном формате*/
    private void setRemindOption(String remindOption) {
        if (remindOption.equals("00:15")) {
            mRemindMeParam.setSelection(0);
        } else if (remindOption.equals("00:30")) {
            mRemindMeParam.setSelection(1);
        } else if (remindOption.equals("01:00")) {
            mRemindMeParam.setSelection(2);
        } else if (remindOption.equals("03:00")) {
            mRemindMeParam.setSelection(3);
        } else {
            mRemindMeParam.setSelection(4);
        }
    }
}
