# SmsRemote online help

## Grant permissions
The app requests permissions on startup to send/receive text messages and place calls.
These permissions are vital for the app's key features. Without granting them the app will not start.
The app utilizes these permissions only for advertized purposes.

## Configuration

Tap the ... button in the action bar and tap on Settings

Enable service if you want the app to respond on commands received in SMS text messages.

Authentication code is a secret passcode which you need to send alongside the commands in the commanding text messages.
This prevents the app to accept commands from unauthorized sources.

Phone name is a given name of the phone. It will be displayed in outgoing notification text messages.
It is useful if you control multiple phones running SmsRemote or such notifications are sent to phones, where the phone number of the phone is not saved in contacts.

Test-Call number: You can instruct the phone remotely to call this number
It is useful in some cases to test call-forwarding.

Notify old/new target: If it is enabled, notification is sent to old/new phone numbers, when call-forwarding is altered.

Call-forward pre- and postfixes and clear code: These MMI codes are used to initiate and clear call-forwarding. Default values work in most coutries, but it is configurable for your convenience.

Supervisor phone number: It is the phone number of the "owner" of the phone. A notification text message is sent to this number if the battery of the phone is getting low.

## Controlling text messages

### Format of the controlling messages is:
> TL \<PIN code\> \<command\> \<arguments\>

### Below commands are supported:

Set up call-forwarding: **TL \<PIN code\> FRWD \<target number\>**
> Example:
> 
> TL 12345 FRWD +36301234567

Clear call-forwarding: **TL \<PIN code\> CLFW**
> Example:
> 
> TL 12345 CLFW


Query call-forwarding: **TL \<PIN code\> QUERY**
> Example:
> 
> TL 12345 QUERY


Make a test-call: **TL \<PIN code\> TESTCALL**
> Example:
> 
> TL 12345 TESTCALL
