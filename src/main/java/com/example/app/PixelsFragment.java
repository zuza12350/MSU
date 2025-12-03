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

import com.example.app.databinding.LayoutPixelsBinding;
import com.example.app.utils.CityMapService;
import com.example.app.utils.SimpleWatcher;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

        binding.btnSendPixels.setEnabled(true);
        binding.btnSendPixels.setAlpha(0.5f);

        TextWatcher watcher = new SimpleWatcher(this::updateButtonStyle);
        binding.x1Val.addTextChangedListener(watcher);
        binding.y1Val.addTextChangedListener(watcher);
        binding.x2Val.addTextChangedListener(watcher);
        binding.y2Val.addTextChangedListener(watcher);

        binding.btnSendPixels.setOnClickListener(this::onSendClicked);
    }

    private void updateButtonStyle() {
        boolean allFilled = allFieldsFilled();
        binding.btnSendPixels.setAlpha(allFilled ? 1f : 0.5f);
    }

    private void onSendClicked(View view) {
        boolean allFilled = allFieldsFilled();
        validateFields();

        if (!allFilled) {
            highlightEmptyFields();
            Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private boolean allFieldsFilled() {
        return notEmpty(binding.x1Val)
                && notEmpty(binding.y1Val)
                && notEmpty(binding.x2Val)
                && notEmpty(binding.y2Val);
    }

    private void highlightEmptyFields() {
        EditText[] fields = {binding.x1Val, binding.y1Val, binding.x2Val, binding.y2Val};
        for (EditText f : fields) {
            if (!notEmpty(f)) {
                f.setError("Must be filled");
                f.setBackgroundTintList(ERROR_TINT);
            } else {
                f.setError(null);
                f.setBackgroundTintList(normalTint);
            }
        }
    }

    private void validateFields() {
        Integer x1 = parseNumber(binding.x1Val.getText().toString());
        Integer y1 = parseNumber(binding.y1Val.getText().toString());
        Integer x2 = parseNumber(binding.x2Val.getText().toString());
        Integer y2 = parseNumber(binding.y2Val.getText().toString());

        boolean valid = true;
        valid &= checkRange(binding.x1Val, x1, 0, 1000, "x1: 0–1000");
        valid &= checkRange(binding.y1Val, y1, 0, 1000, "y1: 0–1000");
        valid &= checkRange(binding.x2Val, x2, 0, 1000, "x2: 0–1000");
        valid &= checkRange(binding.y2Val, y2, 0, 1000, "y2: 0–1000");

        if (!valid) return;

        new Thread(() -> {
            String msg = CityMapService
                    .getInstance()
                    .getFragmentOfMap(x1, y1, x2, y2);


            requireActivity().runOnUiThread(() -> {
                if (!msg.contains("ERROR")) {
                    showOutputImage(msg);
                }
            });
        }).start();
    }

    private void showOutputImage(String msg) {

        if (!msg.startsWith("ERROR")) {
            try {
                byte[] bytes = android.util.Base64.decode(msg, android.util.Base64.DEFAULT);

                android.graphics.Bitmap bitmap =
                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                binding.outputImage.setImageBitmap(bitmap);

                clearAllFields();

            } catch (Exception e) {
                Toast.makeText(requireContext(),
                        "Error decoding image", Toast.LENGTH_SHORT).show();
            }

        } else {

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage(msg)
                    .setPositiveButton("OK", null)
                    .show();
        }

    }

    private void clearAllFields() {
        EditText[] fields = {binding.x1Val, binding.y1Val, binding.x2Val, binding.y2Val};
        for (EditText f : fields) {
            f.setText("");
            f.setError(null);
            f.setBackgroundTintList(normalTint);
        }
        binding.btnSendPixels.setAlpha(0.5f);
    }

    private boolean notEmpty(EditText e) {
        CharSequence cs = e.getText();
        return cs != null && cs.toString().trim().length() > 0;
    }

    private Integer parseNumber(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        try {
            Number n = NumberFormat.getNumberInstance(Locale.getDefault()).parse(trimmed);
            return n.intValue();
        } catch (ParseException ignored) {
            return null;
        }
    }

    private boolean checkRange(EditText field, Integer val, double min, double max, String msg) {
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
