package com.taca.boombuy.ui.sign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.taca.boombuy.R;
import com.taca.boombuy.Single_Value;
import com.taca.boombuy.database.StorageHelper;
import com.taca.boombuy.evt.OTTOBus;
import com.taca.boombuy.net.Network;
import com.taca.boombuy.netmodel.LonInModel;
import com.taca.boombuy.ui.mainview.activity.MainActivity;
import com.taca.boombuy.util.U;

public class SignInActivity extends AppCompatActivity {

    // 버튼
    ImageButton btn_auto_signin;
    TextView btn_signup;

    EditText et_signin_id;
    EditText et_signin_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // 이벤트 받을 녀석(회원가입 완료 메시지)
        OTTOBus.getInstance().getBus().register(this);

        et_signin_id = (EditText) findViewById(R.id.et_signin_id);
        et_signin_password = (EditText) findViewById(R.id.et_signin_password);

        // 자동로그인 후
       if (StorageHelper.getInstance().getBoolean(SignInActivity.this, "auto_login")) {
            et_signin_id.setText(StorageHelper.getInstance().getString(SignInActivity.this, "my_phone_number"));
            et_signin_password.setText(StorageHelper.getInstance().getString(SignInActivity.this, "auto_login_password"));
            // 서버로부터 로그인
            Single_Value.getInstance().lonInModel = new LonInModel();
            Single_Value.getInstance().lonInModel.setPhone(StorageHelper.getInstance().getString(SignInActivity.this, "my_phone_number"));
            Single_Value.getInstance().lonInModel.setPassword(StorageHelper.getInstance().getString(SignInActivity.this, "auto_login_password"));
            Network.getInstance().bb_Login(getApplicationContext(), Single_Value.getInstance().lonInModel);
        }

        // 버튼 매칭 및 클릭리스너
        btn_auto_signin = (ImageButton) findViewById(R.id.btn_auto_signin);
        btn_auto_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 서버로부터 로그인
                Single_Value.getInstance().lonInModel = new LonInModel();
                Single_Value.getInstance().lonInModel.setPhone(et_signin_id.getText().toString());
                Single_Value.getInstance().lonInModel.setPassword(et_signin_password.getText().toString());
                Network.getInstance().bb_Login(getApplicationContext(), Single_Value.getInstance().lonInModel);
            }
        });

        btn_signup = (TextView) findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    request_READ_PHONE_STATE_permission();
                } else {
                    // my_phone_number 쉐어드프리퍼런스에 내 전화번호 저장
                    StorageHelper.getInstance().setString(SignInActivity.this, "my_phone_number", U.getInstance().getMyPhoneNum(SignInActivity.this));
                    Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    // 오토버스 이벤트 도착
    @Subscribe
    public void FinishLoad(String data) {
        Log.i("OTTO", data);
        if (data.contains("성공")) {
            StorageHelper.getInstance().setBoolean(SignInActivity.this, "auto_login", true);
            StorageHelper.getInstance().setString(SignInActivity.this, "auto_login_password", et_signin_password.getText().toString());
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // 오토버스 썼으면 등록해제
            OTTOBus.getInstance().getBus().unregister(this);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignInActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // my_phone_number 쉐어드프리퍼런스에 내 전화번호 저장
                    StorageHelper.getInstance().setString(SignInActivity.this, "my_phone_number", U.getInstance().getMyPhoneNum(SignInActivity.this));
                    Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                    startActivity(intent);
                } else {
                    // 사용자가 권한 동의를 안하므로 종료
                    finish();
                }
            }
        }
    }

    public void request_READ_PHONE_STATE_permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없을 경우

            //최초 권한 요청인지 혹은 사용자에 의한 재요청인지 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                //사용자가 임의로 권한을 취소시킨 경우
                //권한 재요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            } else {
                //최초로 권한을 요청하는 경우(첫실행)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            }
        } else {
            // my_phone_number 쉐어드프리퍼런스에 내 전화번호 저장
            StorageHelper.getInstance().setString(SignInActivity.this, "my_phone_number", U.getInstance().getMyPhoneNum(SignInActivity.this));
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        }
    }
}
