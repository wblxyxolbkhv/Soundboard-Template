package ventum.zephyr.soundboardtemplate.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import jp.wasabeef.glide.transformations.BlurTransformation
import ventum.zephyr.soundboardtemplate.BuildConfig
import ventum.zephyr.soundboardtemplate.R
import ventum.zephyr.soundboardtemplate.adapter.SoundboardPagerAdapter
import ventum.zephyr.soundboardtemplate.listener.SoundItemActionListener
import ventum.zephyr.soundboardtemplate.model.SoundItem
import ventum.zephyr.soundboardtemplate.model.SoundboardCategory
import java.util.*

const val STORAGE_NAME = "ventum.zephyr.soundboardtemplate.SHARED_PREFS"
const val MULTI_STREAM = "STORAGE_NAME" + ".MULTI_STREAM"

abstract class SoundboardActivity : AppCompatActivity(), SoundItemActionListener {

    protected lateinit var binding: ventum.zephyr.soundboardtemplate.databinding.ActivitySoundboardBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var soundboardCategories: ArrayList<SoundboardCategory>
    private lateinit var interstitialAd: InterstitialAd
    protected lateinit var soundPool: SoundPool
    private var clicksAdCounter = 0
    @Suppress("LeakingThis")
    private var clicksToShowAd = getClickToAdsCount()
    private var isMultiStreamsEnable: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_soundboard)
        sharedPreferences = getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)
        initSoundPool()
        soundboardCategories = getSoundboardCategories()
        setupAds()
        setupToolbar()
        setupBackgroundImage()
        setupViewPager()
    }

    private fun initSoundPool() {
        soundPool = SoundPool.Builder().setAudioAttributes(
                AudioAttributes.Builder()
                        .setUsage(getSoundPoolUsage())
                        .setContentType(getSoundPoolContentType())
                        .build())
                .setMaxStreams(10)
                .build()
    }

    protected open fun getSoundPoolUsage() = AudioAttributes.USAGE_GAME

    protected open fun getSoundPoolContentType() = AudioAttributes.CONTENT_TYPE_SONIFICATION

    protected open fun getBlurRadius() = 22

    protected open fun getClickToAdsCount() = Random().nextInt(6) + 10

    private fun setupAds() {
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        binding.adView.loadAd(AdRequest.Builder().build())
        interstitialAd = InterstitialAd(this).apply {
            adUnitId = getString(R.string.admob_interstitial_unit_id)
            adListener = object : AdListener() {
                override fun onAdClosed() {
                    clicksAdCounter = 0
                    clicksToShowAd = getClickToAdsCount()
                    interstitialAd.loadAd(AdRequest.Builder().build())
                }
            }
        }.also { it.loadAd(AdRequest.Builder().build()) }
    }

    abstract fun getSoundboardCategories(): ArrayList<SoundboardCategory>

    private fun onAdShowTrigger() = interstitialAd.let { if (it.isLoaded && ++clicksAdCounter == clicksToShowAd) it.show() }

    override fun onSoundItemClicked(item: SoundItem) {
        if (!BuildConfig.DEBUG) onAdShowTrigger()
        if (!isMultiStreamsEnable) soundPool.autoPause()
        soundPool.play(item.soundId, 1f, 1f, 1, 0, 0f)
    }

    private fun setupBackgroundImage() = Glide.with(this).load(R.drawable.bg_main)
            .apply(bitmapTransform(BlurTransformation(getBlurRadius())))
            .into(binding.bgImageView)

    private fun setupViewPager() {
        binding.viewPager.adapter = SoundboardPagerAdapter(supportFragmentManager, soundboardCategories)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar.let {
            it?.title = getString(R.string.app_name)
            binding.toolbar.setTitleTextColor(getToolbarItemsColor())
        }
    }

    private fun getToolbarItemsColor() = ContextCompat.getColor(this, R.color.toolbar_items_color)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        (0 until menu.size()).forEach {
            menu.getItem(it).icon.setColorFilter(getToolbarItemsColor(), PorterDuff.Mode.SRC_ATOP)
            if (menu.getItem(it).itemId == R.id.action_repeat) {
                isMultiStreamsEnable = sharedPreferences.getBoolean(MULTI_STREAM, true)
                if (!isMultiStreamsEnable) {
                    menu.getItem(it).setIcon(R.drawable.ic_repeat_one_black_24dp)
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var handled = false
        when (item.itemId) {
            R.id.action_share -> {
                showShare()
                handled = true
            }
            R.id.action_rate -> {
                showRateUs()
                handled = true
            }
            R.id.action_repeat -> {
                isMultiStreamsEnable = !isMultiStreamsEnable
                sharedPreferences.edit().putBoolean(MULTI_STREAM, isMultiStreamsEnable).apply()
                item.setIcon(if (isMultiStreamsEnable) R.drawable.ic_repeat_black_24dp else R.drawable.ic_repeat_one_black_24dp)
            }
        }
        return handled || super.onOptionsItemSelected(item)
    }

    private fun showShare() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)))
    }

    private fun showRateUs() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.rate_us_market))))
    }
}
