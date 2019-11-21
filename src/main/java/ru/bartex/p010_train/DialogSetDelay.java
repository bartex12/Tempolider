package ru.bartex.p010_train;

import android.app.Dialog;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import ru.bartex.p010_train.ru.bartex.p010_train.data.P;

/**
 * Created by Андрей on 27.05.2018.
 */
public class DialogSetDelay extends DialogFragment {

    private static final String TAG = "33333";

    EditText editTextDelay;

    public static DialogSetDelay newInstance(String delay){
        Bundle args = new Bundle();
        args.putString(P.ARG_DELAY, delay);
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
        Log.d(TAG, "DialogSetDelay -  onAttach mDelayListener = " + mDelayListener);

    }

    public DialogSetDelay() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Log.d(TAG, "DialogSetDelay -  onCreateDialog mDelayListener = " + mDelayListener);
        // в макете сделана установка фокуса, выделение цветом  и цифровая клавиатура
        //android:focusable="true"
        // android:selectAllOnFocus="true"
        // android:inputType="numberDecimal"

        //принудительно вызываем клавиатуру - повторный вызов ее скроет
        takeOnAndOffSoftInput();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater =getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.set_delay, null);

        editTextDelay = view.findViewById(R.id.editTextDelay);
        editTextDelay.setText((String) getArguments().get(P.ARG_DELAY));
        //так как в макете это уже есть, здесь не надо
        //editTextDelay.requestFocus();
        //editTextDelay.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(view);
        builder.setTitle("Установите задержку старта в секундах");

        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int  delay;
                boolean tempDelay = getDelay();
                if (tempDelay){
                    //читаем задержку в строке ввода
                    delay = Integer.parseInt(editTextDelay.getText().toString());
                    //Вызываем метод интерфейса, передаём задержку в активность SingleFragmentActivity
                    Log.d(TAG, "DialogSetDelay -  до onDelayTransmit");
                    if (mDelayListener==null) Log.d(TAG, "DialogSetDelay - mDelayListener==null");
                    mDelayListener.onDelayTransmit(delay);
                    Log.d(TAG, "DialogSetDelay -  после onDelayTransmit");
                    editTextDelay.clearFocus();

                    //принудительно прячем  клавиатуру - повторный вызов ее покажет
                    takeOnAndOffSoftInput();
                }
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

    public  boolean getDelay(){
        boolean d;
        if (editTextDelay.getText().toString().equals("")) {
            d = false;
            myToast ("Введите задержку\nот 0 до 60 секунд");
        }else {
            int i = Integer.parseInt(editTextDelay.getText().toString());
            if (i>=0 && i<=60) {
                d = true;
            }else {
                d = false;
                myToast ("Введите задержку\n от 0 до 60 секунд\"");
            }
        }
        return d;
    }
    void myToast (String s){
        Toast mToast = Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER,0,0);
        mToast.show();
    }
}
