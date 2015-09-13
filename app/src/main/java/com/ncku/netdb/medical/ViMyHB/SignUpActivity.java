package com.ncku.netdb.medical.ViMyHB;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import java.io.UnsupportedEncodingException;


public class SignUpActivity extends ActionBarActivity {

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        sharedPref = getSharedPreferences("main", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.clear();
        Button button;

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.phoneInput);

                User.userPhoneNumber = input.getText().toString();
                editor.putString(getString(R.string.saved_user_phone_number),
                        User.userPhoneNumber);
                Log.d("input number", input.getText().toString());
                editor.commit();
                if (User.userPhoneNumber.length() == 10) {
                    inputID();
                }
                else {
                    Toast.makeText(view.getContext(), "格式錯誤, 請輸入正確電話號碼格式",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void inputID () {
        setContentView(R.layout.input_user_id);
        Button button;
        button = (Button) findViewById(R.id.button);
        button.setText("註冊");

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.IDInput);
                EditText passwordInput = (EditText) findViewById(R.id.passwdInput);

                User.userID = input.getText().toString();
                User.password = passwordInput.getText().toString();

                editor = sharedPref.edit();
                editor.putString(getString(R.string.saved_user_ID),
                        User.userID);

                editor.putString(getString(R.string.saved_user_password),
                        User.password);
                editor.commit();


                if (User.userID.length() == 4 && User.password.length() > 0) {
                    postSingUp(User.userPhoneNumber, User.userID, User.password);
                    Toast.makeText(view.getContext(), "請稍候...",
                            Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e){

                    }
                    if (User.isLogin) {
                        Toast.makeText(view.getContext(), "註冊成功!",
                                Toast.LENGTH_SHORT).show();
                        Intent refresh = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(refresh);
                        finish();

                    }
                    else {

                        Toast.makeText(view.getContext(), "註冊失敗，請重新輸入",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(view.getContext(), "格式錯誤, 請輸入正確格式",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void postSingUp (String userPhoneNumber, String userID, String password) {

        MultipartEntity postParams = new MultipartEntity();
        try {
            postParams.addPart("user_phone", new StringBody(userPhoneNumber));
            postParams.addPart("social_front_4_id", new StringBody(userID));
            postParams.addPart("password", new StringBody(password));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        new HttpAsyncTask().execute(Config.Host + Config.signUpUrl, "post", postParams);
    }

}
