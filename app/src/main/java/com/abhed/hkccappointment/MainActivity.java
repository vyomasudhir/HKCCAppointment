package com.abhed.hkccappointment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.MutationType;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Appointment;

import static android.icu.util.Calendar.DATE;
import static android.icu.util.Calendar.MINUTE;
import static android.icu.util.Calendar.MONTH;
import static android.icu.util.Calendar.YEAR;
import static android.icu.util.Calendar.getInstance;
import static com.amplifyframework.core.Amplify.API;


public class MainActivity extends AppCompatActivity {

    UserStateDetails loggedUserStateDetails= null;

    private String getTextVal(int viewId) {
        return ((EditText)findViewById(viewId)).getText().toString();
    }

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
                                                //setContentView(R.layout.activity_main);
                                                // initHomeView();
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
                makeToast(msg.substring(0, msg.indexOf("(") ));
            }
        });
    }

    private void Signup() {
        final String username = "+91" + ((EditText) findViewById(R.id.txtSignupUsername)).getText().toString();
        final String password = ((EditText)findViewById(R.id.txtSignupPassword)).getText().toString();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatDayDate(Calendar cal)
    {
        int day=cal.get(Calendar.DATE);
        Date date = cal.getTime();
        if(!((day>10) && (day<19)))
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
    private String formatDayDateTime(Calendar cal)
    {
        SimpleDateFormat format = new SimpleDateFormat("EEEE MMM dd yyyy HH:mm");
        Date dt = cal.getTime();
        String dateString = format.format( dt  );
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatDate(Calendar cal)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = cal.getTime();
        String dateString = format.format( dt  );
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int formatDateAsId(Calendar cal)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date dt = cal.getTime();
        String dateString = format.format( dt  );
        return Integer.parseInt(dateString);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatDateTime(Calendar cal)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = cal.getTime();
        String dateString = format.format( dt  );
        return dateString;
        // Date   date       = format.parse ( dt );

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void ShowOneDay(ArrayList slotsStored, HorizontalScrollView hsv, final int startTime, final int endTime, final Calendar day)
    {
        runOnUiThread(new Runnable() {
            public void run() {

                LinearLayout hourlyLayout = new LinearLayout(MainActivity.this);
                hourlyLayout.setOrientation(LinearLayout.HORIZONTAL);
                hsv.addView(hourlyLayout);


                for (int min = startTime; min <= endTime; min = min + 15) {
                    TextView oneEvent = new TextView(MainActivity.this);
                    hourlyLayout.addView(oneEvent);
                    oneEvent.setId(formatDateAsId(day));
                    oneEvent.setTextAppearance(R.font.roboto_regular);
                    oneEvent.setTextSize(12.0f);
                    //oneEvent.setTextColor(Color.parseColor("#ffffff"));
                    oneEvent.setPadding(10, 5 , 5, 5);
                    oneEvent.setLayoutParams(new LinearLayout.LayoutParams(275, 175));


                    int hr = min / 60;
                    int minute = min - (hr * 60);
                    String minStr = (Integer.toString(minute) + "0").substring(0, 2);
                    String eventDisplay=null;
                    if(hr<=11)
                    {
                        eventDisplay = hr + ":" + minStr + " am";
                    }
                    else
                    {
                        if(hr >= 13) {
                            hr = hr - 12;
                        }
                        eventDisplay = hr + ":" + minStr + " pm";
                    }
                    boolean slotExists = false;
                    Appointment curAppt = null;
                    for (int i = 0; i < slotsStored.size(); i++) {

                        curAppt = (Appointment) slotsStored.get(i);

                        if ( curAppt.isForDay(day) && curAppt.getStartMins() >= min && curAppt.getStartMins() <= min + 14) {
                            if (loggedUserIsScheduler()
                                    || curAppt.getType().startsWith("Open")
                                    || curAppt.getPhone().equals(AWSMobileClient.getInstance().getUsername())) {
                                eventDisplay = eventDisplay + "\n" + curAppt.getDisplayString();
                            }
                            slotExists = true;
                        }
                    }
                    if(slotExists) {
                        if(curAppt.getType().startsWith("Open"))
                        {
                            oneEvent.setBackgroundResource(R.drawable.slot_background_open);
                        }
                        else if(curAppt.getType().startsWith("Reserved"))
                        {
                            if(curAppt.getPhone().equals(AWSMobileClient.getInstance().getUsername()))
                            {
                                oneEvent.setBackgroundResource(R.drawable.slot_background_reserved_forme);
                            }
                            else {
                                oneEvent.setBackgroundResource(R.drawable.slot_background_reserved);
                            }
                        }

                    }
                    else
                    {
                        eventDisplay = eventDisplay + "\nClosed";
                        oneEvent.setBackgroundResource(R.drawable.slot_background);
                    }

                    oneEvent.setText(eventDisplay);
                    oneEvent.setClickable(true);
                    final int finalMin = min;
                    final Calendar curCal = Calendar.getInstance();
                    curCal.setTime(day.getTime());
                    oneEvent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Calendar slot = getSlot(curCal, finalMin);
                            handleSlot(slot);
                        }
                    });

                    TextView padding = new TextView(MainActivity.this);
                    padding.setText(" ");
                    padding.setPadding(1, 5, 1, 5);
                    padding.setBackgroundColor(Color.parseColor("#ffffff"));
                    padding.setLayoutParams(new LinearLayout.LayoutParams(3, 185));
                    hourlyLayout.addView(padding);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleSlot(final Calendar slot) {
        runOnUiThread(new Runnable() {
            public void run() {
                Amplify.API.query(
                        Appointment.class,
                        Appointment.TIME.eq(formatDateTime(slot)),
                        queryResponse -> {
                            boolean hasData = false;
                            for (Appointment appt : queryResponse.getData()) {
                                hasData = true;
                                if (appt.getType().startsWith("Open")) {
                                    handleOpenAppointmentSlot(appt);
                                }
                                else if(appt.getType().startsWith("Reserved")) {
                                    handleBookedAppointmentSlot(appt);
                                }
                                break;
                            }
                            if (!hasData) {
                                handleEmptySlot(slot);
                            }

                        },
                        apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
                );
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadAllSlotsForDay(LinearLayout dailyView, final Calendar day, final int startTime, final int endTime, final String title) {
        runOnUiThread(new Runnable() {
            public void run() {
                if(!title.isEmpty()) {
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText(title);
                    tv.setTypeface(Typeface.create("roboto_light",Typeface.NORMAL));
                    tv.setTextColor(getResources().getColor(R.color.hkccDark));
                    tv.setTextSize(14.0f);
                    tv.setPadding(2, 15, 2, 4);
                    dailyView.addView(tv);
                }

                HorizontalScrollView hsv = new HorizontalScrollView(MainActivity.this);
                dailyView.addView(hsv);


                Amplify.API.query(
                        Appointment.class,
                        Appointment.TIME.beginsWith(formatDate(day)),
                        queryResponse -> {
                            Log.i("loadAllSlotsForDay:","GOT RESPONSEE-----------------------------");
                            ArrayList slotsStored = new ArrayList();
                            for (Appointment appt : queryResponse.getData()) {
                                slotsStored.add(appt);
                            }
                            ShowOneDay(slotsStored, hsv, startTime, endTime, day);

                        },
                        apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
                );
            }
        });
    }

    private void showActionDoneMessage(String message)
    {
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

    private void handleEmptySlot(final Calendar slot) {

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                LinearLayout llApptDetails = findViewById(R.id.llAppointmentDetail);
                llApptDetails.removeAllViews();
                if (loggedUserIsScheduler()) {

                    Button btnOpenSlot = new Button(MainActivity.this);
                    btnOpenSlot.setText("Open Slot\nDate: " + formatDayDateTime(slot));
                    btnOpenSlot.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            createOpenSlot(slot,"General");
                            showActionDoneMessage("Slot opened for " + formatDateTime(slot));
                        }
                    });
                    llApptDetails.addView(btnOpenSlot);

                    //

                    Button btnOpenSlotForVaccination = new Button(MainActivity.this);
                    btnOpenSlotForVaccination.setText("Open Vaccination Slot\nPurpose: Vaccination\n Date: " + formatDayDateTime(slot));
                    btnOpenSlotForVaccination.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            createOpenSlot(slot, "Vaccination Only");
                            showActionDoneMessage("Vaccination slot opened for " + formatDateTime(slot));
                        }
                    });
                    llApptDetails.addView(btnOpenSlotForVaccination);

                } else {
                    Log.i("logged user is", "NOT  ------------------------------scheduler");
                }
            }

        });


    }

    private boolean loggedUserIsScheduler()  {
        if(loggedUserStateDetails != null)
        {
            try {
                Log.i("User Name -  ",  AWSMobileClient.getInstance().getUsername());
                if(AWSMobileClient.getInstance().getUsername().equals("+919900572060")) return true;
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
                LinearLayout llApptDetails = findViewById(R.id.llAppointmentDetail);
                llApptDetails.removeAllViews();
                if (loggedUserIsScheduler()) {
                    Button btnCloseSlot = new Button(MainActivity.this);
                    btnCloseSlot.setText("Close slot for Date: " + formatDayDateTime(appt.getAppointmentDateTimeAsCalendar()));
                    btnCloseSlot.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            closeOpenSlot(appt);
                            showActionDoneMessage("Slot Closed for " + formatDateTime(appt.getAppointmentDateTimeAsCalendar()));
                        }
                    });
                    llApptDetails.addView(btnCloseSlot);
                }
                else
                {
                    final EditText txtPatientName = addField(llApptDetails,"Patient Name");

                    Button btnReserveSlot = new Button(MainActivity.this);
                    btnReserveSlot.setText("Reserve slot for Date: " + formatDayDateTime(appt.getAppointmentDateTimeAsCalendar()));
                    btnReserveSlot.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            reserveSlot(appt, txtPatientName.getText().toString(), AWSMobileClient.getInstance().getUsername());
                            showActionDoneMessage("Slot Closed for " + formatDateTime(appt.getAppointmentDateTimeAsCalendar()));
                        }
                    });
                    llApptDetails.addView(btnReserveSlot);
                }

            }
        });
    }

    private void handleBookedAppointmentSlot(Appointment appt) {
    }

    private EditText addField(LinearLayout llParent, String label)
    {
        LinearLayout llField = new LinearLayout(MainActivity.this);
        llField.setOrientation(LinearLayout.HORIZONTAL);
        llParent.addView(llField,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView lblPatientName = new TextView(MainActivity.this);
        lblPatientName.setText(label);
        llField.addView(lblPatientName,new LinearLayout.LayoutParams( 600 , LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText txtPatientName = new EditText(MainActivity.this);
        llField.addView(txtPatientName);

        return txtPatientName;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void createOpenSlot(final Calendar cal, final String purpose) {
        Appointment appt = Appointment.builder()
                .name("None")
                .phone("9900572060")
                .type("Open - " + purpose)
                .time(formatDateTime(cal))
                .duration(15)
                .description("none")
                .build();


        API.mutate(
                appt,
                MutationType.CREATE,
                response -> {
                    Log.i("ApiQuickStart", "Added Open Slot with id: " + response.getData().getId());
                    GoHome();
                    },
                apiFailure -> Log.e("ApiQuickStart", apiFailure.getMessage(), apiFailure)
        );
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
    private Calendar getSlot(Calendar curCal, int finalMin) {
        Calendar slot = Calendar.getInstance();
        slot.setTime(curCal.getTime());
        slot.add(MINUTE,finalMin);

        return slot;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void GoToAppointments() {

        setContentView(R.layout.activity_calendar);

        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(YEAR), cal.get(MONTH), cal.get(DATE), 0, 0, 0);

        createDailyView(cal);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createDailyView(Calendar forDay)
    {
        LinearLayout dailyView = findViewById(R.id.llDailyView);

        final Calendar curDay = Calendar.getInstance();
        curDay.setTime(forDay.getTime());
        TextView tv = findViewById(R.id.txtCurDateShown);
        tv.setText(formatDayDate(forDay));
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dailyView.removeAllViews();
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
                createDailyView(prevDay);
            }
        });

        loadAllSlotsForDay(dailyView,forDay, 11*60, 12*60 - 15 , "Morning" );
        loadAllSlotsForDay(dailyView,forDay, 12*60, 13*60 -15 ,"" );
        loadAllSlotsForDay(dailyView,forDay, 18*60, 19*60 - 15,"Evening" );
        loadAllSlotsForDay(dailyView,forDay, 19*60, 20*60 - 15,"" );

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
}
