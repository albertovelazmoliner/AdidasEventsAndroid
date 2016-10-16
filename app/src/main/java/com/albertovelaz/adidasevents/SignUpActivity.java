package com.albertovelaz.adidasevents;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.albertovelaz.adidasevents.config.ServiceGenerator;
import com.albertovelaz.adidasevents.interfaces.RestInterface;
import com.albertovelaz.adidasevents.models.JSONModels.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Alberto Vélaz
 * Created by albertovelazmoliner on 15/10/2016.
 */
public class SignUpActivity extends AppCompatActivity {

    // UI references.
    @BindView(R.id.email)               EditText email;
    @BindView(R.id.email_container)     TextInputLayout emailContainer;
    @BindView(R.id.firstName)           EditText firstName;
    @BindView(R.id.firstName_container) TextInputLayout firstNameContainer;
    @BindView(R.id.lastName)            EditText lastName;
    @BindView(R.id.lastName_container)  TextInputLayout lastNameContainer;
    @BindView(R.id.birthdate)           TextView birthdate;
    @BindView(R.id.country)             Spinner country;
    @BindView(R.id.send_progress)       View progressView;
    @BindView(R.id.send_form)           View sendFormView;
    private int mYear, mMonth, mDay;

    private Call currentCall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        setupCountrySpinner();
    }

    private void showDateDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        birthdate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        mDay = dayOfMonth;
                        mMonth = monthOfYear;
                        mYear = year;
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    @OnClick(R.id.send_button)
    public void attemptSignUp(View v) {
        attemptSignUp();
    }

    @OnClick(R.id.birthdateBtn)
    public void showDateDialog(View v) {
        showDateDialog();
    }

    private void setupCountrySpinner() {
        Locale[] locale = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<>();
        String country;
        for( Locale loc : locale ){
            country = loc.getDisplayCountry();
            if( country.length() > 0 && !countries.contains(country) ){
                countries.add( country );
            }

        }
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
        countries.add(0, getString(R.string.select_country));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.spinner_item, countries);

        this.country.setAdapter(adapter);
        this.country.setSelection(0);
    }

    private void attemptSignUp() {
        // Reset errors.
        emailContainer.setError(null);
        firstNameContainer.setError(null);
        lastNameContainer.setError(null);

        // Store values at the time of the login attempt.
        String userEmail = email.getText().toString();
        String userFirstName = firstName.getText().toString();
        String userLastName = lastName.getText().toString();
        String userBirthdate = birthdate.getText().toString();
        String userCountry = country.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(userEmail)) {
            emailContainer.setError(getString(R.string.error_field_required));
            focusView = email;
            cancel = true;
        } else if (!isEmailValid(userEmail)) {
            emailContainer.setError(getString(R.string.error_invalid_email));
            focusView = email;
            cancel = true;
        }
        if (TextUtils.isEmpty(userFirstName)) {
            firstNameContainer.setError(getString(R.string.error_field_required));
            focusView = (focusView == null ) ? firstName : focusView;
            cancel = true;
        } else if (userFirstName.length() < 3) {
            firstNameContainer.setError(getString(R.string.error_min_chars));
            focusView = (focusView == null ) ? firstName : focusView;
            cancel = true;
        }

        if (TextUtils.isEmpty(userLastName)) {
            lastNameContainer.setError(getString(R.string.error_field_required));
            focusView = (focusView == null ) ? lastName : focusView;
            cancel = true;
        } else if (userLastName.length() < 3) {
            lastNameContainer.setError(getString(R.string.error_min_chars));
            focusView = (focusView == null ) ? lastName : focusView;
            cancel = true;
        }

        boolean showAlert = false;
        String alertMessage = "";
        if (!cancel) {
            StringBuilder stringBuilder = new StringBuilder();
            if (TextUtils.isEmpty(userBirthdate)) {
                stringBuilder.append(getString(R.string.error_birthdate_required));
                stringBuilder.append("\n");
                cancel = true;
                showAlert = true;
            }
            if (userCountry.equals(getString(R.string.select_country))) {
                stringBuilder.append(getString(R.string.error_country_required));
                cancel = true;
                showAlert = true;
            }
            alertMessage = stringBuilder.toString();
        }


        if (cancel) {
            if (showAlert) {
                showAlert(alertMessage);
            } else {
                focusView.requestFocus();
            }

        } else {
            showProgress(true);
            sendForm(userEmail, userFirstName, userLastName, userBirthdate, userCountry);
        }
    }

    private void showAlert(String alertMessage) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
            .setMessage(alertMessage)
            .setPositiveButton(getString(R.string.ok), null).create();
        alertDialog.show();
    }

    private boolean isEmailValid(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(email).matches();
    }

    /**
     * Shows the progress UI and hides the send form.
     */

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        sendFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        sendFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                sendFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void sendForm(String email, String firstName, String lastName,
                          String birthdate, String country) {


        RestInterface client = ServiceGenerator.createService(RestInterface.class);

        final Context context = this;
        Call<ApiResponse> call = client.sendData(email, firstName, lastName, birthdate, country);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                showProgress(false);
                if (response.body() != null) {

                } else {
                    Toast.makeText(context,
                            "Failure on sendData", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showProgress(false);
                if (call.isCanceled()) {
                    Log.e("Adidas Events", "request was cancelled");
                }
                else {
                    Toast.makeText(context,
                            "Failure on sendData", Toast.LENGTH_SHORT).show();
                }
            }
        });
        currentCall = call;
    }

    @Override
    protected void onDestroy() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        super.onDestroy();
    }
}

