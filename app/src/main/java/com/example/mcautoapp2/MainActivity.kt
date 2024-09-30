package com.example.mcautoapp2

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.core.app.ActivityCompat

const val TAG = "[MCCAR]:"

class MainActivity : AppCompatActivity() {
    private lateinit var textCarSpeed:TextView
    private lateinit var textGear:TextView
    //val permissions = arrayOf("android.car.permission.CAR_SPEED")
    //val permissionsGear = arrayOf("android.car.permission.CAR_POWERTRAIN")

    private val readCarSpeed = arrayOf("android.car.permission.CAR_SPEED")
    private val readCarGear = arrayOf("android.car.permission.CAR_POWERTRAIN")
    /*private val permissions= arrayOf(
        readCarSpeed,readCarGear
    )*/
    private val permissions = readCarSpeed + readCarGear // İzinleri tek bir diziye birleştiriyoruz
    private val REQUEST_CODE = 1001 // İzin istek kodu

    /** CAR API **/
    private lateinit var myCar:Car
    private lateinit var myCarPropertyManager:CarPropertyManager
    private var myCarPropertyListener = object : CarPropertyManager.CarPropertyEventCallback{
        override fun onChangeEvent(value: CarPropertyValue<Any>){
            Log.d(TAG, "Received on changed car property event")
            if(value.propertyId == VehiclePropertyIds.CURRENT_GEAR){
                textGear.text = "Change"
                Log.d(TAG, "GEAR: Received on changed")
            }
            if(value.propertyId == VehiclePropertyIds.PERF_VEHICLE_SPEED){
                val speed = value.value as Float
                textCarSpeed.text= Math.round(speed).toString()
                Log.d(TAG, "Speed: Received on changed")
            }
        }
        override fun onErrorEvent(p0: Int, p1: Int) {
            //super.onErrorEvent(propId, areaId, errorCode)
            Log.w(TAG, "Received on changed car property event ERROR!")

        }
    }
    /** **/
    //===========================================================================================//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d(TAG, "MC Auto App2 v5.0 is starting...")

        textCarSpeed = findViewById(R.id.textCarSpeed)
        textGear = findViewById(R.id.textGear)

        //initMyCar()

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        Log.d(TAG, "Permissions are requested...")

        /*
        // İzinlerin verilmiş olup olmadığını kontrol et
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsNeeded.isNotEmpty()) {
            // İzinler verilmemiş, kullanıcılardan izin iste
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_CODE)
            Log.d(TAG, "Permissions are requested...")
        } else {
            // Tüm izinler verilmiş, gerekli işlemleri burada yapabilirsiniz
            // örneğin, araç hızını ve vitesini okumak
            Log.d(TAG, "Permissions are already granted...")

        }
        */
    }
    //===========================================================================================//
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
                Log.d(TAG, "İzinler verildi")
                initMyCar()
            } else {
                // İzin reddedildi
                Log.e(TAG, "İzin reddedildi")
                //ActivityCompat.requestPermissions(this, permissions, 200)
            }
        }
    }
    //===========================================================================================//
    private fun initMyCar(){
        //Create the Car Object
        myCar = Car.createCar(this)
        // Araç bağlantısı kuruldu, property manager ile işlem yapabilirsin
        //Create Property Manager
        myCarPropertyManager = myCar.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
        //Subscribe the Gear Change Event
        myCarPropertyManager.registerCallback(
            myCarPropertyListener,
            VehiclePropertyIds.CURRENT_GEAR,
            CarPropertyManager.SENSOR_RATE_ONCHANGE
        )
        //Subscribe the Car Speed Change Event
        myCarPropertyManager.registerCallback(
            myCarPropertyListener,
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            CarPropertyManager.SENSOR_RATE_ONCHANGE
        )
        Log.d(TAG, "myCar: isConnected " + myCar.isConnected())
    }
    //===========================================================================================//
    override fun onStop() {
        super.onStop()
        myCar.disconnect()
    }
}