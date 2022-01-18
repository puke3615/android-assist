package com.puke.assist.core;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.puke.assist.api.AssistLog;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 配置页面支持远端动态输入
 *
 * @author puke
 * @version 2022/1/17
 */
class RobotManager {

    private static final String WS_URL = "ws://119.23.47.238:9000/ws/android";
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static WeakReference<Activity> activityRef;
    private static WsClient client;

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                activityRef = new WeakReference<>(activity);

                if (canConnect(activity)) {
                    EXECUTOR.execute(() -> {
                        try {
                            client = new WsClient(new URI(WS_URL));
                            client.connect();
                        } catch (Exception e) {
                            AssistLog.e("连接失败", e);
                        }
                    });
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                if (canConnect(activity)) {
                    closeConnect();
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if ((activity == getCurrentActivity())) {
                    activityRef = null;
                }
            }
        });
    }

    private static boolean canConnect(Activity activity) {
        return activity instanceof AssistConfigActivity;
    }

    private static Activity getCurrentActivity() {
        return activityRef == null ? null : activityRef.get();
    }

    private static void setText(String text) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        HANDLER.post(() -> {
            View currentFocus = activity.getCurrentFocus();
            if ((!(currentFocus instanceof EditText))) {
                return;
            }

            EditText input = (EditText) currentFocus;
            input.setText(text);
        });
    }

    private static void closeConnect() {
        try {
            if (client != null) {
                if (client.isOpen()) {
                    client.close();
                }
                client = null;
            }
        } catch (Exception ignored) {
        }
    }

    private static class WsClient extends WebSocketClient {

        public WsClient(URI serverUri, Draft draft) {
            super(serverUri, draft);
        }

        public WsClient(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("new connection opened");
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("closed with exit code " + code + " additional info: " + reason);
        }

        @Override
        public void onMessage(String message) {
            System.out.println("received message: " + message);

            try {
                RobotAction action = JSON.parseObject(message, RobotAction.class);
                switch (action.command) {
                    case "INPUT":
                        setText(action.data);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(ByteBuffer message) {
            System.out.println("received ByteBuffer");
        }

        @Override
        public void onError(Exception ex) {
            System.err.println("an error occurred:" + ex);
        }
    }

    public static class RobotAction implements Serializable {
        public String command;
        public String data;
    }
}
