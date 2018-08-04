package ventum.zephyr.sounboardtemplate

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.RippleDrawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import ventum.zephyr.sounboardtemplate.databinding.ItemSoundBinding
import ventum.zephyr.sounboardtemplate.model.SoundItem

class SoundsAdapter(private val soundItems: ArrayList<SoundItem>, val listener: SoundItemActionListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemSoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SoundViewHolder(binding)
    }

    override fun getItemCount(): Int = soundItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SoundViewHolder).bind(soundItems[position])
    }

    private inner class SoundViewHolder(var binding: ItemSoundBinding) : RecyclerView.ViewHolder(binding.root) {

        internal fun bind(itemSound: SoundItem) {
            setupImage(binding.imageRoundedImageView, itemSound.image)
            binding.imageRoundedImageView.setOnClickListener { listener.onSoundItemClicked(adapterPosition) }
        }

        private fun setupImage(view: ImageView, @DrawableRes drawable: Int) {
            val context: Context = view.context
            view.setColorFilter(ContextCompat.getColor(context, R.color.item_image_mask_color), PorterDuff.Mode.SRC_OVER)
            view.setImageDrawable(getRippleDrawable(context, drawable))
        }

        private fun getRippleColorStateList(color: Int): ColorStateList = ColorStateList(arrayOf(intArrayOf()), intArrayOf(color))

        private fun getRippleDrawable(context: Context, @DrawableRes drawable: Int): RippleDrawable = RippleDrawable(
                getRippleColorStateList(ContextCompat.getColor(context, R.color.ripple_effect_color)),
                ContextCompat.getDrawable(context, drawable), null)
    }
}
