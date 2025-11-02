package com.example.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.app.databinding.LayoutPixelsBinding;
import com.example.app.utils.SimpleWatcher;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class PixelsFragment extends Fragment {

    private LayoutPixelsBinding binding;
    private ColorStateList normalTint;
    private static final ColorStateList ERROR_TINT = ColorStateList.valueOf(Color.RED);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LayoutPixelsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        normalTint = ContextCompat.getColorStateList(
                requireContext(),
                com.google.android.material.R.color.design_default_color_background
        );

        // 🔹 przycisk od początku klikalny
        binding.btnSendPixels.setEnabled(true);
        // startowy wygląd – „wyszarzony”
        binding.btnSendPixels.setAlpha(0.5f);

        // 🔹 obserwacja zmian w polach
        TextWatcher watcher = new SimpleWatcher(this::updateButtonStyle);
        binding.x1Val.addTextChangedListener(watcher);
        binding.y1Val.addTextChangedListener(watcher);
        binding.x2Val.addTextChangedListener(watcher);
        binding.y2Val.addTextChangedListener(watcher);

        // 🔹 klik zawsze działa (walidacja decyduje co dalej)
        binding.btnSendPixels.setOnClickListener(this::onSendClicked);
    }

    /** 🔸 Zmienia tylko wizualnie przycisk (szary / aktywny) */
    private void updateButtonStyle() {
        boolean allFilled = allFieldsFilled();
        binding.btnSendPixels.setAlpha(allFilled ? 1f : 0.5f);
        // UWAGA: nie zmieniamy enabled, bo ma być zawsze klikalny
    }

    /** 🔸 Obsługa kliknięcia */
    private void onSendClicked(View view) {
        boolean allFilled = allFieldsFilled();

        if (!allFilled) {
            highlightEmptyFields();
            Toast.makeText(requireContext(), "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        validateFields();
    }

    /** 🔸 Sprawdza czy wszystkie pola niepuste */
    private boolean allFieldsFilled() {
        return notEmpty(binding.x1Val)
                && notEmpty(binding.y1Val)
                && notEmpty(binding.x2Val)
                && notEmpty(binding.y2Val);
    }

    /** 🔸 Podświetla puste pola */
    private void highlightEmptyFields() {
        EditText[] fields = {binding.x1Val, binding.y1Val, binding.x2Val, binding.y2Val};
        for (EditText f : fields) {
            if (!notEmpty(f)) {
                f.setError("Wymagane pole");
                f.setBackgroundTintList(ERROR_TINT);
            } else {
                f.setError(null);
                f.setBackgroundTintList(normalTint);
            }
        }
    }

    /** 🔸 Walidacja i akcja */
    private void validateFields() {
        Double x1 = parseNumber(binding.x1Val.getText().toString());
        Double y1 = parseNumber(binding.y1Val.getText().toString());
        Double x2 = parseNumber(binding.x2Val.getText().toString());
        Double y2 = parseNumber(binding.y2Val.getText().toString());

        boolean valid = true;
        valid &= checkRange(binding.x1Val, x1, 0, 1000, "x1: 0–1000");
        valid &= checkRange(binding.y1Val, y1, 0, 1000, "y1: 0–1000");
        valid &= checkRange(binding.x2Val, x2, 0, 1000, "x2: 0–1000");
        valid &= checkRange(binding.y2Val, y2, 0, 1000, "y2: 0–1000");

        if (!valid) return;

        String msg = String.format(Locale.getDefault(),
                "First corner: x1=%.1f, y1=%.1f\nSecond corner: x2=%.1f, y2=%.1f",
                x1, y1, x2, y2);
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();

        clearAllFields();
    }

    /** 🔸 Czyści pola i wraca do „szarego” przycisku */
    private void clearAllFields() {
        EditText[] fields = {binding.x1Val, binding.y1Val, binding.x2Val, binding.y2Val};
        for (EditText f : fields) {
            f.setText("");
            f.setError(null);
            f.setBackgroundTintList(normalTint);
        }
        binding.btnSendPixels.setAlpha(0.5f);
    }

    /** 🔸 Pomocnicze funkcje */
    private boolean notEmpty(EditText e) {
        CharSequence cs = e.getText();
        return cs != null && cs.toString().trim().length() > 0;
    }

    private Double parseNumber(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        try {
            Number n = NumberFormat.getNumberInstance(Locale.getDefault()).parse(trimmed);
            return n.doubleValue();
        } catch (ParseException ignored) {
            return null;
        }
    }

    private boolean checkRange(EditText field, Double val, double min, double max, String msg) {
        if (val == null || val < min || val > max) {
            field.setError(msg);
            field.setBackgroundTintList(ERROR_TINT);
            return false;
        } else {
            field.setError(null);
            field.setBackgroundTintList(normalTint);
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
