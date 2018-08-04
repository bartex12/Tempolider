package ru.bartex.p010_train;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SetListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(String nameOfFile) {
        //получаем имя файла раскладки и передам его фрагменту в качестве аргумента
        return SetListFragment.newInstance(nameOfFile);
    }
}
