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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Skip if service is not enabled
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(!sharedPrefs.getBoolean("enableService", false))
            return;

        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;

        if(bundle != null) {
            Object[] pdus = (Object[])bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            int i = 0;
            for(i=0;i<msgs.length;i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                processSMS(context, msgs[i]);
            }
        }
    }

    private void processSMS(Context context, SmsMessage sms) {
        String smsText = sms.getMessageBody();

        // Skip if not a control sms
        if(!smsText.startsWith("TL "))
            return;

        String[] words = smsText.split("[ ]+");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(!sharedPrefs.getString("smsAuthCode", "").equals(words[1])) {
            Toast.makeText(context, R.string.app_name + ": Received SMS with invalid auth code", Toast.LENGTH_SHORT).show();
            return;
        }
        if(words.length < 3) {
            Toast.makeText(context, "SmsRemote: Not enough arguments", Toast.LENGTH_SHORT).show();
            return;
        }
        if(words[2].equals("FRWD") && words.length == 4)
            launchAction(context, "FRWD", words[3]);
        else if(words[2].equals("CLFW"))
            launchAction(context, "CLFW", "");
        else if(words[2].equals("TESTCALL"))
            launchAction(context, "TESTCALL", "");
        else if(words[2].equals("QUERY"))
            launchAction(context, "QUERY", sms.getOriginatingAddress());
        else
            Toast.makeText(context, "SmsRemote: Unknown command or not enough arguments", Toast.LENGTH_SHORT).show();
    }

    private void launchAction(Context context, String action, String phoneNumber) {
        if(!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Toast.makeText(context, "SmsRemote: Invalid phone number!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(context, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        bundle.putString("phoneNumber", phoneNumber);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        String toastStr = "";
        if(action.equals("FRWD"))
            toastStr = "SmsRemote: Redirected to " + phoneNumber;
        if(action.equals("CLFW"))
            toastStr = "SmsRemote: Redirection turned OFF";
        if(action.equals("TESTCALL"))
            toastStr = "SmsRemote: Calling the Emergency Number";
        if(action.equals("QUERY"))
            toastStr = "SmsRemote: Querying redirection";
        Toast.makeText(context, toastStr, Toast.LENGTH_SHORT).show();
    }
}