package ru.bartex.p010_train;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.bartex.p010_train.ru.bartex.p010_train.data.P;

/**
 * Created by Андрей on 10.05.2018.
 */
public class DialogSaveSecFragment extends DialogFragment {

    static String TAG = "33333";

    public DialogSaveSecFragment(){}

    //интерфейс для передачи данных из фрагмента в активность
    public interface SaverFragmentSecundomerListener{
        void onNameAndGrafTransmit(String nameFile, boolean showGraf, boolean cancel);
    }

    SaverFragmentSecundomerListener mSaverFragSectListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSaverFragSectListener = (SaverFragmentSecundomerListener)context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //принудительно вызываем клавиатуру - повторный вызов ее скроет
        takeOnAndOffSoftInput();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater =getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.save_data_in_file, null);
        final EditText name = view.findViewById(R.id.editTextNameOfFile);
        final CheckBox dateCheckBox = view.findViewById(R.id.checkBoxDate);
        name.requestFocus();
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(view);
        builder.setTitle("Запись в файл");

        //действия при нажатии кнопки "Сохранить" в диалоге сохранения данных в базу
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //читаем имя файла в строке ввода
                String nameFile = name.getText().toString();

                if(dateCheckBox.isChecked()){
                    nameFile = nameFile + "_" + P.setDateString();
                    Log.d(TAG, "SaverFragmentSecundomer date.isChecked() Имя файла = " + nameFile);
                }
                //Вызываем метод интерфейса, имя файла в TimeMeterActivity
                mSaverFragSectListener.onNameAndGrafTransmit(nameFile, false, false);

                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();
            }
        });

        //действия при нажатии кнопки "Сохранить и показать" в диалоге сохранения данных в базу
        builder.setNeutralButton("Сохранить и показать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //читаем имя файла в строке ввода
                String nameFile = name.getText().toString();
                if(dateCheckBox.isChecked()){
                    nameFile = nameFile + "_" + P.setDateString();
                }
                //Вызываем метод интерфейса, передаем  nameFile и showGraf в TimeMeterActivity
                mSaverFragSectListener.onNameAndGrafTransmit(nameFile, true, false);

                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();
            }
        });

        //действия при нажатии кнопки "Нет" в диалоге сохранения данных в базу
        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //здесь имя не будет использоваться дальше, поэтому так
                String nameFile = name.getText().toString();
                //Вызываем метод интерфейса, имя файла в TimeMeterActivity
                mSaverFragSectListener.onNameAndGrafTransmit(nameFile, false, true);
                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();

            }
        });
        //если не делать запрет на закрытие окна при щелчке за пределами окна, то можно так
        //return bilder.create();
        //А если делать запрет, то так
        Dialog  dialog = builder.create();
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
