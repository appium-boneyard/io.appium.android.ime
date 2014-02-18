package io.appium.android.ime;

import java.nio.charset.Charset;

import android.inputmethodservice.InputMethodService;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;


public class UnicodeIME extends InputMethodService {
    // encodings
    private static final Charset M_UTF7 = Charset.forName("x-IMAP-mailbox-name");
    private static final Charset ASCII  = Charset.forName("US-ASCII");

    // markers of UTF-7 content
    private static final char M_UTF7_SHIFT   = '&';
    private static final char M_UTF7_UNSHIFT = '-';

    // local state
    private boolean isShifted;
    private long metaState;
    private StringBuilder unicodeString;


    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        if (!restarting) {
            metaState = 0;
            isShifted = false;
        }
        unicodeString = null;
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        unicodeString = null;
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public boolean onEvaluateInputViewShown() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int c = getUnicodeChar(keyCode, event);

        if (c == 0) {
            return super.onKeyDown(keyCode, event);
        }

        if (!isShifted) {
            if (c == M_UTF7_SHIFT) {
                shift();
                return true;
            } else if (isAsciiPrintable(c)) {
                commitChar(c);
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            if (c == M_UTF7_UNSHIFT) {
                unshift();
            } else {
                appendChar(c);
            }
            return true;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        metaState = MetaKeyKeyListener.handleKeyUp(metaState, keyCode, event);
        return super.onKeyUp(keyCode, event);
    }

    private void shift() {
        isShifted = true;
        unicodeString = new StringBuilder();
        appendChar(M_UTF7_SHIFT);
    }

    private void unshift() {
        isShifted = false;
        unicodeString.append(M_UTF7_UNSHIFT);
        String decoded = decodeUtf7(unicodeString.toString());
        InputConnection ic = getCurrentInputConnection();
        ic.commitText(decoded, 1);
        unicodeString = null;
    }

    private int getUnicodeChar(int keyCode, KeyEvent event) {
        metaState = MetaKeyKeyListener.handleKeyDown(metaState, keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(metaState));
        metaState = MetaKeyKeyListener.adjustMetaAfterKeypress(metaState);
        return c;
    }

    private void commitChar(int c) {
        getCurrentInputConnection().commitText(String.valueOf((char) c), 1);
    }

    private void appendChar(int c) {
        unicodeString.append((char) c);

        // this will make the intermediate characters print in the text field
        // getCurrentInputConnection().setComposingText(composing, 1);
    }

    private String decodeUtf7(String encStr) {
        byte[] encoded = encStr.getBytes(ASCII);
        return new String(encoded, M_UTF7);
    }

    private boolean isAsciiPrintable(int c) {
        return c >= 0x20 && c <= 0x7E;
    }
}
