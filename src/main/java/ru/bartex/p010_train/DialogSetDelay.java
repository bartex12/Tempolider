package ru.bartex.p010_train;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by Андрей on 27.05.2018.
 */
public class DialogSetDelay extends DialogFragment {

    private static final String TAG = "33333";
    public static String ARG_DELAY = "delay";
    public static final String EXTRA_SIZE = "ru.bartex.p010_train.size";

    public static DialogSetDelay newInstance(String delay){
        Bundle args = new Bundle();
        args.putString(ARG_DELAY, delay);
        DialogSetDelay fragment = new DialogSetDelay();
        fragment.setArguments(args);
        return fragment;
    }

    //интерфейс для передачи данных из фрагмента в активность
    public interface DelayListener{
        void onDelayTransmit(int delay);
    }

    DelayListener mDelayListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDelayListener = (DelayListener)context;
    }

    public DialogSetDelay() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        // в макете сделана установка фокуса, выделение цветом  и цифровая клавиатура
        //android:focusable="true"
        // android:selectAllOnFocus="true"
        // android:inputType="numberDecimal"

        //принудительно вызываем клавиатуру - повторный вызов ее скроет
        takeOnAndOffSoftInput();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater =getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.set_delay, null);
        final EditText editTextDelay = (EditText)view.findViewById(R.id.editTextDelay);

        editTextDelay.setText((String) getArguments().get(ARG_DELAY));
        //так как в макете это уже есть, здесь не надо
        //editTextDelay.requestFocus();
        //editTextDelay.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(view);
        builder.setTitle("Установите задержку старта в секундах");

        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //читаем задержку в строке ввода
                int  delay = Integer.parseInt(editTextDelay.getText().toString());

                //Вызываем метод интерфейса, передаём задержку в активность SingleFragmentActivity
                mDelayListener.onDelayTransmit(delay);
                editTextDelay.clearFocus();

                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //принудительно прячем  клавиатуру - повторный вызов ее покажет
                takeOnAndOffSoftInput();
            }
        });
        //если делать запрет на закрытие окна при щелчке за пределами окна, то можно так
        Dialog  dialog = builder.create();
        //getDialog().getWindow().
         //       setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
