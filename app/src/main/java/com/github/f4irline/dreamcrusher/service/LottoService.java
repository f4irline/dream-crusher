package com.github.f4irline.dreamcrusher.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Button;

import com.github.f4irline.dreamcrusher.LottoLogic;
import com.github.f4irline.dreamcrusher.MainActivity;
import com.github.f4irline.dreamcrusher.R;
import com.github.f4irline.dreamcrusher.utils.Debug;

import java.util.ArrayList;
import java.util.TreeSet;

public class LottoService extends Service implements Runnable {
    final String TAG = this.getClass().getName();

    private IBinder binder;
    private boolean calculatingLotto;

    private TreeSet<Integer> selection;
    private ArrayList<Button> buttons;
    private Thread lottoThread;

    private int THREAD_SPEED;
    private int WEEKS;

    /**
     * Called when bound.
     *
     * @param intent operation
     * @return the bound binder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Debug.print(TAG, "onBind()", "Service bound.", 1, this);
        calculatingLotto = false;
        THREAD_SPEED = 550;
        WEEKS = 0;

        // Returns the IBinder, which wraps LottoService inside it.
        return binder;
    }

    /**
     * Creates the binder component.
     */
    @Override
    public void onCreate () {
        Debug.print(TAG, "onCreate()", "Service created.", 1, this);
        binder = new BinderComponent(this);
    }

    @Override
    public void onDestroy() {
        Debug.print(TAG, "onDestroy()", "Destroyed Service", 1, this);
        calculatingLotto = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debug.print(TAG, "onStartCommand()", "Service started.", 1, this);
        return START_STICKY;
    }

    /**
     * Starts calculating the lotto. Called when user clicks "I FEEL LUCKY" -button.
     * @param userSelection the user's number selection in a TreeSet
     * @param allButtons all the buttons in the grid.
     */
    public void startLotto(TreeSet<Integer> userSelection, ArrayList<Button> allButtons) {
        selection = userSelection;
        buttons = allButtons;

        // If we're not currently calculating the lotto, start a new thread.
        if (!calculatingLotto) {
            lottoThread = new Thread(this);
            Debug.print(TAG, "startLotto", "Amount of threads running: "+Thread.activeCount(), 1, this);
            lottoThread.start();
            calculatingLotto = true;
        }
    }

    /**
     * Called when user clicks the stop iteration button to stop the lotto iteration.
     */
    public void stopLotto() {
        if (calculatingLotto) {
            calculatingLotto = false;
        }
    }

    /**
     * Increases the iteration speed in the thread.
     */
    public void increaseSpeed() {
        if (THREAD_SPEED > 50) {
            THREAD_SPEED -= 100;
        }
    }

    /**
     * Decreases the iteration speed in the thread.
     */
    public void decreaseSpeed() {
        if (THREAD_SPEED < 3000) {
            THREAD_SPEED += 100;
        }
    }

    /**
     * Called when thread is started.
     */
    @Override
    public void run() {
        // While we're calculating (user has not clicked "stop iteration" yet),
        // run this loop.
        while (calculatingLotto) {
            try {
                initButtons();
                checkNumbers();
                // Broadcast the random numbers that the computer randomly generated
                // and broadcast the weeks (1 iteration = 1 week).
                createBroadcast(new Intent("lotto")
                    .putExtra("randomNumbers", new ArrayList<>(LottoLogic.getRandomNumbers()))
                    .putExtra("weeks", WEEKS));

                Thread.sleep(THREAD_SPEED);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WEEKS++;
        }
    }

    /**
     * Initializes the button grid. Called every iteration to remove
     * the random button styles so they can be added on different numbers again.
     */
    private void initButtons() {
        for (Button b : buttons) {
            b.setBackground(getResources().getDrawable(R.drawable.number_button));
        }
    }

    /**
     * Calls LottoLogic first to generate the random numbers and then calls LottoLogic to
     * check how many numbers are same in the random generated numbers and the user selection.
     */
    private void checkNumbers() {
        LottoLogic.generateLottoNumbers();
        int sameNumbers = LottoLogic.checkNumbers(selection);
        // If we've 7 of the same, stop this service and stop lotto as well.
        if (sameNumbers == 7) {
            stopSelf();
            stopLotto();
            // Broadcast victory to the main activity and display notification to user.
            createBroadcast(new Intent("lotto")
                .putExtra("victory", true));
            displayNotification("You won!", "Found 7 of the same numbers!");
        }
        Debug.print(TAG, "checkNumbers", "Amount of same numbers: "+sameNumbers, 1, this);
    }

    /**
     * Handles creating the broadcast.
     * @param broadcastIntent the operation to send the broadcast.
     */
    private void createBroadcast(Intent broadcastIntent) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(broadcastIntent);
    }

    /**
     * Handles displaying notifications.
     *
     * @param title the title of the notification
     * @param text the text of the notification
     */
    private void displayNotification(String title, String text) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Register the applications notification channel
        createNotificationChannel();

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "WIN_NOTIFICATION")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent);

        // Get the service from the device where notifications are handled.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int mId = 1;
        // Display the notification
        notificationManager.notify(mId, notificationBuilder.build());
    }

    /**
     * Registers the applications notification channel with the system.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lotto";
            String description = "Lotto Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("WIN_NOTIFICATION", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Checks if the thread is running (if we're calculating lotto).
     *
     * @return true if the thread is running, false if not
     */
    public boolean getServiceRunning() {
        return calculatingLotto;
    }
}
