package com.example.weather.ui

import android.annotation.SuppressLint
import android.app.DownloadManager.Request
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.ClientError
import com.android.volley.NetworkResponse
import com.android.volley.ServerError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.R
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.ui.utils.TOKEN
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //noLimit
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        //initViews
        binding.apply {
            btnSearch.setOnClickListener {
                val location = etLocation.text.toString().trim()
                if(location.isNotEmpty())
                {
                    //hide error message
                    showError(false)
                    //request to server
                    requestToServer(location)
                }
                else showError(true,"enter a location")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun requestToServer(location: String)
    {
        //volly
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$location&units=metric&appid=$TOKEN"
        val stringRequest = StringRequest(com.android.volley.Request.Method.GET,url,{response->
            //Handler API response
            try
            {

                val jsonResponse = JSONObject(response)
                val mainObject : JSONObject = jsonResponse.getJSONObject("main")
                val temperature = mainObject.getDouble("temp")
                val humidity = mainObject.getInt("humidity")
                val windObject : JSONObject = jsonResponse.getJSONObject("wind")
                val windSpeed = windObject.getDouble("speed")
                val weatherArray : JSONArray = jsonResponse.getJSONArray("weather")
                var description : String?=null
                var main:String?=null
                if (weatherArray.length() > 0)
                {
                    val weatherObject = weatherArray.getJSONObject(0)
                    description = weatherObject.getString("description")
                    main = weatherObject.getString("main")
                }

                binding.layoutWeather.apply{
                        root.visibility = View.VISIBLE
                    //setData
                    locationtitle.text = location
                    val roundedValue = ceil(temperature).toInt().toString()
                    ivTemp.text = roundedValue

                    tvWindSpeed.text = "$windSpeed m/s"
                    tvHumidityValue.text = "$humidity%"
                    tvDescription.text = description.toString()

                    when(main)
                    {
                        "Clear"->{
                            Status.setImageResource(R.drawable.clear)
                        }
                        "Rain"->{
                            Status.setImageResource(R.drawable.rain)
                        }
                        "Snow"->{
                            Status.setImageResource(R.drawable.snow)
                        }
                        "Clouds"->{
                            Status.setImageResource(R.drawable.cloud)
                        }
                    }

                    //click Listener
                    ivBack.setOnClickListener {
                        root.visibility = View.GONE
                    }

                }

            }
            catch (e:JSONException)
            {
                Toast.makeText(this@MainActivity,e.message.toString() ,Toast.LENGTH_SHORT).show()
            }
        },{
            if (it is ClientError || it is ServerError)
            {
                val response : NetworkResponse = it.networkResponse
                val statusCode = response.statusCode
                if (statusCode == 404) showError(true,"Location NotFound")
                else Toast.makeText(this,"error in connection to api",Toast.LENGTH_SHORT).show()
            }
        })
        queue.add(stringRequest)
    }

    private fun showError(haveError:Boolean,message:String?=null)
    {
        binding.tvError.apply {
            if (haveError)
            {
                text = message
                visibility = View.VISIBLE
            }
            else
            {
                visibility = View.GONE
            }
        }
    }
}