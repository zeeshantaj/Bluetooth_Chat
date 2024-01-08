package com.example.bluetooth_chat_java_app.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bluetooth_chat_java_app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Chat_Fragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_layout,container,false);
    }

    private EditText messageEd;
    private FloatingActionButton sendBtn;
    private ImageView cameraImg;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messageEd = view.findViewById(R.id.chatEd);
        sendBtn = view.findViewById(R.id.sendBtn);
        cameraImg = view.findViewById(R.id.cameraImg);

        setIconVisibility();

        messageEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setIconVisibility();
            }
        });
    }
    private void setIconVisibility(){
        if (!messageEd.getText().toString().isEmpty()){
            sendBtn.setImageResource(R.drawable.send_icon);
            cameraImg.setVisibility(View.GONE);
        }
        else {
            sendBtn.setImageResource(R.drawable.mic_icon);
            cameraImg.setVisibility(View.VISIBLE);
        }

    }
}

