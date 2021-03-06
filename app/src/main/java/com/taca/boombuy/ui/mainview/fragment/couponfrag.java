package com.taca.boombuy.ui.mainview.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link couponfrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link couponfrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class couponfrag extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    LayoutInflater inflater;

    ListView listview;
    CouponListViewAdapter couponAdapter;

    String []data = {"AAA","BBB","CCC"};

    boolean ottoflag = false;

    ResItems resItems;

    int page_num = 1;
    int cur_page_num;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        this.inflater = inflater;
        View view =  inflater.inflate(R.layout.fragment_couponfrag, container, false);
        couponAdapter = new CouponListViewAdapter();

        getCoupon(1);

        listview = (ListView) view.findViewById(R.id.listview);
        //listview.setNestedScrollingEnabled(false);

        CircleImageView fab = (CircleImageView) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().finish();
            }
        });

        return view;
    }

    class CouponViewHolder{

        @BindView(R.id.lv_pname)
        TextView lv_pname;

        @BindView(R.id.lv_checkbox)
        CheckBox lv_checkbox;

        @BindView(R.id.lv_imageview)
        ImageView lv_imageview;

        @BindView(R.id.lv_pprice)
        TextView lv_pprice;

        @BindView(R.id.lv_detailinfo)
        Button lv_detailinfo;


        public CouponViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    class CouponListViewAdapter extends BaseAdapter{

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

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CouponViewHolder couponViewHolder;
            convertView = inflater.inflate(R.layout.custom_listview_cell, parent, false);

            couponViewHolder = new CouponViewHolder(convertView);

            couponViewHolder.lv_pname.setText(getItem(position).getName());
            ImageProc.getInstance().drawImage(getItem(position).getLocation(), couponViewHolder.lv_imageview);
            couponViewHolder.lv_pprice.setText(String.format("%,3d", getItem(position).getPrice())+"원");

            couponViewHolder.lv_checkbox.setOnCheckedChangeListener(new MyCouponCheck(position));
            couponViewHolder.lv_checkbox.setChecked(getItem(position).isChecked());

           /* couponViewHolder.lv_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                    if (isChecked) {
                        Collections.reverse(item_single.getInstance().itemDTOArrayList); // 새로운 데이터를 리스트의 앞에 추가 해야하므로 리버스한 후 추가 후 다시 리버스
                        item_single.getInstance().itemDTOArrayList.add(item_single.getInstance().itemDTO);
                        Collections.reverse(item_single.getInstance().itemDTOArrayList);

                        // 선택한 곳
                        Toast.makeText(getActivity(), "선택", Toast.LENGTH_SHORT).show();

                    } else {
                        item_single.getInstance().itemDTOArrayList.remove(item_single.getInstance().itemDTO);
                        Toast.makeText(getActivity(), position + "번째 선택 취소", Toast.LENGTH_SHORT).show();
                    }
                }
            });*/

            couponViewHolder.lv_detailinfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), GiftSelectDetailInfoActivity.class);
                    ItemDTO item = resItems.getResult().get(position);
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
                    getCoupon(getCount());
                }
            }

            return convertView;
        }
    }

    class MyCouponCheck implements  CompoundButton.OnCheckedChangeListener
    {
        ItemDTO itemDTO;
        int position;
        public MyCouponCheck(int position){
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


    public void getCoupon(final int getCount) {
        Call<ResItems> NetSearchCoupon = NetSSL.getInstance().getMemberImpFactory().NetSearchCoupon(page_num, 20);
        NetSearchCoupon.enqueue(new Callback<ResItems>() {
            @Override
            public void onResponse(Call<ResItems> call, Response<ResItems> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getResult() != null) {
                        cur_page_num = page_num;
                        //OttoBus.getInstance().getSearchCoupons_Bus().post(response.body());
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


    public void FinishLoad(ResItems data, int getCount){

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

        listview.setAdapter(couponAdapter);
        ((couponfrag.CouponListViewAdapter)listview.getAdapter()).notifyDataSetChanged();

    }

    public couponfrag() {
    }

    public static couponfrag newInstance(String param1, String param2) {
        couponfrag fragment = new couponfrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //OttoBus.getInstance().getSearchCoupons_Bus().register(this);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}