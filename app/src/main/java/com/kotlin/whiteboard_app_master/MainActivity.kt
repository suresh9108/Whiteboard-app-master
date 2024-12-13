package com.kotlin.whiteboard_app_master

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.kotlin.whiteboard_app_master.databinding.ActivityMainBinding
import yuku.ambilwarna.AmbilWarnaDialog

class MainActivity : AppCompatActivity() {

    private var selectedColor = Color.WHITE
    private var editText: EditText? = null

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.drawIb.setOnClickListener {
            binding.drawingView.isSelecting = false
            openColorPicker(Color.RED) { color ->
                binding.drawingView.setBrushColor(color)
            }
        }

        binding.selectionIb.setOnClickListener {
            binding.drawingView.isSelecting = true
        }

        binding.drawingView.onTextSelected = { x, y ->
            addEditText(x, y)
        }

        binding.undoIb.setOnClickListener {
            binding.drawingView.isSelecting = false
            binding.drawingView.undo()
        }

        binding.redoIb.setOnClickListener {
            binding.drawingView.isSelecting = false
            binding.drawingView.redo()
        }

        binding.eraserIb.setOnClickListener {
            binding.drawingView.isSelecting = false
            binding.drawingView.erase(selectedColor)
        }

        binding.clearAllIb.setOnClickListener {
            binding.drawingView.isSelecting = false
            binding.drawingView.clearDrawingBoard()
        }

        binding.fillColorIb.setOnClickListener {
            openColorPicker(selectedColor) { color ->
                selectedColor = color
                binding.drawingView.setBackgroundColor(color)
            }
        }
    }

    private fun openColorPicker(initialColor: Int, onColorSelected: (Int) -> Unit) {
        val colorPicker = AmbilWarnaDialog(this, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                // Handle cancel action if needed
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                onColorSelected(color)
            }
        })
        colorPicker.show()
    }

    private fun addEditText(x: Float, y: Float) {
        if (editText == null) {
            Log.e("TAG", "addEditText: Creating new EditText")

            // Create and configure the EditText
            editText = EditText(this).apply {
                id = View.generateViewId()  // Ensure a unique ID
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.TRANSPARENT)
                textSize = 20f
                setSingleLine()
            }

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = R.id.drawing_container
                topToTop = R.id.drawing_container
            }

            // Add the view to the container and adjust position
            findViewById<ConstraintLayout>(R.id.drawing_container).apply {
                addView(editText, layoutParams)
            }

            // Adjust position with translation
            editText?.apply {
                translationX = x
                translationY = y
                requestFocus()
            }
        }
    }
}
