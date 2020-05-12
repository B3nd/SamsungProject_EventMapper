package com.example.samsung_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class FirstVkActivity extends Activity {
    private String[] scope = new String[]{VKAccessToken.EMAIL};
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_vk_layout);

        VKSdk.login(this, scope);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(final VKAccessToken res) {

                auth.signInWithEmailAndPassword(res.email, res.accessToken).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Successful logged in!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(FirstVkActivity.this, MainUserActivity.class));
                        } else {
                            auth.createUserWithEmailAndPassword(res.email, res.accessToken).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(), "Successful logged in!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(FirstVkActivity.this, MainUserActivity.class));
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error caught : "  + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(FirstVkActivity.this, EnterActivity.class));
                                    }
                                }
                            });

                        }
                    }
                });



                /*
                String req_str = "http://api.vk.com/";///method/users.get?user_ids=" + res.userId + "&fields=first_name&access_token=" + res.accessToken + "&v=5.103";

                //https://api.vk.com/method/users.get?user_ids=349142579&fields=first_name&access_token=4f89828fb5cc36e8fab0f6e922e85c025c316d398dbffaef61d34ce6a5beeec725178760c6f4dfeabf47c&v=5.103


                Retrofit retrofit = new Retrofit.Builder().baseUrl(req_str).addConverterFactory(GsonConverterFactory.create()).build();
                VkRest service = retrofit.create(VkRest.class);
                Call<Model> call = service.request(res.userId, "first_name", res.accessToken, "5.103");
                call.enqueue(new Callback<Model>() {
                    @Override
                    public void onResponse(Call<Model> call, Response<Model> response) {
                        Model model = response.body();
                        String str = model.first_name + " " + model.last_name;

                        //Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                        new CurrentUser().name = str;
                        startActivity(new Intent(FirstVkActivity.this, MainUserActivity.class));

                    }

                    @Override
                    public void onFailure(Call<Model> call, Throwable t) {

                    }
                });

                 */



            }
            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(FirstVkActivity.this, EnterActivity.class));
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }




}