package com.joysee.tvbox.settings.upgrade.entity;

import android.text.InputType;
import android.text.method.NumberKeyListener;

public class FilterChars {
    public static final char[] FilterChars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_', '.', '@', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', };

    public static NumberKeyListener numberKeyListener = new NumberKeyListener() {

        @Override
        public int getInputType() {

            return InputType.TYPE_CLASS_TEXT;
        }

        @Override
        protected char[] getAcceptedChars() {

            return FilterChars;
        }
    };
}
