package sweetbeanjelly.project.testweather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import sweetbeanjelly.project.testweather.network.ApiEndpoint
import com.cooltechworks.views.shimmer.ShimmerRecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_next_day.view.*
import org.json.JSONException
import org.json.JSONObject
import sweetbeanjelly.project.testweather.adapter.NextDayAdapter
import sweetbeanjelly.project.testweather.model.ModelNextDay
import java.text.SimpleDateFormat
import java.util.*

class FragmentNextDays : BottomSheetDialogFragment(), LocationListener {
    var lat: Double? = null
    var lng: Double? = null
    var nextDayAdapter: NextDayAdapter? = null
    var weather_list: ShimmerRecyclerView? = null
    var fabClose: FloatingActionButton? = null
    var modelNextDays: MutableList<ModelNextDay> = ArrayList()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view?.parent as View).setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_next_day, container, false)

        nextDayAdapter = NextDayAdapter(activity!!, modelNextDays)
        weather_list = rootView.rvListWeather
        weather_list?.layoutManager = LinearLayoutManager(activity)
        weather_list?.setHasFixedSize(true)
        weather_list?.adapter = nextDayAdapter
        weather_list?.showShimmerAdapter()

        fabClose = rootView.findViewById(R.id.fabClose)
        fabClose?.setOnClickListener {
            dismiss()
        }

        getLatLong()

        return rootView
    }

    @SuppressLint("MissingPermission")
    private fun getLatLong () {
        val locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        val location = locationManager.getLastKnownLocation(provider)
        if (location != null) {
            onLocationChanged(location)
        } else {
            locationManager.requestLocationUpdates(provider, 20000, 0f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude
        Handler().postDelayed({ getListWeather() }, 3000)
    }

    private fun getListWeather() {
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.Daily + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppidDaily)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val jsonArray = response.getJSONArray("daily")
                        for (i in 0 until jsonArray.length()) {
                            val dataApi = ModelNextDay()
                            val objectList = jsonArray.getJSONObject(i)
                            val jsonObjectOne = objectList.getJSONObject("temp")
                            val jsonArrayOne = objectList.getJSONArray("weather")
                            val jsonObjectTwo = jsonArrayOne.getJSONObject(0)
                            val longDate = objectList.optLong("dt")
                            val formatDate = SimpleDateFormat("dd일")
                            val readableDate = formatDate.format(Date(longDate * 1000))
                            val longDay = objectList.optLong("dt")
                            val format = SimpleDateFormat("EEEE")
                            val readableDay = format.format(Date(longDay * 1000))

                            dataApi.nameDate = "$readableDate $readableDay"
                            dataApi.descWeather = jsonObjectTwo.getString("description")
                            dataApi.tempMin = jsonObjectOne.getDouble("min")
                            dataApi.tempMax = jsonObjectOne.getDouble("max")
                            modelNextDays.add(dataApi)
                        }
                        nextDayAdapter?.notifyDataSetChanged()
                        weather_list?.hideShimmerAdapter()
                    } catch (e: JSONException) { e.printStackTrace() }
                }

                override fun onError(e: ANError) {
                    Toast.makeText(activity, "인터넷에 연결할 수 없습니다!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        fun newInstance(string: String?): FragmentNextDays {
            val fragmentNextDays = FragmentNextDays()
            val args = Bundle()
            args.putString("string", string)
            fragmentNextDays.arguments = args
            return fragmentNextDays
        }
    }
}