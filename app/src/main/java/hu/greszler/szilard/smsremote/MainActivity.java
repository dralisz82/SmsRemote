/*
 *     SmsRemote - Remote controls an Android phone by text messages.
 *     Copyright (C) 2022  Szilárd Greszler
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package hu.greszler.szilard.smsremote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean checkPerms(boolean request) {
        List<String> permissionsToRequest = new ArrayList<String>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(Manifest.permission.CALL_PHONE);
        if(request && permissionsToRequest.size() > 0) {
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)||
                    shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)||
                    shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)||
                    shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE))
                AppInfo.showPermissionsRationale(this, permissionsToRequest);
        }
        return (permissionsToRequest.size() == 0);
    }

    public boolean requestPerms(List<String> permissionsToRequest) {
        final int REQUEST_CODE=1;
        ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_CODE);

        if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED))
            return false;
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Checking and requesting permissions, if needed
        checkPerms(true);

        // Subscribing to battery events
        BroadcastReceiver receiver = new BatteryLevelReceiver();
        IntentFilter filter =new IntentFilter(BatteryManager.EXTRA_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        this.registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Settings");
        menu.add(Menu.NONE, 1, 0, "Help");
        menu.add(Menu.NONE, 2, 0, "About");
        menu.add(Menu.NONE, 3, 0, "Donate");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case 1:
                AppInfo.showHelp(this);
                return true;
            case 2:
                AppInfo.showAbout(this);
                return true;
            case 3:
                AppInfo.showDonate(this);
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle bundle = getIntent().getExtras();
        TextView tv = (TextView)findViewById(R.id.textView_status);
        tv.setText("No action is in progress...\nCurrently redirected to: " + sharedPrefs.getString("targetPhoneNumber", "nobody"));
        if(bundle == null) {
            return;
        }
        String action = bundle.getString("action");
        String phoneNumber = bundle.getString("phoneNumber");

        if(action == null) {
            finish();
            return;
        }
        if((action.equals("FRWD")||(action.equals("QUERY"))) && !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            finish();
            return;
        }

        String callString = "";
        String message = "";

        // Perform actions
        if(action.equals("FRWD")) {
            callString = sharedPrefs.getString("callForwardPrefix", "**21*")+phoneNumber+sharedPrefs.getString("callForwardPostfix", "#");
            tv.setText("Setting call forwarding to number: "+phoneNumber+" is in progress...");
            Telephony.placeCall(this, callString);
            // Send sms to old target
            if(sharedPrefs.getBoolean("notifyOld", true)) {
                message = sharedPrefs.getString("phoneName", "Remote phone")+" is now redirected to "+phoneNumber;
                String oldTarget = sharedPrefs.getString("targetPhoneNumber", "");
                if(oldTarget.length() > 0 && !oldTarget.equals("nobody"))
                    Telephony.sendSms(this, oldTarget, message);
            }
            // Send sms to new target
            if(sharedPrefs.getBoolean("notifyNew", true)) {
                message = sharedPrefs.getString("phoneName", "Remote phone")+" is now redirected to your phone.";
                Telephony.sendSms(this, phoneNumber, message);
            }
            sharedPrefs.edit().putString("targetPhoneNumber", phoneNumber).commit();
        }
        if(action.equals("CLFW")) {
            callString = sharedPrefs.getString("clearForwardCode", "##21#");
            tv.setText("Clearing call forwarding is in progress...");
            Telephony.placeCall(this, callString);
            // Send sms to old target
            if(sharedPrefs.getBoolean("notifyOld", true)) {
                message = sharedPrefs.getString("phoneName", "Remote phone")+" is now redirected to nobody.";
                Telephony.sendSms(this, sharedPrefs.getString("targetPhoneNumber", ""), message);
            }
            sharedPrefs.edit().putString("targetPhoneNumber", "nobody").commit();
        }
        if(action.equals("TESTCALL")) {
            callString = sharedPrefs.getString("testcallNumber" , "");
            tv.setText("Setting call forwarding to number: "+phoneNumber+" is in progress...");
            Telephony.placeCall(this, callString);
        }
        if(action.equals("QUERY")) {
            tv.setText("Querying redirection from number: "+phoneNumber+" is in progress...");
            message = "Remote phone is currently redirected to: "+sharedPrefs.getString("targetPhoneNumber", "nobody");
            Telephony.sendSms(this, phoneNumber, message);
        }

        finish();
    }
}