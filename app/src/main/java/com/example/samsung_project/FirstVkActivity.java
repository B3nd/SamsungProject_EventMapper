package com.example.samsung_project;

import android.app.Activity;
import android.content.Intent;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.util.Base64;
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
import com.vk.sdk.api.httpClient.VKHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

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
            String str;
            @Override
            public void onResult(final VKAccessToken res) {
                //Создание пароля из адреса электронной почты и ID пользователя
                final String generatedPassword = Integer.toHexString(res.userId.hashCode()) + res.email.charAt(res.email.length() - 2) + "-Rwv+" + res.userId.substring(res.userId.length() / 2) + Integer.toHexString(res.email.substring(0, res.email.length() - 2).hashCode()) + "qqbs" + res.userId.charAt(2) + "*/lzaq" + Integer.toHexString(res.email.substring(res.email.length() / 2, res.email.length() - 4).hashCode());
                //Шифрование созданного выше пароля
                final String base64pass = Base64.encodeToString(generatedPassword.getBytes(), 0);
                auth.signInWithEmailAndPassword(res.email, base64pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Если пользователя нет в системе - регистрируем
                            Toast.makeText(getApplicationContext(), "Successful logged in!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(FirstVkActivity.this, MainUserActivity.class));
                        } else {
                            auth.createUserWithEmailAndPassword(res.email, base64pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
                try {
                    URI u = new URI("https://api.vk.com/method/users.get?user_ids=" + res.userId + "&fields=&access_token=" + res.accessToken + "&v=5.103");
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet g = new HttpGet(u)
                    HttpResponse response = httpclient.execute(g);
                    StatusLine statusLine = response.getStatusLine();
                    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        String string = out.toString();
                        out.close();
                    } else {
                        response.getEntity().getContent().close();
                    }



                } catch (Exception e){
                    e.getLocalizedMessage();
                }

                 */




/*
                auth.signInWithEmailAndPassword(res.email, res.secret).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Successful logged in!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(FirstVkActivity.this, MainUserActivity.class));
                        } else {
                            auth.createUserWithEmailAndPassword(res.email, res.secret).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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

 */




/*
                String req_str = "http://api.vk.com/";///method/users.get?user_ids=" + res.userId + "&fields=first_name&access_token=" + res.accessToken + "&v=5.103";

                //https://api.vk.com/method/users.get?user_ids=349142579&fields=first_name&access_token=4f89828fb5cc36e8fab0f6e922e85c025c316d398dbffaef61d34ce6a5beeec725178760c6f4dfeabf47c&v=5.103


                Retrofit retrofit = new Retrofit.Builder().baseUrl(req_str).addConverterFactory(GsonConverterFactory.create()).build();
                VkRest service = retrofit.create(VkRest.class);
                Call<Model> call = service.request(res.userId, "first_name,last_name", res.accessToken, "5.103");
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