package com.mahesaiqbal.samplepdf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.mahesaiqbal.samplepdf.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    private lateinit var pdfFile: File
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Downloading file
        downloadPDFFromURL()
        onClickListener()
    }
    
    private fun downloadPDFFromURL() {
        PRDownloader.initialize(applicationContext)
    
        binding.pbLoading.visibility = View.VISIBLE
        val fileName = "SamplePDF_example_file.pdf"
        
        Log.d("MainAct", "FileUtils.getPdfUrl: ${FileUtils.getPdfUrl()}")
        Log.d("MainAct", "FileUtils.getRootDirPath: ${FileUtils.getRootDirPath(this)}")
        Log.d("MainAct", "FileUtils filename: $fileName")
        
        downloadPdfFromInternet(
            FileUtils.getPdfUrl(),
            FileUtils.getRootDirPath(this),
            fileName
        )
    }
    
    private fun downloadPdfFromInternet(url: String, dirPath: String, fileName: String) {
        PRDownloader.download(url, dirPath, fileName).build().start(
            object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_LONG).show()
                    val downloadedFile = File(dirPath, fileName)
                    binding.pbLoading.visibility = View.GONE
                    showPdfFromFile(downloadedFile)
                }
    
                override fun onError(error: Error?) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error in downloading file : $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
    
    private fun showPdfFromFile(file: File) {
        pdfFile = file
        
        Log.d("MainAct", "File showPdfFromFile absolutePath: ${pdfFile.absolutePath}")
        Log.d("MainAct", "File showPdfFromFile name: ${pdfFile.name}")
        
        binding.pdfView.fromFile(pdfFile)
            .password(null)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onPageError { page, _ ->
                Toast.makeText(this@MainActivity, "Error at page: $page", Toast.LENGTH_LONG).show()
            }
            .load()
    }
    
    private fun onClickListener() {
        binding.fabShare.setOnClickListener {
            val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.$localClassName.provider", pdfFile)
    
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sharing PDF from Sample PDF")
                putExtra(Intent.EXTRA_TEXT, "Sharing file PDF from SamplePDF to others")
            }
            
            val createChooser = Intent.createChooser(shareIntent, "Share PDF SimplePDF App")
            val resInfoList = packageManager.queryIntentActivities(createChooser, PackageManager.MATCH_DEFAULT_ONLY)
            resInfoList.forEach { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(createChooser)
        }
    }
    
    private fun createSampleDir() {
        val folderName = "SamplePDF Downloads"
        val file = File(Environment.getExternalStorageDirectory(), folderName)
    
        if (!file.exists()) {
            file.mkdir()
            Toast.makeText(this, "Successfully created file on: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            Log.d("FileUtils", "Successfully created file on: ${file.absolutePath}")
        } else {
            Toast.makeText(this, "Folder already exist on: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            Log.d("FileUtils", "Folder already exist on: ${file.absolutePath}")
        }
    }
}