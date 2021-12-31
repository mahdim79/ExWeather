package com.dust.exweather.utils.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatCheckBox
import com.dust.exweather.MyApplication

class CCheckBox: AppCompatCheckBox {
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