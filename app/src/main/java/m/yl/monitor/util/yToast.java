package m.yl.monitor.util;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import m.yl.monitor.Application;

/**
 * Created by Yl on 16/11/28.
 */

public class yToast {
    private static Toast sToast = null;
    private static Toast tToast = null;
    private static View v;
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_SOFTWARN = 2;
    public static final int TYPE_WARN = 3;
    public static final int TYPE_ERROR = 4;
    private static final int IMG_NONE = 100;
//    private static final int IMG_SUCCESS;
//    private static final int IMG_SOFT_WARNING;
//    private static final int IMG_WARNING;
//    private static final int IMG_ERROR;

    public yToast() {
    }

    public static void doing() {
        show((CharSequence)"亲，该功能正在内测哦，敬请期待");
    }

    public static void show(CharSequence string) {
        show(string, false);
    }

    public static void show(int stringId) {
        show(stringId, false);
    }

    public static void show(int stringId, boolean isLong) {
        show(Application.getInstance().getResources().getString(stringId), isLong);
    }

    public static synchronized void show(final CharSequence string, final boolean isLong) {
        Application.getInstance().runOnUIThread(new Runnable() {
            public void run() {
                try {
                    if(yToast.sToast == null) {
                        yToast.sToast = Toast.makeText(Application.getInstance(), "", isLong?1:0);
                    }

                    yToast.sToast.setDuration(isLong?1:0);
                    yToast.sToast.setText((CharSequence)(TextUtils.isEmpty(string)?"":string));
                    yToast.sToast.show();
                } catch (Exception var2) {
                    var2.printStackTrace();
                }

            }
        });
    }

    public static synchronized void show(final View view) {
        Application.getInstance().runOnUIThread(new Runnable() {
            public void run() {
                try {
                    if(yToast.sToast == null) {
                        yToast.sToast = new Toast(Application.getInstance());
                        yToast.sToast.setDuration(0);
                    }

                    yToast.sToast.setView(view);
                    yToast.sToast.show();
                } catch (Exception var2) {
                    var2.printStackTrace();
                }

            }
        });
    }

//    public static void tip(CharSequence string, int type) {
//        tip(string, type, false);
//    }
//
//    public static void tip(int stringId, int type) {
//        tip(stringId, type, false);
//    }

//    public static void tip(int stringId, int type, boolean isLong) {
//        tip(Application.getInstance().getResources().getString(stringId), type, isLong);
//    }

//    public static synchronized void tip(CharSequence string, int type, boolean isLong) {
//        int imgid = 100;
//        switch(type) {
//            case 1:
//                imgid = IMG_SUCCESS;
//                break;
//            case 2:
//                imgid = IMG_SOFT_WARNING;
//                break;
//            case 3:
//                imgid = IMG_WARNING;
//                break;
//            case 4:
//                imgid = IMG_ERROR;
//        }
//
//        makeTipsView(string.toString(), imgid);
//        if(tToast == null) {
//            tToast = new Toast(Application.getInstance().getBaseContext());
//        }
//
//        tToast.setView(v);
//        tToast.setGravity(17, 0, 0);
//        tToast.setDuration(isLong?1:0);
//        tToast.show();
//    }

//    private static View makeTipsView(String msg, int tipsImage) {
//        LayoutInflater lf = LayoutInflater.from(Application.getInstance().getApplicationContext());
//        if(v == null) {
//            v = lf.inflate(layout.wiz_view_tips, (ViewGroup)null, true);
//        }
//
//        TextView tips_msg = (TextView)v.findViewById(id.tips_msg);
//        ImageView tips_icon = (ImageView)v.findViewById(id.tips_icon);
//        if(tipsImage != 100) {
//            tips_icon.setImageResource(tipsImage);
//            tips_icon.setVisibility(0);
//        } else {
//            tips_icon.setVisibility(8);
//        }
//
//        tips_msg.setText(msg);
//        return v;
//    }

//    static {
//        IMG_SUCCESS = drawable.wiz_tips_success;
//        IMG_SOFT_WARNING = drawable.wiz_tips_smile;
//        IMG_WARNING = drawable.wiz_tips_warning;
//        IMG_ERROR = drawable.wiz_tips_error;
//    }
}
