package com.sidm.easyscan.presentation.ui

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sidm.easyscan.R

class DetailsActivity : AppCompatActivity() {

    private lateinit var doc: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val b : Bundle? = intent.extras
        doc = b?.get("doc").toString()

    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        parent?.findViewById<TextView>(R.id.tv_test)?.text = doc
        return super.onCreateView(parent, name, context, attrs)
    }

}