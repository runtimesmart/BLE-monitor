package m.yl.monitor.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

import m.yl.monitor.util.yLog;

/**
 * Created by Yl on 16/11/28.
 */

public class BaseActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        yLog.e("------onCreate-------");
    }

    @Override
    protected void onStart() {
        super.onStart();
        yLog.e("------onStart-------");

    }

    @Override
    protected void onResume() {
        super.onResume();
        yLog.e("------onResume-------");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        yLog.e("------onRestart-------");

    }

    @Override
    protected void onPause() {
        super.onPause();
        yLog.e("------onPause-------");



    }

    @Override
    protected void onStop() {
        super.onStop();
        yLog.e("------onStop-------");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        yLog.e("------onDestroy-------");

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
