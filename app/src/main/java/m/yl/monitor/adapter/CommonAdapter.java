package m.yl.monitor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yl on 16/12/24.
 */

public abstract class CommonAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected List<T> mList;
    protected int mLayoutId;

    public CommonAdapter(Context Context, int layoutId) {
        mList = new ArrayList<T>();
        mContext = Context;
        mLayoutId = layoutId;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        // TODO Auto-generated method stub
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public void setData(List<T> list) {
        mList = list;
        notifyDataSetChanged();
    }
    public void removeItem(int position) {
        if (position < 0 || position >= getCount()) {
            return;
        }
        mList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = ViewHolder.getContvetView(mContext, convertView,
                parent, position, mLayoutId);
        converView(holder,getItem(position));
        return holder.getConvertView();
    }

    protected abstract void converView(ViewHolder holder,T t);

}
