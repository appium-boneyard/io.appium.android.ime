# io.appium.android.ime

The Android emulator does not play well with non-ASCII characters. Text sent to the emulator through Selenium will be filtered through the default keyboard, which means that things get stripped down to ASCII. "परीक्षणम्" ("Testing" in Sanskrit) becomes "prksnm".

To get around this, this [input method](http://developer.android.com/guide/topics/text/creating-input-method.html), when set as the default, catches Unicode text encoded as [Modified UTF-7](http://tools.ietf.org/html/rfc3501) and recodes it as Unicode. Text input fields can then receive and send any characters that Unicode can encode.

To install, with the emulator running execute the following:

```shell
ant debug
adb uninstall io.appium.android.ime
adb install bin/UnicodeIME-debug.apk
```

This builds the application, removes any old version of it (if installed), and then installs the recently built one.

Once the input method is installed on the emulator, execute the following:

```shell
abd shell ime enable io.appium.android.ime/.UnicodeIME
adb shell ime set io.appium.android.ime/.UnicodeIME
```

(Alternatively, on the device, access `Settings`, then `Language & Input`. Make sure `Appium Android Input Manager for Unicode` is selected, and finally go to `Default` and set it to `Appium Android Input Manager for Unicode`.)

Your device is now set to receive encoded text from [Appium](http://appium.io/)!

On the Appium Bootstrap side, there needs to be an encoding of the text into Modified UTF-7, sending encoded text to the device through `setText`:

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

## Caveats

The encoding system uses the characters `&` and `-` to demarcate encoded text, which means that there is the potential for those characters within otherwise normal text to be handled wierdly. This can be obviated by encoding any text with `&` in it.

If you set the text into the same edit field multiple times without resetting, the IME is recycled. This means that if a `&` is inserted, and at a later point a `-` is inserted, the text between will be placed before the text of the last call. Keep, therefore, your tests atomic.

The Android emulator cannot handle certain scripts, though the text is there and can be retrieved with no problems. The problem is only display (generally, it comes out looking like whitespace).
