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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import java.util.List;

public class AppInfo {
    private static void showMessage(Context context, String str, DialogInterface.OnClickListener ocl) {
        SpannableString message = new SpannableString(str);
        Linkify.addLinks(message, Linkify.WEB_URLS);
        final TextView messageTV = new TextView(context);
        messageTV.setText(message);
        messageTV.setPadding(25, 25, 25, 25);
        messageTV.setMovementMethod(LinkMovementMethod.getInstance());
        AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
        alertbox.setView(messageTV);
        alertbox.setNeutralButton("Close", null);
        if(ocl != null)
            alertbox.setPositiveButton("OK", ocl);
        alertbox.show();
    }

    public static void showAbout(Context context) {
        String version = "";
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String str = "SmsRemote\n" +
                "Version: "+version + "\n\n" +
                "Created by Szilárd Greszler\n" +
                "Copyright (c) 2022\n\n" +
                "More info: https://github.com/dralisz82/SmsRemote";
        showMessage(context, str, null);
    }

    public static void showHelp(Context context) {
        showMessage(context, "Visit online help:\n\n" +
                "https://github.com/dralisz82/SmsRemote/blob/main/HELP.md", null);
    }

    public static void showDonate(Context context) {
        showMessage(context, "You may send me donation via PayPal:\n\n" +
                "https://paypal.me/greszlerszilard\n\n" +
                "This makes it possible for me to secure time and resources to maintain and improve the app.\n" +
                "Thanks!", null);
    }

    public static void showPermissionsRationale(Context context, List<String> permissionsToRequest) {
        showMessage(context, "SmsRemote receives commands in incoming SMS and sends notifications in outgoing SMS messages.\n" +
                "For this purpose permission is needed to receive and send SMS messages.\n\n" +
                "Core feature is remote set and clear of call-forwarding. Permissions to place a call is needed to fire MMI rules.\n\n" +
                "See our privacy policy: https://szilard.greszler.hu/en/2022/02/08/privacy-policy-for-smsremote-android-app/",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MainActivity)context).requestPerms(permissionsToRequest);
                    }
                });
    }
}
