package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityDescoverDevicesBinding


class DescoverDevicesActivity : AppCompatActivity() {

    companion object {
        public val MESSAGE_STATE_CHANGED = 0
        public val MESSAGE_READ = 1
        public val MESSAGE_WRITE = 2
        public val MESSAGE_DEVICE_NAME = 3
        public val MESSAGE_TOAST = 4
        const val DEVICE_NAME = "deviceName"
        const val TOAST = "toast"
            const val STATE_NONE = 0 as Int
            const val STATE_LISTEN = 1 as Int
            const val STATE_CONNECTING = 2 as Int
            const val STATE_CONNECTED = 3 as Int
        const val STATE_FAILED = 4
        const val STATE_RECEIVED = 5

    }

    private val REQUEST_CODE = 1
    private val SELECT_DEVICE = 102
    private val PERMISSION_REQUEST_LOCATION = 2
    private lateinit var binding: ActivityDescoverDevicesBinding
    private lateinit var actionBar: ActionBar
    private lateinit var progressDialog: ProgressDialog
    private var strings = ArrayList<String>()
    private lateinit var arrayadapter : ArrayAdapter<String>
    private lateinit var availabledevices : ArrayAdapter<String>
    private var myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var founddevices = ArrayList<String>()
    private var connectedDevice: String? = null
    private var connected: Boolean = false
    private var mChatService: BluetoothChatService? = null
    private var map = hashMapOf<String, BluetoothDevice?>()
    private lateinit var  mConnectedDeviceName: String
    private var string = ""

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityDescoverDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar=supportActionBar!!
        actionBar.title="Discover Devices"

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Searching for devices...")
        progressDialog.setCanceledOnTouchOutside(false)

        if (myBluetoothAdapter == null)
            showAlertAndExit()


            // Get a set of currently paired devices
            val pairedDevices = myBluetoothAdapter?.bondedDevices


            // If there are paired devices, add each one to the ArrayAdapter


        mChatService = BluetoothChatService(this, mHandler)
        if(!myBluetoothAdapter.isEnabled)
        {
            var enablei = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enablei,7)
        }

        binding.DescoverBtn.setOnClickListener {
            discoverDevices()
        }
        binding.listViewDescover.setOnItemClickListener { adapterView, view, i, l ->

            var info = (view as TextView).text.toString()
            var adresa = info.substring(info.length - 17)
            connectDevice(adresa)
           // var intent = Intent()
           // intent.putExtra("deviceAddress",adresa)
           // setResult(RESULT_OK,intent)
           // startActivityForResult(intent,SELECT_DEVICE)
            //binding.DescoverBtn.text=adresa
           // finish()
        }
        binding.sendBtn.setOnClickListener {
            string = binding.messEt.text.toString()
            mChatService!!.write(string.toByteArray())

        }
    }
    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> {
                    when (msg.arg1) {
                        BluetoothChatService.STATE_CONNECTED -> {
                            binding.status.text = "Connected"
                           /* connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_connected))
                            Snackbar.make(findViewById(R.id.mainScreen),"Connected to " + mConnectedDeviceName,Snackbar.LENGTH_SHORT).show()
                            //mConversationArrayAdapter.clear()*/


                            connected = true
                        }

                        BluetoothChatService.STATE_CONNECTING -> {
                            binding.status.text = "Connecting"
                           /* connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_connecting))*/
                            connected = false
                        }

                        BluetoothChatService.STATE_LISTEN -> {
                            binding.status.text = "Listening"
                            /*connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_red))
                            Snackbar.make(findViewById(R.id.mainScreen),getString(R.string.not_connected),Snackbar.LENGTH_SHORT).show()*/
                            connected = false
                        }
                        BluetoothChatService.STATE_NONE -> {
                            binding.status.text = "None"
                            /*connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_red))
                            Snackbar.make(findViewById(R.id.mainScreen),getString(R.string.not_connected),Snackbar.LENGTH_SHORT).show()*/
                            connected = false
                        }

                    }
                }

               Constants.MESSAGE_WRITE -> {
                 //  binding.status.text = "Not connected"
                    /*val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    val writeMessage = String(writeBuf)
                    //Toast.makeText(this@MainActivity,"Me: $writeMessage",Toast.LENGTH_SHORT).show()
                    //mConversationArrayAdapter.add("Me:  " + writeMessage)
                    val milliSecondsTime = System.currentTimeMillis()
                    chatFragment.communicate(com.webianks.bluechat.Message(writeMessage,milliSecondsTime,Constants.MESSAGE_TYPE_SENT))*/

                }
                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    val milliSecondsTime = System.currentTimeMillis()
                    binding.receivedMsg.text=readMessage
                  //  binding.status.text = "Not connected"
                    /*val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    val milliSecondsTime = System.currentTimeMillis()
                    //Toast.makeText(this@MainActivity,"$mConnectedDeviceName : $readMessage",Toast.LENGTH_SHORT).show()
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage)
                    chatFragment.communicate(com.webianks.bluechat.Message(readMessage,milliSecondsTime,Constants.MESSAGE_TYPE_RECEIVED))*/
                }
                Constants.MESSAGE_DEVICE_NAME -> {
                   // binding.status.text = "Not connected"
                    // save the connected device's name
                  /*  mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                    binding.status.text = getString(R.string.connected_to) + " " +mConnectedDeviceName
                    connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_connected))
                    Snackbar.make(findViewById(R.id.mainScreen),"Connected to " + mConnectedDeviceName,Snackbar.LENGTH_SHORT).show()
                    connected = true
                    showChatFragment()*/

                }
                Constants.MESSAGE_TOAST-> {
                   // binding.status.text = "Not connected"
                   /* binding.status.text = getString(R.string.not_connected)
                    connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_red))
                    Snackbar.make(findViewById(R.id.mainScreen),msg.data.getString(Constants.TOAST),Snackbar.LENGTH_SHORT).show()
                    connected = false*/
                }
            }
        }
    }

    private fun pocnisopisivanje() {
        var intentchat = Intent(this,ChatingActivity::class.java)
        startActivity(intentchat)
    }
//private val receiver = object : BroadcastReceiver()

    private fun setState(subTitle: CharSequence) {
        supportActionBar!!.subtitle = subTitle
    }
   // @SuppressLint("MissingPermission")
    @SuppressLint("MissingPermission")
    private fun discoverDevices() {
        if(!myBluetoothAdapter.isEnabled)
            myBluetoothAdapter.enable()

        if(myBluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            var discoveryIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300)
            startActivity(discoveryIntent)
        }

       if(myBluetoothAdapter.isDiscovering)
           myBluetoothAdapter.cancelDiscovery()
        if(strings.size!=0)
            strings.clear()

     //  progressDialog.show()
       myBluetoothAdapter.startDiscovery()
       var intentfil = IntentFilter(BluetoothDevice.ACTION_FOUND)
       registerReceiver(receiver,intentfil)
       var intentfil2 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
       registerReceiver(receiver,intentfil2)
       arrayadapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strings)
       binding.listViewDescover.setAdapter(arrayadapter)


    }
    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if(device?.bondState != BluetoothDevice.BOND_BONDED) {
                    if(!founddevices.contains(device!!.address)) {
                        strings.add(device!!.name + "\n" + device!!.address)
                        arrayadapter.notifyDataSetChanged()
                        founddevices.add(device!!.address)
                    }
                    //arrayadapter.add(device?.name)
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
               //ggg progressDialog.dismiss()
                if(arrayadapter.count == 0)
                    Toast.makeText(context,"No devices found!",Toast.LENGTH_SHORT).show()
                else
                {
                    Toast.makeText(context,"Discovery Finished",Toast.LENGTH_SHORT).show()
                }

            }

        }
    }



    @SuppressLint("MissingPermission")
    private fun connectDevice(deviceAddress: String) {


        // Cancel discovery because it's costly and we're about to connect
        myBluetoothAdapter?.cancelDiscovery()


        val device = myBluetoothAdapter?.getRemoteDevice(deviceAddress)

            Toast.makeText(this,device?.address+" "+deviceAddress,Toast.LENGTH_SHORT).show()

       // binding.status.text = "Connecting..."


        // Attempt to connect to the device


        mChatService?.connect(this,device, true)



    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,ChatActivity::class.java))
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == SELECT_DEVICE && resultCode == RESULT_OK)
        {
            var adress = data?.getStringExtra("deviceAddress")
            Toast.makeText(this,"Address: "+adress,Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
    private fun checkPermissions1() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) !=
                PackageManager.PERMISSION_GRANTED) {

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Dozvola")
                builder.setMessage("Da li zelite bluetooth")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    // the dialog will be opened so we have to save that

                    requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADMIN), 124)
                }
                builder.show()

            } else {

            }
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) !=
                PackageManager.PERMISSION_GRANTED) {

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Dozvola")
                builder.setMessage("Da li zelite bluetooth")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    // the dialog will be opened so we have to save that
                    requestPermissions(arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN
                    ), REQUEST_CODE)
                }
                builder.show()

            } else {

            }
        }


        else {

        }

    }


    private fun checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Dozvola")
                builder.setMessage("Da li zelite")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    // the dialog will be opened so we have to save that

                    requestPermissions(arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), PERMISSION_REQUEST_LOCATION)
                }
                builder.show()

            } else {

            }
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) !=
                PackageManager.PERMISSION_GRANTED) {

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Dozvola")
                builder.setMessage("Da li zelite bluetooth")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    // the dialog will be opened so we have to save that
                    requestPermissions(arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN
                    ), REQUEST_CODE)
                }
                builder.show()

            } else {

            }
        }


        else {

        }

    }
    private fun showAlertAndExit() {

        AlertDialog.Builder(this)
            .setTitle("Not compatible")
            .setMessage("No support")
            .setPositiveButton("Exit", { _, _ -> System.exit(0) })
            .show()
    }


}