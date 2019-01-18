package m.yl.monitor.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Yl on 16/12/24.
 */
public class ViewHolder {
    private int mPostion;
    private View mConvertView;
    private SparseArray<View> views;

    private ViewHolder(Context context, ViewGroup parent, int postion,
                       int layoutId) {
        mPostion = postion;
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        views = new SparseArray<View>();
        mConvertView.setTag(this);

    }

    public static ViewHolder getContvetView(Context context, View convertView,
                                            ViewGroup parent, int postion, int layoutId) {
        if (convertView == null) {
            return new ViewHolder(context, parent, postion, layoutId);
        } else {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.mPostion = postion;
            return holder;
        }

    }

    public View getView(int viewId) {
        View view = views.get(viewId);

        if (view == null) {
            view = mConvertView.findViewById(viewId);
            views.put(viewId, view);
        }
        return view;

    }

    public View getConvertView() {
        return mConvertView;

    }

    public ViewHolder setText(int viewId, String text) {
        ((TextView) getView(viewId)).setText(text);
        return this;
    }
}
