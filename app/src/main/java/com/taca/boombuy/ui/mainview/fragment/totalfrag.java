package com.taca.boombuy.ui.mainview.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.taca.boombuy.NetRetrofit.NetSSL;
import com.taca.boombuy.R;
import com.taca.boombuy.Resmodel.ResItems;
import com.taca.boombuy.networkmodel.ItemDTO;
import com.taca.boombuy.singleton.item_single;
import com.taca.boombuy.ui.mainview.activity.GiftSelectDetailInfoActivity;
import com.taca.boombuy.util.ImageProc;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class totalfrag extends Fragment {

    ResItems resItems;
    //ResBbSearchItem resBbSearchItem = new ResBbSearchItem();

    CustomListAdapter listAdapter;
    ListView listView;

    ImageView circleImageView;

    int page_num = 1;
    int cur_page_num;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.activity_totalfrag, container, false);

        listAdapter = null;

        circleImageView = (ImageView) rootView.findViewById(R.id.fab);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        listView = (ListView) rootView.findViewById(R.id.listview);
        listAdapter = new CustomListAdapter();

        getTotalItems(1);
        return rootView;
    }

    class ViewHolder {

        CheckBox lv_checkbox;
        ImageView lv_imageview;
        TextView lv_pname;
        TextView lv_pprice;
        Button lv_detailinfo;
    }


    // 상품 리스트 어뎁터
    class CustomListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return resItems.getResult().size();
        }

        @Override
        public ItemDTO getItem(int position) {
            return resItems.getResult().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        ViewHolder holder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.custom_listview_cell, null);

                holder = new ViewHolder();
                holder.lv_checkbox = (CheckBox) convertView.findViewById(R.id.lv_checkbox);
                holder.lv_imageview = (ImageView) convertView.findViewById(R.id.lv_imageview);
                holder.lv_pname = (TextView) convertView.findViewById(R.id.lv_pname);
                holder.lv_pprice = (TextView) convertView.findViewById(R.id.lv_pprice);
                holder.lv_detailinfo = (Button) convertView.findViewById(R.id.lv_detailinfo);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Log.i("IMAGE URL", getItem(position).getLocation());

            ImageProc.getInstance().drawImage(getItem(position).getLocation(), holder.lv_imageview);

            holder.lv_pname.setText(getItem(position).getName());
            // pcontent
            holder.lv_pprice.setText(String.format("%,3d", getItem(position).getPrice()) + "원");
            holder.lv_checkbox.setOnCheckedChangeListener(new MyTotalCheck(position));
            holder.lv_checkbox.setChecked(getItem(position).isChecked());

            holder.lv_detailinfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), GiftSelectDetailInfoActivity.class);
                    ItemDTO item = getItem(position);
                    intent.putExtra("item", item);
                    startActivity(intent);
                }
            });

            // 마지막 체크
            if (position == getCount() - 1) {
                // 최종 페이지라면 더이상 목록이 없습니다.등 메세지 처리 하면 됨.
                // 아니라면 다은 페이지를 가져온다.
                //Toast.makeText(getActivity(), "마지막", Toast.LENGTH_SHORT).show();
                Log.i("UI", "마지막");
                if (page_num == cur_page_num) {
                    page_num++;
                    // 통신
                    getTotalItems(getCount());
                }
            }

            return convertView;
        }
    }
    class MyTotalCheck implements  CompoundButton.OnCheckedChangeListener
    {
        ItemDTO itemDTO;
        int position;
        public MyTotalCheck(int position){
            this.position = position;
            itemDTO = resItems.getResult().get(position);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (isChecked) {
                Collections.reverse(item_single.getInstance().itemDTOArrayList); // 새로운 데이터를 리스트의 앞에 추가 해야하므로 리버스한 후 추가 후 다시 리버스
                boolean isDup = false;
                for(ItemDTO it : item_single.getInstance().itemDTOArrayList){
                    if( it.getId() == this.itemDTO.getId() ){
                        isDup = true;
                        break;
                    }
                }

                if(!isDup)
                    item_single.getInstance().itemDTOArrayList.add(this.itemDTO);
                Collections.reverse(item_single.getInstance().itemDTOArrayList);

                this.itemDTO.setChecked(true);


            } else {
                this.itemDTO.setChecked(false);
                item_single.getInstance().itemDTOArrayList.remove(this.itemDTO);

            }
        }
    };
    public void getTotalItems(final int getCount) {
        Call<ResItems> NetSearchItems = NetSSL.getInstance().getMemberImpFactory().NetSearchItems(page_num, 20);
        NetSearchItems.enqueue(new Callback<ResItems>() {
            @Override
            public void onResponse(Call<ResItems> call, Response<ResItems> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getResult() != null) {
                        cur_page_num = page_num;
                        //OttoBus.getInstance().getSearchItems_Bus().post(response.body());
                        FinishLoad(response.body(), getCount);
                    } else {
                        Log.i("RESPONSE RESULT 1: ", response.message());
                    }
                } else {
                    Log.i("RESPONSE RESULT 2 : ", response.message());
                }
            }
            @Override
            public void onFailure(Call<ResItems> call, Throwable t) {

            }
        });
    }

    public void FinishLoad(ResItems data, int getCount) {
        if (page_num == 1) {
            resItems = data;
            for(ItemDTO it : item_single.getInstance().itemDTOArrayList){
                for(ItemDTO newIt : resItems.getResult()){
                    if( it.getId()== newIt.getId() ){
                        newIt.setChecked(true);
                    }
                }
            }
        } else {
            resItems.getResult().addAll(data.getResult());
        }

        listView.setAdapter(listAdapter);
        listView.setSelection(getCount -1);
        ((totalfrag.CustomListAdapter) listView.getAdapter()).notifyDataSetChanged();

    }
}