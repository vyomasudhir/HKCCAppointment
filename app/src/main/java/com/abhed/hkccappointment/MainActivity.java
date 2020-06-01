package com.abhed.hkccappointment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.MutationType;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Appointment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.icu.util.Calendar.DATE;
import static android.icu.util.Calendar.DAY_OF_WEEK;
import static android.icu.util.Calendar.HOUR_OF_DAY;
import static android.icu.util.Calendar.MINUTE;
import static android.icu.util.Calendar.MONTH;
import static android.icu.util.Calendar.YEAR;
import static android.view.View.VISIBLE;
import static com.amplifyframework.core.Amplify.API;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity {

    UserStateDetails loggedUserStateDetails = null;
    static TextView lblStartDate;
    static TextView lblEndDate;
    ArrayList slotsStored = new ArrayList();
    private Calendar currentDay = Calendar.getInstance();
    static Calendar bulkStartDate;
    static Calendar bulkEndDate;
    boolean loggedUserIsAdmin = false;
    String loggedUserName = null;
    //Abhed has started contributing to this project

    private View.OnClickListener setStartDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerFragment newFragment = new DatePickerFragment();
            newFragment.lbl = lblStartDate;
            newFragment.date = bulkStartDate;
            newFragment.show(getSupportFragmentManager(), "datePicker" + v.getId());
        }
    };
    private View.OnClickListener setEndDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerFragment newFragment = new DatePickerFragment();
            newFragment.lbl = lblEndDate;
            newFragment.date = bulkEndDate;
            newFragment.show(getSupportFragmentManager(), "datePicker" + v.getId());
        }
    };
    private View.OnClickListener bulkOpenClose = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View v) {
            showSpinner();
            switch (v.getId()) {
                case 1:
                    //open morning of current day
                    openBulkSlots(currentDay, 10 * 60 + 30, 13 * 60);
                    break;
                case 2:
                    //close morning
                    closeBulkSlots(currentDay, 10 * 60 + 30, 13 * 60);
                    break;
                case 3:
                    //open evening of current day
                    openBulkSlots(currentDay, 18 * 60, 20 * 60);
                    break;
                case 4:
                    //close evening
                    closeBulkSlots(currentDay, 18 * 60, 20 * 60);
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                    while (bulkStartDate.getTimeInMillis() < bulkEndDate.getTimeInMillis()) {

                        Calendar curDate = Calendar.getInstance();
                        curDate.setTime(bulkStartDate.getTime());
                        Log.i("9031 bulkdate:", DateFormatter.formatDate(curDate));

                        slotsStored.clear();
                        Amplify.API.query(
                                Appointment.class,
                                Appointment.TIME.beginsWith(DateFormatter.formatDate(curDate)),
                                queryResponse -> {
                                    Log.i("9031 bulkdate response:", DateFormatter.formatDate(curDate));
                                    for (Appointment appt : queryResponse.getData()) {
                                        slotsStored.add(appt);
                                    }
                                    switch (v.getId()) {
                                        case 5: //normal schedule
                                            if (curDate.get(DAY_OF_WEEK) != Calendar.SATURDAY) {
                                                openBulkSlots(curDate, 10 * 60 + 30, 13 * 60);
                                            }
                                            if (curDate.get(DAY_OF_WEEK) != Calendar.SATURDAY && curDate.get(DAY_OF_WEEK) != Calendar.SUNDAY) {
                                                openBulkSlots(curDate, 18 * 60, 20 * 60);
                                            }
                                            break;
                                        case 6: // clear all
                                            closeBulkSlots(curDate, 10 * 60 + 30, 13 * 60);
                                            closeBulkSlots(curDate, 18 * 60, 20 * 60);
                                            break;
                                        case 7: // clear mornings
                                            closeBulkSlots(curDate, 10 * 60 + 30, 13 * 60);
                                            break;
                                        case 8: // clear all
                                            closeBulkSlots(curDate, 18 * 60, 20 * 60);
                                            break;
                                    }
                                },
                                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
                        );
                        bulkStartDate.add(DATE, 1);
                    }
                    break;
            }
            GoToAppointments();

        }
    };

    public static int dip2pix(@NonNull Context context, int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                context.getResources().getDisplayMetrics());
    }



    private void openBulkSlots(Calendar forDay, int startMin, int endMin) {
        int min;
        for (min = startMin; min < endMin; min = min + 15) {
            final Calendar slotCalendar = Calendar.getInstance();
            final int hr = min / 60;
            final int minute = min - (hr * 60);
            slotCalendar.set(forDay.get(YEAR), forDay.get(MONTH), forDay.get(DATE), hr, minute, 0);
            boolean slotExists = false;
            for (int i = 0; i < slotsStored.size(); i++) {
                Appointment curAppt = (Appointment) slotsStored.get(i);
                if (curAppt.isForDay(forDay) && curAppt.getStartMins() >= min && curAppt.getStartMins() <= min + 14) {
                    slotExists = true;
                    break;
                }
            }
            if (!slotExists) {
                createOpenSlot(slotCalendar, "General");
            }
        }
    }

    private void GoSignUp() {
        runOnUiThread(() -> {

            setContentView(R.layout.activity_signup);
            final Button btnSignUp = findViewById(R.id.btnSignup);
            btnSignUp.setOnClickListener(v -> Signup());
        });
    }

    private void GoSignIn() {
        runOnUiThread(() -> {
            setContentView(R.layout.activity_signin);

            final ImageView buttonSignIn = findViewById(R.id.btnSignIn);

            buttonSignIn.setOnClickListener(v -> Signin("+91" + getTextVal(R.id.txtSignInName), getTextVal(R.id.txtSigninPassword)));

            final TextView buttonSignUp = findViewById(R.id.btnGoSignUp);

            buttonSignUp.setOnClickListener(v -> GoSignUp());
        });
    }

    private void Signout() {
        AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
            @Override
            public void onResult(final Void result) {
                Log.d("status", "signed-out");
                makeToast("Signed Out Successfully");
                GoSignIn();

            }

            @Override
            public void onError(Exception e) {
                Log.e("status", "sign-out error", e);
                //makeToast(e.getMessage());
                GoSignIn();
            }
        });
    }

    private String getTextVal(int viewId) {
        return ((EditText) findViewById(viewId)).getText().toString();
    }

    private void Signin(String username, String password) {
        AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
            @Override
            public void onResult(final SignInResult signInResult) {
                runOnUiThread(() -> {
                    Log.d("Sign in", ": " + signInResult.getSignInState());
                    switch (signInResult.getSignInState()) {
                        case DONE:
                            makeToast("Sign-in done.");
                            GoHome();
                            break;
                        case SMS_MFA:
                            makeToast("Please confirm sign-in with SMS.");
                            break;
                        case NEW_PASSWORD_REQUIRED:
                            makeToast("Please confirm sign-in with new password.");
                            break;
                        default:
                            makeToast("Unsupported sign-in confirmation: " + signInResult.getSignInState());
                            break;
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                //Log.e("Error", "Sign-in error", e);
                String msg = e.getMessage();
                makeToast(msg);
            }
        });
    }

    private void makeToast(final String s) {

        runOnUiThread(new Runnable() {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            public void run() {
                Toast toast = Toast.makeText(context, s, duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

    }

    private void Signup() {
        final String username = "+91" + ((EditText) findViewById(R.id.txtSignupUsername)).getText().toString();
        final String password = ((EditText) findViewById(R.id.txtSignupPassword)).getText().toString();
        final String cPpassword = ((EditText) findViewById(R.id.txtSignupCPassword)).getText().toString();
        //
        final String emailId = ((EditText) findViewById(R.id.txtSignupEmail)).getText().toString();
        //
        if (!password.equals(cPpassword)) {
            makeToast("Passwords don't match.");
            return;
        }

        final Map<String, String> attributes = new HashMap<>();
        //attributes.put("custom:subscription", "free");
        attributes.put("email", emailId);
        AWSMobileClient.getInstance().signUp(username, password, attributes, null, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(() -> {
                    Log.i("Sign-up callback: ", " State");
                    if (!signUpResult.getConfirmationState()) {
                        final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                        makeToast("Confirm sign-up with");
                    } else {
                        makeToast("Sign-up done.");
                    }
                    GoSignIn();
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("Sign-up error", String.valueOf(e));
                String msg = e.getMessage();
                makeToast(msg);
            }
        });
    }

    private void closeBulkSlots(Calendar forDay, int startMin, int endMin) {
        int min;
        for (min = startMin; min < endMin; min = min + 15) {
            for (int i = 0; i < slotsStored.size(); i++) {
                Appointment curAppt = (Appointment) slotsStored.get(i);
                if (curAppt.isForDay(forDay) && curAppt.getStartMins() >= min && curAppt.getStartMins() <= min + 14) {
                    closeOpenSlot(curAppt);
                    break;
                }
            }
        }
    }

    private void GoHome() {
        runOnUiThread(() -> {

            //setTitle(Html.fromHtml("<font color='#121833'> Healthy Kids Children's Clinic</font>",0));

            AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    Log.i("9031", "have result");
                    // makeToast(userStateDetails.getUserState().toString());
                    loggedUserStateDetails = userStateDetails;
                    Log.i("9031", "userStateDetails.getUserState() = " + userStateDetails.getUserState());
                    switch (userStateDetails.getUserState()) {
                        case SIGNED_IN:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @RequiresApi(api = Build.VERSION_CODES.N)
                                        public void run() {
                                            loggedUserName = AWSMobileClient.getInstance().getUsername();
                                            loggedUserIsAdmin = loggedUserName.equals("+919900572060");

                                            GoToAppointments();
                                        }
                                    });
                                }
                            });
                            break;
                        case SIGNED_OUT:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GoSignIn();
                                }
                            });
                            break;

                        default:
                            AWSMobileClient.getInstance().signOut();
                            Signout();
                            break;
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("INIT", e.toString());
                }
            });
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void ShowOneDay(ArrayList slotsStored, HorizontalScrollView hsv, final int startTime, final int endTime, final Calendar day) {
        runOnUiThread(new Runnable() {
            public void run() {

                LinearLayout hourlyLayout = new LinearLayout(MainActivity.this);
                hourlyLayout.setOrientation(LinearLayout.HORIZONTAL);
                hsv.addView(hourlyLayout);

                for (int min = startTime; min <= endTime; min = min + 15) {
                    TextView oneEvent = new TextView(MainActivity.this);

                    oneEvent.setId(DateFormatter.formatDateAsId(day));
                    oneEvent.setTextSize(10.0f);
                    oneEvent.setPadding(10, 5, 5, 5);
                    oneEvent.setGravity(Gravity.CENTER_HORIZONTAL);
                    oneEvent.setLayoutParams(new LinearLayout.LayoutParams(dip2pix(getApplicationContext(), 75), dip2pix(getApplicationContext(), 50)));


                    int hr = min / 60;
                    int minute = min - (hr * 60);
                    String minStr = (minute + "0").substring(0, 2);
                    Spanned eventDisplay = null;
                    if (hr <= 11) {
                        eventDisplay = Html.fromHtml("<b>" + hr + ":" + minStr + " am</b>");
                    } else {
                        if (hr >= 13) {
                            hr = hr - 12;
                        }
                        eventDisplay = Html.fromHtml("<b>" + hr + ":" + minStr + " pm</b>");
                    }
                    boolean slotExists = false;
                    Appointment curAppt = null;
                    for (int i = 0; i < slotsStored.size(); i++) {

                        curAppt = (Appointment) slotsStored.get(i);

                        if (curAppt.isForDay(day) && curAppt.getStartMins() >= min && curAppt.getStartMins() <= min + 14) {
                            if (loggedUserIsScheduler()
                                    || curAppt.getType().startsWith("Open")
                                    || curAppt.getPhone().equals(loggedUserName)) {
                                eventDisplay = (Spanned) TextUtils.concat(eventDisplay, "\n", curAppt.getDisplayString());
                            }
                            slotExists = true;
                            if (!loggedUserIsScheduler() && curAppt.getType().startsWith("Reserved") && !loggedUserName.equals(curAppt.getPhone())) {
                                slotExists = false;
                                curAppt = null;
                            }
                            break;
                        }
                        curAppt = null;
                    }
                    if (slotExists) {

                        if (curAppt.getType().startsWith("Open")) {
                            oneEvent.setBackgroundResource(R.drawable.slot_background_open);
                        } else if (curAppt.getType().startsWith("Reserved")) {
                            if (curAppt.getPhone().equals(loggedUserName)) {
                                oneEvent.setBackgroundResource(R.drawable.slot_background_reserved_forme);
                            } else {

                                oneEvent.setBackgroundResource(R.drawable.slot_background_reserved);
                            }
                        }

                    } else {
                        eventDisplay = (Spanned) TextUtils.concat(eventDisplay, "\nClosed");
                        //eventDisplay = eventDisplay + "\nClosed";
                        oneEvent.setBackgroundResource(R.drawable.slot_background);
                    }

                    oneEvent.setText(eventDisplay);

                    final int finalMin = min;
                    final Calendar curSlotCal = Calendar.getInstance();
                    curSlotCal.setTime(day.getTime());

                    Calendar slotTime = getCalFromDateAndTime(curSlotCal, finalMin);
                    Calendar now = Calendar.getInstance();
                    Log.i("9031", "Diff before " + DateFormatter.formatDayDateTime(slotTime) + ":" + DateFormatter.formatDayDateTime(now));
                    if (slotTime.getTimeInMillis() > now.getTimeInMillis()) {
                        oneEvent.setClickable(true);
                        Log.i("9031", "Diff " + slotTime.getTimeInMillis() + ":" + now.getTimeInMillis());
                    } else {
                        oneEvent.setClickable(false);
                        oneEvent.setPaintFlags(oneEvent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        if (!loggedUserIsScheduler()) slotExists = false;
                    }
                    oneEvent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showSpinner();
                            Calendar slotTime = getCalFromDateAndTime(curSlotCal, finalMin);
                            Calendar now = Calendar.getInstance();
                            if (slotTime.getTimeInMillis() > now.getTimeInMillis()) {
                                handleSlot(slotTime, oneEvent);
                            }
                        }
                    });

                    if (loggedUserIsScheduler() || slotExists) {
                        hourlyLayout.addView(oneEvent);


                        TextView padding = new TextView(MainActivity.this);
                        padding.setText("  ");
                        padding.setPadding(5, 5, 5, 5);
                        padding.setBackgroundColor(Color.parseColor("#ffffff"));
                        padding.setLayoutParams(new LinearLayout.LayoutParams(dip2pix(getApplicationContext(), 5), dip2pix(getApplicationContext(), 55)));
                        hourlyLayout.addView(padding);
                    }
                }

            }
        });
    }

    private void showSpinner() {
        runOnUiThread(new Runnable() {
            public void run() {
                ProgressBar spinner = findViewById(R.id.progressBar1);
                if (spinner != null)
                    spinner.setVisibility(VISIBLE);
            }
        });
    }

    private void hideSpinner() {
        runOnUiThread(new Runnable() {
            public void run() {
                ProgressBar spinner = findViewById(R.id.progressBar1);
                if (spinner != null)
                    spinner.setVisibility(View.GONE);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleSlot(final Calendar slot, View selectedSlot) {
        runOnUiThread(new Runnable() {
            public void run() {

                Amplify.API.query(
                        Appointment.class,
                        Appointment.TIME.eq(DateFormatter.formatDateTime(slot)),
                        queryResponse -> {

                            boolean hasData = false;
                            for (Appointment appt : queryResponse.getData()) {
                                hasData = true;
                                if (appt.getType().startsWith("Open")) {
                                    AppointmentActionHandler.handleOpenAppointmentSlot(MainActivity.this, appt);
                                } else if (appt.getType().startsWith("Reserved")) {
                                    AppointmentActionHandler.handleBookedAppointmentSlot(MainActivity.this, appt);
                                }
                                break;
                            }
                            if (!hasData) {
                                AppointmentActionHandler.handleEmptySlot(MainActivity.this, slot);
                            }
                            hideSpinner();
                        },
                        apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
                );
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadAllSlotsForDay(LinearLayout dailyView, final Calendar day, final int startTime, final int endTime) {

        runOnUiThread(() -> {


            HorizontalScrollView hsv = new HorizontalScrollView(MainActivity.this);
            dailyView.addView(hsv);

            ShowOneDay(slotsStored, hsv, startTime, endTime, day);

        });
    }


    private ImageButton addImageButton(LinearLayout llParent, @IdRes int drawableImg, int width, int height, View.OnClickListener btnClick, int id) {
        ImageButton btn = new ImageButton(MainActivity.this);
        btn.setId(id);
        btn.setBackgroundResource(drawableImg);
        btn.setLayoutParams(new LinearLayout.LayoutParams(dip2pix(getApplicationContext(), width), dip2pix(getApplicationContext(), height)));
        btn.setOnClickListener(btnClick);
        llParent.addView(btn);
        return btn;
    }

    public boolean loggedUserIsScheduler() {

        return loggedUserIsAdmin;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void closeOpenSlot(Appointment appt) {
        showSpinner();
        API.mutate(
                appt,
                MutationType.DELETE,
                response -> {
                    Log.i("ApiQuickStart", "Deleted Slot" );
                    GoToAppointments();
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void reserveSlot(Appointment openSlot, String who, String phone) {
        showSpinner();
        Appointment appt = Appointment.builder()
                .name(who)
                .phone(phone)
                .type("Reserved - " + openSlot.getPurpose())
                .time(DateFormatter.formatDateTime(openSlot.getAppointmentDateTimeAsCalendar()))
                .duration(15)
                .description("none")
                .id(openSlot.getId())
                .build();

        API.mutate(
                appt,
                MutationType.UPDATE,
                response -> {
                    Log.i("ApiQuickStart", "Deleted Slot" );
                    GoToAppointments();
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Calendar getCalFromDateAndTime(Calendar date, int time) {
        Calendar slot = Calendar.getInstance();
        //slot.setTime(date.getTime());
        slot.set(date.get(YEAR),date.get(MONTH),date.get(DATE),0,0,0);
        slot.add(MINUTE, time);

        return slot;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void createOpenSlot(final Calendar cal, final String purpose) {
        showSpinner();
        Appointment appt = Appointment.builder()
                .name("None")
                .phone("")
                .type("Open - " + purpose)
                .time(DateFormatter.formatDateTime(cal))
                .duration(15)
                .description("none")
                .build();


        API.mutate(
                appt,
                MutationType.CREATE,
                response -> {
                    GoToAppointments();
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    public void cancelAppointment(Appointment slot) {
        showSpinner();
        Appointment appt = Appointment.builder()
                .name("")
                .phone("")
                .type("Open - General")
                .time(DateFormatter.formatDateTime(slot.getAppointmentDateTimeAsCalendar()))
                .duration(15)
                .description("none")
                .id(slot.getId())
                .build();

        API.mutate(
                appt,
                MutationType.UPDATE,
                response -> {
                    Log.i("ApiQuickStart", "Deleted Slot");
                    GoToAppointments();
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void GoToAppointments() {
        //deleteCache(MainActivity.this);
        runOnUiThread(new Runnable() {

            public void run() {

                setContentView(R.layout.activity_calendar);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setLogo(R.drawable.actionbarlogo);
                getSupportActionBar().setTitle(" Healthy Kids Children's Clinic");
                getSupportActionBar().setDisplayUseLogoEnabled(true);

                showSpinner();
                TextView ph = findViewById(R.id.txtUserPhoneNumberFooter);
                ph.setText("#565 1st Main (Ring Road), Kengeri Satellite Town, 560060 | +91 9900572060");

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDay.getTime());

                if (cal.get(HOUR_OF_DAY) >= 20) {
                    cal.add(DATE, 1);
                }
                cal.set(cal.get(YEAR), cal.get(MONTH), cal.get(DATE), 0, 0, 0);
                Log.i("9031", "calling createDailyView()");
                createDailyView(cal);

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createDailyView(Calendar forDay) {
        MainActivity.this.currentDay = forDay;
        showSpinner();

        LinearLayout dailyView = findViewById(R.id.llDailyView);
        LinearLayout apptView = findViewById(R.id.llAppointmentDetail);
        dailyView.removeAllViews();
        apptView.removeAllViews();

        final Calendar curDay = Calendar.getInstance();
        //curDay.setTime(forDay.getTime());
        TextView tv = findViewById(R.id.txtCurDateShown);
        tv.setText(DateFormatter.formatDayDate(forDay));
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDailyView(curDay);
            }
        });

        final Calendar nextDay = Calendar.getInstance();
        nextDay.setTime(forDay.getTime());
        nextDay.add(DATE,1);
        ImageView btnShowNextDay = findViewById(R.id.btnNextDay);
        btnShowNextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDailyView(nextDay);
            }
        });

        final Calendar prevDay = Calendar.getInstance();
        prevDay.setTime(forDay.getTime());
        prevDay.add(DATE,-1);
        ImageView btnShowPrevDay = findViewById(R.id.btnPrevDay);
        btnShowPrevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDailyView(prevDay);
            }
        });


        slotsStored.clear();
        Log.i("9031", "Queryng");
        Amplify.API.query(
                Appointment.class,
                Appointment.TIME.beginsWith(DateFormatter.formatDate(forDay)),
                queryResponse -> {
                    Log.i("9031", "Queryng response");
                    for (Appointment appt : queryResponse.getData()) {
                        slotsStored.add(appt);
                    }
                    renderAppointmentsUIForDay(forDay);
                    hideSpinner();
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    private void renderAppointmentsUIForDay(Calendar forDay) {
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {

                LinearLayout dailyView = findViewById(R.id.llDailyView);
                LinearLayout apptView = findViewById(R.id.llAppointmentDetail);
                dailyView.removeAllViews();
                apptView.removeAllViews();

                LinearLayout llMorning = new LinearLayout(MainActivity.this);
                llMorning.setOrientation(LinearLayout.HORIZONTAL);
                dailyView.addView(llMorning);
                TextView morn = UIBuilder.addLabel(MainActivity.this, llMorning, "Morning  ");

                loadAllSlotsForDay(dailyView, forDay, 10 * 60 + 30, 12 * 60 - 15);
                loadAllSlotsForDay(dailyView, forDay, 12 * 60, 13 * 60 - 15);
                TextView pad = UIBuilder.addLabel(MainActivity.this, dailyView, "");

                LinearLayout llEvening = new LinearLayout(MainActivity.this);
                llEvening.setOrientation(LinearLayout.HORIZONTAL);
                dailyView.addView(llEvening);
                TextView eve = UIBuilder.addLabel(MainActivity.this, llEvening, "Evening  ");

                loadAllSlotsForDay(dailyView, forDay, 18 * 60, 19 * 60 - 15);
                loadAllSlotsForDay(dailyView, forDay, 19 * 60, 20 * 60 - 15);

                if (loggedUserIsScheduler()) {
                    //Daily Actions
                    ImageButton openMorning = addImageButton(llMorning, R.drawable.open, 75, 30, bulkOpenClose, 1);
                    ImageButton closeMorning = addImageButton(llMorning, R.drawable.close, 75, 30, bulkOpenClose, 2);
                    ImageButton openEve = addImageButton(llEvening, R.drawable.open, 75, 30, bulkOpenClose, 3);
                    ImageButton closeEve = addImageButton(llEvening, R.drawable.close, 75, 30, bulkOpenClose, 4);

                    //Custom Bulk Actions
                    TextView pad2 = UIBuilder.addLabel(MainActivity.this, dailyView, "");
                    TextView schedHeading = UIBuilder.addLabel(MainActivity.this, dailyView, "Manage Schedule");


                    LinearLayout llStartDatePicker = new LinearLayout(MainActivity.this);
                    llStartDatePicker.setOrientation(LinearLayout.HORIZONTAL);
                    TextView lblFrom = UIBuilder.addLabelSmall(MainActivity.this, llStartDatePicker, "From: ");
                    bulkStartDate = Calendar.getInstance();
                    bulkStartDate.add(DATE, 1);
                    lblStartDate = UIBuilder.addLabelSmall(MainActivity.this, llStartDatePicker, DateFormatter.formatDayDate(bulkStartDate));
                    ImageButton btnStartDatePicker = addImageButton(llStartDatePicker, R.drawable.datepicker, 25, 25, setStartDate, 9521);
                    dailyView.addView(llStartDatePicker);

                    LinearLayout llEndDatePicker = new LinearLayout(MainActivity.this);
                    llEndDatePicker.setOrientation(LinearLayout.HORIZONTAL);
                    TextView lblTo = UIBuilder.addLabelSmall(MainActivity.this, llEndDatePicker, "To: ");
                    bulkEndDate = Calendar.getInstance();
                    bulkEndDate.add(DATE, 7);
                    lblEndDate = UIBuilder.addLabelSmall(MainActivity.this, llEndDatePicker, DateFormatter.formatDayDate(bulkEndDate));
                    ImageButton btnEndDatePicker = addImageButton(llEndDatePicker, R.drawable.datepicker, 25, 25, setEndDate, 9522);
                    dailyView.addView(llEndDatePicker);

                    LinearLayout llSetClearSched = new LinearLayout(MainActivity.this);
                    llEndDatePicker.setOrientation(LinearLayout.HORIZONTAL);
                    ImageButton btnSetNormalSchedule = addImageButton(llSetClearSched, R.drawable.setnormalschedule, 135, 40, bulkOpenClose, 5);
                    ImageButton btnClearSchedule = addImageButton(llSetClearSched, R.drawable.clearschedule, 135, 40, bulkOpenClose, 6);
                    dailyView.addView(llSetClearSched);

                    LinearLayout llClearMornEveSched = new LinearLayout(MainActivity.this);
                    llClearMornEveSched.setOrientation(LinearLayout.HORIZONTAL);
                    ImageButton btnClearMorning = addImageButton(llClearMornEveSched, R.drawable.clearmornings, 135, 40, bulkOpenClose, 7);
                    ImageButton btnClearEvening = addImageButton(llClearMornEveSched, R.drawable.clearevenings, 135, 40, bulkOpenClose, 8);
                    dailyView.addView(llClearMornEveSched);



                }
                hideSpinner();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_home:
                GoHome();
                return true;
            case R.id.menu_logout:
                Signout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("ApiQuickstart", "All set and ready to go!");

        } catch (AmplifyException exception) {
            Log.e("ApiQuickstart", exception.getMessage(), exception);
        }

        GoHome();
    }

    @Override
    public void onBackPressed() {
        GoHome();
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public TextView lbl;
        public Calendar date;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final java.util.Calendar c = java.util.Calendar.getInstance();
            int year = c.get(java.util.Calendar.YEAR);
            int month = c.get(java.util.Calendar.MONTH);
            int day = c.get(java.util.Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);

        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar bulkDate = Calendar.getInstance();
            bulkDate.set(year, month, day);
            if (bulkDate.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                date.setTime(bulkDate.getTime());
                lbl.setText(DateFormatter.formatDayDate(bulkDate));
            }
        }

    }
}
