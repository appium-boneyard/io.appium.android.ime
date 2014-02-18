io.appium.android.ime
=====================

The Android emulator does not play well with non-ASCII characters. Text sent to the emulator through Selenium will be filtered through the default keyboard, which means that things get stripped down to ASCII. "परीक्षणम्" ("Testing" in Sanskrit) becomes "prksnm".

To get around this, this [input method](http://developer.android.com/guide/topics/text/creating-input-method.html), when set as the default, catches Unicode text encoded as [Modified UTF-7](http://tools.ietf.org/html/rfc3501) and recodes it as Unicode. Text input fields can then receive and send any characters that Unicode can encode.

To install, with the emulator running execute the following:

```shell
ant debug
adb uninstall io.appium.android.ime
adb install bin/UnicodeIME-debug.apk
```

This builds the application, removes any old version of it, and then installs the recently built one.

Once the input method is installed on the emulator, go to the emulator and access `Settings`, then `Language & Input`. Make sure `Appium Android Input Manager for Unicode` is selected, and finally go to `Default` and set it to `Appium Android Input Manager for Unicode`.

Your emulator is now set to receive encoded text from [Appium](http://appium.io/)!

On the Appium Bootstrap side, there needs to be an encoding of the text into Modified UTF-7:

```java
import java.nio.charset.Charset;


public class UnicodeEncoder {
    private static final Charset M_UTF7 = Charset.forName("x-IMAP-mailbox-name");
    private static final Charset ASCII  = Charset.forName("US-ASCII");


    public static String encode(String text) {
        byte[] encoded = text.getBytes(M_UTF7);
        return new String(encoded, ASCII);
    }
}
```
