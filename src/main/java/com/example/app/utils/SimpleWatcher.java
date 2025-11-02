package com.example.app.utils;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleWatcher implements TextWatcher {
    private final Runnable after;
    public SimpleWatcher(Runnable after) { this.after = after; }
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override public void afterTextChanged(Editable s) { after.run(); }
}
