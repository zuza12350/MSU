package com.example.app;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app.databinding.FragmentAreaSelectBinding;
import com.example.app.utils.CityMapService;

public class MapPreviewFragment extends Fragment {

    private static final String TAG = "MapPreviewFragment";
    private FragmentAreaSelectBinding binding;

    private final PointF pStart = new PointF();
    private final PointF pEnd = new PointF();
    private boolean firstSet = false, secondSet = false;

    @Nullable
    private Bitmap currentBitmap = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAreaSelectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        binding.previewImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        binding.overlay.setBackground(null);
        binding.overlay.setClickable(true);

        loadInitialMap();

        GestureDetector detector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        float x = e.getX();
                        float y = e.getY();

                        if (!firstSet) {
                            pStart.set(x, y);
                            firstSet = true;
                            binding.coordsLabel.setText("Select another point");
                            binding.overlay.setRect(null);
                        } else {
                            pEnd.set(x, y);
                            secondSet = true;
                            RectF r = normalizedRect(pStart, pEnd);
                            binding.overlay.setRect(r);
                            binding.coordsLabel.setText("Selected");
                        }
                        return true;
                    }
                });

        binding.overlay.setOnTouchListener((view1, event) -> detector.onTouchEvent(event));

        binding.btnConfirmArea.setOnClickListener(v1 -> {
            if (!secondSet) {
                Toast.makeText(requireContext(), "Select two points", Toast.LENGTH_SHORT).show();
                return;
            }

            RectF rectView = normalizedRect(pStart, pEnd);
            RectF rectBitmap = mapViewRectToBitmap(rectView, binding.previewImage);

            int x1 = Math.round(rectBitmap.left);
            int y1 = Math.round(rectBitmap.top);
            int x2 = Math.round(rectBitmap.right);
            int y2 = Math.round(rectBitmap.bottom);

            sendSelectionToServer(x1, y1, x2, y2);
        });
    }

    /**
     * Ładuje mapę startową z serwisu i ustawia ją w ImageView.
     * Dodatkowo tymczasowo ukrywa overlay przed ustawieniem obrazka.
     */
    private void loadInitialMap() {
        new Thread(() -> {
            Log.d(TAG, "Requesting initial map from service...");
            String base64 = CityMapService.getInstance().getInitialMap();

            if (base64 == null) {
                Log.w(TAG, "getInitialMap returned null");
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "GetInitialMap returned null", Toast.LENGTH_LONG).show());
                return;
            }

            Log.d(TAG, "Raw base64 startsWith data:? " + base64.startsWith("data:"));
            if (base64.startsWith("ERROR:")) {
                String msg = "GetInitialMap error: " + base64;
                Log.w(TAG, msg);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show());
                return;
            }

            if (base64.startsWith("data:")) {
                int comma = base64.indexOf(',');
                if (comma > 0 && comma < base64.length() - 1) {
                    base64 = base64.substring(comma + 1);
                    Log.d(TAG, "Stripped data URI prefix from base64");
                } else {
                    Log.w(TAG, "Bad data: prefix or no comma found");
                }
            }

            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Log.d(TAG, "Decoded bytes length = " + (bytes == null ? "null" : bytes.length));

                if (bytes == null || bytes.length == 0) {
                    Log.e(TAG, "Decoded bytes empty");
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Decoded data empty", Toast.LENGTH_LONG).show());
                    return;
                }

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

                if (bmp == null) {
                    Log.w(TAG, "Bitmap decode returned null with opts, trying without options");
                    bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }

                if (bmp == null) {
                    Log.e(TAG, "Cannot decode initial image (bmp == null)");
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Cannot decode initial image", Toast.LENGTH_LONG).show());
                    return;
                }

                final Bitmap finalBmp = bmp;
                requireActivity().runOnUiThread(() -> {
                    binding.overlay.setVisibility(View.GONE);

                    currentBitmap = finalBmp;
                    binding.previewImage.setImageBitmap(finalBmp);
                    binding.previewImage.setVisibility(View.VISIBLE);
                    binding.previewImage.requestLayout();
                    binding.previewImage.invalidate();

                    binding.overlay.setBackground(null);
                    binding.overlay.setVisibility(View.VISIBLE);
                    binding.overlay.bringToFront();

                    binding.overlay.setRect(null);
                    firstSet = false;
                    secondSet = false;
                    binding.coordsLabel.setText("Select points: upper left, down right");
                });

            } catch (Exception ex) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Decode error: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void sendSelectionToServer(int x1, int y1, int x2, int y2) {
        Toast.makeText(requireContext(), "Sending selection...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            String base64 = CityMapService.getInstance()
                    .getFragmentOfMap(x1, y1, x2, y2);

            if (base64 == null || base64.startsWith("ERROR:")) {
                String msg = (base64 == null) ? "Service returned null" : ("Service error: " + base64);
                Log.w(TAG, msg);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show());
                return;
            }

            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                if (bmp == null) {
                    Log.e(TAG, "Decoded fragment bitmap is null");
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Cannot decode fragment image", Toast.LENGTH_LONG).show());
                    return;
                }

                requireActivity().runOnUiThread(() ->
                        showPreviewDialog(bmp, x1, y1, x2, y2));
            } catch (Exception ex) {
                Log.e(TAG, "Decode error for fragment: " + ex.getMessage(), ex);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Decode error: " + ex.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void showPreviewDialog(Bitmap crop, int x1, int y1, int x2, int y2) {
        ImageView iv = new ImageView(requireContext());
        iv.setImageBitmap(crop);
        iv.setAdjustViewBounds(true);

        new AlertDialog.Builder(requireContext())
                .setMessage(String.format("x1=%d, y1=%d → x2=%d, y2=%d", x1, y1, x2, y2))
                .setView(iv)
                .setPositiveButton("OK", null)
                .show();
    }

    private static RectF normalizedRect(PointF a, PointF b) {
        return new RectF(Math.min(a.x, b.x), Math.min(a.y, b.y),
                Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    private RectF mapViewRectToBitmap(RectF rectView, ImageView iv) {
        if (iv.getDrawable() == null) {
            Log.w(TAG, "mapViewRectToBitmap: drawable == null, zwracam rectView");
            return rectView;
        }

        Matrix invert = new Matrix();
        iv.getImageMatrix().invert(invert);

        float[] pts = {rectView.left, rectView.top, rectView.right, rectView.bottom};
        invert.mapPoints(pts);

        int bw = (currentBitmap != null) ? currentBitmap.getWidth() : iv.getDrawable().getIntrinsicWidth();
        int bh = (currentBitmap != null) ? currentBitmap.getHeight() : iv.getDrawable().getIntrinsicHeight();

        if (bw <= 0 || bh <= 0) {
            Log.w(TAG, "mapViewRectToBitmap: invalid bitmap size, bw=" + bw + " bh=" + bh);
            return rectView;
        }

        float L = clamp(pts[0], 0, bw);
        float T = clamp(pts[1], 0, bh);
        float R = clamp(pts[2], 0, bw);
        float B = clamp(pts[3], 0, bh);

        return new RectF(Math.min(L, R), Math.min(T, B), Math.max(L, R), Math.max(T, B));
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        currentBitmap = null;
        binding = null;
    }
}
