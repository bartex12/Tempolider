package ru.bartex.p010_train;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.DialogFragment;
import ru.bartex.p010_train.ru.bartex.p010_train.data.P;
import ru.bartex.p010_train.ru.bartex.p010_train.data.TempDBHelper;

/**
 * Created by Андрей on 06.05.2018.
 */
public class DialogSaveTempFragment extends DialogFragment {

    static String TAG = "33333";
    String finishFileName; //имя файла, передаваемое в аргументах фрагмента

    TempDBHelper mTempDBHelper;

    public DialogSaveTempFragment(){}

    public static DialogSaveTempFragment newInstance(String nameOfFile){
        Bundle args = new Bundle();
        args.putString(P.ARG_NAME_OF_FILE,nameOfFile);
        DialogSaveTempFragment fragment = new DialogSaveTempFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface SaverFragmentListener{
        void onFileNameTransmit(String oldNameFile, String newNameFile);
    }

    SaverFragmentListener mSaverFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSaverFragmentListener = (SaverFragmentListener)context;
        Log.d(TAG, "DialogSaveTempFragment: onAttach   mSaverFragmentListener = " +
                mSaverFragmentListener);
        mTempDBHelper = new TempDBHelper(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "DialogSaveTempFragment: onCreate  ");

        if ((getArguments()) != null){
            //имя файла из аргументов
            finishFileName = getArguments().getString(P.ARG_NAME_OF_FILE);

        }else finishFileName = "";
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //принудительно вызываем клавиатуру - повторный вызов ее скроет
        takeOnAndOffSoftInput();

        AlertDialog.Builder bilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.save_data_in_file, null);
        final EditText name = view.findViewById(R.id.editTextNameOfFile);
        name.setText(finishFileName);
        name.requestFocus();
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        bilder.setView(view);
        bilder.setTitle("Сохранить как");
        bilder.setIcon(R.drawable.ic_save_black_24dp);

        final CheckBox date = view.findViewById(R.id.checkBoxDate);

        /*
        date.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            String dayTimeFile = "";
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String nameFile = name.getText().toString();;
                if(isChecked){
                    dayTimeFile ="_" + P.setDateString();
                    nameFile = nameFile  + dayTimeFile;
                    name.setText(nameFile);
                    Log.d(TAG, "dayTimeFile = " + dayTimeFile + "  nameFile = " + nameFile);
                    name.setEnabled(false);
                }else {
                    String oldName = nameFile.replace(dayTimeFile,"");
                    name.setText(oldName);
                    Log.d(TAG, "nameFile = " + nameFile + "  oldName = " + oldName);
                    name.setEnabled(true);
                }

            }
        });
        */

        Button btnSaveYes = view.findViewById(R.id.buttonSaveYes);
        btnSaveYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameFile = name.getText().toString();

                if(date.isChecked()){
                    nameFile = nameFile + "_" + P.setDateString();
                    Log.d(TAG, "SaverFragment date.isChecked() Имя файла = " + nameFile);
                }

                //++++++++++++++++++   проверяем, есть ли такое имя   +++++++++++++//
                long fileId = mTempDBHelper.getIdFromFileName(nameFile);
                Log.d(TAG, "nameFile = " +nameFile + "  fileId = " +fileId);

                //если имя - пустая строка
                if (nameFile.trim().isEmpty()){
                    Snackbar.make(view, "Введите непустое имя раскладки", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.d(TAG, "Введите непустое имя раскладки ");
                    return;

                    //если такое имя уже есть в базе
                }else if (fileId != -1) {
                    Snackbar.make(view, "Такое имя уже существует. Введите другое имя.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.d(TAG, "Такое имя уже существует. Введите другое имя. fileId = " +fileId);
                    return;

                    //если имя не повторяется, оно не пустое то
                }else {
                    Log.d(TAG, "Такое имя отсутствует fileId = " + fileId);

                    //Вызываем метод интерфейса, передаем  имя файла в SingleFragmentActivity
                    mSaverFragmentListener.onFileNameTransmit(finishFileName,nameFile);

                    //принудительно прячем  клавиатуру - повторный вызов ее покажет
                    takeOnAndOffSoftInput();
                    //getActivity().finish(); //закрывает и диалог и активность
                    getDialog().dismiss();  //закрывает только диалог
                }
            }
        });

        Button btnSaveNo = view.findViewById(R.id.buttonSaveNo);
        btnSaveNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();
                //getActivity().finish(); //закрывает и диалог и активность
                getDialog().dismiss();  //закрывает только диалог
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
