package com.bilboldev.pixeldungeonskills.network;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import com.bilboldev.noosa.Game;

public final class AndroidNetworkAccess implements NetworkAccess.Backend, PartyPrompts.Backend {
    private static final int REQUEST = 4712;
    private static final String LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK";
    private volatile Activity activity;
    private NetworkAccess.Result pending;
    private EditText input;
    private PartyPrompts.Input editor;
    private boolean changing;
    private int softInputMode;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayout;
    private Runnable removeBackHandler;

    public AndroidNetworkAccess(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void request(NetworkAccess.Result result) {
        Activity current = activity;
        if (current == null) {
            result.complete(false);
            return;
        }
        current.runOnUiThread(() -> {
            if (activity != current || current.isFinishing()) {
                result.complete(false);
            } else if (Build.VERSION.SDK_INT < 37 || current.getApplicationInfo().targetSdkVersion < 37) {
                result.complete(true);
            } else if (current.checkSelfPermission(LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED) {
                result.complete(true);
            } else if (pending != null) {
                result.complete(false);
            } else {
                pending = result;
                current.requestPermissions(new String[]{LOCAL_NETWORK}, REQUEST);
            }
        });
    }

    public boolean onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
        if (request != REQUEST) return false;
        NetworkAccess.Result result = pending;
        pending = null;
        boolean granted = false;
        for (int i = 0; i < permissions.length && i < grants.length; i++) {
            if (LOCAL_NETWORK.equals(permissions[i])) granted = grants[i] == PackageManager.PERMISSION_GRANTED;
        }
        if (result != null) result.complete(granted);
        return true;
    }

    @Override
    public void edit(PartyPrompts.Input target) {
        Activity current = activity;
        if (current == null) return;
        current.runOnUiThread(() -> {
            if (activity != current || current.isFinishing() || PartyPrompts.editing() != target) return;
            changing = true;
            if (input == null) {

                input = new EditText(current) {
                    @Override
                    protected void onSelectionChanged(int start, int end) {
                        super.onSelectionChanged(start, end);
                        edited();
                    }
                };
                input.setSingleLine(true);
                if (Build.VERSION.SDK_INT >= 34) input.setAutoHandwritingEnabled(false);
                input.setAlpha(0);
                input.setPadding(0, 0, 0, 0);
                input.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }
                    public void afterTextChanged(Editable text) { edited(); }
                });
                input.setOnEditorActionListener((view, action, event) -> {
                    if (editor != null && (event == null || event.getAction() == KeyEvent.ACTION_DOWN)) PartyPrompts.accept(editor);
                    return true;
                });
                input.setOnKeyListener((view, key, event) -> {
                    if (key == KeyEvent.KEYCODE_BACK || key == KeyEvent.KEYCODE_ESCAPE) {
                        if (event.getAction() == KeyEvent.ACTION_UP && editor != null) PartyPrompts.cancel(editor);
                        return true;
                    }
                    if (key != KeyEvent.KEYCODE_TAB) return false;
                    if (event.getAction() == KeyEvent.ACTION_DOWN && editor != null) PartyPrompts.next(editor, event.isShiftPressed());
                    return true;
                });
                ViewGroup content = current.findViewById(android.R.id.content);
                content.addView(input, new ViewGroup.LayoutParams(1, 1));
                if (Build.VERSION.SDK_INT >= 33) {
                    removeBackHandler = registerBack(current, () -> {
                        if (editor != null) PartyPrompts.cancel(editor);
                    });
                }
                softInputMode = current.getWindow().getAttributes().softInputMode;
                current.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                keyboardLayout = () -> {
                    if (input == null || Build.VERSION.SDK_INT >= 30) return;
                    Rect visible = new Rect();
                    current.getWindow().getDecorView().getWindowVisibleDisplayFrame(visible);
                    int bottom = content.getHeight() - visible.bottom;
                    PartyPrompts.keyboardTop = bottom > 100 * Game.density ? visible.bottom : 0;
                };
                content.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayout);
                if (Build.VERSION.SDK_INT >= 30) {
                    input.setOnApplyWindowInsetsListener((view, insets) -> {
                        PartyPrompts.keyboardTop = insets.isVisible(WindowInsets.Type.ime())
                                ? content.getHeight() - insets.getInsets(WindowInsets.Type.ime()).bottom : 0;
                        return insets;
                    });
                }
            }
            editor = target;
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            if (target.kind == PartyPrompts.Kind.PORT) {
                input.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                input.setRawInputType(InputType.TYPE_CLASS_NUMBER);
            } else if (target.kind == PartyPrompts.Kind.ADDRESS) {
                input.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
            input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN
                    | (target.kind == PartyPrompts.Kind.CODE ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT));
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(target.limit)});
            input.setText(target.value());
            input.setSelection(Math.min(target.anchor(), input.length()), Math.min(target.cursor(), input.length()));
            input.requestFocus();
            changing = false;
            if (!input.getText().toString().equals(target.value())) edited();
            InputMethodManager keyboard = (InputMethodManager)current.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.restartInput(input);
            input.post(() -> {
                if (input != null && editor == target) keyboard.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            });
        });
    }

    private void edited() {
        if (!changing && input != null && editor != null) {
            PartyPrompts.changed(editor, input.getText().toString(), input.getSelectionEnd(), input.getSelectionStart());
        }
    }

    @android.annotation.TargetApi(33)
    private static Runnable registerBack(Activity activity, Runnable action) {
        OnBackInvokedCallback callback = action::run;
        activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
        return () -> activity.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(callback);
    }

    @Override
    public void endEdit() {
        Activity current = activity;
        if (current == null) return;
        current.runOnUiThread(() -> {
            if (PartyPrompts.editing() == null) removeEditor(current);
        });
    }

    private void removeEditor(Activity current) {
        editor = null;
        if (removeBackHandler != null) removeBackHandler.run();
        removeBackHandler = null;
        if (input != null) {
            InputMethodManager keyboard = (InputMethodManager)current.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
            ViewGroup content = (ViewGroup)input.getParent();
            if (content != null) {
                ViewTreeObserver observer = content.getViewTreeObserver();
                if (keyboardLayout != null && observer.isAlive()) observer.removeOnGlobalLayoutListener(keyboardLayout);
                content.removeView(input);
            }
            if (Build.VERSION.SDK_INT >= 30) input.setOnApplyWindowInsetsListener(null);
            input = null;
            current.getWindow().setSoftInputMode(softInputMode);
        }
        keyboardLayout = null;
        if (PartyPrompts.editing() == null) PartyPrompts.keyboardTop = 0;
    }

    @Override
    public void copy(String value) {
        Activity current = activity;
        if (current == null) return;
        current.runOnUiThread(() -> {
            if (activity != current) return;
            ClipboardManager clipboard = (ClipboardManager)current.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("Skillful Pixel Dungeon", value));
        });
    }

    @Override
    public void paste(PartyPrompts.Result result) {
        Activity current = activity;
        if (current == null) return;
        current.runOnUiThread(() -> {
            if (activity != current) return;
            ClipboardManager clipboard = (ClipboardManager)current.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = clipboard == null ? null : clipboard.getPrimaryClip();
            result.complete(clip == null || clip.getItemCount() == 0 ? "" : clip.getItemAt(0).coerceToText(current).toString());
        });
    }

    public void close() {
        Activity current = activity;
        activity = null;
        PartyPrompts.uninstall(this);
        NetworkAccess.uninstall(this);
        if (current != null) current.runOnUiThread(() -> removeEditor(current));
        NetworkAccess.Result result = pending;
        pending = null;
        if (result != null) result.complete(false);
    }
}
