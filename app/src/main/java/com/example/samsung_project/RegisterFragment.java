package com.example.samsung_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterFragment extends Fragment {
    public RegisterFragment() {}
    private DatabaseReference db_ref;
    FirebaseAuth reg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_register, null);
        reg = FirebaseAuth.getInstance();
        db_ref = FirebaseDatabase.getInstance().getReference();
        Button register =root.findViewById(R.id.btn_register);
        final EditText[] resetting_color = new EditText[1];
        final EditText name = (EditText) root.findViewById(R.id.et_name);
        final EditText email = (EditText) root.findViewById(R.id.et_email);
        final EditText pass = (EditText) root.findViewById(R.id.et_password);
        final EditText re_pass = (EditText) root.findViewById(R.id.et_repassword);
        final TextView register_result = (TextView) root.findViewById(R.id.register_result);
        register_result.setTextColor(Color.RED);
        resetting_color[0] = name;

        register.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //Получаем текст из полей
                final String name_text = name.getText().toString();
                String email_text = email.getText().toString();
                String password = pass.getText().toString();
                String re_password = re_pass.getText().toString();

                //Проверка на пустоты в полях
                if(!name_text.equals("")) {
                    if (!email_text.equals("")) {
                        if(!password.equals("")) {
                            if (!re_password.equals("")){

                                if (password.equals(re_password)) {
                                        reg.createUserWithEmailAndPassword(email_text, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if(task.isSuccessful()){
                                                    String currentUserID = reg.getCurrentUser().getUid();
                                                    db_ref.child("Users").child(currentUserID).child("name").setValue(name_text);

                                                    Intent i = new Intent(RegisterFragment.this.getActivity(), MainUserActivity.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                }
                                                else{
                                                    Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                } else {
                                    register_result.setText("Введённые пароли не совпадают");
                                }
                            } else {
                                resetting_color[0].setBackgroundColor(Color.TRANSPARENT);
                                re_pass.setBackgroundColor(Color.RED);
                                register_result.setText("Введите пароль повторно");
                                resetting_color[0] = re_pass;
                            }
                        }else{
                            resetting_color[0].setBackgroundColor(Color.TRANSPARENT);
                            pass.setBackgroundColor(Color.RED);
                            register_result.setText("Введите пароль");
                            resetting_color[0] = pass;
                        }
                    }else{
                        resetting_color[0].setBackgroundColor(Color.TRANSPARENT);
                        email.setBackgroundColor(Color.RED);
                        register_result.setText("Введите адрес email");
                        resetting_color[0] = email;
                    }
                }else{
                    resetting_color[0].setBackgroundColor(Color.TRANSPARENT);
                    name.setBackgroundColor(Color.RED);
                    register_result.setText("Введите имя");
                    resetting_color[0] = name;
                }
            }
        });
        return root;
    }

}