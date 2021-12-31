package com.dust.exweather.utils.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.dust.exweather.MyApplication

class CTextView : AppCompatTextView {
    constructor(context: Context) : super(context) {
        setUpTypeFace()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setUpTypeFace()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        setUpTypeFace()
    }

    private fun setUpTypeFace(){
        typeface = (context.applicationContext as MyApplication).getTypeFace()
    }
}