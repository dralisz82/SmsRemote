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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

public class Telephony {
    public static void placeCall(final Context context, final String callString) {
        Intent intentCallForward = new Intent(Intent.ACTION_CALL);
        Uri uri2 = Uri.fromParts("tel", callString, "#");
        intentCallForward.setData(uri2);
        context.startActivity(intentCallForward);
    }

    public static void sendSms(final Context context, final String phoneNumber, final String message) {
        if(phoneNumber.length() == 0 || !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber))
            return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        SmsManager smsMgr = SmsManager.getDefault();
        smsMgr.sendTextMessage(phoneNumber, null, message, pi, null);
    }
}
