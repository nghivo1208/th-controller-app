package com.nghivo.thcontroller.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nghivo.thcontroller.R;

public class UpdateBottomSheet extends BottomSheetDialogFragment {

    public static UpdateBottomSheet newInstance(
            float max, float min,
            OnClickOkListener onClickOkListener
    ) {
        UpdateBottomSheet fragment = new UpdateBottomSheet();
        fragment.mOnClickOk = onClickOkListener;
        fragment.mMax = max;
        fragment.mMin = min;
        return fragment;
    }

    public interface OnClickOkListener {
        void onClick(float min, float max);
    }

    private float mMax = 0, mMin = 0;
    private OnClickOkListener mOnClickOk;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.dialog_input_temp, null);
        dialog.setContentView(contentView);
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));

        View btnBack = contentView.findViewById(R.id.btnClose);
        btnBack.setOnClickListener(v -> dismissAllowingStateLoss());

        EditText edtMin = contentView.findViewById(R.id.edtMin);
        edtMin.setText(mMin + "");

        EditText edtMax = contentView.findViewById(R.id.edtMax);
        edtMax.setText(mMax + "");

        View btnNext = contentView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            String min = edtMin.getText().toString();
            String max = edtMax.getText().toString();
            mOnClickOk.onClick(Float.parseFloat(min), Float.parseFloat(max));
            dismissAllowingStateLoss();
        });
    }

}
