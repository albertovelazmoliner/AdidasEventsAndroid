package com.albertovelaz.adidasevents;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.albertovelaz.adidasevents.config.ServiceGenerator;
import com.albertovelaz.adidasevents.interfaces.RestInterface;
import com.albertovelaz.adidasevents.models.AdidasEvent;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.image) public ImageView imageView;
    @BindView(R.id.eventName) public TextView eventName;
    @BindView(R.id.joinBtn) public Button joinBtn;
    @BindView(R.id.loadBtn) public Button loadBtn;


    private Call currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (isConnected()) {
            loadData();
        } else {
            showAlert(getString(R.string.no_connected));
            loadBtn.setVisibility(View.VISIBLE);
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @OnClick(R.id.joinBtn)
    public void joinEvent(View v) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.loadBtn)
    public void loadEvent(View v) {
        loadData();
        loadBtn.setVisibility(View.GONE);
    }

    private void loadData() {
        RestInterface client = ServiceGenerator.createService(RestInterface.class);
        Call<AdidasEvent> call = client.loadDataEvent();
        final Context context = this;
        call.enqueue(new Callback<AdidasEvent>() {
            @Override
            public void onResponse(Call<AdidasEvent> call, Response<AdidasEvent> response) {
                if (response.body() != null) {
                    AdidasEvent event = response.body();
                    eventName.setText(event.getName());
                    Glide.with(context)
                            .load(event.getImage())
                            .into(imageView);
                    joinBtn.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(context,
                            "Failure on getEventData", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdidasEvent> call, Throwable t) {
                if (call.isCanceled()) {
                    Log.e("Adidas Events", "request was cancelled");
                }
                else {
                    Toast.makeText(context,
                            "Failure on getEventData", Toast.LENGTH_SHORT).show();
                }
            }
        });
        currentCall = call;
    }

    private void showAlert(String alertMessage) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(alertMessage)
                .setPositiveButton(getString(R.string.ok), null).create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        super.onDestroy();
    }
}
