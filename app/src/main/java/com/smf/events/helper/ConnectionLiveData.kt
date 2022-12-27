package com.smf.events.helper

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData

/**
 * Save all available networks with an internet connection to a set (@validNetworks).
 * As long as the size of the set > 0, this LiveData emits true.
 * MinSdk = 21.
 * */
class ConnectionLiveData(context: Context) : LiveData<Boolean>() {

    val TAG = "C-Manager"
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    private fun checkValidNetworks() {
        Log.d(TAG, "checkValidNetworks: ${validNetworks.size}")
        postValue(validNetworks.size > 0)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onInactive() {
        validNetworks.clear()
        cm.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback() = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        /*
          Called when a network is detected. If that network has internet, save it in the Set.
          */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onAvailable(network: Network) {
            Log.d(TAG, "onAvailable: ${network}")
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            Log.d(TAG, "onAvailable: ${network}, $hasInternetCapability")
            if (hasInternetCapability == true) {
                validNetworks.add(network)
            }
            if (validNetworks.size <= 1) {
                Log.d(TAG, "connect onAvailable inside if: ${validNetworks.size}")
                checkValidNetworks()
            }
        }

        /*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
         */
        override fun onLost(network: Network) {
            Log.d(TAG, "onLost: ${network}")
            validNetworks.remove(network)
            if (validNetworks.size == 0) {
                checkValidNetworks()
            }
        }

    }

}