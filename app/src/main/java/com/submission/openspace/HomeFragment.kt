package com.submission.openspace

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.ganti_nama.view.*
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var rootView: View
    private var buttonLapor: Button? = null

    private var mAuth: FirebaseAuth? = null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    private lateinit var tvNama: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.home_fragment, container, false)
        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()
        getGlobalData()
        inisialisasiButtonLapor()
        inisialisasiProfile()
        inisialisasiLokasi()
        inisialisasiTanggal()
        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun inisialisasiTanggal() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR).toString()
        val month = c.get(Calendar.MONTH).toString()
        val day = c.get(Calendar.DATE).toString()
        val dateInString = "$day/$month/$year"

        val tanggal: TextView = rootView.findViewById(R.id.tanggal)
        tanggal.text = "Informasi per $dateInString"
    }

    private fun inisialisasiLokasi() {
        if (ActivityCompat.checkSelfPermission(
                rootView.context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as AppCompatActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MapFragment.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener(activity as AppCompatActivity) { location ->
            if (location != null) {
                lastLocation = location

                val addresses: List<Address>
                val geocoder = Geocoder(activity, Locale.getDefault())

                addresses = geocoder.getFromLocation(
                    lastLocation.latitude,
                    lastLocation.longitude,
                    1
                ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                val city: String = addresses[0].locality
                val state: String = addresses[0].adminArea

                val alamat = "$city, $state"

                val tvLokasi = rootView.findViewById<TextView>(R.id.tv_lokasi)
                tvLokasi.text = alamat
            }
        }
    }

    private fun inisialisasiProfile() {
        val currentUser = mAuth!!.currentUser
        tvNama = rootView.textView
        tvNama.setOnClickListener {
            alertDialog()
        }

        myRef.child("Users").child(currentUser!!.uid).child("nama")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val namaPengguna = snapshot.getValue(String::class.java)
                    tvNama.text = namaPengguna
                }
            })
    }

    private fun alertDialog() {
        val currentUser = mAuth!!.currentUser
        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.ganti_nama, null)
        val mBuilder = AlertDialog.Builder((activity as AppCompatActivity))
            .setView(mDialogView)
            .setTitle("Nama anda")
        val mAlertDialog = mBuilder.show()
        mDialogView.okay.setOnClickListener {
            mAlertDialog.dismiss()
            val namaPengguna = mDialogView.dialog_nama.text.toString()
            myRef.child("Users").child(currentUser!!.uid).child("nama").setValue(namaPengguna)
            Toast.makeText(activity, "Nama anda adalah $namaPengguna", Toast.LENGTH_SHORT)
                .show()
        }
        mDialogView.cancel.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun inisialisasiButtonLapor() {
        buttonLapor = rootView.findViewById(R.id.btnLapor)

        buttonLapor!!.setOnClickListener {
            personDetectionIntent()
        }
    }

    private fun personDetectionIntent() {
        try {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle("Laporkan")
            builder.setMessage("Tolong ambil gambar dari kerumunan orang")
            builder.setPositiveButton("Ok") { _, _ ->
                val intent = Intent(context, DetectorActivity::class.java)
                startActivity(intent)
            }
            builder.setNegativeButton("Cancel") { _, _ -> }
            builder.show()

        } catch (e: ActivityNotFoundException) {
        }
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