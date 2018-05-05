package kr.susemi99.server;

import android.text.TextUtils;
import android.util.Log;

import com.aevi.android.rxmessenger.service.AbstractMessengerService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyService extends AbstractMessengerService {
  private static final String IMMEDIATE = "IMMEDIATE";
  private static final String DELAY = "DELAY";
  private static final String CONTINUOUS = "CONTINUOUS";
  private static final String FORCE_INTERRUPT = "FORCE_INTERRUPT";

  private Disposable continuousDisposable, delayDisposable;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i("APP# MyService | onCreate", "=========== 서비스 시작됨 ===========");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i("APP# MyService | onCreate", "----------- 서비스 종료됨 -----------");

    try {
      continuousDisposable.dispose();
    } catch (Exception ignore) {}

    try {
      delayDisposable.dispose();
    } catch (Exception ignore) {}
  }

  @Override
  protected void handleRequest(String clientId, String requestData, String packageName) {
    Log.i("APP# MyService | handleRequest", "clientId: " + clientId + ", request data: " + requestData + ", packageName: " + packageName);

    if (IMMEDIATE.equals(requestData)) {
      handleImmediate(clientId);
    }
    else if (DELAY.equals(requestData)) {
      handleDelay(clientId);
    }
    else if (CONTINUOUS.equals(requestData)) {
      handleContinuous(clientId);
    }
    else if (FORCE_INTERRUPT.equals(requestData)) {
      handleForceInterrupt();
    }
    else {
      sendErrorMessageToClient(clientId, "404", "not found");
      sendEndStreamMessageToClient(clientId);
    }
  }

  private void handleImmediate(String clientId) {
    try {
      sendMessageToClient(clientId, DateFormatter.format(System.currentTimeMillis()) + "(" + MySingleton.getInstance().getCount() + ")");
    } catch (Exception e) {
      sendErrorMessageToClient(clientId, "001", e.getMessage());
      sendEndStreamMessageToClient(clientId);
    }

    sendEndStreamMessageToClient(clientId);
  }

  private void handleDelay(String clientId) {
    ArrayList<String> response = new ArrayList<>();
    response.add("received: " + DateFormatter.format(System.currentTimeMillis()));

    delayDisposable = Completable.timer(30, TimeUnit.SECONDS)
      .subscribeOn(Schedulers.computation())
      .subscribe(() -> {
        response.add("response: " + DateFormatter.format(System.currentTimeMillis()));
        sendMessageToClient(clientId, TextUtils.join("\n", response));
        sendEndStreamMessageToClient(clientId);
      }, throwable -> {
        throwable.printStackTrace();
        sendErrorMessageToClient(clientId, "002", throwable.getMessage());
        sendEndStreamMessageToClient(clientId);
      });
  }

  private void handleContinuous(String clientId) {
    continuousDisposable = Observable.interval(3, TimeUnit.SECONDS)
      .subscribeOn(Schedulers.computation())
      .subscribe(__ -> sendMessageToClient(clientId, "time tick: " + DateFormatter.format(System.currentTimeMillis())),
        throwable -> {
          sendErrorMessageToClient(clientId, "003", throwable.getMessage());
          sendEndStreamMessageToClient(clientId);
        });
  }

  private void handleForceInterrupt() {
    throw new NullPointerException();
  }
}
