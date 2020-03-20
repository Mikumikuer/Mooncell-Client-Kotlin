package wiki.fgo.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var isChecked: Boolean = false

    private var cssLayer: String =
        "javascript:var style = document.createElement(\"style\");style.type = \"text/css\";style.innerHTML=\".minerva-footer{display:none;}\";style.id=\"addStyle\";document.getElementsByTagName(\"HEAD\").item(0).appendChild(style);"

    private var searchBaseUrl: String = "https://fgo.wiki/index.php?search="

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            true
        }

        R.id.action_login -> {
            webView.loadUrl("https://fgo.wiki/w/特殊:用户登录")
            true
        }

        R.id.action_about -> {
            webView.loadUrl("https://fgo.wiki/w/Mooncell:关于")
            true
        }

        R.id.action_share -> {
            val clipboard =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("text", webView.url)
            clipboard.setPrimaryClip(clip)

            Snackbar.make(webView, "已复制到剪切板", Snackbar.LENGTH_SHORT).show()
            true
        }

        R.id.action_favorite -> {
            if (!isChecked) {
                my_toolbar.menu.findItem(R.id.action_favorite).icon =
                    ContextCompat.getDrawable(this, R.drawable.ic_action_favorite)
                isChecked = true
                Toast.makeText(applicationContext, "收藏成功", Toast.LENGTH_SHORT).show()
            } else {
                my_toolbar.menu.findItem(R.id.action_favorite).icon =
                    ContextCompat.getDrawable(this, R.drawable.ic_action_favorite_empty)
                isChecked = false
                Toast.makeText(applicationContext, "取消收藏", Toast.LENGTH_SHORT).show()
            }
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.subitem_04 -> Toast.makeText(applicationContext, "123", Toast.LENGTH_SHORT).show()
            R.id.subitem_05 -> Toast.makeText(applicationContext, "456", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(applicationContext, "gone", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        nav_view.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            my_toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        m_search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
//                Toast.makeText(applicationContext, query.toString(), Toast.LENGTH_SHORT).show()
                val searchUrl = searchBaseUrl + query.toString()
                webView.loadUrl(searchUrl)
                // Clear the text in search bar but (don't trigger a new search!)
                m_search_view.setQuery("", false)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        supportActionBar?.setDisplayShowTitleEnabled(false)
        loadWebView()
    }

    private fun loadWebView() {
        val url = "https://fgo.wiki/index.php?title=首页&mobileaction=toggle_view_mobile"
        // Set web view client
        setWebView()
        webView.loadUrl(url)
        WebView.setWebContentsDebuggingEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                swipeLayout.setProgressViewEndTarget(false, 250)
                swipeLayout.isRefreshing = true
                super.onPageStarted(view, url, favicon)
                webView.loadUrl(cssLayer)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webView.loadUrl(cssLayer)
                swipeLayout.isRefreshing = false
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (Uri.parse(url).host == "fgo.wiki") {
                    // This is my web site, so do not override; let my WebView load the page
                    return false
                }
                // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    startActivity(this)
                }
                return true
            }
        }

        swipeLayout.setOnRefreshListener {
            webView.loadUrl(cssLayer)
            webView.reload()
        }
    }

    private fun setWebView() {
        // Get the web view settings instance
        val settings = webView.settings
        //5.0以上开启混合模式加载
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.javaScriptEnabled = true
        // Enable and setup web view cache
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCachePath(cacheDir.path)
        settings.setSupportZoom(false)
        // Enable zooming in web view
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        // Enable disable images in web view
        settings.blockNetworkImage = false
        // Whether the WebView should load image resources
        settings.loadsImagesAutomatically = true
        //设置UA
        settings.userAgentString =
            settings.userAgentString + " mooncellApp/" + BuildConfig.VERSION_NAME

        // More web view settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true
        }
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        // More optional settings, you can enable it by yourself
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.loadWithOverviewMode = true
        settings.setGeolocationEnabled(true)
        settings.allowFileAccess = true
        //webview setting
        webView.fitsSystemWindows = true
    }
}
