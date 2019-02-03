package com.github.f4irline.dreamcrusher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.f4irline.dreamcrusher.service.BinderComponent;
import com.github.f4irline.dreamcrusher.service.LottoService;
import com.github.f4irline.dreamcrusher.utils.Debug;

import java.util.ArrayList;
import java.util.TreeSet;

public class MainActivity extends BaseActivity {
    private TreeSet<Integer> selection = new TreeSet<>();
    private ArrayList<Button> buttons = new ArrayList<>();
    private boolean selectionFull = false;

    private TextView yearsText;
    private int years;
    private String yearsSpent;

    private ConnectionComponent connectionToService;
    private LottoService lottoService;
    private boolean isBound = false;

    private Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Log with the following command in CLI:
         * "adb logcat com.github.f4irline.dreamcrusher.MainActivity:D com.github.f4irline.dreamcrusher.service.LottoService *:S"
         */
        Debug.loadDebug(this);

        appContext = this;
        yearsText = findViewById(R.id.yearsText);
        yearsSpent = "0 years spent";
        yearsText.setText(yearsSpent);
        years = 0;

        initButtonsArray();
        connectionToService = new ConnectionComponent();

        // Register the activity to receive broadcasts locally, with the filter "lotto".
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastComponent(), new IntentFilter("lotto"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.print(TAG, "onStart()", "Activity started.", 1, this);

        // Bind to service and create the service as long as the binding exists.
        // Start the service after.
        Intent intent = new Intent(this, LottoService.class);
        bindService(intent, connectionToService, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    /**
     * Stop the activity when user presses back, because Android
     * doesn't save the user selection when pressing back (to be implemented)
     */
    @Override
    public void onBackPressed() {
        onStop();
        System.exit(0);
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            Debug.print(TAG, "onStop()", "Activity stopped.", 1, this);
            unbindService(connectionToService);
            isBound = false;
        }
    }

    /**
     * Initializes the buttons array for easy deactivating and activating of buttons etc.
     */
    private void initButtonsArray() {
        TableLayout buttonsTable = (TableLayout) findViewById(R.id.tableOfNumbers);
        ArrayList<View> tableButtons = buttonsTable.getTouchables();

        for (View v : tableButtons) {
            Button button = (Button) findViewById(v.getId());
            buttons.add(button);
        }
    }

    /**
     * Handles choosing numbers from the TableLayout.
     *
     * If the button was not activated, it activates it and
     * adds it to the TreeSet. If it was activated, it deactivates
     * it and removes it from the TreeSet.
     *
     * @param v the button which launched the event.
     */
    public void chooseNumber(View v) {
        Button b = (Button) findViewById(v.getId());

        if (b.isActivated()) {
            int number = Integer.parseInt(b.getText().toString());
            selection.remove(number);
            b.setActivated(false);
        } else {
            int number = Integer.parseInt(b.getText().toString());
            selection.add(number);
            b.setActivated(true);
        }

        checkSelectionSize();
    }

    /**
     * Checks if the selection size is 7 and if it is, it disables the buttons.
     * If it's not (a button was deactivated), it enables the buttons again.
     */
    private void checkSelectionSize() {
        if (selection.size() > 6) {
            toggleButtons(false);
            selectionFull = true;
        } else if (selectionFull) {
            toggleButtons(true);
            selectionFull = false;
        }
    }

    /**
     * Iterates through the buttons ArrayList and either enables or disables
     * all the buttons.
     *
     * @param enable true if less than 7 buttons are activated, false if 7 buttons are
     *               activated.
     */
    private void toggleButtons(boolean enable) {
        for (Button b : buttons) {
            if (!b.isActivated()) {
                b.setEnabled(enable);
            }
        }
    }

    /**
     * Handles the click event from "I Feel Lucky" button.
     *
     * When it's clicked the first time, it starts the lotto and sets
     * the text to "Stop Iteration". Next click it stops the lotto and sets
     * the text back to "I Feel Lucky".
     *
     * @param v the lotto button.
     */
    public void startLottoHandler(View v) {
        if (!lottoService.getServiceRunning()) {
            setStopButton();
            lottoService.startLotto(selection, buttons);
        } else {
            setStartButton();
            lottoService.stopLotto();
        }
    }

    /**
     * Sets the lotto button to display "Stop Iteration".
     *
     */
    private void setStopButton() {
        Button startButton = (Button) findViewById(R.id.start);
        startButton.setText("Stop Iteration");
    }

    /**
     * Sets the lotto button to display "I Feel Lucky".
     *
     */
    private void setStartButton() {
        Button startButton = (Button) findViewById(R.id.start);
        startButton.setText("I Feel Lucky");
    }

    /**
     * Handles the styling of the random generated 7 numbers.
     *
     * @param randomNumbers the random numbers.
     */
    private void styleRandomButtons(ArrayList<Integer> randomNumbers) {
        // Loop through the arraylist which holds the grid of buttons
        for (Button b : buttons) {
            // Get number from every button
            int number = Integer.parseInt(b.getText().toString());
            // If the random numbers arraylist contains the number, set the button style to be
            // the random style
            if (randomNumbers.contains(number)) {
                b.setBackground(getResources().getDrawable(R.drawable.random_number_button));
            }
        }
    }

    /**
     * Prepares the screen's standard options menu to be displayed.
     * @param menu the options menu
     * @return true
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Initializes the contents of the standard options menu.
     * @param menu the options menu.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    /**
     * Called whenever a item in the options menu is selected.
     * @param item the item which was clicked.
     * @return true if a valid item was clicked, false otherwise for fallback.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.plus):
                lottoService.increaseSpeed();
                return true;
            case (R.id.minus):
                lottoService.decreaseSpeed();
                return true;
            case (R.id.five):
                lottoService.setDifficulty(5);
                item.setChecked(true);
                return true;
            case (R.id.six):
                lottoService.setDifficulty(6);
                item.setChecked(true);
                return true;
            case (R.id.seven):
                lottoService.setDifficulty(7);
                item.setChecked(true);
                return true;
        }
        return false;
    }

    /**
     * Helper component to bind the service.
     */
    class ConnectionComponent implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Debug.print(TAG, "onServiceConnected()", "Service connected", 1, getParent());
            // We've bound to BinderComponent, cast the IBinder and get the BinderComponent instance.
            BinderComponent binder = (BinderComponent) service;
            lottoService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Debug.print(TAG, "onServiceDisconnected()", "Service disconnected", 1, getParent());
            isBound = false;
        }
    }

    /**
     * Handles receiving broadcasts from the LottoService.
     */
    class BroadcastComponent extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extras from the broadcast
            Bundle extras = intent.getExtras();

            // Check that the extras are not null
            if (extras != null) {
                // Check if we've won, if we have, reset start button
                if (extras.getBoolean("victory")) {
                    setStartButton();
                    Toast.makeText(appContext, "Found " + extras.getInt("amount") + " of the same numbers!", Toast.LENGTH_LONG).show();
                }

                // Check if we have randomNumbers key, if we do, send them to styleRandomButtons() method
                // which handles toggling the randomNumbers style.
                if (extras.containsKey("randomNumbers")) {
                    ArrayList<Integer> randomNumbers = extras.getIntegerArrayList("randomNumbers");
                    styleRandomButtons(randomNumbers);
                }

                // Count years that we've spent trying to win (1 iteration = 1 week)
                if (extras.containsKey("weeks")) {
                    int weeks = extras.getInt("weeks");
                    if (weeks % 52 == 0) {
                        years++;
                        yearsSpent = years+" years spent.";
                    }
                    yearsText.setText(yearsSpent);
                }
            }
        }
    }
}
