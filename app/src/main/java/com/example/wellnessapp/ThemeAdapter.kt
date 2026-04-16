package com.example.wellnessapp

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ThemeAdapter(
    private val themes: List<ThemeModel>,
    private val onThemeSelected: (ThemeModel) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

    class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvThemeName)
        val desc: TextView = view.findViewById(R.id.tvThemeDesc)
        val btnApply: Button = view.findViewById(R.id.btnApplyTheme)
        val previewContainer: RelativeLayout = view.findViewById(R.id.themeBackground)
        val morphicCircle: CardView = view.findViewById(R.id.morphicCircle)
        val innerGlass: View = view.findViewById(R.id.innerGlass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_card, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]
        holder.name.text = theme.name
        holder.desc.text = theme.description
        
        val accentColor = theme.accentColor
        val glassAlpha = Color.argb(40, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        val glassBorder = Color.argb(120, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        
        // Main Card Glass Background
        val shape = GradientDrawable()
        shape.setColor(theme.backgroundColor)
        shape.cornerRadius = 32f * holder.itemView.context.resources.displayMetrics.density
        shape.setStroke(4, glassBorder)
        holder.previewContainer.background = shape

        // Morphic Ornament Styling
        holder.morphicCircle.setCardBackgroundColor(glassAlpha)
        val innerShape = GradientDrawable()
        innerShape.setShape(GradientDrawable.OVAL)
        innerShape.setColor(glassAlpha)
        innerShape.setStroke(6, glassBorder)
        holder.innerGlass.background = innerShape

        holder.name.setTextColor(Color.WHITE)
        holder.desc.setTextColor(Color.argb(180, 255, 255, 255))
        
        // Button Styling
        holder.btnApply.setBackgroundColor(accentColor)
        holder.btnApply.setTextColor(if (isColorDark(accentColor)) Color.WHITE else Color.BLACK)

        holder.btnApply.setOnClickListener { onThemeSelected(theme) }
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    override fun getItemCount(): Int = themes.size
}
