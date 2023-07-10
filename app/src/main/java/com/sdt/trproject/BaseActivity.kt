package com.sdt.trproject

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlin.math.roundToInt


open class BaseActivity : AppCompatActivity() {


    private lateinit var container: View;
    protected lateinit var sidebar: DrawerLayout;
    private lateinit var navigationViewNonMember: NavigationView

    // 차후 작업
    private lateinit var navigationViewMember: NavigationView

    private var header: View? = null
    private var footer: View? = null
    private var isEmptyTrailing: Boolean = false
    private var trailing: ViewGroup? = null

    protected var titleNum : String = "하이요"


    private val sharedPreferences by lazy {
        getSharedPreferences(SharedPrefKeys.PREF_NAME, Context.MODE_PRIVATE)
    }

    protected val navigationHeader: View
        get() {

            header = header ?: navigationViewNonMember.getHeaderView(0)
            return header!!
        }

    protected val navigationFooter: View
        get() {
            footer = footer ?: findViewById(R.id.footer_non_member, true)
            return footer!!
        }
    protected fun setTitleText(text: String) {
        val title = findViewById<TextView>(R.id.appbarTitle, true)
        title.text = text
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.appbar)
        val appbarMainContainer = findViewById<ConstraintLayout>(R.id.appbarMainContainer, true)



        // toolbarContainer 변수에 toolbar_container 뷰를 할당
        val toolbarContainer = findViewById<FrameLayout>(R.id.toolbar_container, true)
        // container 변수에 activity_main 레이아웃을 inflate하여 생성한 뷰를 할당
        container = layoutInflater.inflate(R.layout.activity_main, null, false)
        // toolbarContainer에 container 뷰를 추가
        toolbarContainer.addView(container)

        // sidebar 변수에 drawer_layout 뷰를 할당
        sidebar = findViewById<DrawerLayout>(R.id.drawer_layout, true)
        // navigationView 변수에 nav_view 뷰를 할당


        navigationViewNonMember = findViewById(R.id.nav_view_non_member, true)


        // toolbar 변수에 toolbar 뷰를 할당
        val toolbar = findViewById<Toolbar>(R.id.toolbar, true)

        // trailing 변수에 toolbar에서 trailing_ll 뷰를 찾아 할당
        trailing = toolbar.findViewById<ViewGroup>(R.id.trailing_ll)
        useEmptyTrailing()


        // toolbar에서 nav_btn 뷰를 찾아 클릭 리스너를 설정.
        // 클릭하면 sidebar의 상태를 변경(열기/닫기)
        toolbar.findViewById<View>(R.id.nav_btn).apply {
            setOnClickListener {
                if (!sidebar.isDrawerOpen(GravityCompat.START)) {
                    sidebar.openDrawer(GravityCompat.START)
                } else {
                    sidebar.closeDrawer(GravityCompat.START)
                }
            }
        }
        setSupportActionBar(toolbar)


    }

    // navigationView의 메뉴에서 idx 인덱스에 해당하는 아이템을 반환하는 메소드
    protected fun getNavigationHeaderMenuItem(idx: Int): MenuItem {
        // 메뉴 아이템 클릭 리스너 : .setOnMenuItemClickListener {}
        return navigationViewNonMember.menu[idx]
    }

    // trailing에 새로운 뷰를 추가하는 메소드. layout을 인플레이트하여 새로운 뷰를 생성하고,
    // 이 뷰의 크기를 설정한 후 trailing에 추가하고, 추가된 뷰를 반환.
    protected fun addTrailing(
        @LayoutRes layout: Int
    ): View? {
        val trailing = this.trailing ?: return null
        if (isEmptyTrailing) {
            trailing.removeAllViews()
            isEmptyTrailing = false
        }

        val v: View = layoutInflater.inflate(layout, null, false)
        setDefaultMenuItemSize(v)
        trailing.addView(v)

        return v
    }

    // isEmptyTrailing 변수를 true로 설정하고 trailing에 새로운 뷰를 추가하는 메소드
    private fun useEmptyTrailing() {
        isEmptyTrailing = true
        val trailing = this.trailing ?: return
        val v: View = View(this)
        setDefaultMenuItemSize(v)
        trailing.addView(v)
    }

    // 주어진 뷰의 크기를 설정하는 메소드
    private fun setDefaultMenuItemSize(v: View?) {
        val displayMetrics = DisplayMetrics()

        // 안드로이드 버전에 따라 디스플레이의 메트릭스를 얻는 방법이 다릅니다.
        // 버전 R 이상이라면 display를 이용하여 메트릭스를 얻고,
        // 그렇지 않다면 windowManager를 이용하여 메트릭스를 얻습니다.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            //defaultDisplay = getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)
            val display = this.display
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = this.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }
        //val dpi = displayMetrics.densityDpi // px = dp * dpi / 160
        val density = displayMetrics.density // px = dp * density

        // v의 크기를 설정. width는 밀도에 따라 계산하고, height는 trailing이 비어있다면 1,
        // 그렇지 않다면 액션바의 크기로 설정.
        v?.layoutParams = ViewGroup.LayoutParams(
            (24 * density).roundToInt(),
            if (isEmptyTrailing) {
                1
            } else {
                val styledAttributes: TypedArray =
                    theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
                val actionBarSize = styledAttributes.getDimension(0, 0f).toInt()
                styledAttributes.recycle()

                actionBarSize
            },
        )
    }
    protected fun resizeBitmap(profilePhotoBtn: ImageView) {
        val targetSize = 250 // 원하는 정사각형 크기

        val imagePath =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/My_photo.jpg" // 가져올 이미지 파일의 경로

        // 경로로부터 비트맵 이미지 생성
        val bitmap = BitmapFactory.decodeFile(imagePath)

        // 이미지 크기 조정
        // TODO: null 인 경우 로직 구성할것.
//        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, false)

        // 원형 이미지로 변환
        // TODO: null인 경우 로직 구성.
//        val circularBitmap = createCircularBitmap(resizedBitmap)

        // ImageView에 비트맵 이미지 설정
        // TODO: Null인 경우 로직 구성
//        profilePhotoBtn.setImageBitmap(circularBitmap)
    }

    private fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val outputBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val canvas = Canvas(outputBitmap).apply {
            drawOval(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
        }

        return outputBitmap
    }


    // findViewById 메소드를 오버라이드. container에서 주어진 id를 가진 뷰를 찾아 반환.
    override fun <T : View?> findViewById(id: Int): T {
        return container.findViewById(id)
    }

    // fromRoot 매개변수에 따라 뷰를 찾는 범위가 다르게 동작하도록 하는 메소드.
    // fromRoot가 true라면 최상위 뷰에서 찾고, false라면 container에서 찾아 반환.
    fun <T : View?> findViewById(id: Int, fromRoot: Boolean = true): T {
        return if (fromRoot) {
            super.findViewById(id)
        } else {
            container.findViewById(id)
        }
    }
}