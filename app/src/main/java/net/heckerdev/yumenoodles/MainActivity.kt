package net.heckerdev.yumenoodles

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var errorPageContainer: View
    private lateinit var errorTitle: TextView
    private lateinit var errorMessage: TextView
    private lateinit var errorDetails: TextView
    private lateinit var retryButton: Button
    private var lastFailedUrl: String? = null
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply Material You dynamic colors
        DynamicColors.applyToActivityIfAvailable(this)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize error page views
        errorPageContainer = findViewById(R.id.errorPageContainer)
        errorTitle = findViewById(R.id.errorTitle)
        errorMessage = findViewById(R.id.errorMessage)
        errorDetails = findViewById(R.id.errorDetails)
        retryButton = findViewById(R.id.retryButton)
        
        retryButton.setOnClickListener {
            lastFailedUrl?.let { url ->
                hideErrorPage()
                webView.loadUrl(url)
            }
        }
        
        // Initialize WebView
        webView = findViewById(R.id.webView)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                hideErrorPage()
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                
                // Only show error page for main frame errors
                if (request?.isForMainFrame == true) {
                    lastFailedUrl = request.url.toString()
                    showErrorPage(error)
                }
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Page loaded successfully
            }
        }
        
        webView.webChromeClient = WebChromeClient()
        
        // Enable JavaScript
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.setSupportZoom(true)
        
        // Load URL - Change this to your desired website
        webView.loadUrl("https://portfolio.heckerdev.net/")
    }
    
    private fun showErrorPage(error: WebResourceError?) {
        errorPageContainer.visibility = View.VISIBLE
        webView.visibility = View.GONE
        
        val errorCode = error?.errorCode ?: -1
        val description = error?.description?.toString() ?: "Unknown error"
        
        when (errorCode) {
            WebViewClient.ERROR_HOST_LOOKUP -> {
                errorTitle.text = "No Internet Connection"
                errorMessage.text = "Unable to connect to the server"
                errorDetails.text = "Please check your internet connection and try again"
            }
            WebViewClient.ERROR_CONNECT -> {
                errorTitle.text = "Connection Failed"
                errorMessage.text = "Unable to establish connection"
                errorDetails.text = "The server might be down or unreachable"
            }
            WebViewClient.ERROR_TIMEOUT -> {
                errorTitle.text = "Connection Timeout"
                errorMessage.text = "The server took too long to respond"
                errorDetails.text = "Please try again later"
            }
            WebViewClient.ERROR_FILE_NOT_FOUND -> {
                errorTitle.text = "Page Not Found"
                errorMessage.text = "The requested page doesn't exist"
                errorDetails.text = "Error 404"
            }
            else -> {
                errorTitle.text = "Error Loading Page"
                errorMessage.text = description
                errorDetails.text = "Error code: $errorCode"
            }
        }
    }
    
    private fun hideErrorPage() {
        errorPageContainer.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}