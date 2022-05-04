package com.example.myapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityChatBinding
import com.example.myapplication.databinding.ActivityPairedDevicesBinding

private const val REQUEST_ENABLE_BT = 1
open class ChatActivity : AppCompatActivity() {

    private lateinit var context: Context
    private val LOCATION_PERMISSION_REQUEST: Int = 101
    private val SELECT_DEVICE: Int = 102
    private lateinit var binding: ActivityChatBinding
    private lateinit var actionBar: ActionBar
    //private lateinit var bt : Set<BluetoothDevice>
    //private var map = hashMapOf<String, BluetoothDevice?>()
    private var myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    public lateinit var mChatService : BluetoothChatService
    private var connected = false
    private var string =""
    private lateinit var strings : MutableList<String>
    private lateinit var myarrayAdapter : ArrayAdapter<String>
    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        binding= ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar=supportActionBar!!
        actionBar.title="Paired Devices"

        mChatService = BluetoothChatService(this,mHandler)
        strings = ArrayList<String>()

        myarrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, strings)
        binding.listConversation.setAdapter(myarrayAdapter)

        binding.sendBtn.setOnClickListener {
            string = binding.enterMessage.text.toString()
            mChatService.write(string.toByteArray())
            myarrayAdapter.add(string)
            binding.enterMessage.text.clear()

            //getViewByPosition(position,binding.listView)!!.setBackgroundColor(Color.MAGENTA)

            position++
        }

        context = this

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        //Proverava se if za poziv iz drugog puta
        var address: String? = intent?.getStringExtra("Address")
        if ( address != null) {
            var out: String = "Address: $address"
            Toast.makeText(context, out, Toast.LENGTH_SHORT).show()
            connectDevice(address)
        }

        //Izvrsava se kada se prvi put startuje profile
        else {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                //Device doesn't support Bluetooth
                Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show()
                finish() //automatic close app if Bluetooth service is not available!
            }
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.BLUETOOTH_CONNECT
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
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_search_devices) {
            checkPermissions()
            return true
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, Array(10){android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST)
        }
        else{
            startActivityForResult(Intent(context, DeviceListActivity::class.java), SELECT_DEVICE)
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startActivityForResult(Intent(context, DeviceListActivity::class.java), SELECT_DEVICE)
                finish()
            }
            else {
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Location permission is required.\n Please grant")
                    .setPositiveButton("Grant", DialogInterface.OnClickListener { dialogInterface, i -> checkPermissions() })
                    .setNegativeButton("Deny", DialogInterface.OnClickListener { dialogInterface, i -> this.finish() })
                    .show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectDevice(deviceAddress: String) {


        // Cancel discovery because it's costly and we're about to connect
        myBluetoothAdapter?.cancelDiscovery()


        val device = myBluetoothAdapter?.getRemoteDevice(deviceAddress)



        // binding.status.text = "Connecting..."


        // Attempt to connect to the device


        mChatService?.connect(this, device, true)


    }
    fun clearlistview() {
        strings.clear()
        myarrayAdapter.notifyDataSetChanged()
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
                            //clearlistview()
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

                     // binding.status.text = "read"
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    val milliSecondsTime = System.currentTimeMillis()
                    myarrayAdapter.add(readMessage)
                    // binding.status.text=position.toString()
                    // getViewByPosition(position,binding.listView)!!.setBackgroundColor(Color.CYAN)

                    position++
                    //binding.receivedMsg.text=readMessage
                    //Toast.makeText(this@MainActivity,"$mConnectedDeviceName : $readMessage",Toast.LENGTH_SHORT).show()
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage)
                    //chatFragment.communicate(com.webianks.bluechat.Message(readMessage,milliSecondsTime,Constants.MESSAGE_TYPE_RECEIVED))
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
}