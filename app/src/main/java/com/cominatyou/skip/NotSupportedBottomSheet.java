package com.cominatyou.skip;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.cominatyou.skip.databinding.BottomSheetNotSupportedBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NotSupportedBottomSheet extends BottomSheetDialogFragment {
    private final MainActivity parent;
    public NotSupportedBottomSheet(MainActivity parent) {
        this.parent = parent;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final BottomSheetNotSupportedBinding binding = BottomSheetNotSupportedBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        parent.finish();
    }
}
