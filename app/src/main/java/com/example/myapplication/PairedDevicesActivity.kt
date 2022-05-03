package com.example.myapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPairedDevicesBinding


class PairedDevicesActivity : AppCompatActivity() {



    private lateinit var binding: ActivityPairedDevicesBinding
    private lateinit var actionBar: ActionBar
    private lateinit var bt : Set<BluetoothDevice>
    private var map = hashMapOf<String, BluetoothDevice?>()
    private var myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    public lateinit var mChatService : BluetoothChatService
    private var connected = false
    private var string =""
    private lateinit var strings : MutableList<String>
    private lateinit var myarrayAdapter : ArrayAdapter<String>
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding= ActivityPairedDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar=supportActionBar!!
        actionBar.title="Paired Devices"

        mChatService = BluetoothChatService(this,mHandler)


        binding.PairedBtn.setOnClickListener {
            Toast.makeText(this,"wtf",Toast.LENGTH_SHORT)
            listPairedDevices()
        }
        binding.listView.setOnItemClickListener { adapterView, view, i, l ->

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
            mChatService.write(string.toByteArray())
            myarrayAdapter.add(string)
            //getViewByPosition(position,binding.listView)!!.setBackgroundColor(Color.MAGENTA)

            position++

        }



    }





    @SuppressLint("MissingPermission")
    private fun listPairedDevices() {
        Toast.makeText(this,"klik", Toast.LENGTH_SHORT)
        //var myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bt = myBluetoothAdapter.bondedDevices as Set<BluetoothDevice>
        if(bt==null)
            Toast.makeText(this,"null", Toast.LENGTH_SHORT)

        strings = ArrayList<String>()
        var index = 0
        if(bt!=null) {
            if (bt.size > 0) {
                bt.forEach {
                    strings?.add(it.name+"\n"+it!!.address)
                    map[it.address] = it
                    index++
                }
                Toast.makeText(this,"Doslo", Toast.LENGTH_SHORT)
                //var lista = listOf<String>(strings)
                myarrayAdapter = ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, strings)
                binding.listView.setAdapter(myarrayAdapter)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun connectDevice(deviceAddress: String) {


        // Cancel discovery because it's costly and we're about to connect
        myBluetoothAdapter?.cancelDiscovery()


        val device = myBluetoothAdapter?.getRemoteDevice(deviceAddress)

        Toast.makeText(this, device?.address + " " + deviceAddress, Toast.LENGTH_SHORT).show()

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
                            clearlistview()
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

                    //  binding.status.text = "Not connected"
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
    fun getViewByPosition(pos: Int, listView: ListView): View? {
        val firstListItemPosition: Int = listView.getFirstVisiblePosition()
        val lastListItemPosition: Int = firstListItemPosition + listView.getChildCount() - 1
        return if (pos < firstListItemPosition || pos > lastListItemPosition) {
            listView.getAdapter().getView(pos, null, listView)
        } else {
            val childIndex = pos - firstListItemPosition
            listView.getChildAt(childIndex)
        }
    }

}