package com.android.base.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangZhiGuo on 2016-11-9.
 * describe 通用ViewPager适配器
 */
public class CommonPagerAdapter<T> extends PagerAdapter {

    private Context mContext;
    private int itemId;
    private List<T> mData;

    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;

    public CommonPagerAdapter(Context context, int itemLayoutId) {
        mContext = context;
        itemId = itemLayoutId;
        mData = new ArrayList<>();
    }

    /* setData之前调用 */
    public void setOnClickListener(View.OnClickListener listener) {
        clickListener = listener;
    }

    /* setData之前调用 */
    public void setOnLongClickListener(View.OnLongClickListener listener) {
        longClickListener = listener;
    }

    public void newData(List<T> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(List<T> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(T data) {
        mData.add(data);
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView view = (ImageView) View.inflate(mContext, itemId, null);
        T item = mData.get(position);
        // setImage
        Glide.with(mContext)
                .load(item)
                .crossFade(100)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(view);
        // setListener
        if (clickListener != null) {
            view.setOnClickListener(clickListener);
        }
        if (longClickListener != null) {
            view.setOnLongClickListener(longClickListener);
        }
        // addView
        container.addView(view);
        return view;
    }
}
