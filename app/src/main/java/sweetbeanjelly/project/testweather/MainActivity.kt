package sweetbeanjelly.project.testweather

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import sweetbeanjelly.project.testweather.model.ModelMain
import sweetbeanjelly.project.testweather.network.ApiEndpoint
import android.text.format.DateFormat
import sweetbeanjelly.project.testweather.adapter.MainAdapter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), LocationListener {
    private var lat: Double? = null
    private var lng: Double? = null
    private var strDate: String? = null
    private var mProgressBar: ProgressDialog? = null
    private var mainAdapter: MainAdapter? = null
    private val modelMain: MutableList<ModelMain> = ArrayList()
    var permissionArrays = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    lateinit var txt_date: TextView
    lateinit var txt_weather: TextView
    lateinit var txt_time: TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        val MyVersion = Build.VERSION.SDK_INT
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission1() && checkIfAlreadyhavePermission2()) { } else { requestPermissions(permissionArrays, 101) }
        }

        txt_date = findViewById(R.id.txt_date)
        txt_weather = findViewById(R.id.txt_weather)
        txt_time = findViewById(R.id.txt_time)

        val dateNow = Calendar.getInstance().time
        strDate = DateFormat.format("EEE", dateNow) as String

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Mohon Tunggu")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Sedang menampilkan data...")

        val fragmentNextDays = FragmentNextDays.newInstance("FragmentNextDays")
        mainAdapter = MainAdapter(modelMain)

        val rvListWeather = findViewById<RecyclerView>(R.id.rvListWeather)
        rvListWeather.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        rvListWeather.setHasFixedSize(true)
        rvListWeather.setAdapter(mainAdapter)

        val fabNextDays = findViewById<ExtendedFloatingActionButton>(R.id.fabNextDays)
        fabNextDays.setOnClickListener {
            fragmentNextDays.show(supportFragmentManager, fragmentNextDays.tag)
        }

        getToday()
        getLocation()
    }

    override fun onResume() {
        super.onResume()
        val systemTime = System.currentTimeMillis()
        val format = SimpleDateFormat("a HH시 mm분", Locale.KOREA).format(systemTime)
        txt_time.text = format
    }

    private fun getToday() {
        val date = Calendar.getInstance().time
        val tanggal = DateFormat.format("yyyy년 MM월 dd일", date) as String
        val formatDate = "$tanggal ${strDate}요일"
        txt_date.text = formatDate
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 115)
            return
        }
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
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

        getCurrentWeather()
        getListWeather()
    }

    private fun getCurrentWeather() {
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.CurrentWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            val jsonArrayOne = response.getJSONArray("weather")
                            val jsonObjectOne = jsonArrayOne.getJSONObject(0)
                            val jsonObjectTwo = response.getJSONObject("main")
                            val strWeather = jsonObjectOne.getString("main")
                            val strDescWeather = jsonObjectOne.getString("description")
                            val city = response.getString("name")
                            val temp = jsonObjectTwo.getDouble("temp")

                            when (strDescWeather) {
                                "broken clouds", "overcast clouds", "scattered clouds", "few clouds" -> {
                                    txt_weather.text = "흐림"
                                }
                                "light rain" -> {
                                    txt_weather.text = "약한 비"
                                }
                                "haze" -> {
                                    txt_weather.text = "안개"
                                }
                                "moderate rain" -> {
                                    txt_weather.text = "흐리고 비"
                                }
                                "heavy intensity rain" -> {
                                    txt_weather.text = "폭우"
                                }
                                "clear sky" -> {
                                    txt_weather.text = "맑음"
                                }
                                else -> {
                                    txt_weather.text = strWeather
                                }
                            }

                            if(city == "Gijang") txt_city.text = "부산시 기장"
                            txt_temp.text = String.format(Locale.getDefault(), "%.0f°C", temp)

                        } catch (e: JSONException) { e.printStackTrace() }
                    }

                    override fun onError(e: ANError) { e.printStackTrace() }
                })
    }

    private fun getListWeather() {
        mProgressBar?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.ListWeather + "lat=" + lat + "&lon=" + lng + ApiEndpoint.UnitsAppid)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            mProgressBar?.dismiss()
                            val jsonArray = response.getJSONArray("list")
                            for (i in 0..6) {
                                val dataApi = ModelMain()
                                val objectList = jsonArray.getJSONObject(i)
                                val jsonObjectOne = objectList.getJSONObject("main")
                                val jsonArrayOne = objectList.getJSONArray("weather")
                                val jsonObjectTwo = jsonArrayOne.getJSONObject(0)
                                var timeNow = objectList.getString("dt_txt")
                                val formatDefault = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                val formatTimeCustom = SimpleDateFormat("kk:mm")

                                try {
                                    val timesFormat = formatDefault.parse(timeNow)
                                    timeNow = formatTimeCustom.format(timesFormat)
                                } catch (e: ParseException) {
                                    e.printStackTrace()
                                }

                                dataApi.timeNow = timeNow
                                dataApi.currentTemp = jsonObjectOne.getDouble("temp")
                                dataApi.descWeather = jsonObjectTwo.getString("description")
                                dataApi.tempMin = jsonObjectOne.getDouble("temp_min")
                                dataApi.tempMax = jsonObjectOne.getDouble("temp_max")
                                modelMain.add(dataApi)
                            }
                            mainAdapter?.notifyDataSetChanged()
                        } catch (e: JSONException) { e.printStackTrace() }
                    }

                    override fun onError(e: ANError) {
                        mProgressBar?.dismiss()
                        Toast.makeText(this@MainActivity, "인터넷에 연결할 수 없습니다!", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun checkIfAlreadyhavePermission1(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyhavePermission2(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            } else {
                getLocation()
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }
}