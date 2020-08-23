package com.example.robin.currencydetector

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.robin.currencydetector.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var sheetBehavior: BottomSheetBehavior<*>
    private lateinit var classifier: ImageClassifier
    var processing: Boolean = false
    lateinit var mp: MediaPlayer
    val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()
        classifier = ImageClassifier(assets)

        binding.btnRetry.setOnClickListener {
            binding.codeData.text = " "
            if (binding.cameraView.visibility == View.VISIBLE) {
                showPreview()
            } else {
                hidePreview()
            }
        }

        val lparam = binding.bottomLayout.layoutParams as CoordinatorLayout.LayoutParams
        lparam.behavior = BottomSheetBehavior<View>()
        binding.bottomLayout.layoutParams = lparam


        sheetBehavior = BottomSheetBehavior.from(binding.bottomLayout)
        sheetBehavior.peekHeight = 224

        val lp = binding.fabProgressCircle.layoutParams as CoordinatorLayout.LayoutParams
        binding.fabProgressCircle.layoutParams = lp

        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.fabTakePhoto.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start()
            }
        })

        binding.fabTakePhoto.setOnClickListener {
            checkPermission()
            binding.cameraView.captureImage { cameraKitView, bytes ->
                if (!processing) {
                    this.runOnUiThread {
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        getCurrencyDetails(bitmap)
                        showPreview()
                        imagePreview.setImageBitmap(bitmap)
                    }
                }
            }
//            binding.cameraView.captureImage { cameraKitImage ->
//                this.runOnUiThread {
//                    getCurrencyDetails(cameraKitImage.bitmap)
//                    showPreview()
//                    imagePreview.setImageBitmap(cameraKitImage.bitmap)
//                }
//            }
        }

    }

    private fun showPreview() {
        binding.framePreview.visibility = View.VISIBLE
        binding.cameraView.visibility = View.GONE
    }

    private fun hidePreview() {
        binding.framePreview.visibility = View.GONE
        binding.cameraView.visibility = View.VISIBLE
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
            binding.cameraView
        }
    }

    @SuppressLint("CheckResult")
    private fun getCurrencyDetails(bitmap: Bitmap) {
        processing = true
        val photobitmap = Bitmap.createScaledBitmap(bitmap, Keys.INPUT_SIZE, Keys.INPUT_SIZE, false)
        classifier.recognizeImage(photobitmap).subscribeBy(
            onSuccess = {
                if (it[0].title.equals("100_new_back") || it[0].title.equals("100_new_front") || it[0].title.equals("100_old_back") || it[0].title.equals(
                        "100_old_back"
                    )
                ) {
                    binding.codeData.text = getString(R.string.hundred)
                    mp = MediaPlayer.create(this, R.raw.a100)
                    mp.start()
                } else if (it[0].title.equals("10_new_back") || it[0].title.equals("10_new_front") || it[0].title.equals(
                        "10_old_back"
                    ) || it[0].title.equals("10_old_back")
                ) {
                    binding.codeData.text = getString(R.string.ten)
                    mp = MediaPlayer.create(this, R.raw.a10)
                    mp.start()
                } else if (it[0].title.equals("20_new_back") || it[0].title.equals("20_new_front") || it[0].title.equals(
                        "20_old_back"
                    ) || it[0].title.equals("20_old_back")
                ) {
                    binding.codeData.text = getString(R.string.twenty)
                    mp = MediaPlayer.create(this, R.raw.a20)
                    mp.start()
                } else if (it[0].title.equals("50_new_back") || it[0].title.equals("50_new_front") || it[0].title.equals(
                        "50_old_back"
                    ) || it[0].title.equals("50_old_back")
                ) {
                    binding.codeData.text = getString(R.string.fifty)
                    mp = MediaPlayer.create(this, R.raw.a50)
                    mp.start()
                } else if (it[0].title.equals("200_back") || it[0].title.equals("200_front")) {
                    binding.codeData.text = getString(R.string.two_hundred)
                    mp = MediaPlayer.create(this, R.raw.a200)
                    mp.start()
                } else if (it[0].title.equals("500_new_back") || it[0].title.equals("500_new_front")) {
                    binding.codeData.text = getString(R.string.five_hundred)
                    mp = MediaPlayer.create(this, R.raw.a500)
                    mp.start()
                } else if (it[0].title.equals("2000_front") || it[0].title.equals("2000_back")) {
                    binding.codeData.text = getString(R.string.two_thousand)
                    mp = MediaPlayer.create(this, R.raw.a2000)
                    mp.start()
                } else{
                    binding.codeData.text = getString(R.string.try_again)
                }
                processing = false
                binding.fabProgressCircle.hide()
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        )
    }

    override fun onStart() {
        super.onStart()
        binding.cameraView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.cameraView.onResume()
    }

    override fun onPause() {
        binding.cameraView.onStop()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }

}
