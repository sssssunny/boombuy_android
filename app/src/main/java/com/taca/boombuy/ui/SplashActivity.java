package com.taca.boombuy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.taca.boombuy.R;
import com.taca.boombuy.Single_Value;
import com.taca.boombuy.ui.sign.LoginActivity;
import com.taca.boombuy.util.ImageProc;

public class SplashActivity extends Activity {

    private int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageProc.getInstance().getImageLoader(this);

        // 초기값들 초기화
        Single_Value.getInstance().initValues();

        /*Single_Value.getInstance().vo_gift_total_member = new ArrayList<>();

        Single_Value.getInstance().vo_giftitem_lists.clear();
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
        Single_Value.getInstance().vo_giftitem_list = new VO_giftitem_list();
        Single_Value.getInstance().vo_giftitem_list.setProduct_imageView_cell(icon);
        Single_Value.getInstance().vo_giftitem_list.setProduct_title_cell("basic");
        Single_Value.getInstance().vo_giftitem_list.setProduct_price_cell("0");
        Single_Value.getInstance().vo_giftitem_lists.add(Single_Value.getInstance().vo_giftitem_list);*/

        /*item_single.getInstance().itemDTO = new ItemDTO(
                0, 0, "", 0, "", ""
        );
        item_single.getInstance().itemDTOArrayList.add(item_single.getInstance().itemDTO);*/


       /* Single_Value.getInstance().item_arraylist.clear();
        Bitmap icon2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.product_icon);
        for (int i = 0; i < 5; i++) {
            Single_Value.getInstance().vo_giftitem_list = new VO_giftitem_list();
            Single_Value.getInstance().vo_giftitem_list.setProduct_imageView_cell(icon2);
            Single_Value.getInstance().vo_giftitem_list.setProduct_title_cell("상품" + i);
            Single_Value.getInstance().vo_giftitem_list.setProduct_price_cell("30,001원");
            Single_Value.getInstance().item_arraylist.add(Single_Value.getInstance().vo_giftitem_list);
        }*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
