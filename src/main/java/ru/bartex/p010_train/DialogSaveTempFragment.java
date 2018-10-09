package ru.bartex.p010_train;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import ru.bartex.p010_train.ru.bartex.p010_train.data.P;

/**
 * Created by Андрей on 06.05.2018.
 */
public class DialogSaveTempFragment extends DialogFragment {

     static String TAG = "33333";

    public DialogSaveTempFragment(){}

    public interface SaverFragmentListener{
        void onArrayListTransmit(String nameFile);
    }

    SaverFragmentListener mSaverFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSaverFragmentListener = (SaverFragmentListener)context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //принудительно вызываем клавиатуру - повторный вызов ее скроет
        takeOnAndOffSoftInput();

        AlertDialog.Builder bilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.save_data_in_file, null);
        final EditText name = view.findViewById(R.id.editTextNameOfFile);
        final CheckBox date = view.findViewById(R.id.checkBoxDate);
        name.requestFocus();
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        bilder.setView(view);
        bilder.setTitle("Запись в файл");
        bilder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String nameFile = name.getText().toString();

                if(date.isChecked()){
                    nameFile = nameFile + "_" + P.setDateString();
                    Log.d(TAG, "SaverFragment date.isChecked() Имя файла = " + nameFile);
                }
                //Вызываем метод интерфейса, передаем  имя файла в SingleFragmentActivity
                mSaverFragmentListener.onArrayListTransmit(nameFile);

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
