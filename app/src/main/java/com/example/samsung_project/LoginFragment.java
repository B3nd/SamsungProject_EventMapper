package com.example.samsung_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;


public class LoginFragment extends Fragment  {

    Button auth;
    SharedPreferences pref;
    TextView Access;
    TextView serverResponse;
    String responsePOST;
    FirebaseAuth firebaseAuth;
    private DatabaseReference db_ref;
    FirebaseUser user;

    public LoginFragment() {}



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_login, null);

        final EditText email = (EditText) root.findViewById(R.id.et_email);
        final EditText password = (EditText) root.findViewById(R.id.et_password);
        final EditText[] resetting_color = new EditText[1];
        resetting_color[0] = email;
        final TextView login_answer = (TextView) root.findViewById(R.id.login_result);
        login_answer.setTextColor(Color.RED);
        final Button login = root.findViewById(R.id.btn_login);
        auth = root.findViewById(R.id.vk_login);
        firebaseAuth = FirebaseAuth.getInstance();
        db_ref = FirebaseDatabase.getInstance().getReference();
        pref = root.getContext().getSharedPreferences("AppPref", MODE_PRIVATE);
        if(pref.contains("email")){
            String pref_email = pref.getString("email",  "");
            email.setText(pref_email);
        }

        Access = login_answer;
        serverResponse = (TextView) root.findViewById(R.id.response);

        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginFragment.this.getActivity(), FirstVkActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //Получаем текст из полей
                final String user_email = email.getText().toString();
                String user_password = password.getText().toString();
                if(!user_email.equals("")) {
                    if(!user_password.equals("")) {

                            firebaseAuth.signInWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        user = firebaseAuth.getCurrentUser();
                                        SharedPreferences.Editor e = pref.edit();
                                        e.putString(user_email, "email").apply();
                                        Intent i = new Intent(LoginFragment.this.getActivity(), MainUserActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    } else {
                                        login_answer.setText("Вы ввели неправильный email или пароль");
                                    }
                                }
                            });


                    } else {
                        resetting_color[0].setBackgroundColor(Color.TRANSPARENT);
                        password.setBackgroundColor(Color.RED);
                        login_answer.setText("Введите пароль");
                        resetting_color[0] = password;
                    }
                } else {
                    resetting_color[0].setBackgroundColor(Color.TRANSPARENT);
                    email.setBackgroundColor(Color.RED);
                    login_answer.setText("Введите адрес email");
                    resetting_color[0] = email;
                }
            }
        });
        return root;
    }
}