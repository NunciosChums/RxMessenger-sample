package kr.susemi99.client;

import android.content.ComponentName;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.aevi.android.rxmessenger.client.ObservableMessengerClient;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
  private static final String IMMEDIATE = "IMMEDIATE";
  private static final String DELAY = "DELAY";
  private static final String CONTINUOUS = "CONTINUOUS";
  private static final String FORCE_INTERRUPT = "FORCE_INTERRUPT";

  private static final ComponentName SERVICE = new ComponentName("kr.susemi99.server", "kr.susemi99.server.MyService");

  private TextView immediateResponse, delayResponse, continuousResponse;
  private Disposable immediateDisposable, delayDisposable, continuousDisposable;
  private ArrayList<ObservableMessengerClient> clients = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    immediateResponse = findViewById(R.id.immediateResponse);
    delayResponse = findViewById(R.id.delayResponse);
    continuousResponse = findViewById(R.id.continuousResponse);

    findViewById(R.id.immediateButton).setOnClickListener(__ -> requestImmediate());
    findViewById(R.id.delayButton).setOnClickListener(__ -> requestDelay());
    findViewById(R.id.continuousButton).setOnClickListener(__ -> requestContinuous());
    findViewById(R.id.interruptButton).setOnClickListener(__ -> requestForceInterrupt());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    try {
      immediateDisposable.dispose();
    } catch (Exception ignore) {}

    try {
      delayDisposable.dispose();
    } catch (Exception ignore) {}

    try {
      continuousDisposable.dispose();
    } catch (Exception ignore) {}

    for (ObservableMessengerClient client : clients) {
      client.closeConnection();
    }
  }

  private ObservableMessengerClient client() {
    ObservableMessengerClient client = new ObservableMessengerClient(getApplicationContext(), SERVICE);
    clients.add(client);
    return client;
  }

  private void requestImmediate() {
    immediateResponse.setText(null);

    try {
      immediateDisposable.dispose();
    } catch (Exception ignore) {}

    ObservableMessengerClient client = client();

    immediateDisposable = client.sendMessage(IMMEDIATE)
      .subscribe(response -> {
        Log.i("APP# MainActivity | requestImmediate", "response: " + response);
        immediateResponse.setText(response);
      }, throwable -> {
        throwable.printStackTrace();
        immediateResponse.setText(throwable.getMessage());
      }, () -> {
        client.closeConnection();
        clients.remove(client);
      });
  }

  private void requestDelay() {
    delayResponse.setText("request: " + DateFormatter.format(System.currentTimeMillis()));

    try {
      delayDisposable.dispose();
    } catch (Exception ignore) {}

    ObservableMessengerClient client = client();

    delayDisposable = client.sendMessage(DELAY)
      .subscribe(response -> {
        Log.i("APP# MainActivity | requestDelay", "response: " + response);
        delayResponse.append("\n" + response);
      }, throwable -> {
        throwable.printStackTrace();
        delayResponse.setText(throwable.getMessage());
      }, () -> {
        client.closeConnection();
        clients.remove(client);
      });
  }

  private void requestContinuous() {
    continuousResponse.setText(null);

    try {
      continuousDisposable.dispose();
    } catch (Exception ignore) {}

    continuousDisposable = client().sendMessage(CONTINUOUS)
      .subscribe(
        response -> continuousResponse.setText(response),
        throwable -> {
          throwable.printStackTrace();
          continuousResponse.setText(throwable.getMessage());
        });
  }

  private void requestForceInterrupt() {
    client().sendMessage(FORCE_INTERRUPT).subscribe();
  }
}
