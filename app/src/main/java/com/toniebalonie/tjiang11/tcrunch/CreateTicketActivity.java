package com.toniebalonie.tjiang11.tcrunch;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.toniebalonie.tjiang11.tcrunch.models.Classroom;
import com.toniebalonie.tjiang11.tcrunch.models.Ticket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class CreateTicketActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private Spinner classSpinner;
    private TextView setDate;
    private TextView setTime;
    private TextView setLength;

    private Button createTicketButton;
    private EditText question;

    private int startyear; private int startmonth; private int startday;
    private int starthour; private int startminute;
    private int endyear; private int endmonth; private int endday;
    private int endhour; private int endminute;

    private Long startTime;
    private Long endTime;
    private int ticketLength;

    private String classId;
    private String className;
    private ArrayList<String> classList;
    private HashMap<String, Classroom> classMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ticket);

        classId = getIntent().getStringExtra("classId");
        className = getIntent().getStringExtra("className");
        classList = getIntent().getStringArrayListExtra("classes");
        classMap = (HashMap<String, Classroom>) getIntent().getSerializableExtra("classMap");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        classSpinner = (Spinner) findViewById(R.id.class_spinner);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, classList);
        classSpinner.setAdapter(spinnerArrayAdapter);

        for (int i = 0; i < classList.size(); i++) {
            if (className.equals(classList.get(i))) {
                classSpinner.setSelection(i);
            }
        }

        setDate = (TextView) findViewById(R.id.set_date);
        setTime = (TextView) findViewById(R.id.set_time);
        question = (EditText) findViewById(R.id.ask_question);
        createTicketButton = (Button) findViewById(R.id.create_ticket_button);

        ticketLength = 1;

        setDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        createTicketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTicket();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialogFragment.newInstance().show(getFragmentManager(), "f");
    }

    private void showTimePickerDialog() {
        TimePickerDialogFragment.newInstance().show(getFragmentManager(), "g");
    }

    public void doDatePickerDialogPositiveClick(int day, int month, int year, String dayOfWeek) {
        String newDate = dayOfWeek + ", " + (month + 1) + "/" + day + "/" + year;
        startyear = year; startmonth = month; startday = day;

        setDate.setText(newDate);
    }

    public void doTimePickerDialogPositiveClick(int tpHour, int tpMinute) {
        int hour = tpHour;
        String AM_PM;
        AM_PM = tpHour < 12 ? "AM" : "PM";
        if (tpHour == 0) hour = 12;
        if (tpHour > 12) hour = tpHour - 12;
        String zeroPad = "";
        if (tpMinute < 10) zeroPad = "0";
        String newTime = "" + hour + ":" + zeroPad + tpMinute + " " + AM_PM;

        starthour = tpHour; startminute = tpMinute;

        setTime.setText(newTime);
    }

    public void doTimePickerDialogNegativeClick() {
        Log.i("CreateTicketActivity", "TimePickerDialogNegativeClick");
    }

    public void createTicket() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(startyear, startmonth, startday - 1);
        calendar.set(Calendar.HOUR, starthour);
        calendar.set(Calendar.MINUTE, startminute);
        calendar.add(Calendar.HOUR, 12);
        long startTime = calendar.getTimeInMillis();

        final int msPerHour = 3600000;
        long endTime = startTime + ticketLength * msPerHour;
        Ticket dummyTicket = new Ticket(question.getText().toString(),
                Ticket.QuestionType.FreeResponse, startTime, endTime, classSpinner.getSelectedItem().toString());
        ArrayList<String> answerChoices = new ArrayList<>(Arrays.asList("Choice A", "Choice B", "Choice C"));
        dummyTicket.setAnswerChoices(answerChoices);
        if (mAuth.getCurrentUser() != null) {
            String theClassId = classMap.get(classSpinner.getSelectedItem().toString()).getId();
            DatabaseReference newTicketRef2 = mDatabase.child("tickets").child(theClassId).push();
            String newTicketId = newTicketRef2.getKey();
            dummyTicket.setId(newTicketId);
            newTicketRef2.setValue(dummyTicket);
            finish();
        } else {
            Toast.makeText(this, "Could not find current user.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
