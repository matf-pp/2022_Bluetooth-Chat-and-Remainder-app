package com.example.myapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.myapplication.ChatUtils.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * Created by Qaifi on 4/5/2018.
 */
class ChatUtils(private val context: Context, private val handler: Handler) {
    private val bluetoothAdapter: BluetoothAdapter
    private var connectThread: ConnectThread? = null
    private var acceptThread: AcceptThread? = null
    private var connectedThread: ConnectedThread? = null
    private val APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    private val APP_NAME = "BluetoothChatApp"
    private var state1: Int



    fun getstate1(): Int {
        return state1
    }

    @Synchronized
    fun setstate1(state1: Int) {
        this.state1 = state1
        handler.obtainMessage(DescoverDevicesActivity.MESSAGE_STATE_CHANGED, state1, -1).sendToTarget()
    }

    @Synchronized
    private fun start() {
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        setstate1(state1_LISTEN)
    }

    @Synchronized
    fun stop() {
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (acceptThread != null) {
            acceptThread!!.cancel()
            acceptThread = null
        }
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        setstate1(state1_NONE)
    }

    fun connect(device: BluetoothDevice) {
        if (state1 == state1_CONNECTING) {
            connectThread!!.cancel()
            connectThread = null
        }
        connectThread = ConnectThread(device)
        connectThread!!.start()
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        setstate1(state1_CONNECTING)
    }

    fun write(buffer: ByteArray?) {
        var connThread: ConnectedThread?
        synchronized(this) {
            if (state1 != state1_CONNECTED) {
                return
            }
            connThread = connectedThread
        }
        connThread!!.write(buffer)
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket?
        override fun run() {
            var socket: BluetoothSocket? = null
            try {
                socket = serverSocket!!.accept()
            } catch (e: IOException) {
                Log.e("Accept->Run", e.toString())
                try {
                    serverSocket!!.close()
                } catch (e1: IOException) {
                    Log.e("Accept->Close", e.toString())
                }
            }
            if (socket != null) {
                when (state1) {
                    state1_LISTEN, state1_CONNECTING -> connected(socket, socket.remoteDevice)
                    state1_NONE, state1_CONNECTED -> try {
                        socket.close()
                    } catch (e: IOException) {
                        Log.e("Accept->CloseSocket", e.toString())
                    }
                }

            }
        }

        fun cancel() {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                Log.e("Accept->CloseServer", e.toString())
            }
        }

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)  //////////////
            } catch (e: IOException) {
                Log.e("Accept->Constructor", e.toString())
            }
            serverSocket = tmp
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket?
        @SuppressLint("MissingPermission")
        override fun run() {
            try {
                socket!!.connect()    ////////////////////////////////////
            } catch (e: IOException) {
                Log.e("Connect->Run", e.toString())
                try {
                    socket!!.close()
                } catch (e1: IOException) {
                    Log.e("Connect->CloseSocket", e.toString())
                }
                connectionFailed()
                return
            }
            synchronized(this@ChatUtils) { connectThread = null }
            connected(socket, device)
        }

        fun cancel() {
            try {
                socket!!.close()
            } catch (e: IOException) {
                Log.e("Connect->Cancel", e.toString())
            }
        }

        init {
            var tmp: BluetoothSocket? = null
            try {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID)  ////////////////////////
            } catch (e: IOException) {
                Log.e("Connect->Constructor", e.toString())
            }
            socket = tmp
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket?) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(1024)
            val bytes: Int
            try {
                bytes = inputStream!!.read(buffer)
                handler.obtainMessage(DescoverDevicesActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget()
            } catch (e: IOException) {
                connectionLost()
            }
        }

        fun write(buffer: ByteArray?) {
            try {
                outputStream!!.write(buffer)
                handler.obtainMessage(DescoverDevicesActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget()
            } catch (e: IOException) {
            }
        }

        fun cancel() {
            try {
                socket!!.close()
            } catch (e: IOException) {
            }
        }

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket!!.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }
            inputStream = tmpIn
            outputStream = tmpOut
        }
    }

    private fun connectionLost() {
        val message = handler.obtainMessage(DescoverDevicesActivity.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(DescoverDevicesActivity.TOAST, "Connection Lost")
        message.data = bundle
        handler.sendMessage(message)
        start()
    }

    @Synchronized
    private fun connectionFailed() {
        val message = handler.obtainMessage(DescoverDevicesActivity.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(DescoverDevicesActivity.TOAST, "Cant connect to the device")
        message.data = bundle
        handler.sendMessage(message)
        start()
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun connected(socket: BluetoothSocket?, device: BluetoothDevice) {
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        connectedThread = ConnectedThread(socket)
        connectedThread!!.start()
        val message = handler.obtainMessage(DescoverDevicesActivity.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(DescoverDevicesActivity.DEVICE_NAME, device.name)  ////////////////
        message.data = bundle
        handler.sendMessage(message)
        setstate1(state1_CONNECTED)
    }

    companion object {
        const val state1_NONE = 0 as Int
        const val state1_LISTEN = 1 as Int
        const val state1_CONNECTING = 2 as Int
        const val state1_CONNECTED = 3 as Int
    }

    init {
        state1 = state1_NONE
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }
}