package com.taca.boombuy.ui.mainview.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.taca.boombuy.NetRetrofit.NetSSL;
import com.taca.boombuy.R;
import com.taca.boombuy.Resmodel.ResItems;
import com.taca.boombuy.evt.OttoBus;
import com.taca.boombuy.networkmodel.ItemDTO;
import com.taca.boombuy.ui.mainview.activity.GiftSelectDetailInfoActivity;
import com.taca.boombuy.util.ImageProc;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class brandSelectfrag extends Fragment {

    public brandSelectfrag() {
        // Required empty public constructor
    }
    ResItems resItems;

    RecyclerView selectedbrand_recyclerview;
    RecyclerAdapter recyclerAdapter;

    GridLayoutManager gridLayoutManager;
    int bid =0;

    //////

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    //////



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OttoBus.getInstance().getSearchBrandItem_Bus().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_selectbrandsitem, container, false);

        selectedbrand_recyclerview = (RecyclerView) view.findViewById(R.id.selectedbrand_recyclerview);
        recyclerAdapter = new RecyclerAdapter();

        gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 1);
        gridLayoutManager.setOrientation(OrientationHelper.VERTICAL);

        selectedbrand_recyclerview.setLayoutManager(gridLayoutManager);


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getActivity().finish();

                //onBackPressed();

            }
        });

        //fragmentTransaction.commit();

        Bundle bundle= getArguments();

        Log.i("선택된 프레그먼트 ", bid+"제발");
        bid = bundle.getInt("bid");

        Log.i("브랜드번호 : " ,  bid +"");

        Call<ResItems> NetSearchBrandItem = NetSSL.getInstance().getMemberImpFactory().NetSearchBrandItem(bid);

        NetSearchBrandItem.enqueue(new Callback<ResItems>() {
            @Override
            public void onResponse(Call<ResItems> call, Response<ResItems> response) {

                if(response.isSuccessful() ){
                    if(response.body() != null && response.body().getResult() != null){

                        OttoBus.getInstance().getSearchBrandItem_Bus().post(response.body());

                        Log.i("DATA", response.body().toString());

                    }else{

                        Log.i("RESPONSE RESULT 1: " , response.message());

                    }
                }else{

                    Log.i("RESPONSE RESULT 2 : " , response.message());
                }

            }

            @Override
            public void onFailure(Call<ResItems> call, Throwable t) {

                Log.i("FAILURE : ", t.getMessage());
            }
        });

        return view;
    }

    @Subscribe
    public void FinishLoad(ResItems data){
        resItems = data;
        selectedbrand_recyclerview.setNestedScrollingEnabled(false);
        selectedbrand_recyclerview.setAdapter(recyclerAdapter);
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.lv_checkbox)
        CheckBox lv_checkbox;
        @BindView(R.id.lv_imageview)
        ImageView lv_imageview;
        @BindView(R.id.lv_pname)
        TextView lv_pname;
        @BindView(R.id.lv_pprice)
        TextView lv_pprice;
        @BindView(R.id.lv_detailinfo)
        Button lv_detailinfo;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder>{


        @Override
        public int getItemCount() {
            return resItems.getResult().size();
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.custom_listview_cell, null);
            return new RecyclerViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, final int position) {

            ImageProc.getInstance().drawImage(resItems.getResult().get(position).getLocation(), holder.lv_imageview);

            holder.lv_detailinfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(getActivity(), "hello", Toast.LENGTH_SHORT).show();
                }
            });

            holder.lv_pname.setText(resItems.getResult().get(position).getName());
            holder.lv_pprice.setText(resItems.getResult().get(position).getPrice()+"원");

            holder.lv_detailinfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), GiftSelectDetailInfoActivity.class);
                    ItemDTO item = resItems.getResult().get(position);
                    intent.putExtra("item", item);
                    startActivity(intent);
                }
            });




        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
//
//        Fragment brandfrag = new brandfrag();
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//        fragmentTransaction.add(R.id.container_Frag, brandfrag);
//        fragmentTransaction.commit();
    }


}