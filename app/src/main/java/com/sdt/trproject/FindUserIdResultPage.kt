package com.sdt.trproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.ImageView


class FindUserIdResultPage : AppCompatActivity() {

    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView

    companion object {
        const val INPUT_USER_ID = "ID"
    }

    private lateinit var foundUserId: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_find_user_id_result_page)

        foundUserId = findViewById<TextView>(R.id.foundUserId)
        val userId = "회원번호 : ${intent.getStringExtra(INPUT_USER_ID)}"
        foundUserId.setText(userId)

        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("회원번호 찾기")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }


        Log.d("GetStringExtra", "${intent.getStringExtra(INPUT_USER_ID)}")
//

        foundUserId.setOnClickListener(){
            val userIdArray = userId.split(" ")[2]
            copyTextOnClick(this, userIdArray)
            finish()
        }
        // 버튼을 누르면 split 된 회원번호가 저장된다.
    }



    private fun copyTextOnClick(context: Context, text : String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
    }


}