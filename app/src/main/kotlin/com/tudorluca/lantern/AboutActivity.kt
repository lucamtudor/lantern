package com.tudorluca.lantern

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import kotlin.properties.Delegates
import android.widget.ImageView
import android.widget.Button
import com.tudorluca.lantern.utils.*
import android.util.Log
import com.tudorluca.lantern.utils.IabHelper.OnIabSetupFinishedListener
import com.tudorluca.lantern.utils.IabHelper.QueryInventoryFinishedListener
import android.view.ViewGroup
import java.util.Currency
import android.content.Intent
import com.tudorluca.lantern.utils.IabHelper.OnIabPurchaseFinishedListener
import android.widget.Toast
import android.widget.TextView
import android.net.Uri
import com.tudorluca.lantern.widget.DonationSeekBar

open class AboutActivity() : ActionBarActivity() {

    private val TAG = "AboutActivity"

    private val mDonationContainer: ViewGroup by Delegates.lazy { findView<ViewGroup>(R.id.donation_container) }
    private val mHappyFace: ImageView by Delegates.lazy { findView<ImageView>(R.id.donation_happy_face) }
    private val mFaceDrawables = array(R.drawable.smiley_01_yellow, R.drawable.smiley_02_yellow, R.drawable.smiley_03_yellow, R.drawable.smiley_04_yellow)
    private val mDonationSeekBar: DonationSeekBar by Delegates.lazy { findView<DonationSeekBar>(R.id.donation_seekbar) }
    private val mDonationButton: Button by Delegates.lazy { findView<Button>(R.id.donation_button) }
    private val mVersionLabel: TextView by Delegates.lazy { findView<TextView>(R.id.version_label) }
    private val mProjectKotlin: ImageView by Delegates.lazy { findView<ImageView>(R.id.project_kotlin) }

    private var mHelper: IabHelper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        mHelper = IabHelper(this, LanternApplication.key)

        mHelper?.startSetup(object : OnIabSetupFinishedListener {
            override fun onIabSetupFinished(result: IabResult?) {
                if (result?.isFailure() ?: true) {
                    complain("Problem setting up in-app billing: " + result?.getMessage());
                    return
                }

                Log.d(TAG, "Setup successful.");

                // IAB is fully set up. Now, let's get details about donations.
                mHelper?.queryInventoryAsync(true, SKUS, mAvailableDonationsListener)
            }
        })

        mDonationContainer.hideChildren()
        mDonationSeekBar.onDonationChanged = {(seekBar, index) ->
            mHappyFace.setImageResource(mFaceDrawables[index])
        }

        mDonationButton.setOnClickListener {
            val selectedDonationSku = SKUS[mDonationSeekBar.selectedLegendIndex]
            mHelper?.launchPurchaseFlow(this@AboutActivity, selectedDonationSku, 1001, mDonationFinishedListener)
        }

        mVersionLabel.setText("${getString(R.string.version)}  ${BuildConfig.VERSION_NAME}")
        mProjectKotlin.setOnClickListener {
            val browseIntent = Intent(Intent.ACTION_VIEW)
            browseIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            browseIntent.setData(Uri.parse("http://kotlinlang.org/"))
            startActivity(browseIntent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val handled = mHelper?.handleActivityResult(requestCode, resultCode, data) ?: false
        if (!handled) {
            super<ActionBarActivity>.onActivityResult(requestCode, resultCode, data)
        }
    }

    val mAvailableDonationsListener = object : QueryInventoryFinishedListener {
        override fun onQueryInventoryFinished(result: IabResult?, inv: Inventory?) {
            if (result?.isFailure() ?: true) {
                complain("Problem query inventory: " + result?.getMessage());
                return
            }
            val smallDonation = inv?.getSkuDetails(Donation.SMALL.sku)!!
            val mediumDonation = inv?.getSkuDetails(Donation.MEDIUM.sku)!!
            val generousDonation = inv?.getSkuDetails(Donation.GENEROUS.sku)!!
            val largeDonation = inv?.getSkuDetails(Donation.LARGE.sku)!!

            val donationPrices = array(smallDonation, mediumDonation, generousDonation, largeDonation).map {
                val priceRounded = Math.round(it.getPriceAmountMicros()!! / 1000000f)
                val currencySymbol = Currency.getInstance(it.getPriceCurrencyCode())?.getSymbol() ?: ""
                "$currencySymbol $priceRounded"
            }.toList()
            mDonationSeekBar.legendValues = donationPrices

            mDonationContainer.showAllChildren()
        }
    }

    val mDonationFinishedListener = object : OnIabPurchaseFinishedListener {
        override fun onIabPurchaseFinished(result: IabResult?, purchase: Purchase?) {
            if (result?.isFailure() ?: true) {
                complain(message = result?.getMessage() ?: "")
            }

            Log.d(TAG, "Donation successful.");

            if (purchase != null) {
                val thankYou = when (purchase.getSku()) {
                    Donation.SMALL.sku -> getString(R.string.thank_you_great)
                    Donation.MEDIUM.sku -> getString(R.string.thank_you_awesome)
                    Donation.LARGE.sku -> getString(R.string.thank_you_ultimate)
                    else -> getString(R.string.thank_you)
                }

                Toast.makeText(this@AboutActivity, thankYou, Toast.LENGTH_LONG).show()

                mHelper?.consumeAsync(purchase) {(purchase, result) ->
                    Log.d(TAG, "Donation item consumed: ${purchase ?: "unknown"}");
                }
            }
        }
    }

    override fun onDestroy() {
        super<ActionBarActivity>.onDestroy()
        mHelper?.dispose()
        mHelper = null
    }

    fun complain(message: String) {
        Log.e(TAG, "**** Lantern donation Error: " + message);
    }
}
