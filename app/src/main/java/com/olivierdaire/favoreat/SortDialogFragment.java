package com.olivierdaire.favoreat;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;


public class SortDialogFragment extends DialogFragment {

    public interface EditNameDialogListener {
        void onFinishEditDialog(int inputPrice, String inputType, int inputRating);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_sort, null);

        final Spinner spin;
        spin = (Spinner)view.findViewById(R.id.typeSpinner);

        List<String> list = Arrays.asList(getResources().getStringArray(R.array.type_array));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(dataAdapter);

        final TextView textViewPrice = (TextView) view.findViewById(R.id.textPriceSort);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.RestaurantPriceSeek);
        //Set Price Text
        textViewPrice.setText("< " + seekBar.getProgress() + " $");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                textViewPrice.setText("< " + progress + " $");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final RatingBar rating = (RatingBar) view.findViewById(R.id.RestaurantRating);

        builder
                .setTitle("Sort Restaurant")
                .setView(view);
        builder.setPositiveButton("Sort", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditNameDialogListener activity = (EditNameDialogListener) getActivity();
                activity.onFinishEditDialog(Integer.parseInt(textViewPrice.getText().toString().replaceAll("[^0-9]", "")), spin.getSelectedItem().toString(), Math.round(rating.getRating()));
                dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }


}
