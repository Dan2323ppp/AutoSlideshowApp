package jp.techacademy.keisuke.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する

        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        var mTimer: Timer? = null
        // タイマー用の時間のための変数
        var mTimerSec = 0.0
        var mHandler = Handler()

//最初の画像表示、もし画像が一枚もなかったら　ボタン押せなくするとか
        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView.setImageURI(imageUri)
        } else {
            start_button.isClickable = false
            next_button.isClickable = false
            back_button.isClickable = false
        }
        start_button.setOnClickListener {
//            フラグの使い方わからん
            val text = start_button.text.toString()
            val start = "再生"

            if (text == start) {
//                再生状態、停止を表示中、スライドショー再生
                start_button.text = "停止"
                next_button.isClickable = false
                back_button.isClickable = false

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 0.1
                        mHandler.post {
//                            画像を2秒ごとにスライド処理
                            if (mTimerSec.toInt() == 2) {
                                mTimerSec = 0.0

                                if (cursor!!.moveToNext()) {
                                    val fieldIndex =
                                        cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                    val id = cursor.getLong(fieldIndex)
                                    val imageUri =
                                        ContentUris.withAppendedId(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            id
                                        )
                                    imageView.setImageURI(imageUri)
                                } else {
                                    cursor!!.moveToFirst()
                                    val fieldIndex =
                                        cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                    val id = cursor.getLong(fieldIndex)
                                    val imageUri =
                                        ContentUris.withAppendedId(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            id
                                        )
                                    imageView.setImageURI(imageUri)
                                }
                            }
                        }
                    }
                }, 0, 100)//最初に始動させるまで、ループ間隔
//                停止状態、再生を表示中、スライドショーストップ
            } else {
                mTimer!!.cancel()
                mTimerSec = 0.0
                start_button.text = "再生"
                next_button.isClickable = true
                back_button.isClickable = true
//                start_button.background = getDrawable(R.color.colorPrimary)
            }
        }
        next_button.setOnClickListener {
            if (cursor!!.moveToNext()) {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)
            } else {
                cursor!!.moveToFirst()
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)
            }
        }
        back_button.setOnClickListener {
//            Backちゃうんかーい
//            停止後
            if (cursor!!.moveToPrevious()) {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)
            } else {
                cursor!!.moveToLast()
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)
            }
        }

//        cursor.close()
    }
}
