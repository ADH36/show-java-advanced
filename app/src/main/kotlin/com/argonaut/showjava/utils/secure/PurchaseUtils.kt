

package com.argonaut.showjava.utils.secure

import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.argonaut.showjava.R
import com.argonaut.showjava.activities.BaseActivity
import io.michaelrocks.paranoid.Obfuscate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.solovyev.android.checkout.*
import timber.log.Timber

@Obfuscate
class PurchaseUtils(
    private val activityContext: BaseActivity,
    val secureUtils: SecureUtils,
    val isLoading: (Boolean) -> Unit = {}
) {

    private val disposables: CompositeDisposable = CompositeDisposable()
    private var completeCallback: () -> Unit = {}

    lateinit var checkout: ActivityCheckout
    private lateinit var inventory: Inventory
    private var lessVerbose: Boolean = false

    fun doOnComplete(completeCallback: () -> Unit) {
        this.completeCallback = completeCallback
    }

    fun initializeCheckout(
        withPurchaseFlow: Boolean = false,
        lessVerbose: Boolean = false
    ): ActivityCheckout {
        this.lessVerbose = lessVerbose
        checkout = Checkout.forActivity(activityContext, secureUtils.getBilling())
        checkout.start()

        if (withPurchaseFlow) {
            checkout.createPurchaseFlow(PurchaseListener())
        }

        inventory = checkout.makeInventory()
        inventory.load(
            Inventory.Request.create()
                .loadAllPurchases()
                .loadSkus(ProductTypes.IN_APP, secureUtils.iapProductId),
            InventoryCallback()
        )

        return checkout
    }

    inner class PurchaseListener : EmptyRequestListener<Purchase>() {
        override fun onSuccess(purchase: Purchase) {
            if (purchase.sku == secureUtils.iapProductId && purchase.state == Purchase.State.PURCHASED) {
                onPurchaseComplete(purchase)
            }
        }

        override fun onError(response: Int, e: Exception) {
            val messageKey = when (response) {
                ResponseCodes.USER_CANCELED -> R.string.errorUserCancelled
                ResponseCodes.ITEM_ALREADY_OWNED -> R.string.errorAlreadyOwned
                ResponseCodes.ITEM_UNAVAILABLE -> R.string.errorItemUnavailable
                ResponseCodes.ACCOUNT_ERROR -> R.string.errorAccount
                ResponseCodes.ERROR -> R.string.errorPayment
                else -> R.string.errorRequest
            }
            isLoading(false)
            Toast.makeText(
                activityContext,
                activityContext.getString(messageKey),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    inner class InventoryCallback : Inventory.Callback {
        override fun onLoaded(products: Inventory.Products) {
            var wasAnyPurchased = false
            products.forEach {
                val isPurchased = it.isPurchased(secureUtils.iapProductId)
                if (isPurchased) {
                    it.getPurchaseInState(
                        secureUtils.iapProductId,
                        Purchase.State.PURCHASED
                    )?.let { purchase ->
                        onPurchaseComplete(purchase)
                        wasAnyPurchased = true
                    }
                }
            }
            if (!wasAnyPurchased) {
                secureUtils.onPurchaseRevert()
            }
        }
    }

    private fun onPurchaseComplete(purchase: Purchase) {
        Timber.d("Purchase complete: %s", purchase.sku)
        isLoading(true)
        disposables.add(
            secureUtils.makeJsonRequest(
                secureUtils.purchaseVerifierPath, mapOf(
                    "packageName" to activityContext.packageName,
                    "productId" to purchase.sku,
                    "token" to purchase.token
                )
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        isLoading(false)
                        Timber.d("Verification done: %s", it.toString())
                        if (secureUtils.isPurchaseValid(purchase, it)) {
                            if (!secureUtils.hasPurchasedPro()) {
                                activityContext.firebaseAnalytics.logEvent(
                                    FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, null
                                )
                                Toast.makeText(
                                    activityContext,
                                    R.string.purchaseSuccess,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                            secureUtils.onPurchaseComplete(purchase)
                            completeCallback()
                        } else {
                            if (!lessVerbose) {
                                Toast.makeText(
                                    activityContext,
                                    R.string.purchaseVerificationFailed,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            secureUtils.onPurchaseRevert()
                        }
                    }, {
                        isLoading(false)
                        Timber.e(it)
                        if (!lessVerbose) {
                            Toast.makeText(
                                activityContext,
                                R.string.purchaseVerificationFailed,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
        )
        secureUtils.onPurchaseComplete(purchase)
    }

    fun onDestroy() {
        disposables.clear()
        if (this::checkout.isInitialized) {
            checkout.stop()
        }
    }

}