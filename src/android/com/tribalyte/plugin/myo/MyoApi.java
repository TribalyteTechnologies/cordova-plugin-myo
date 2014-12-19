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

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.scanner.ScanActivity;

/**
 * Class implementing the communication between the Cordova plugin and the Myo SDK API for Android
 * 
 * @author rbarriuso
 *
 */
public class MyoApi extends CordovaPlugin {
	
	public static final boolean LOG_ENABLED = true;
	
	private static final String TAG = MyoApi.class.getSimpleName();	

	/* Actions on Hub */
	private static final String ACTION_INIT = "init";
	private static final String ACTION_SHUTDOWN = "shutdown";
	private static final String ACTION_OPEN_SCAN_DLG = "openScanDialog";
	private static final String ACTION_ATTACH_ADJ = "attachToAdjacentMyo";
	private static final String ACTION_ATTACH_ADJS = "attachToAdjacentMyos";
	private static final String ACTION_ATTACH_MAC = "attachByMacAddress";
	private static final String ACTION_DETACH = "detach";
	private static final String ACTION_SET_LOCK_POLICY = "setLockingPolicy";
	private static final String ACTION_GET_LOCK_POLICY = "getLockingPolicy";
	private static final String ACTION_SET_SENDUSAGE = "setSendUsageData";
	private static final String ACTION_IS_SENDUSAGE = "isSendingUsageData";
	private static final String ACTION_GET_ATTACH_ALLOWANCE = "getMyoAttachAllowance";
	private static final String ACTION_SET_ATTACH_ALLOWANCE = "setMyoAttachAllowance";
	private static final String ACTION_GET_DEVICES = "getConnectedDevices";
	private static final String ACTION_NOW = "now";
	private static final String ACTION_ON = "on";
	private static final String ACTION_OFF = "off";
	/* Actions on Myo */
	private static final String ACTION_MYO_IS_UNLOCKED = "myo_isUnlocked";
	private static final String ACTION_MYO_LOCK = "myo_lock";
	private static final String ACTION_MYO_UNLOCK = "myo_unlock";
	private static final String ACTION_MYO_REQUEST_RSSI = "myo_requestRssi";
	private static final String ACTION_MYO_VIBRATE = "myo_vibrate";
	private static final String ACTION_MYO_NOTIFY_USER = "myo_notifyUserAction";
	private static final String ACTION_MYO_GET_CONNECT_STATE = "myo_getConnectionState";
	private static final String ACTION_MYO_IS_CONNECTED = "myo_isConnected";

	private final MyoEventController mController = new MyoEventController();
	private Hub mHub = null;

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		mHub = Hub.getInstance();
		logd("Plugin initialized");
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext cbc)
			throws JSONException {
		boolean res = true;
		logd("Execute: " + action + ", args len: " + args.length() + ", args: " + args.toString());
		if(ACTION_INIT.equals(action)){
			initHub(cbc);
		}else if(ACTION_SHUTDOWN.equals(action)){
			mHub.shutdown();
			cbc.success();
		}else if(ACTION_OPEN_SCAN_DLG.equals(action)){
			startScanActivity(cbc);
		}else if(ACTION_ATTACH_ADJ.equals(action)){
			attachToAdjacentMyo(null, cbc);
		}else if(ACTION_ATTACH_ADJS.equals(action)){
			attachToAdjacentMyo(args.getInt(0), cbc);
		}else if(ACTION_ATTACH_MAC.equals(action)){
			attachToAdjacentMyo(args.getString(0), cbc);
		}else if(ACTION_DETACH.equals(action)){
			mHub.detach(args.getString(0));
			cbc.success();
		}else if(ACTION_SET_LOCK_POLICY.equals(action)){
			mHub.setLockingPolicy(Hub.LockingPolicy.valueOf(args.getString(0)));
			cbc.success();
		}else if(ACTION_GET_LOCK_POLICY.equals(action)){
			String policy = mHub.getLockingPolicy().name();
			cbc.success(policy);
		}else if(ACTION_SET_SENDUSAGE.equals(action)){
			mHub.setSendUsageData(args.getBoolean(0));
			cbc.success();
		}else if(ACTION_IS_SENDUSAGE.equals(action)){
			cbc.success(mHub.isSendingUsageData() ? 1 : 0);
		}else if(ACTION_GET_ATTACH_ALLOWANCE.equals(action)){
			cbc.success(mHub.getMyoAttachAllowance());
		}else if(ACTION_SET_ATTACH_ALLOWANCE.equals(action)){
			mHub.setMyoAttachAllowance(args.getInt(0));
			cbc.success();
		}else if(ACTION_GET_DEVICES.equals(action)){
			JSONArray myoList = new JSONArray();
			for(Myo myo : mHub.getConnectedDevices()){
				myoList.put(JsonMapper.toJson(myo));
			}
			cbc.success(myoList);
		}else if(ACTION_NOW.equals(action)){
			cbc.success(Long.toString(mHub.now()));
		}else if(ACTION_ON.equals(action)){
			mController.setEventHandler(args.getString(0), cbc);
		}else if(ACTION_OFF.equals(action)){
			mController.removeEventHandler(args.getString(0));
		}else if(ACTION_MYO_IS_UNLOCKED.equals(action)){
			Myo myo = mController.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				cbc.success(myo.isUnlocked() ? 1 : 0);
			}
		}else if(ACTION_MYO_LOCK.equals(action)){
			Myo myo = mController.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.lock();
				cbc.success();
			}
		}else if(ACTION_MYO_UNLOCK.equals(action)){
			Myo myo = mController.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.unlock(Myo.UnlockType.valueOf(args.getString(1)));
				cbc.success();
			}
		}else if(ACTION_MYO_REQUEST_RSSI.equals(action)){
			Myo myo = mController.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.requestRssi();
				cbc.success();
			}
		}else if(ACTION_MYO_VIBRATE.equals(action)){
			Myo myo = mController.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.vibrate(Myo.VibrationType.valueOf(args.getString(1)));
				cbc.success();
			}
		}else if(ACTION_MYO_NOTIFY_USER.equals(action)){
			Myo myo = mController.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.notifyUserAction();
				cbc.success();
			}
		}else if(ACTION_MYO_GET_CONNECT_STATE.equals(action)){
			Myo myo = mController.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				cbc.success(myo.getConnectionState().name());
			}
		}else if(ACTION_MYO_IS_CONNECTED.equals(action)){
			Myo myo = mController.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				cbc.success(myo.isConnected() ? 1 : 0);
			}
		}else{
			logw("Action not supported: " + action);
			res = false; //Will result in a "MethodNotFound" error
		}

		//TODO: add Hub.Scanner functionality?
		
		return res; 
	}

	private void initHub(final CallbackContext cbc) {
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() { //Need to call Hubg.init from the UI thread in order to use ScanActivity
				Context ctx = cordova.getActivity();
				String appId = ctx.getPackageName();
				if (mHub.init(ctx, appId)) {
					mHub.addListener(mController);
					logd("Myo Hub initialized with appId " + appId);
					cbc.success();
				} else {
					loge("Could not initialize the Hub", null);
					cbc.error("Could not initialize the Hub");
				}
			}
		});
	}

	//The scan activity is only for test purposes
	private void startScanActivity(final CallbackContext cbc) {
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				Activity ctx = cordova.getActivity();
				Intent intent = new Intent(ctx, ScanActivity.class);
				//TODO: remove flags
				//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.startActivity(intent);
				logd("Test scan activity started");
				cbc.success();
			}
		});
	}
	
	private void attachToAdjacentMyo(final Object reqVal, final CallbackContext cbc){
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				//TODO: check that the Bluetooth interface is on and show a message if not (on the UI thread)
				boolean res = true;
				if(reqVal == null){
					mHub.attachToAdjacentMyo();
				}else if(reqVal instanceof Number){
					mHub.attachToAdjacentMyos((Integer)reqVal);
				}else if(reqVal instanceof String){
					mHub.attachByMacAddress((String)reqVal);
				}else{
					res = false;
				}
				if(res){
					logd("Attaching...");
					cbc.success();
				}else{
					loge("Unsupported argument passed: " + reqVal, null);
					cbc.error("Wrong argument passed: " + reqVal);
				}
			}
		});
	}
	
	private void logd(String msg) {
		if (LOG_ENABLED) {
			Log.d(TAG, msg);
		}
	}

	private void logw(String msg) {
		if (LOG_ENABLED) {
			Log.w(TAG, msg);
		}
	}

	private void loge(String msg, Throwable t) {
		if (LOG_ENABLED) {
			Log.d(TAG, msg, t);
		}
	}

	@Override
	public void onPause(boolean multitasking) {
		logd("onPause. Multitask: " + multitasking);
		super.onPause(multitasking);
	}

	@Override
	public void onResume(boolean multitasking) {
		super.onResume(multitasking);
		logd("onResume");
	}

	@Override
	public void onNewIntent(Intent intent) {
		logd("onNewIntent");
		super.onNewIntent(intent);
	}

	@Override
	public void onDestroy() {
		logd("onDestroy. Shutting down Hub");
		try{
			if(mHub != null){
				mHub.removeListener(mController);
				mHub.shutdown();
			}
		}catch(Exception e){
			logw("Exception while closing Hub: " + e.getLocalizedMessage());
		}
		super.onDestroy();
	}

	@Override
	public Object onMessage(String id, Object data) { //Receive messages from other plugins through the webView
		logd("onMessage. Id: " + id + ", data: " + data);
		return super.onMessage(id, data);
	}

	@Override
	public void onReset() {
		logd("onReset");
		super.onReset();
	}

}