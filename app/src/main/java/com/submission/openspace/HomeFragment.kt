package com.submission.openspace

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.home_fragment.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var rootView: View

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.home_fragment, container, false)
        setHasOptionsMenu(true)
        getGlobalData()
        return rootView
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun getGlobalData() {
        val url: String = "https://corona.lmao.ninja/v3/covid-19/countries/indonesia"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
                val jsonObject = JSONObject(it.toString())

                val time = jsonObject.getLong("updated")
                val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
                val res = Date(time)
                formatter.format(res)
                println("Current Date and Time is: $res")

                tanggal_real.text = res.toString()
                txtInfected.text = jsonObject.getString("cases")
                txtRecoverd.text = jsonObject.getString("recovered")
                txtDeceased.text = jsonObject.getString("deaths")
            },
            {
                Toast.makeText(
                    context,
                    "Request Failed!",
                    Toast.LENGTH_SHORT
                ).show()
                tanggal_real.text = "-"
                txtInfected.text = "-"
                txtRecoverd.text = "-"
                txtDeceased.text = "-"
            }
        )
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(stringRequest)
    }

    companion object {
        var TAG = HomeFragment::class.java.simpleName
        private const val ARG_POSITION: String = "position"

        //        const val REQUEST_VIDEO_CAPTURE = 1
        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, 1)
            fragment.arguments = args
            return fragment
        }
    }
}