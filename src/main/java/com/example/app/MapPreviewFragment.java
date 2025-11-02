package com.example.app;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
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

public class MapPreviewFragment extends Fragment {

    private FragmentAreaSelectBinding binding;

    private final PointF pStart = new PointF();
    private final PointF pEnd = new PointF();
    private boolean firstSet = false, secondSet = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAreaSelectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        binding.mapImage.setImageBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.city_1000)
        );

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
//                            firstSet = false;
//                            secondSet = false;
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
            RectF rectBitmap = mapViewRectToBitmap(rectView, binding.mapImage);

            int x1 = Math.round(rectBitmap.left);
            int y1 = Math.round(rectBitmap.top);
            int x2 = Math.round(rectBitmap.right);
            int y2 = Math.round(rectBitmap.bottom);

            try {
                Bitmap base = BitmapFactory.decodeResource(getResources(), R.drawable.city_1000);
                int w = Math.max(1, x2 - x1);
                int h = Math.max(1, y2 - y1);
                Bitmap crop = Bitmap.createBitmap(base, x1, y1, w, h);
                showPreviewDialog(crop, x1, y1, x2, y2);
            } catch (Exception ex) {
                Toast.makeText(requireContext(), "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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

    private static RectF mapViewRectToBitmap(RectF rectView, ImageView iv) {
        Matrix invert = new Matrix();
        iv.getImageMatrix().invert(invert);

        float[] pts = {rectView.left, rectView.top, rectView.right, rectView.bottom};
        invert.mapPoints(pts);

        int bw = iv.getDrawable().getIntrinsicWidth();
        int bh = iv.getDrawable().getIntrinsicHeight();

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
        binding = null;
    }
}
