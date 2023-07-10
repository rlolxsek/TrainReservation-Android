package com.sdt.trproject.ksh.enquiry;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sdt.trproject.R;

public class EnquiryActivity extends AppCompatActivity {
    private TextView appbarTitle;
    private ImageView clearBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enquiry);
        appbarTitle = findViewById(R.id.appbarTitle);
        appbarTitle.setText("1:1 문의 하기");

        clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public <T extends Fragment> void navigate(T fragment){
        navigate(fragment, true);
    }
    public <T extends Fragment> void navigate(T fragment, boolean addToBackStack){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.enquiry_fragment, fragment,null);
        if(addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
    public void navigateToBack(){
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
    }

}