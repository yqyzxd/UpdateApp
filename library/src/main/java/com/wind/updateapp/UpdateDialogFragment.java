package com.wind.updateapp;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


/**
 * Created by wind on 16/5/10.
 */
public class UpdateDialogFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final String SP_KEY_IGNORE_UPDATE="sp_key_ignore_update";
    public static final String ARG_KEY_FORCEUPDATE = "arg_key_forceupdate";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     /*   getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));*/
        View contentView=inflater.inflate(R.layout.dialog_update,container,false);
        return contentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = Gravity.CENTER;
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#cc000000")));
        super.onActivityCreated(savedInstanceState);
    }

    private CheckBox cb_ignore;
    TextView tv_update;
    TextView tv_delay;
    TextView tv_version;
    TextView tv_update_content;
    private boolean forceUpdate;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        forceUpdate=getArguments().getBoolean(ARG_KEY_FORCEUPDATE,false);
        initView(view);
        initListener();
    }

    private void initListener() {
        cb_ignore.setOnCheckedChangeListener(this);
        tv_update.setOnClickListener(this);
        tv_delay.setOnClickListener(this);

    }

    private void initView(View view) {
        cb_ignore=(CheckBox)view.findViewById(R.id.cb_ignore);
        if (forceUpdate){
            cb_ignore.setVisibility(View.GONE);
        }else {
            cb_ignore.setVisibility(View.VISIBLE);
        }

        tv_update=(TextView)view.findViewById(R.id.tv_update);
        tv_delay=(TextView)view.findViewById(R.id.tv_delay);
        tv_version=(TextView)view.findViewById(R.id.tv_version);
        tv_update_content=(TextView)view.findViewById(R.id.tv_update_content);

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        callback.ignoreUpdate(isChecked);
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.tv_update){
            dismiss();
            callback.update();
        }else if(view.getId()==R.id.tv_delay){
            dismiss();
        }
    }

    private UpdateCallback callback;
    public void setUpdateCallback(UpdateCallback callback){
        this.callback=callback;
    }
    public interface UpdateCallback{
        void  update();
        void ignoreUpdate(boolean isIgnore);
    }
}
