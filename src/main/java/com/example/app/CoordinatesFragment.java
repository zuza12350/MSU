package com.example.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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

import com.example.app.databinding.LayoutCoordinatesBinding;
import com.example.app.utils.SimpleWatcher;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Pattern;

public class CoordinatesFragment extends Fragment {

    private LayoutCoordinatesBinding binding;

    // ===== przeniesione stałe i pola =====
    private static final Pattern COORD_PATTERN = Pattern.compile("^-?\\d{1,2}[.,]\\d{1,5}$");
    private ColorStateList normalTint;
    private static final ColorStateList ERROR_TINT = ColorStateList.valueOf(Color.RED);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LayoutCoordinatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // domyślny kolor tła pól
        normalTint = ContextCompat.getColorStateList(
                requireContext(),
                com.google.android.material.R.color.design_default_color_background
        );

        // przycisk na start nieaktywny
        binding.btnSendCoordinates.setEnabled(false);
        binding.btnSendCoordinates.setAlpha(.5f);

        // reaguj na zmiany w polach
        TextWatcher watcher = new SimpleWatcher(this::toggleButtonEnabled);
        binding.latitudeVal.addTextChangedListener(watcher);
        binding.longitudeVal.addTextChangedListener(watcher);
        binding.latitudeLeftVal.addTextChangedListener(watcher);
        binding.longitudeLeftVal.addTextChangedListener(watcher);

        // walidacja i „wysyłka”
        binding.btnSendCoordinates.setOnClickListener(this::validateFields);
    }

    // ===== logika walidacji (jak w aktywności, dostosowana do fragmentu) =====
    private void validateFields(View view) {
        boolean okRightLat = validateField(binding.latitudeVal);
        boolean okRightLon = validateField(binding.longitudeVal);
        boolean okLeftLat  = validateField(binding.latitudeLeftVal);
        boolean okLeftLon  = validateField(binding.longitudeLeftVal);

        if (!(okRightLat & okRightLon & okLeftLat & okLeftLon)) {
            return;
        }

        Double rLat = parseNumber(binding.latitudeVal.getText().toString());
        Double rLon = parseNumber(binding.longitudeVal.getText().toString());
        Double lLat = parseNumber(binding.latitudeLeftVal.getText().toString());
        Double lLon = parseNumber(binding.longitudeLeftVal.getText().toString());

        boolean rangeOk = true;
        rangeOk &= checkRange(binding.latitudeVal,  rLat, -90, 90,  "Latitude range -90..90");
        rangeOk &= checkRange(binding.longitudeVal, rLon, -180, 180,"Longitude range -180..180");
        rangeOk &= checkRange(binding.latitudeLeftVal,  lLat, -90, 90,  "Latitude range -90..90");
        rangeOk &= checkRange(binding.longitudeLeftVal, lLon, -180, 180,"Longitude range -180..180");
        if (!rangeOk) return;

        String msg = String.format(Locale.getDefault(),
                "Right corner:  Lat %.5f, Lon %.5f\nLeft corner:   Lat %.5f, Lon %.5f",
                rLat, rLon, lLat, lLon);
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();

        // TODO: tutaj wywołaj repo/endpoint
        clearAllFields();
    }

    private void clearAllFields() {
        EditText[] fields = {
                binding.latitudeVal,
                binding.longitudeVal,
                binding.latitudeLeftVal,
                binding.longitudeLeftVal
        };
        for (EditText f : fields) {
            f.setText("");
            f.setError(null);
            f.setBackgroundTintList(normalTint);
        }
        binding.btnSendCoordinates.setEnabled(false);
        binding.btnSendCoordinates.setAlpha(.5f);
    }

    private void toggleButtonEnabled() {
        boolean allFilled =
                notEmpty(binding.latitudeVal) &&
                        notEmpty(binding.longitudeVal) &&
                        notEmpty(binding.latitudeLeftVal) &&
                        notEmpty(binding.longitudeLeftVal);

        binding.btnSendCoordinates.setEnabled(allFilled);
        binding.btnSendCoordinates.setAlpha(allFilled ? 1f : .5f);
    }

    private boolean notEmpty(EditText e) {
        CharSequence cs = e.getText();
        return cs != null && cs.toString().trim().length() > 0;
    }

    private boolean validateField(EditText field) {
        String s = field.getText() == null ? "" : field.getText().toString().trim();
        boolean ok = COORD_PATTERN.matcher(s).matches();
        if (!ok) {
            field.setError("Format: 5 digits after comma");
            field.setBackgroundTintList(ERROR_TINT);
        } else {
            field.setError(null);
            field.setBackgroundTintList(normalTint);
        }
        return ok;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
