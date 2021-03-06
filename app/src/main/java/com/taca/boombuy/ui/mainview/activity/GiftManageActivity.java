package com.taca.boombuy.ui.mainview.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.taca.boombuy.NetRetrofit.NetSSL;
import com.taca.boombuy.R;
import com.taca.boombuy.Resmodel.ResBasic;
import com.taca.boombuy.Resmodel.ResMyProfile;
import com.taca.boombuy.Single_Value;
import com.taca.boombuy.database.StorageHelper;
import com.taca.boombuy.ui.mainview.fragment.ReceivedGiftFrag;
import com.taca.boombuy.ui.mainview.fragment.SentGiftFrag;
import com.taca.boombuy.util.ImageProc;
import com.taca.boombuy.util.U;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GiftManageActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SentGiftFrag.OnFragmentInteractionListener,
        ReceivedGiftFrag.OnFragmentInteractionListener{

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private FragmentAdapter fragPagerAdapter ;

    ViewPager mViewPager;
    TabLayout tabLayout;

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    // 프로필 사진
    CircleImageView iv_profile;
    // 프로필 이름
    TextView tv_profile_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_gift_manage);

        ImageProc.getInstance().getImageLoader(this);

        Log.i("TOTAL PRODUCT", Single_Value.getInstance().vo_gift_total_member.toString());

        mViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        fragPagerAdapter = new FragmentAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(fragPagerAdapter);

        if(getIntent().getStringExtra("receivedFCM") != null){
            mViewPager.setCurrentItem(1);
        }
        tabLayout.setupWithViewPager(mViewPager);

        toolbar = (Toolbar) findViewById(R.id.toolbar_gift_manage);
        setSupportActionBar(toolbar);

        // 네비게이션 //////////////////////////////////////////////////////////////////////////////////
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_gift_manage);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view_gift_manage);
        navigationView.setNavigationItemSelectedListener(this);
        ///////////////////////////////////////////////////////////////////////////////////////////////
        getProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        getProfile();
    }
    public void onGoHome(View view) {
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
        finish();
    }

    public void getProfile() {
        // 내 프로필 가져오기//////////////////////////////////////////////////////////////////////////////
        Call<ResMyProfile> NetMyProfile = NetSSL.getInstance().getMemberImpFactory().NetMyProfile();
        NetMyProfile.enqueue(new Callback<ResMyProfile>() {
            @Override
            public void onResponse(Call<ResMyProfile> call, Response<ResMyProfile> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getResult() != null) {
                        // 드로어 내 이름 설정
                        // 유저 이름 쉐어드프리퍼런스에 저장
                        StorageHelper.getInstance().setString(getApplicationContext(), "user_name", response.body().getResult().getName());

                        tv_profile_name = (TextView) findViewById(R.id.tv_profile_name);
                        tv_profile_name.setText(response.body().getResult().getName() + " 님");

                        // 드로어 내 이미지 설정
                        iv_profile = (CircleImageView) findViewById(R.id.iv_profile);


                        // 네비게이션 프로필 사진 누르면 갤러리로 이동
                        iv_profile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onChangeProfile();
                            }
                        });
                        //

                        ImageProc.getInstance().drawImage(response.body().getResult().getLocation(), iv_profile);
                    } else {
                        Log.i("RESPONSE RESULT 1: ", response.message());
                    }
                } else {
                    Log.i("RESPONSE RESULT 2 : ", response.message());
                }
            }

            @Override
            public void onFailure(Call<ResMyProfile> call, Throwable t) {

            }
        });
    }
    // 네비게이션 프로필 사진 변경 작업 //////////////////////////////////////////////////////
    // 네비게이션의 프로필 이미지를 눌렀을 때 앨범으로 이동
    public void onChangeProfile() {

        //iv_profile = (CircleImageView) findViewById(R.id.iv_profile);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        //requestCode 100
        startActivityForResult(photoPickerIntent, 100);
    }

    // 앨범에서 사진 하나 선택했을 때 result를 받아서 비트맵으로 변경 후 프로필에 적용
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case 100:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = imageReturnedIntent.getData();
                        Log.i("파일 경로 : ", getPath(selectedImage) + "");
                        onFileUpload(getPath(selectedImage));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void onFileUpload(String path) {
        File file = new File(path); // 이미지파일주소는 확인됨
        Map<String, RequestBody> map = new HashMap<>();
        //RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        map.put("photos\"; filename=\"a.jpg\"", fileBody);
        Call<ResBasic> NetChangeImage = NetSSL.getInstance().getMemberImpFactory().NetChangeImage(map);
        NetChangeImage.enqueue(new Callback<ResBasic>() {
            @Override
            public void onResponse(Call<ResBasic> call, Response<ResBasic> response) {
                if (response.isSuccessful()) {
                    Log.i("Result : ", "성공");
                    getProfile();
                } else {

                    Log.i("RESPONSE RESULT 2 : ", response.message());
                }
            }

            @Override
            public void onFailure(Call<ResBasic> call, Throwable t) {
                Log.i("RESPONSE RESULT 3 : ", t.getMessage());
            }
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // 네비게이션 아이템 클릭했을 때 이벤트
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_boombuy_shop) {
            Intent intent = new Intent(GiftManageActivity.this, MainProduct.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_gift_manage) {

        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(GiftManageActivity.this, SettingActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_sync_friends) {
            // 전화번호 동기화
            U.getInstance().sendPhoneNumber(this);
        } else if (id == R.id.nav_withdrawal) {
            // 회원 탈퇴
            Call<ResBasic> NetWithdrawal = NetSSL.getInstance().getMemberImpFactory().NetWithdrawal();
            NetWithdrawal.enqueue(new Callback<ResBasic>() {
                @Override
                public void onResponse(Call<ResBasic> call, Response<ResBasic> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null && response.body().getMessage() != null) {
                            Log.i("Result : ", response.body().getMessage());

                            // 자동로그인 지우기
                            // 패스워드 지우기
                            // 어플 종료
                            StorageHelper.getInstance().setBoolean(GiftManageActivity.this, "auto_login", false);
                            StorageHelper.getInstance().setString(GiftManageActivity.this, "auto_login_password", "");
                            finish();

                        } else {
                            Log.i("RESPONSE RESULT 1: ", response.message());
                        }
                    } else {

                        Log.i("RESPONSE RESULT 2 : ", response.message());
                    }
                }

                @Override
                public void onFailure(Call<ResBasic> call, Throwable t) {
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_gift_manage);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gift_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    class FragmentAdapter extends FragmentPagerAdapter {

        Fragment[] frags = new Fragment[]{
                new SentGiftFrag(), new ReceivedGiftFrag()
        };

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if(position == 0) {

                return "보낸 선물";

            }else{
                return "받은 선물";
            }
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_gift_manage);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
