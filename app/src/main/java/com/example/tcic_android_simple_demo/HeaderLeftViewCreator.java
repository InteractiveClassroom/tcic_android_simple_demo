package com.example.tcic_android_simple_demo;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.qcloudclass.tcic.NativeViewCreator;


/**
 * Header Left View 的具体实现
 * 负责创建包含文本和 info 图标的 Header Left View
 */
public class HeaderLeftViewCreator implements NativeViewCreator {

    @Override
    public View createView(Context context, int id, Object args) {
        // 水平布局：文本 + info 图标
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setBackgroundColor(Color.TRANSPARENT);

        // 创建文本
        TextView textView = new TextView(context);
        String text = "Header Left 原生View";
        textView.setText(text);
        textView.setTextSize(12); // 12px
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(textParams);
        layout.addView(textView);

        // 创建 info 图标
        ImageView infoIcon = new ImageView(context);
        infoIcon.setImageResource(android.R.drawable.ic_menu_info_details); // 使用系统 info 图标
        infoIcon.setColorFilter(Color.WHITE); // 设置图标颜色为白色

        // 在 createView 里
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp2px(context, 32), dp2px(context, 32)); // 32dp x 32dp
        iconParams.setMargins(8, 0, 0, 0); // 左边距 8px
        infoIcon.setLayoutParams(iconParams);
        layout.addView(infoIcon);

        return layout;
    }
    // dp 转 px 工具
    private int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void disposeView(View view) {
        // 可以在这里处理 View 的清理工作
        // 比如取消监听器、释放资源等
    }
}