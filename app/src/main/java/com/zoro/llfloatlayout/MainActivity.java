package com.zoro.llfloatlayout;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.zoro.llfloatlayout.library.FloatLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener{

    static final int[] COLORS = {0xFFD0021B, 0xFFF5A623, 0xFFF8E71C, 0xFF8B572A, 0xFF7ED321, 0xFF50E3C2, 0xFFB8E986};

    Switch switchLines;
    Switch switchMore;
    EditText editLines;
    EditText editInfo;
    Button btnAdd;
    Button btnClean;
    FloatLayout floatLayout;
    List<View> views = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews(){
        switchLines = (Switch) findViewById(R.id.switch_lines);
        switchMore = (Switch) findViewById(R.id.switch_more_view);
        editLines = (EditText) findViewById(R.id.edit_lines);
        editInfo = (EditText) findViewById(R.id.edit_info);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnClean = (Button) findViewById(R.id.btnClean);
        floatLayout = (FloatLayout) findViewById(R.id.float_layout);
        btnAdd.setOnClickListener(this);
        btnClean.setOnClickListener(this);

        final View moreView = LayoutInflater.from(this).inflate(R.layout.view_more, null);
        moreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "click more", Toast.LENGTH_SHORT).show();
            }
        });

        editLines.setText("2");
        switchLines.setChecked(true);
        switchLines.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    try {
                        int lines = Integer.parseInt(editLines.getText().toString());
                        floatLayout.setLines(lines);
                    } catch (Exception e) {
                        floatLayout.setLines(2);
                        editLines.setText("2");
                    }
                } else {
                    floatLayout.setLines(-1);
                }
                updateViews();
            }
        });
        switchMore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    floatLayout.setMoreDetailsView(moreView);
                }else{
                    floatLayout.setMoreDetailsView(null);
                }
                updateViews();
            }
        });
    }

    private void addView(){
        String info = editInfo.getText().toString();
        if(TextUtils.isEmpty(info)){
            editInfo.setError("please input some info!");
            return;
        }
        Random random = new Random();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(COLORS[random.nextInt(COLORS.length)]);
        View view  = LayoutInflater.from(this).inflate(R.layout.item_view, null);
        TextView txt = (TextView) view.findViewById(R.id.txt_item);
        txt.setBackground(gradientDrawable);
        txt.setText(info);
        views.add(view);
        floatLayout.addView(view);
    }

    private void updateViews(){
        floatLayout.removeAllViews();
        for(View view : views){
            floatLayout.addView(view);
        }
    }

    private void cleanViews(){
       floatLayout.removeAllViews();
        views.clear();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnAdd:
                addView();
                break;
            case R.id.btnClean:
                cleanViews();
                break;
        }
    }
}
