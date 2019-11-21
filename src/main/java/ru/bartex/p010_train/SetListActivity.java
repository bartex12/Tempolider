package ru.bartex.p010_train;


import androidx.fragment.app.Fragment;

public class SetListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(String nameOfFile) {
        //получаем имя файла раскладки и передам его фрагменту в качестве аргумента
        return SetListFragment.newInstance(nameOfFile);
    }
}
