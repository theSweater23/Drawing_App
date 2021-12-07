package com.thesweater.drawingapp

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    companion object {
        const val WRITE_EXTERNAL_STORAGE_PERMISSION = 1
    }

    lateinit var drawingView: DrawingView
    lateinit var brushSizeBtn: ImageButton
    lateinit var brushColorBtn: ImageButton
    lateinit var undoBtn: ImageButton
    lateinit var saveBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        drawingView.setBrushSize(10.toFloat())

        brushSizeBtn = findViewById(R.id.brushSizeBtn)
        brushSizeBtn.setOnClickListener{
            brushSizeDialog()
        }

        brushColorBtn = findViewById(R.id.brush_color)
        brushColorBtn.setOnClickListener{
            brushColorDialog()
        }

        undoBtn = findViewById(R.id.undo)
        undoBtn.setOnClickListener{
            drawingView.Undo()
        }

        saveBtn = findViewById(R.id.save)
        saveBtn.setOnClickListener{
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                BitmapAsyncTask(getBitmapFromView(drawingView)).execute()
            }
            else {
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun getBitmapFromView(view: View) : Bitmap {
        var bitmap = Bitmap.createBitmap(view.height, view.width, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        view.draw(canvas)

        return bitmap
    }

    private inner class BitmapAsyncTask(var bm: Bitmap) : AsyncTask<Any, Void, String>() {
        override fun doInBackground(vararg params: Any?): String {

            var result = "empty"

            if (bm != null){
                try {
                    var bytes = ByteArrayOutputStream()
                    bm.compress(Bitmap.CompressFormat.PNG, 100, bytes)

                    var file = File(externalCacheDir!!.absolutePath.toString()
                            + File.separator + "KidsDrawingApp_" + System.currentTimeMillis()/1000 + ".png")

                    var fos = FileOutputStream(file)
                    fos.write(bytes.toByteArray())
                    fos.close()

                    result = file.absolutePath
                } catch (e: Exception) {

                }
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result!!.isNotEmpty()) {
                Toast.makeText(this@MainActivity, "File saved : $result", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun brushSizeDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.brush_size_slider)
        dialog.setTitle("Choose Size")

        val size_slider: Slider = dialog.findViewById(R.id.slider)

        size_slider.addOnChangeListener { slider, value, fromUser ->
            drawingView.setBrushSize(value)
        }

        dialog.show()
    }

    private fun brushColorDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.color_selector)
        dialog.setTitle("Choose Color")

        val color_img: ImageView = dialog.findViewById(R.id.color_img)

        val red_slider: Slider = dialog.findViewById(R.id.slider_red)
        val green_slider: Slider = dialog.findViewById(R.id.slider_green)
        val blue_slider: Slider = dialog.findViewById(R.id.slider_blue)

        var r: Int = 0
        var g: Int = 0
        var b: Int = 0

        red_slider.addOnChangeListener { slider, value, fromUser ->
            r = value.toInt()
            var clr = Color.rgb(r,g,b)
            color_img.setBackgroundColor(clr)
        }

        green_slider.addOnChangeListener { slider, value, fromUser ->
            g = value.toInt()
            var clr = Color.rgb(r,g,b)
            color_img.setBackgroundColor(clr)
        }

        blue_slider.addOnChangeListener { slider, value, fromUser ->
            b = value.toInt()
            var clr = Color.rgb(r,g,b)
            color_img.setBackgroundColor(clr)
        }

        dialog.setOnDismissListener {
            brushColorBtn.setBackgroundColor(Color.rgb(r,g,b))
            drawingView.setBrushColor(r,g,b)
        }
        dialog.show()
    }
}