/*
 * The MIT License (http://www.opensource.org/licenses/mit-license.html)
 * 
 * Copyright (c) 2014 Tribalyte Technologies S.L. (http://www.tribalyte.com/)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE. 
 */

package com.tribalyte.plugin.myo;

import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;

import android.util.Log;

import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;


/**
 * Class responsible for managing the Myo events through the DeviceListener
 * interface and also for keeping track of the connected Myo devices.
 * 
 * @author rbarriuso
 *
 */
class MyoEventController implements DeviceListener{
	
	private static final String TAG = MyoEventController.class.getSimpleName();
	private final Map<String, CallbackContext> mEvHandlers = new HashMap<String, CallbackContext>();
	private final Map<String, MyoWithJson> mMyoMap = new HashMap<String, MyoWithJson>();
	private final Object mHandlersLock = new Object();
	
	static class MyoWithJson{
		Myo myo;
		Object myoJson;
		MyoWithJson(Myo myo) {
			this.myo = myo;
			this.myoJson = JsonMapper.toJson(myo);
		}
		@Override
		public String toString() {
			return "MyoWithJson [myo=" + myo + ", myoJson=" + myoJson + "]"; //TODO: improve
		}
	}
	
	@Override
	public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xd) {
		logd("onArmSync. Arm: " + arm + ", dir: " + xd);
		callHandler("armSync", myo, timestamp, new String[]{"arm", arm.name(), "xdirection", xd.name()});
	}

	@Override
	public void onArmUnsync(Myo myo, long timestamp) {
		logd("onArmUnsync");
		callHandler("armUnsync", myo, timestamp, null);
	}

	@Override
	public void onAttach(Myo myo, long timestamp) {
		logd("onAttach");
		MyoWithJson myoWithJson = new MyoWithJson(myo);
		mMyoMap.put(myo.getMacAddress(), myoWithJson);
		logd("Added Myo instance to map: " + myoWithJson);
		callHandler("attach", myo, timestamp, null);
	}

	@Override
	public void onDetach(Myo myo, long timestamp) {
		logd("onDetach");
		callHandler("detach", myo, timestamp, null);
		MyoWithJson myoWithJson = mMyoMap.remove(myo.getMacAddress());
		logd("Removed Myo instance from map: " + myoWithJson);
	}
	
	@Override
	public void onConnect(Myo myo, long timestamp) {
		logd("onConnect");
		callHandler("connect", myo, timestamp, null);
	}		

	@Override
	public void onDisconnect(Myo myo, long timestamp) {
		logd("onDisconnect");
		callHandler("disconnect", myo, timestamp, null);
	}

	@Override
	public void onLock(Myo myo, long timestamp) {
		logd("onLock");
		callHandler("lock", myo, timestamp, null);
	}

	@Override
	public void onUnlock(Myo myo, long timestamp) {
		logd("onUnlock");
		callHandler("unlock", myo, timestamp, null);
	}
	
	@Override
	public void onPose(Myo myo, long timestamp, Pose pose) {
		logd("onPose");
		callHandler("pose", myo, timestamp, new String[]{"pose", pose.name()});
	}

	@Override
	public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
		//logd("onAccelerometerData. accel: " + accel);
		callHandler("accelerometerData", myo, timestamp, new Object[]{"accel", JsonMapper.toJson(accel)});
	}

	@Override
	public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
		//logd("onGyroscopeData. gyro: " + gyro);
		callHandler("gyroscopeData", myo, timestamp, new Object[]{"gyro", JsonMapper.toJson(gyro)});
	}

	@Override
	public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
		//logd("onOrientationData. rotation: " + rotation);
		callHandler("orientationData", myo, timestamp, new Object[]{"rotation", JsonMapper.toJson(rotation)});
	}

	@Override
	public void onRssi(Myo myo, long timestamp, int rssi) {
		//logd("onRssi. rssi: " + rssi);
		callHandler("rssi", myo, timestamp, new Object[]{"rssi", rssi});
	}
	
	public void setEventHandler(String evType, CallbackContext cbc){ //TODO: support multiple listeners per event
		logd("Adding event handler for type: " + evType);
		CallbackContext prevCbc = null;
		synchronized(mHandlersLock){
			prevCbc = mEvHandlers.get(evType);
			mEvHandlers.put(evType, cbc);
		}
		if(prevCbc != null){
			logd("Event handler already registered, removing previous one");
			sendRemovedResultToHandler(prevCbc);
		}
	}
	
	public void removeEventHandler(String evType){
		CallbackContext cbc = null;
		synchronized(mHandlersLock){
			cbc = mEvHandlers.remove(evType);
		}
		if(cbc != null){
			logd("Removing event handler for type " + evType);
			sendRemovedResultToHandler(cbc);
		}
	}
	
	public Myo getMyoOrErr(String mac, CallbackContext cbc){
		MyoWithJson res = mMyoMap.get(mac);
		if(res == null){
			loge("Myo with MAC address " + mac + " not found", null);
			cbc.error("Myo with MAC address " + mac + " not found");
		}
		return res.myo;
	}
	
	//Extras is an array whose even elements are keys (String) and odd elements are values (Object)
	private void callHandler(String evType, Myo myo, long timestamp, Object[] extras){
		CallbackContext cbc = null;
		synchronized(mHandlersLock){
			cbc = mEvHandlers.get(evType);
		}
		if(cbc != null){
			//logd("Sending event " + evType + " to registered caller");
			MyoWithJson myoWithJson = mMyoMap.get(myo.getMacAddress());
			if(myoWithJson != null){
				try{
					JSONObject res = new JSONObject();
					res.put("eventName", evType);
					res.put("myo", myoWithJson.myoJson);
					res.put("timestamp", Long.valueOf(timestamp));
					for(int i = 0; extras != null && i < extras.length; i+=2){
						res.put((String)extras[i], extras[i+1]);
					}
					PluginResult pResult = new PluginResult(PluginResult.Status.OK, res);
					pResult.setKeepCallback(true);
					cbc.sendPluginResult(pResult);
				}catch(Exception e){
					loge("Exception while calling handler: ", e);
					cbc.error(e.getLocalizedMessage());
				}
			}else{
				loge("ERROR: JSON Myo not found for MAC " + myo.getMacAddress(), null);
			}
		}
	}
	
	private void sendRemovedResultToHandler(CallbackContext cbc){
		PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT, "Event handler removed");
		pluginResult.setKeepCallback(false);
		cbc.sendPluginResult(pluginResult);
	}
	
	private static void logd(String msg) {
		if(MyoApi.LOG_ENABLED){
			Log.d(MyoEventController.TAG, msg);
		}
	}
	
	private static void loge(String msg, Throwable t) {
		if(MyoApi.LOG_ENABLED){
			Log.d(TAG, msg, t);
		}
	}
	
}