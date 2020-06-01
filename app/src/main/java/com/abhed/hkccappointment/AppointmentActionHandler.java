package com.abhed.hkccappointment;

import android.app.Activity;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.amplifyframework.datastore.generated.model.Appointment;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class AppointmentActionHandler {

    private static void clearViews(Activity activity) {
        LinearLayout llDailyView = activity.findViewById(R.id.llDailyView);
        llDailyView.removeAllViews();

        LinearLayout llApptDetails = activity.findViewById(R.id.llAppointmentDetail);
        llApptDetails.removeAllViews();

        FrameLayout flp = activity.findViewById(R.id.flPrev);
        flp.setVisibility(View.INVISIBLE);

        FrameLayout fln = activity.findViewById(R.id.flNext);
        fln.setVisibility(View.INVISIBLE);

        TextView txtCurDate = activity.findViewById(R.id.txtCurDateShown);
        txtCurDate.setVisibility(View.INVISIBLE);
    }

    private static void renderAppointmentHeader(MainActivity activity, Calendar apptTime, Appointment appt, String title) {
        LinearLayout llDailyView = activity.findViewById(R.id.llDailyView);

        final TextView lblAppt = UIBuilder.addLabel(activity, llDailyView, title);
        final TextView lblDate = UIBuilder.addLabelSmall(activity, llDailyView, "Date: " + DateFormatter.formatDayDate(apptTime));
        final TextView lblTime = UIBuilder.addLabelSmall(activity, llDailyView, "Time: " + DateFormatter.formatTime(apptTime));

        if (appt != null) {
            if (!appt.getName().isEmpty() && !appt.getName().equals("None")) {
                final TextView lblPatientName = UIBuilder.addLabelSmall(activity, llDailyView, "Patient: " + appt.getName());
            }
            if (!appt.getPhone().isEmpty()) {
                final TextView lblPhoneNumber = UIBuilder.addLabelSmall(activity, llDailyView, "Phone: " + appt.getPhone());
            }
        }

        TextView pad = UIBuilder.addLabel(activity, llDailyView, "");
    }


    public static void handleEmptySlot(MainActivity activity, final Calendar slot) {

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                LinearLayout llDailyView = activity.findViewById(R.id.llDailyView);

                clearViews(activity);
                renderAppointmentHeader(activity, slot, null, "Open Slot for Patients:");


                if (activity.loggedUserIsScheduler()) {

                    Button btnOpenSlot = UIBuilder.addButton(activity, llDailyView, "Open General Slot");
                    btnOpenSlot.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            activity.createOpenSlot(slot, "General");

                        }
                    });


                    Button btnOpenSlotForVaccination = UIBuilder.addButton(activity, llDailyView, "Open Vaccination Slot");
                    btnOpenSlotForVaccination.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            activity.createOpenSlot(slot, "Vaccination Only");

                        }
                    });


                } else {
                    Log.i("logged user is", "NOT  ------------------------------scheduler");
                }
            }

        });


    }

    public static void handleOpenAppointmentSlot(MainActivity activity, final Appointment appt) {
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                LinearLayout llDailyView = activity.findViewById(R.id.llDailyView);

                clearViews(activity);


                if (activity.loggedUserIsScheduler()) {
                    renderAppointmentHeader(activity, appt.getAppointmentDateTimeAsCalendar(), appt, "New Appointment or Close Slot:");
                } else {
                    renderAppointmentHeader(activity, appt.getAppointmentDateTimeAsCalendar(), appt, "New Appointment:");
                }

                final EditText txtPatientName = UIBuilder.addField(activity, llDailyView, "Patient Name");

                final EditText txtPatientPhone = UIBuilder.addField(activity, llDailyView, "Phone");
                if (!activity.loggedUserIsScheduler()) {
                    txtPatientPhone.setVisibility(View.INVISIBLE);
                }

                Button btnReserveSlot = UIBuilder.addButton(activity, llDailyView, "Reserve");

                btnReserveSlot.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String phoneNumber = activity.loggedUserName;
                        if (activity.loggedUserIsScheduler() && !txtPatientPhone.getText().toString().isEmpty()) {
                            phoneNumber = txtPatientPhone.getText().toString();
                        }
                        activity.reserveSlot(appt, txtPatientName.getText().toString(), phoneNumber);

                    }
                });

                if (activity.loggedUserIsScheduler()) {
                    TextView pad = UIBuilder.addLabel(activity, llDailyView, "");
                    UIBuilder.addSeparator(activity, llDailyView);
                    Button btnCloseSlot = UIBuilder.addButton(activity, llDailyView, "Close");
                    btnCloseSlot.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            activity.closeOpenSlot(appt);

                        }
                    });
                }

            }
        });
    }


    public static void handleBookedAppointmentSlot(MainActivity activity, Appointment appt) {

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                LinearLayout llDailyView = activity.findViewById(R.id.llDailyView);

                clearViews(activity);

                renderAppointmentHeader(activity, appt.getAppointmentDateTimeAsCalendar(), appt, "Appointment Details:");

                Button btnCloseSlot = UIBuilder.addButton(activity, llDailyView, "Cancel Appointment");
                btnCloseSlot.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        activity.cancelAppointment(appt);
                    }
                });

                if (activity.loggedUserIsScheduler()) {
                    Button btnCallPatient = UIBuilder.addButton(activity, llDailyView, "Call Patient");
                    btnCallPatient.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            String temp = "tel:" + appt.getPhone();
                            intent.setData(Uri.parse(temp));

                            activity.startActivity(intent);
                        }
                    });

                } else {


                }
            }
        });
    }


}
