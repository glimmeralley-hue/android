package com.example.wellnessapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ThemeAdapter(
    private val themes: List<ThemeModel>,
    private val onThemeSelected: (ThemeModel) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

    class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val background: RelativeLayout = view.findViewById(R.id.themeBackground)
        val name: TextView = view.findViewById(R.id.tvThemeName)
        val desc: TextView = view.findViewById(R.id.tvThemeDesc)
        val btnApply: Button = view.findViewById(R.id.btnApplyTheme)
        val preview: ImageView = view.findViewById(R.id.ivThemePreview)
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
        holder.background.setBackgroundColor(theme.backgroundColor)
        
        val textColor = if (theme.isDark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        holder.name.setTextColor(textColor)
        holder.desc.setTextColor(if (theme.isDark) 0x80FFFFFF.toInt() else 0x80000000.toInt())
        
        // Show a simple preview icon based on theme
        holder.preview.setColorFilter(theme.accentColor)

        holder.btnApply.setOnClickListener { onThemeSelected(theme) }
    }

    override fun getItemCount(): Int = themes.size
}
