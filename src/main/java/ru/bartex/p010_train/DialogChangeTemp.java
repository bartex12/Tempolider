package ru.bartex.p010_train;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;

/**
 * Created by Андрей on 30.05.2018.
 */
public class DialogChangeTemp extends DialogFragment {

    boolean up =true;

    static String TAG = "33333";
    private static final String ARG_VALUE = "ValueOfDelta";

    public interface ChangeTempUpDownListener {
        void changeTempUpDown(int valueDelta, boolean up);
    }

    ChangeTempUpDownListener mChangeTempUpDownListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mChangeTempUpDownListener = (ChangeTempUpDownListener)context;
    }

    public DialogChangeTemp(){}

    public static DialogChangeTemp newInstance(int value){
        Bundle args = new Bundle();
        args.putInt(ARG_VALUE, value);
        DialogChangeTemp fragment = new DialogChangeTemp();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //принудительно вызываем клавиатуру - повторный вызов ее скроет
        takeOnAndOffSoftInput();

        AlertDialog.Builder bilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_chahge_temp, null);

        final EditText valueChangTemp = (EditText)view.findViewById(R.id.editTextChangeTempValue);

        final RadioGroup radioGroup =(RadioGroup)view.findViewById(R.id.radioGroupTempUpDown);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.radioButtonTempUp:
                        up = true;
                        break;
                    case R.id.radioButtonTempDown:
                        up = false;
                        break;
                }
            }
        });
        //так как в макете это уже есть, здесь не надо
        //value.requestFocus();
        //value.setInputType(InputType.TYPE_CLASS_NUMBER);
        int valueOfDelta = (Integer) getArguments().get(ARG_VALUE);
        valueChangTemp.setText(String.valueOf(valueOfDelta));
        bilder.setView(view);
        bilder.setTitle("Введите величину изменения темпа, %");
        //bilder.setIcon(R.drawable.ic_swap_vert_black_32dp);

        bilder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Вызываем метод интерфейса и передаём величину дельта
                mChangeTempUpDownListener.changeTempUpDown(
                        Integer.parseInt(valueChangTemp.getText().toString()), up);

                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();
            }
        });

        bilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();

            }
        });
        //если не делать запрет на закрытие окна при щелчке за пределами окна, то можно так
        //return bilder.create();
        //А если делать запрет, то так
        AlertDialog  dialog = bilder.create();
        //запрет на закрытие окна при щелчке за пределами окна
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    //принудительно вызываем клавиатуру - повторный вызов ее скроет
    private void takeOnAndOffSoftInput(){
        InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}
