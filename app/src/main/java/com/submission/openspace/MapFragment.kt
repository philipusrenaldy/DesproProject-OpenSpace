@file:Suppress("DEPRECATION", "SameParameterValue")

package com.submission.openspace

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.android.synthetic.main.bottomsheet.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var rootView: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var rbPemerintah: RadioButton? = null
    private var rbRealtime: RadioButton? = null
    private var rbPrediksi: RadioButton? = null
    private var circleJaksel: Circle? = null
    private var circleJakbar: Circle? = null
    private var circleJakut: Circle? = null
    private var circleJaktim: Circle? = null
    private var circleDepok: Circle? = null
    private var circleBekasi: Circle? = null
    private var circleCirebon: Circle? = null
    private var circleBogor: Circle? = null
    private var circleCilegon: Circle? = null
    private var circleTangsel: Circle? = null
    private var circleTangerang: Circle? = null
    private var heatmapOverlay: TileOverlay? = null
    private var stateMap: Int = 1
    private var bulan: String = "01"
    private var tangalSekarang: String = "11"
    private var risikoRevisi: View? = null
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var myRef: DatabaseReference = database.reference
    private val dataLaporan: ArrayList<WeightedLatLng> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        rootView = inflater.inflate(R.layout.fragment_map, container, false)
//        inisialisasiLokasi()
//        createLocationRequest()
//        inisialisasiBS()
//        inisialisasiAutoComplete()
//        initializeRb()
//        inisialisasiDatePicker()

        risikoRevisi = rootView.findViewById(R.id.risiko_revisi)
        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun inisialisasiDatePicker() {
        val tvDate: TextView = rootView.findViewById(R.id.tv_hari)
        val tvJam: TextView = rootView.findViewById(R.id.tv_jam)
        val c = Calendar.getInstance()
        val yearr = c.get(Calendar.YEAR)
        bulan = c.get(Calendar.MONTH).toString()
        tangalSekarang = c.get(Calendar.DATE).toString()
        val jam = c.get(Calendar.HOUR_OF_DAY)
        val menit = c.get(Calendar.MINUTE)
        tvJam.text = "$jam:$menit"
        tvDate.text = "$tangalSekarang - $bulan - $yearr"
        tvDate.setOnClickListener {
            val dpd = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                bulan = (monthOfYear + 1).toString()
                tangalSekarang = dayOfMonth.toString()
                tvDate.text = "$tangalSekarang - $bulan - $year"

                if (stateMap == 3) {
                    heatmapOverlay!!.remove()
                    Log.d("testTanggal", "x$tangalSekarang-$bulan")
                    val format = "$tangalSekarang-$bulan"
                    modePrediksi(format)
                }
            }, yearr, bulan.toInt(), tangalSekarang.toInt())
            dpd.show()
        }

        tvJam.setOnClickListener {
            val tpd = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                tvJam.text = "$hourOfDay:$minute"
            }, jam, menit, true)
            tpd.show()
        }
    }

    private fun initializeRb() {
        rbPemerintah = rootView.findViewById(R.id.rb_pemerintah)
        rbRealtime = rootView.findViewById(R.id.rb_realtime)
        rbPrediksi = rootView.findViewById(R.id.rb_prediksi)

        rbPemerintah!!.setOnClickListener {
            rbPemerintah!!.isChecked = true
            rbRealtime!!.isChecked = false
            rbPrediksi!!.isChecked = false

            if (stateMap != 1) {
                heatmapOverlay!!.remove()
                modePemerintah()
                risikoRevisi!!.animate().alpha(1.0f)
                stateMap = 1
            }
        }

        rbRealtime!!.setOnClickListener {
            rbPemerintah!!.isChecked = false
            rbRealtime!!.isChecked = true
            rbPrediksi!!.isChecked = false

            if (stateMap != 2) {
                risikoRevisi!!.animate().alpha(0.0f)
                circleTangerang!!.remove()
                circleTangsel!!.remove()
                circleJakut!!.remove()
                circleJakbar!!.remove()
                circleJaksel!!.remove()
                circleJaktim!!.remove()
                circleBogor!!.remove()
                circleCilegon!!.remove()
                circleCirebon!!.remove()
                circleBekasi!!.remove()
                circleDepok!!.remove()
                if (heatmapOverlay != null) {
                    heatmapOverlay!!.remove()
                }
                modeHeatmap()
                stateMap = 2
            }
        }

        rbPrediksi!!.setOnClickListener {
            rbPemerintah!!.isChecked = false
            rbRealtime!!.isChecked = false
            rbPrediksi!!.isChecked = true

            if (stateMap != 3) {
                risikoRevisi!!.animate().alpha(0.0f)
                val gbkLatLng = LatLng(-6.218335, 106.802216)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(gbkLatLng, 15f))

                circleTangerang!!.remove()
                circleTangsel!!.remove()
                circleJakut!!.remove()
                circleJakbar!!.remove()
                circleJaksel!!.remove()
                circleJaktim!!.remove()
                circleBogor!!.remove()
                circleCilegon!!.remove()
                circleCirebon!!.remove()
                circleBekasi!!.remove()
                circleDepok!!.remove()
                if (heatmapOverlay != null) {
                    heatmapOverlay!!.remove()
                }
                val dummy = "1-11"
                modePrediksi(dummy)
                stateMap = 3
            }
        }
    }

    private fun modePrediksi(dummy: String) {
        val data = generatePrediksiData(dummy)

        val heatMapProvider = HeatmapTileProvider.Builder()
            .weightedData(data) // load our weighted data
            .radius(50) // optional, in pixels, can be anything between 20 and 50
            .maxIntensity(1000.0) // set the maximum intensity
            .build()

        heatmapOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(heatMapProvider))
    }

    private fun generatePrediksiData(dummy: String): ArrayList<WeightedLatLng> {
        val data = ArrayList<WeightedLatLng>()

        val jsonData = getJsonDataFromAsset("datasetPrediksi.json")
        jsonData?.let {
            for (i in 0 until it.length()) {

                val entry = it.getJSONObject(i)
                val tanggalJson: String = entry.getString("tanggal")

                if (dummy == tanggalJson) {
                    val lat = entry.getDouble("latitude")
                    val lon = entry.getDouble("longitude")
                    val density = entry.getDouble("density")
                    val densityBener: Double = (49.8594 * density) + 1747.6

                    if (density != 0.0) {
                        val weightedLatLng = WeightedLatLng(LatLng(lat, lon), densityBener)
                        data.add(weightedLatLng)
                    }
                }
            }
        }
        return data
    }

    private fun modeHeatmap() {
        val data = generateHeatMapData()
        generateHeatMapDataLaporan(dataLaporan)

        val mergedData = data + dataLaporan

        val heatMapProvider = HeatmapTileProvider.Builder()
            .weightedData(mergedData)
            .radius(35)
            .maxIntensity(1000.0)
            .build()

        heatmapOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(heatMapProvider))
    }

    private fun generateHeatMapDataLaporan(dataLaporan: java.util.ArrayList<WeightedLatLng>) {
        myRef.child("koleksiLaporan")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataLaporan.clear()
                    for (laporan in dataSnapshot.children) {
                        val lati: Double = laporan.child("latitude").value.toString().toDouble()
                        val loni: Double = laporan.child("longitude").value.toString().toDouble()
                        val deni: Double = laporan.child("density").value.toString().toDouble()
                        val weightedLatLng = WeightedLatLng(LatLng(lati, loni), deni)
                        dataLaporan.add(weightedLatLng)
                    }
                }
            })
        Log.d("BOS", dataLaporan.size.toString())
    }

    private fun generateHeatMapData(): ArrayList<WeightedLatLng> {
        val data = ArrayList<WeightedLatLng>()

        val jsonData = getJsonDataFromAsset("datasetfathan.json")
        jsonData?.let {
            for (i in 0 until it.length()) {
                val entry = it.getJSONObject(i)
                val lat = entry.getDouble("latitude")
                val lon = entry.getDouble("longitude")
                val density = entry.getDouble("density")

                if (density != 0.0) {
                    val weightedLatLng = WeightedLatLng(LatLng(lat, lon), density)
                    data.add(weightedLatLng)
                }
            }
        }
        return data
    }
    val google_maps_key = AIzaSyBMR9HBBsfWu7BfR6EXMENvOjPYybM_k88
    private fun inisialisasiAutoComplete() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.place_autocomplete) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i("TEMPAT", "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                Log.i("TEMPAT", "An error occurred: $status")
            }
        })
    }

    private fun inisialisasiBS() {
        bottomSheetBehavior = BottomSheetBehavior.from(rootView.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity as Activity)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(
                        activity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                rootView.context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as AppCompatActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun inisialisasiLokasi() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        map = p0

        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
        map.setOnMarkerClickListener(this)
        setUpMap()

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(activity as AppCompatActivity) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
        modePemerintah()
    }

    private fun modePemerintah() {
        val datanya = generateCircleData()

        for (i in 0 until datanya.size) {
            val namaTempat = datanya[i].getString("namaTempat")
            val lat = datanya[i].getDouble("latitude")
            val lon = datanya[i].getDouble("longitude")
            val luas = datanya[i].getDouble("luas") * 2

            val circleOptions = CircleOptions()
                .center(LatLng(lat, lon))
                .radius(luas)
                .fillColor(Color.argb(128, 255, 0, 0))
                .strokeWidth(0.0F)

            when (namaTempat) {
                "jakarta utara" -> circleJakut = map.addCircle(circleOptions)
                "jakarta selatan" -> circleJaksel = map.addCircle(circleOptions)
                "jakarta barat" -> circleJakbar = map.addCircle(circleOptions)
                "jakarta timur" -> circleJaktim = map.addCircle(circleOptions)
                "depok" -> circleDepok = map.addCircle(circleOptions)
                "bekasi" -> circleBekasi = map.addCircle(circleOptions)
                "cirebon" -> circleCirebon = map.addCircle(circleOptions)
                "bogor" -> circleBogor = map.addCircle(circleOptions)
                "cilegon" -> circleCilegon = map.addCircle(circleOptions)
                "tangerang" -> circleTangerang = map.addCircle(circleOptions)
                "tangsel" -> circleTangsel = map.addCircle(circleOptions)
            }
        }
    }

    private fun generateCircleData(): ArrayList<JSONObject> {
        val data = ArrayList<JSONObject>()

        val jsonData = getJsonDataFromAsset("datasetPetaSebaran.json")
        jsonData?.let {
            for (i in 0 until it.length()) {
                val entry = it.getJSONObject(i)
                data.add(entry)
            }
        }
        return data
    }

    private fun getJsonDataFromAsset(fileName: String): JSONArray? {
        return try {
            val jsonString =
                requireContext().assets.open(fileName).bufferedReader().use { it.readText() }
            JSONArray(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                rootView.context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as AppCompatActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(activity as AppCompatActivity) { location ->
            lastLocation = location
            val currentLatLng = LatLng(location.latitude, location.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CHECK_SETTINGS) {
//            if (resultCode == Activity.RESULT_OK) {
//                locationUpdateState = true
//                startLocationUpdates()
//            }
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (!locationUpdateState) {
//            startLocationUpdates()
//        }
//    }

    companion object {
        private const val ARG_POSITION: String = "position"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        fun newInstance(): MapFragment {
            val fragment = MapFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, 1)
            fragment.arguments = args
            return fragment
        }
    }
}