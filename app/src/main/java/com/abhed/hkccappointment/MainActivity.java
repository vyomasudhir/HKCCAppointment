package com.abhed.hkccappointment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.icu.util.Calendar.DATE;
import static android.icu.util.Calendar.HOUR_OF_DAY;
import static android.icu.util.Calendar.MINUTE;
import static android.icu.util.Calendar.MONTH;
import static android.icu.util.Calendar.YEAR;
import static android.view.View.VISIBLE;
import static com.amplifyframework.core.Amplify.API;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity {

    UserStateDetails loggedUserStateDetails = null;
    ArrayList slotsStored = new ArrayList();
    private Calendar currentDay = Calendar.getInstance();
    private View.OnClickListener bulkOpenClose = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View v) {
            int min = 0;
            switch (v.getId()) {
                case 1:
                    //open morning of current day
                    for (min = 11 * 60; min < 13 * 60; min = min + 15) {
                        final Calendar slotCalendar = Calendar.getInstance();
                        final int hr = min / 60;
                        final int minute = min - (hr * 60);
                        slotCalendar.set(currentDay.get(YEAR), currentDay.get(MONTH), currentDay.get(DATE), hr, minute);
                        createOpenSlot(slotCalendar, "General", false);
                    }

                    break;
                case 2:
                    //close morning
                    for (min = 11 * 60; min < 13 * 60; min = min + 15) {
                        final Calendar slotCalendar = Calendar.getInstance();
                        final int hr = min / 60;
                        final int minute = min - (hr * 60);
                        slotCalendar.set(currentDay.get(YEAR), currentDay.get(MONTH), currentDay.get(DATE), hr, minute);
                        //clo(slotCalendar,"General",false);
                    }
                    break;
                case 3:
                    //DO something
                    break;
                case 4:
                    //DO something
            }
            GoHome();

        }
    };

    private void GoHome() {
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {

                //setTitle(Html.fromHtml("<font color='#121833'> Healthy Kids Children's Clinic</font>",0));

                AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        // makeToast(userStateDetails.getUserState().toString());
                        loggedUserStateDetails = userStateDetails;
                        switch (userStateDetails.getUserState()) {
                            case SIGNED_IN:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @RequiresApi(api = Build.VERSION_CODES.N)
                                            public void run() {
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
            }
        });
    }

    private void GoSignUp() {
        runOnUiThread(new Runnable() {
            public void run() {

                setContentView(R.layout.activity_signup);
                final Button btnSignUp = findViewById(R.id.btnSignup);
                btnSignUp.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Signup();
                    }
                });
            }
        });
    }

    private void GoSignIn() {
        runOnUiThread(new Runnable() {
            public void run() {
                setContentView(R.layout.activity_signin);

                final ImageView buttonSignIn = findViewById(R.id.btnSignIn);

                buttonSignIn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Signin("+91" + getTextVal(R.id.txtSignInName), getTextVal(R.id.txtSigninPassword));
                    }
                });

                final TextView buttonSignUp = findViewById(R.id.btnGoSignUp);

                buttonSignUp.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        GoSignUp();
                    }
                });
            }
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                //Log.e("Error", "Sign-in error", e);
                String msg = e.getMessage();
                makeToast(msg.substring(0, msg.indexOf("(")));
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Sign-up callback: ", " State");
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            makeToast("Confirm sign-up with: " + details.getDestination());
                        } else {
                            makeToast("Sign-up done.");
                        }
                        GoSignIn();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("Sign-up error", String.valueOf(e));
                String msg = e.getMessage();
                makeToast(msg.substring(0, msg.indexOf("(")));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatDayDate(Calendar cal) {
        int day = cal.get(Calendar.DATE);
        Date date = cal.getTime();
        if (!((day > 10) && (day < 19)))
            switch (day % 10) {
                case 1:
                    return new SimpleDateFormat("EEEE, d'st' 'of' MMMM yyyy").format(date);
                case 2:
                    return new SimpleDateFormat("EEEE, d'nd' 'of' MMMM yyyy").format(date);
                case 3:
                    return new SimpleDateFormat("EEEE, d'rd' 'of' MMMM yyyy").format(date);
                default:
                    return new SimpleDateFormat("EEEE, d'th' 'of' MMMM yyyy").format(date);
            }
        return new SimpleDateFormat("EEEE, d'th' 'of' MMMM yyyy").format(date);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatDayDateTime(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE MMM dd yyyy HH:mm");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatTime(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatDate(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int formatDateAsId(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return Integer.parseInt(dateString);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatDateTime(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = cal.getTime();
        String dateString = format.format(dt);
        return dateString;
        // Date   date       = format.parse ( dt );

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

                    oneEvent.setId(formatDateAsId(day));
                    oneEvent.setTextAppearance(R.font.roboto_regular);
                    oneEvent.setTextSize(12.0f);
                    //oneEvent.setTextColor(Color.parseColor("#ffffff"));
                    oneEvent.setPadding(10, 5, 5, 5);
                    oneEvent.setGravity(Gravity.CENTER_HORIZONTAL);
                    oneEvent.setLayoutParams(new LinearLayout.LayoutParams(275, 175));


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
                                    || curAppt.getPhone().equals(AWSMobileClient.getInstance().getUsername())) {
                                eventDisplay = (Spanned) TextUtils.concat(eventDisplay, "\n", curAppt.getDisplayString());
                                // eventDisplay = eventDisplay + "\n" + curAppt.getDisplayString();
                            }
                            slotExists = true;
                            if (!loggedUserIsScheduler() && curAppt.getType().startsWith("Reserved") && !getLoggedUsername().equals(curAppt.getPhone())) {
                                slotExists = false;
                                curAppt = null;
                            }
                            break;
                        }
                        curAppt = null;
                    }
                    if (slotExists) {
                        Log.i("9031", "working on " + curAppt.getType());
                        if (curAppt.getType().startsWith("Open")) {
                            oneEvent.setBackgroundResource(R.drawable.slot_background_open);
                        } else if (curAppt.getType().startsWith("Reserved")) {
                            if (curAppt.getPhone().equals(AWSMobileClient.getInstance().getUsername())) {
                                oneEvent.setBackgroundResource(R.drawable.slot_background_reserved_forme);
                            } else {
                                Log.i("9031", "setting reserved for type " + curAppt.getType());
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
                    if (slotTime.getTimeInMillis() > now.getTimeInMillis()) {
                        oneEvent.setClickable(true);
                    } else {
                        oneEvent.setClickable(false);
                        oneEvent.setPaintFlags(oneEvent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        if (!loggedUserIsScheduler()) slotExists = false;
                    }
                    oneEvent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                        padding.setText(" ");
                        padding.setPadding(1, 5, 1, 5);
                        padding.setBackgroundColor(Color.parseColor("#ffffff"));
                        padding.setLayoutParams(new LinearLayout.LayoutParams(3, 185));
                        hourlyLayout.addView(padding);
                    }
                }
            }
        });
    }

    private void showSpinner() {
        ProgressBar spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(VISIBLE);
    }

    private void hideSpinner() {
        ProgressBar spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleSlot(final Calendar slot, View selectedSlot) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i("9031", "handleSlot " + formatDayDateTime(slot));
                Amplify.API.query(
                        Appointment.class,
                        Appointment.TIME.eq(formatDateTime(slot)),
                        queryResponse -> {

                            boolean hasData = false;
                            for (Appointment appt : queryResponse.getData()) {
                                hasData = true;
                                if (appt.getType().startsWith("Open")) {
                                    handleOpenAppointmentSlot(appt);
                                } else if (appt.getType().startsWith("Reserved")) {
                                    handleBookedAppointmentSlot(appt);
                                }
                                break;
                            }
                            if (!hasData) {
                                handleEmptySlot(slot, selectedSlot);
                            }

                        },
                        apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
                );
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadAllSlotsForDay(LinearLayout dailyView, final Calendar day, final int startTime, final int endTime) {

        runOnUiThread(new Runnable() {
            public void run() {
                ProgressBar spinner = findViewById(R.id.progressBar1);
                spinner.setVisibility(VISIBLE);

                HorizontalScrollView hsv = new HorizontalScrollView(MainActivity.this);
                dailyView.addView(hsv);

                ShowOneDay(slotsStored, hsv, startTime, endTime, day);

            }
        });
    }

    private void showActionDoneMessage(String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                LinearLayout llApptDetails = findViewById(R.id.llAppointmentDetail);
                llApptDetails.removeAllViews();

                TextView tv = new TextView(MainActivity.this);
                tv.setText(message);

                llApptDetails.addView(tv);
            }
        });

    }

    private void handleEmptySlot(final Calendar slot, View selectedSlot) {

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                Log.i("9031", "handleEmptySlot");
                LinearLayout llDailyView = findViewById(R.id.llDailyView);
                llDailyView.removeAllViews();

                LinearLayout llApptDetails = findViewById(R.id.llAppointmentDetail);
                llApptDetails.removeAllViews();

                final TextView lblApptDetails = addLabel(llApptDetails, "Appointment Time: " + formatTime(slot));

                if (loggedUserIsScheduler()) {

                    Button btnOpenSlot = addButton(llApptDetails, "Open General Slot");
                    btnOpenSlot.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            createOpenSlot(slot, "General", true);
                            showActionDoneMessage("Slot opened for " + formatDateTime(slot));
                        }
                    });


                    Button btnOpenSlotForVaccination = addButton(llApptDetails, "Open Vaccination Slot");
                    btnOpenSlotForVaccination.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            createOpenSlot(slot, "Vaccination Only", true);
                            showActionDoneMessage("Vaccination slot opened for " + formatDateTime(slot));
                        }
                    });


                } else {
                    Log.i("logged user is", "NOT  ------------------------------scheduler");
                }
            }

        });


    }

    private String getLoggedUsername() {
        try {

            return AWSMobileClient.getInstance().getUsername();
        } catch (Exception e) {
            return "9999999999";
        }
    }

    private boolean loggedUserIsScheduler() {
        if (loggedUserStateDetails != null) {
            try {
                //Log.i("User Name -  ",  AWSMobileClient.getInstance().getUsername());
                if (AWSMobileClient.getInstance().getUsername().equals("+919900572060"))
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    private void handleOpenAppointmentSlot(final Appointment appt) {
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                LinearLayout llDailyView = findViewById(R.id.llDailyView);
                llDailyView.removeAllViews();

                LinearLayout llApptDetails = findViewById(R.id.llAppointmentDetail);
                llApptDetails.removeAllViews();

                final TextView lblApptDetails = addLabel(llApptDetails, "Appointment Time: " + formatTime(appt.getAppointmentDateTimeAsCalendar()));

                Log.i("9031", "handleOpenAppointmentSlot");
                if (loggedUserIsScheduler()) {
                    //Button btnCloseSlot = new Button(MainActivity.this);
                    Button btnCloseSlot = addButton(llApptDetails, "Close");
                    btnCloseSlot.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            closeOpenSlot(appt);
                            showActionDoneMessage("Slot Closed for " + formatDateTime(appt.getAppointmentDateTimeAsCalendar()));
                        }
                    });
                } else {


                }

                final EditText txtPatientName = addField(llApptDetails, "Patient Name");

                Button btnReserveSlot = addButton(llApptDetails, "Reserve");

                btnReserveSlot.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        reserveSlot(appt, txtPatientName.getText().toString(), AWSMobileClient.getInstance().getUsername());
                        showActionDoneMessage("Slot Closed for " + formatDateTime(appt.getAppointmentDateTimeAsCalendar()));
                    }
                });

            }
        });
    }

    private Button addButton(LinearLayout llParent, String s) {
        Button btn = new Button(MainActivity.this);
        btn.setBackgroundResource(R.drawable.rect_blue);
        btn.setText(s);
        btn.setTextColor(Color.WHITE);
        llParent.addView(btn);

        TextView tv = new TextView(MainActivity.this);
        tv.setTextSize(2.0f);
        tv.setPadding(10, 10, 10, 10);

        llParent.addView(tv);

        return btn;
    }

    private ImageButton addImageButton(LinearLayout llParent, @IdRes int drawable, int width, int height, View.OnClickListener btnClick, int id) {
        ImageButton btn = new ImageButton(MainActivity.this);
        btn.setId(id);
        btn.setBackgroundResource(drawable);
        btn.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        btn.setOnClickListener(btnClick);
        llParent.addView(btn);
        return btn;
    }

    private TextView addLabel(LinearLayout llParent, String s) {
        TextView tv = new TextView(MainActivity.this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setTextColor(Color.parseColor("#121833"));
        tv.setText(s);
        tv.setTextSize(18.0f);

        Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.sfnsdisplay);

        tv.setTypeface(typeface, Typeface.BOLD);


        llParent.addView(tv);

        TextView pad = new TextView(MainActivity.this);
        pad.setTextSize(2.0f);
        pad.setPadding(10, 10, 10, 10);

        llParent.addView(pad);

        return tv;

    }

    private void handleBookedAppointmentSlot(Appointment appt) {

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                LinearLayout llDailyView = findViewById(R.id.llDailyView);
                llDailyView.removeAllViews();

                LinearLayout llApptDetails = findViewById(R.id.llAppointmentDetail);
                llApptDetails.removeAllViews();

                final TextView lblApptDetails = addLabel(llApptDetails, "Appointment Time: " + formatTime(appt.getAppointmentDateTimeAsCalendar()));

                Log.i("9031", "handleBookedAppointmentSlot");
                Button btnCloseSlot = addButton(llApptDetails, "Cancel Appointment");
                btnCloseSlot.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        cancelAppointment(appt);
                    }
                });

                if (loggedUserIsScheduler()) {
                    Button btnCallPatient = addButton(llApptDetails, "Call Patient");
                    btnCallPatient.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            String temp = "tel:" + appt.getPhone();
                            intent.setData(Uri.parse(temp));

                            startActivity(intent);


                        }
                    });

                } else {


                }
            }
        });
    }

    private EditText addField(LinearLayout llParent, String label) {
        EditText editText = new EditText(MainActivity.this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint(label);
        //editText.setBackground(Color.parseColor("#00000000"));
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTextColor(Color.parseColor("#030303"));
        editText.setHintTextColor(Color.parseColor("#d3d3d3"));

        llParent.addView(editText);

        TextView tv = new TextView(MainActivity.this);
        tv.setTextSize(2.0f);
        tv.setPadding(10, 10, 10, 10);

        llParent.addView(tv);

        return editText;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void closeOpenSlot(Appointment appt) {

        API.mutate(
                appt,
                MutationType.DELETE,
                response -> {
                    Log.i("ApiQuickStart", "Deleted Slot" );
                    GoHome();
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void reserveSlot(Appointment openSlot, String who, String phone) {
        Appointment appt = Appointment.builder()
                .name(who)
                .phone(phone)
                .type("Reserved - " + openSlot.getPurpose())
                .time(formatDateTime(openSlot.getAppointmentDateTimeAsCalendar()))
                .duration(15)
                .description("none")
                .id(openSlot.getId())
                .build();

        API.mutate(
                appt,
                MutationType.UPDATE,
                response -> {
                    Log.i("ApiQuickStart", "Deleted Slot" );
                    GoHome();
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void createOpenSlot(final Calendar cal, final String purpose, boolean withUIRefresh) {
        Appointment appt = Appointment.builder()
                .name("None")
                .phone("+919900572060")
                .type("Open - " + purpose)
                .time(formatDateTime(cal))
                .duration(15)
                .description("none")
                .build();


        API.mutate(
                appt,
                MutationType.CREATE,
                response -> {
                    if (withUIRefresh) {
                        //Log.i("ApiQuickStart", "Added Open Slot with id: " + response.getData().getId());
                        GoHome();
                    }
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    private void cancelAppointment(Appointment slot) {
        Appointment appt = Appointment.builder()
                .name("")
                .phone("")
                .type("Open - General")
                .time(formatDateTime(slot.getAppointmentDateTimeAsCalendar()))
                .duration(15)
                .description("none")
                .id(slot.getId())
                .build();

        API.mutate(
                appt,
                MutationType.UPDATE,
                response -> {
                    Log.i("ApiQuickStart", "Deleted Slot");
                    GoHome();
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Calendar getCalFromDateAndTime(Calendar date, int time) {
        Calendar slot = Calendar.getInstance();
        slot.setTime(date.getTime());
        slot.add(MINUTE, time);

        return slot;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void GoToAppointments() {

        setContentView(R.layout.activity_calendar);

        TextView ph = findViewById(R.id.txtUserPhoneNumberFooter);
        ph.setText(getLoggedUsername());

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDay.getTime());
        Log.i("9031", "HOUR=" + cal.get(HOUR_OF_DAY));
        if (cal.get(HOUR_OF_DAY) >= 20) {
            cal.add(DATE, 1);
        }
        cal.set(cal.get(YEAR), cal.get(MONTH), cal.get(DATE), 0, 0, 0);

        createDailyView(cal);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createDailyView(Calendar forDay) {
        MainActivity.this.currentDay = forDay;

        LinearLayout dailyView = findViewById(R.id.llDailyView);
        LinearLayout apptView = findViewById(R.id.llAppointmentDetail);


        final Calendar curDay = Calendar.getInstance();
        //curDay.setTime(forDay.getTime());
        TextView tv = findViewById(R.id.txtCurDateShown);
        tv.setText(formatDayDate(forDay));
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dailyView.removeAllViews();
                apptView.removeAllViews();
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
                dailyView.removeAllViews();
                apptView.removeAllViews();
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
                dailyView.removeAllViews();
                apptView.removeAllViews();
                createDailyView(prevDay);
            }
        });

        slotsStored.clear();
        showSpinner();
        Amplify.API.query(
                Appointment.class,
                Appointment.TIME.beginsWith(formatDate(forDay)),
                queryResponse -> {
                    for (Appointment appt : queryResponse.getData()) {
                        slotsStored.add(appt);
                    }
                    makeAppointmentsUIForDay(forDay);
                },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
    }

    private void makeAppointmentsUIForDay(Calendar forDay) {
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                showSpinner();
                LinearLayout dailyView = findViewById(R.id.llDailyView);
                LinearLayout apptView = findViewById(R.id.llAppointmentDetail);

                LinearLayout llMorning = new LinearLayout(MainActivity.this);
                llMorning.setOrientation(LinearLayout.HORIZONTAL);
                dailyView.addView(llMorning);
                TextView morn = addLabel(llMorning, "Morning");

                loadAllSlotsForDay(dailyView, forDay, 11 * 60, 12 * 60 - 15);
                loadAllSlotsForDay(dailyView, forDay, 12 * 60, 13 * 60 - 15);
                TextView pad = addLabel(dailyView, "");

                LinearLayout llEvening = new LinearLayout(MainActivity.this);
                llEvening.setOrientation(LinearLayout.HORIZONTAL);
                dailyView.addView(llEvening);
                TextView eve = addLabel(llEvening, "Evening");

                loadAllSlotsForDay(dailyView, forDay, 18 * 60, 19 * 60 - 15);
                loadAllSlotsForDay(dailyView, forDay, 19 * 60, 20 * 60 - 15);

                if (loggedUserIsScheduler()) {
                    ImageButton openMorning = addImageButton(llMorning, R.drawable.open, 200, 90, bulkOpenClose, 1);
                    ImageButton closeMorning = addImageButton(llMorning, R.drawable.close, 200, 90, bulkOpenClose, 2);
                    ImageButton openEve = addImageButton(llEvening, R.drawable.open, 200, 90, bulkOpenClose, 3);
                    ImageButton closeEve = addImageButton(llEvening, R.drawable.close, 200, 90, bulkOpenClose, 4);

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
}
