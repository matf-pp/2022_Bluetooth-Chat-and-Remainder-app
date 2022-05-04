
package com.example.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat


class DeviceListActivity : AppCompatActivity() {
    private lateinit var listPairedDevices: ListView
    private lateinit var listAvailableDevices: ListView

    private lateinit var adapterPairedDevices: ArrayAdapter<String>
    private lateinit var adapterAvailableDevices: ArrayAdapter<String>
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var context: Context
    private lateinit var progressScanDevices: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        context = this

        init()
    }

    private val deviceListener = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            var action: String? = p1?.action
            if (BluetoothDevice.ACTION_FOUND == action){
                var device: BluetoothDevice? = p1?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    if (ActivityCompat.checkSelfPermission(
                            p0!!,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }
                    if (device.bondState != BluetoothDevice.BOND_BONDED && adapterAvailableDevices.getPosition(device.name + "\n" + device.address) == -1) {
                        adapterAvailableDevices.add(device.name + "\n" + device.address)
                    }

                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action){
                progressScanDevices.visibility = View.GONE
                if (adapterAvailableDevices.count == 0){
                    Toast.makeText(p0, "No new device found", Toast.LENGTH_SHORT).show()
                }else {
                    //Toast.makeText(p0, "Click on the device to start the chat", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun init(){
        listPairedDevices = findViewById(R.id.List_paired_devices)
        listAvailableDevices = findViewById(R.id.List_available_devices)

        adapterPairedDevices = ArrayAdapter<String>(context, R.layout.device_list_item)
        adapterAvailableDevices = ArrayAdapter<String>(context, R.layout.device_list_item)

        progressScanDevices = findViewById(R.id.progress_scan_devices)

        listPairedDevices.adapter = adapterPairedDevices
        listAvailableDevices.adapter = adapterAvailableDevices

        listPairedDevices.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            var info: String = (view as TextView).text.toString()
            var adress: String = info.substring(info.length - 17)
            var intent: Intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("Address", adress)
            setResult(RESULT_OK, intent)
            startActivityForResult(intent, 102)
            finish()

        }

        listAvailableDevices.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            var info: String = (view as TextView).text.toString()
            var adress: String = info.substring(info.length - 17)
            var intent: Intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("Address", adress)
            setResult(RESULT_OK, intent)
            startActivityForResult(intent, 102)
            finish()

        }


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            var pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

            if (pairedDevices != null && pairedDevices.isNotEmpty()){
                for (device: BluetoothDevice in pairedDevices){
                    adapterPairedDevices.add(device.name + "\n" + device.address)
                }
            }

        }

        var intentFilter1: IntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(deviceListener, intentFilter1)
        var intentFilter2: IntentFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(deviceListener, intentFilter2)

    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_device_list,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_scan_devices){
            scanDevices()
            return true
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun scanDevices() {
        progressScanDevices.visibility = View.VISIBLE
        adapterAvailableDevices.clear()
        Toast.makeText(this, "Scan started", Toast.LENGTH_SHORT).show()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            bluetoothAdapter.startDiscovery()

        }

    }



}
