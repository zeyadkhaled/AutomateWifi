package com.zeyad.automatewifi;

import android.app.TimePickerDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

  private Button schedule;
  private Button deSchedule;
  private TimePickerDialog timePickerDialog;
  private Timer timer;
  private RadioGroup wifiStateSelect;
  private RadioButton wifiState;
  private EditText time;
  private int hour;
  private int minutes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toast.makeText(getApplicationContext(), "Select WiFi state you want to set", Toast.LENGTH_LONG).show();
    bindViews();
    hour = 12;
    minutes = 00;

    schedule.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        schedule();
      }
    });

    deSchedule.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        deSchedule();
      }
    });

    time.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        timePickerDialog = new TimePickerDialog(MainActivity.this,
            new TimePickerDialog.OnTimeSetListener() {

              @Override
              public void onTimeSet(TimePicker view, int hourOfDay,
                  int minute) {

                //Handle single digit minutes
                String minStr = minute + "";
                if ( minStr.length() == 1) {
                  minStr = "0" + minStr;
                }

                time.setText("Time Selected: " + hourOfDay + ":" + minStr);
                hour = hourOfDay;
                minutes = minute;
                schedule.setEnabled(true);

                // Hide keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

              }
            }, hour, minutes, false);
        timePickerDialog.show();
      }
    });
  }

  public void schedule() {

    long delay = findDelay();
    if ( delay > 0) { // if selected time is wrong display error

      schedule.setEnabled(false);
      deSchedule.setEnabled(true);

      TimerTask closeWifi = new TimerTask() {
        @Override
        public void run() {

          Looper.prepare();
          int selectedId = wifiStateSelect.getCheckedRadioButtonId();
          wifiState = findViewById(selectedId);
          String stateResult = wifiState.getText().toString();
          WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

          if (stateResult.equals("On")) {
            wifiManager.setWifiEnabled(true);
          } else {
            wifiManager.setWifiEnabled(false);
          }
        }
      };

      timer = new Timer();
      timer.schedule( closeWifi , delay);

      //Handle single digit minutes
      String minStr = minutes + "";
      if ( minStr.length() == 1) {
        minStr = "0" + minStr;
      }
      Toast.makeText(getApplicationContext(), "Task Scheduled at " + hour + ":" + minStr, Toast.LENGTH_LONG).show();

    } else
      Toast.makeText(getApplicationContext(), "Invalid time selected", Toast.LENGTH_LONG).show();
  }

  private void deSchedule() {
    deSchedule.setEnabled(false);
    Toast.makeText(getApplicationContext(), "Task Cancelled", Toast.LENGTH_LONG).show();
    time.setText("Click to select time");
    timer.cancel();
  }

  /**
   * Find the delay amount by calculating the difference between the current time and selected time
   * @return delay
   */
  private long findDelay() {
    Calendar cal = Calendar.getInstance();
    long now = cal.getTimeInMillis();
    cal.set(Calendar.HOUR_OF_DAY , hour);
    cal.set(Calendar.MINUTE, minutes);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTimeInMillis() - now;
  }

  private void bindViews() {
    wifiStateSelect = findViewById(R.id.wifi);
    schedule = findViewById(R.id.schedule);
    deSchedule = findViewById(R.id.deschedule);
    time = findViewById(R.id.timeSelector);
  }
}
