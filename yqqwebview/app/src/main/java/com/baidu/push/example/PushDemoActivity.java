package com.baidu.push.yqqwebview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.android.pushservice.BasicPushNotificationBuilder;
import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.ufosdk.UfoSDK;

import java.util.List;

/*
 * 云推送Demo主Activity。
 * 代码中，注释以Push标注开头的，表示接下来的代码块是Push接口调用示例
 */
public class PushDemoActivity extends Activity implements OnClickListener {

    private static final String TAG = PushDemoActivity.class.getSimpleName();
    Button initWithApiKey = null;
    Button displayRichMedia = null;
    Button setTags = null;
    Button delTags = null;
    Button clearLog = null;
    Button showTags = null;
    Button unbind = null;
    Button setunDisturb = null;
    TextView logText = null;
    ScrollView scrollView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.logStringCache = Utils.getLogText(getApplicationContext());

        setContentView(R.layout.activity_main);
        initWithApiKey = (Button) findViewById(R.id.btn_initAK);
        displayRichMedia = (Button) findViewById(R.id.btn_rich);
        setTags = (Button) findViewById(R.id.btn_setTags);
        delTags = (Button) findViewById(R.id.btn_delTags);
        clearLog = (Button) findViewById(R.id.btn_clear_log);
        showTags = (Button) findViewById(R.id.btn_showTags);
        unbind = (Button) findViewById(R.id.btn_unbindTags);
        setunDisturb = (Button) findViewById(R.id.btn_setunDisturb);
        logText = (TextView) findViewById(R.id.text_log);
        scrollView = (ScrollView) findViewById(R.id.stroll_text);

        initWithApiKey.setOnClickListener(this);
        setTags.setOnClickListener(this);
        delTags.setOnClickListener(this);
        displayRichMedia.setOnClickListener(this);
        clearLog.setOnClickListener(this);
        showTags.setOnClickListener(this);
        unbind.setOnClickListener(this);
        setunDisturb.setOnClickListener(this);

        // Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
        // 这里把apikey存放于manifest文件中，只是一种存放方式，
        // 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
        // "api_key")
        ！！请将AndroidManifest.xml api_key 字段值修改为自己的 api_key 方可使用 ！！
        ！！ATTENTION：You need to modify the value of api_key to your own in AndroidManifest.xml to use this Demo !!
        // 启动百度push
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(PushDemoActivity.this, "api_key"));
        // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
        // PushManager.enableLbs(getApplicationContext());

        /**
         * 以下通知栏设置2选1。使用默认通知时，无需添加以下设置代码。
         */

        // 1.默认通知
        // 若您的应用需要适配Android O（8.x）系统，且将目标版本targetSdkVersion设置为26及以上时：
        // SDK提供设置Android O（8.x）新特性---通知渠道的设置接口。
        // 若不额外设置，SDK将使用渠道名默认值"Push"；您也可以仿照以下3行代码自定义channelId/channelName。
        // 注：非targetSdkVersion 26的应用无需以下调用且不会生效
        BasicPushNotificationBuilder bBuilder = new BasicPushNotificationBuilder();
        bBuilder.setChannelId("testDefaultChannelId");
        bBuilder.setChannelName("testDefaultChannelName");
        // PushManager.setDefaultNotificationBuilder(this, bBuilder); //使自定义channel生效

        // 2.自定义通知
        // 设置自定义的通知样式，具体API介绍见用户手册
        // 请在通知推送界面中，高级设置->通知栏样式->自定义样式，选中并且填写值：1，
        // 与下方代码中 PushManager.setNotificationBuilder(this, 1, cBuilder)中的第二个参数对应
        CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
                R.layout.notification_custom_builder,
                R.id.notification_icon,
                R.id.notification_title,
                R.id.notification_text);

        cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
        cBuilder.setNotificationDefaults(Notification.DEFAULT_VIBRATE);
        cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
        cBuilder.setLayoutDrawable(R.drawable.simple_notification_icon);
        cBuilder.setNotificationSound(Uri.withAppendedPath(
                Audio.Media.INTERNAL_CONTENT_URI, "6").toString());
        // 若您的应用需要适配Android O（8.x）系统，且将目标版本targetSdkVersion设置为26及以上时：
        // 可自定义channelId/channelName, 若不设置则使用默认值"Push"；
        // 注：非targetSdkVersion 26的应用无需以下2行调用且不会生效
        cBuilder.setChannelId("testId");
        cBuilder.setChannelName("testName");
        // 推送高级设置，通知栏样式设置为下面的ID，ID应与server下发字段notification_builder_id值保持一致
        PushManager.setNotificationBuilder(this, 1, cBuilder);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_initAK:
                initWithApiKey();
                break;
            case R.id.btn_rich:
                openRichMediaList();
                break;
            case R.id.btn_setTags:
                setTags();
                break;
            case R.id.btn_delTags:
                deleteTags();
                break;
            case R.id.btn_clear_log:
                Utils.logStringCache = "";
                Utils.setLogText(getApplicationContext(), Utils.logStringCache);
                updateDisplay();
                break;
            case R.id.btn_showTags:
                showTags();
                break;
            case R.id.btn_unbindTags:
                unBindForApp();
                break;
            case R.id.btn_setunDisturb:
                setunDistur();
                break;
            default:
                break;
        }
    }

    // 打开富媒体列表界面
    private void openRichMediaList() {
        Intent sendIntent = new Intent();
        sendIntent.setClassName(getBaseContext(),
                "com.baidu.android.pushservice.richmedia.MediaListActivity");
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PushDemoActivity.this.startActivity(sendIntent);
    }

    // 删除tag操作
    private void deleteTags() {
        LinearLayout layout = new LinearLayout(PushDemoActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText textviewGid = new EditText(PushDemoActivity.this);
        textviewGid.setHint(R.string.tags_hint);
        layout.addView(textviewGid);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                PushDemoActivity.this);
        builder.setView(layout);
        builder.setPositiveButton(R.string.text_btn_delTags,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Push: 删除tag调用方式
                        List<String> tags = Utils.getTagsList(textviewGid
                                .getText().toString());
                        PushManager.delTags(getApplicationContext(), tags);
                    }
                });
        builder.show();
    }

    // 设置标签,以英文逗号隔开
    private void setTags() {
        LinearLayout layout = new LinearLayout(PushDemoActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText textviewGid = new EditText(PushDemoActivity.this);
        textviewGid.setHint(R.string.tags_hint);
        layout.addView(textviewGid);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                PushDemoActivity.this);
        builder.setView(layout);
        builder.setPositiveButton(R.string.text_btn_setTags,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Push: 设置tag调用方式
                        List<String> tags = Utils.getTagsList(textviewGid
                                .getText().toString());
                        PushManager.setTags(getApplicationContext(), tags);
                    }

                });
        builder.show();
    }

    // api_key 绑定
    private void initWithApiKey() {
        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(PushDemoActivity.this, "api_key"));
    }

    // 解绑
    private void unBindForApp() {
        // Push：解绑
        PushManager.stopWork(getApplicationContext());
    }

    // 列举tag操作
    private void showTags() {
        // Push：标签列表
        PushManager.listTags(getApplicationContext());
    }

    // 设置免打扰时段
    private void setunDistur() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.bpush_setundistur_time);

        final TimePicker startPicker = (TimePicker) window
                .findViewById(R.id.start_time_picker);
        final TimePicker endPicker = (TimePicker) window
                .findViewById(R.id.end_time_picker);
        startPicker.setIs24HourView(true);
        endPicker.setIs24HourView(true);
        startPicker
                .setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
        endPicker
                .setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        Button set = (Button) window.findViewById(R.id.btn_set);
        set.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int startHour = startPicker.getCurrentHour();
                int startMinute = startPicker.getCurrentMinute();
                int endHour = endPicker.getCurrentHour();
                int endMinute = endPicker.getCurrentMinute();

                if (startHour == 0 && startMinute == 0 && endHour == 0
                        && endMinute == 0) {
                    Toast.makeText(getApplicationContext(), R.string.text_cancel_disturb,
                            Toast.LENGTH_SHORT).show();
                } else if (startHour > endHour
                        || (startHour == endHour && startMinute > endMinute)) {
                    setToastText(getString(R.string.text_first_day) + startHour + ":" + startMinute,
                            getString(R.string.text_second_day) + endHour + ":" + endMinute);
                } else {
                    setToastText(startHour + ":" + startMinute, endHour + ":"
                            + endMinute);
                }

                // Push: 设置免打扰时段
                // startHour startMinute：开始 时间 ，24小时制，取值范围 0~23 0~59
                // endHour endMinute：结束 时间 ，24小时制，取值范围 0~23 0~59
                PushManager.setNoDisturbMode(getApplicationContext(),
                        startHour, startMinute, endHour, endMinute);

                alertDialog.cancel();
            }

        });
        Button guide = (Button) window.findViewById(R.id.btn_guide);
        guide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(PushDemoActivity.this)
                        .setTitle(R.string.text_disturb_title)
                        .setMessage(R.string.text_disturb_explain)
                        .setPositiveButton(R.string.prompt_confirm, null)
                        .show();
            }
        });

        Button cancel = (Button) window.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

    }

    private void setToastText(String start, String end) {
        String text = getString(R.string.text_toast, start, end);
        int indexTotal = 13 + start.length();
        int indexPosition = indexTotal + 3 + end.length();
        SpannableString s = new SpannableString(text);
        s.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.red)),
                13, indexTotal, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        s.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.red)),
                indexTotal + 3, indexPosition,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(Menu.NONE, Menu.FIRST + 1, 1, R.string.prompt_about).setIcon(
                android.R.drawable.ic_menu_info_details);

        menu.add(Menu.NONE, Menu.FIRST + 2, 2, R.string.prompt_help).setIcon(
                android.R.drawable.ic_menu_help);

        menu.add(Menu.NONE, Menu.FIRST + 3, 3, R.string.prompt_feedback);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (Menu.FIRST + 1 == item.getItemId()) {
            showAbout();
            return true;
        }
        if (Menu.FIRST + 2 == item.getItemId()) {
            showHelp();
            return true;
        }

        if (Menu.FIRST + 3 == item.getItemId()) {
            showFeedback();
        }
        return false;
    }

    // 反馈
    private void showFeedback() {
        Intent intent = UfoSDK.getStartFaqIntent(this);
        startActivity(intent);
    }

    // 关于
    private void showAbout() {
        Dialog alertDialog = new AlertDialog.Builder(PushDemoActivity.this)
                .setTitle(R.string.prompt_about).setMessage(R.string.text_about)
                .setPositiveButton(R.string.prompt_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Log.i(TAG, "onclick...");
                    }

                }).create();
        alertDialog.show();
    }

    // 帮助
    private void showHelp() {
        Dialog alertDialog = new AlertDialog.Builder(PushDemoActivity.this)
                .setTitle(R.string.prompt_help).setMessage(R.string.text_help)
                .setPositiveButton(R.string.prompt_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Log.i(TAG, "onclick...");
                    }

                }).create();
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        updateDisplay();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        updateDisplay();
    }

    @Override
    public void onDestroy() {
        Utils.setLogText(getApplicationContext(), Utils.logStringCache);
        super.onDestroy();
    }

    // 更新界面显示内容
    private void updateDisplay() {
        Log.d(TAG, "updateDisplay, logText:" + logText + " cache: "
                + Utils.logStringCache);
        if (logText != null) {
            logText.setText(Utils.logStringCache);
        }
        if (scrollView != null) {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}
