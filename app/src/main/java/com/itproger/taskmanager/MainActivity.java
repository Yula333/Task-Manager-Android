package com.itproger.taskmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DataBase dataBase;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private EditText list_name_field;
    private SharedPreferences sharedPreferences;
    private TextView info_app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataBase = new DataBase(this);

        listView = findViewById(R.id.task_list);
        list_name_field = findViewById(R.id.list_name_field);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);       //установили защищенный режим работы с памятью телефона

        //при старте программы из памяти телефона подгрузим по ключу list_name значение
        // которое ввел пользователь и отобразим изначально в поле для ввода текстовой надписи текст которое он ранее прописывал
        String list_name = sharedPreferences.getString("list_name", "");
        list_name_field.setText(list_name);

        info_app = findViewById(R.id.info_app);
        info_app.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_info_text));

        loadAllTask();
        changeTextAction();
    }

    private void changeTextAction() {
        list_name_field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("list_name", String.valueOf(list_name_field.getText()));       //текст сохраняем в памяти телефона по ключу "list_name"
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //функция для подгрузки всех записей и вывода их в объекте listView
    private void loadAllTask() {
        ArrayList<String> allTask = dataBase.getAllTask();
        if(arrayAdapter == null){
            arrayAdapter = new ArrayAdapter<String>(this, R.layout.task_list_row, R.id.text_label_row, allTask);
            listView.setAdapter(arrayAdapter);
        }else{
            arrayAdapter.clear();
            arrayAdapter.addAll(allTask);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        Drawable icon = menu.getItem(0).getIcon();
        icon.mutate();
        icon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_new_task){              //если нажимаем на иконку из меню
            final EditText userTaskField = new EditText(this);      //создаем объект, поле для ввода информации
            AlertDialog dialog = new AlertDialog.Builder(this)          //создание всплывающего окна
                        .setTitle("Добавление нового задания")
                        .setMessage("Что бы вы хотели добавить?")
                        .setView(userTaskField)
                        .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(userTaskField.getText());     //получаем значение, данные, которые ввел пользователь
                                dataBase.insertData(task);      //добавление новой записи в БД
                                loadAllTask();                  //обновляем все записи, которые отображаются в приложении

                            }
                        })
                        .setNegativeButton("Ничего", null)
                        .create();
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteTask(View button){
        View parent = (View) button.getParent();  //при нажатии на кнопку получаем родительский объект где она расположена-RelativeLayout
        TextView textView = parent.findViewById(R.id.text_label_row);       //через родительский объект получаем доступ к текстовой надписи
        final String task = String.valueOf(textView.getText());          //получаем текст из необходимой текстовой надписи

        parent.animate().alpha(0).setDuration(1000).withEndAction(new Runnable() {      //ставим угасание строки в течении 1 сек
            @Override
            public void run() {
                dataBase.deleteData(task);
                loadAllTask();           //обновляем записи в приложении
                parent.animate().alpha(1).setDuration(0);       //возвращаем видимость строке
            }
        });

    }

}
