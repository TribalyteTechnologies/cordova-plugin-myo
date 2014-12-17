//TODO: add header
/**
 ** Implement: execute, pause and resume, onReset
 * Based on SDK myo-android-sdk-beta4 / myo-android-sdk-0.9.0
 **/

package com.tribalyte.plugin.myo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Hub.LockingPolicy;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Myo.ConnectionState;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

public class MyoApi extends CordovaPlugin {
	private static final String TAG = MyoApi.class.getSimpleName();

	private static final boolean LOG_ENABLED = true;

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
	
	private static final class MyoListener implements DeviceListener{
		private static final String TAG = MyoListener.class.getSimpleName();
		private final Map<String, CallbackContext> mEvHandlers = new HashMap<String, CallbackContext>();
		private final Map<String, Myo> mMyoMap = new HashMap<String, Myo>();
		private final Object mHandlersLock = new Object();
		
		private void callHandler(String evType, Myo myo, long timestamp, Object arg1, Object arg2){
			CallbackContext cbc = null;
			synchronized(mHandlersLock){
				cbc = mEvHandlers.get(evType);
			}
			if(cbc != null){
				logd("Sending event " + evType + " to registered caller");
				try{
					JSONObject res = new JSONObject();
					res.put("eventName", evType);
					res.put("myo", myo.getMacAddress());
					res.put("timestamp", Long.valueOf(timestamp));
					if(arg1 != null){
						res.put("extra1", arg1);
					}
					if(arg2 != null){
						res.put("extra2", arg2);
					}
					PluginResult pResult = new PluginResult(PluginResult.Status.OK, res);
					pResult.setKeepCallback(true);
					cbc.sendPluginResult(pResult);
				}catch(Exception e){
					logd("Exception while calling handler: " + e.getLocalizedMessage());
					cbc.error(e.getLocalizedMessage());
				}
			}
		}
		
		@Override
		public void onAccelerometerData(Myo myo, long timestamp, Vector3 v3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xd) {
			logd("onArmSync. Dir: " + xd);
			callHandler("armSync", myo, timestamp, arm.name(), xd.name());
		}

		@Override
		public void onArmUnsync(Myo myo, long timestamp) {
			logd("onArmUnsync");
			callHandler("armUnsync", myo, timestamp, null, null);
		}

		@Override
		public void onAttach(Myo myo, long timestamp) {
			logd("onAttach");
			callHandler("attach", myo, timestamp, null, null);
		}

		@Override
		public void onConnect(Myo myo, long timestamp) {
			logd("onConnect");
			callHandler("connect", myo, timestamp, null, null);
		}

		@Override
		public void onDetach(Myo myo, long timestamp) {
			logd("onDetach");
			callHandler("detach", myo, timestamp, null, null);
		}

		@Override
		public void onDisconnect(Myo myo, long timestamp) {
			logd("onDisconnect");
			callHandler("disconnect", myo, timestamp, null, null);
		}

		@Override
		public void onGyroscopeData(Myo myo, long timestamp, Vector3 v3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onLock(Myo myo, long timestamp) {
			logd("onLock");
			callHandler("lock", myo, timestamp, null, null);
		}

		@Override
		public void onOrientationData(Myo myo, long timestamp, Quaternion quat) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPose(Myo myo, long timestamp, Pose pose) {
			logd("onPose");
			callHandler("pose", myo, timestamp, pose.name(), null);
		}

		@Override
		public void onRssi(Myo myo, long timestamp, int rssi) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onUnlock(Myo myo, long timestamp) {
			logd("onUnlock");
			callHandler("unlock", myo, timestamp, null, null);
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
			Myo res = mMyoMap.get(mac);
			if(res == null){
				cbc.error("Myo with MAC address " + mac + " not found");
			}
			return res;
		}
		
		private void sendRemovedResultToHandler(CallbackContext cbc){
			PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT, "Event handler removed");
			pluginResult.setKeepCallback(false);
			cbc.sendPluginResult(pluginResult);
		}
		
		private void logd(String msg) {
			if(MyoApi.LOG_ENABLED){
				Log.d(MyoListener.TAG, msg);
			}
		}
		
	}

	private final MyoListener mListener = new MyoListener();
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
			mHub.setLockingPolicy(LockingPolicy.valueOf(args.getString(0)));
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
			List<Myo> devList = mHub.getConnectedDevices();
			//TODO: implement when Myo object is defined on the JS side
			cbc.error("Operation not implemented: " + action);
		}else if(ACTION_NOW.equals(action)){
			cbc.success(Long.toString(mHub.now()));
		}else if(ACTION_ON.equals(action)){
			mListener.setEventHandler(args.getString(0), cbc);
		}else if(ACTION_OFF.equals(action)){
			mListener.removeEventHandler(args.getString(0));
		}else if(ACTION_MYO_IS_UNLOCKED.equals(action)){
			Myo myo = mListener.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				cbc.success(myo.isUnlocked() ? 1 : 0);
			}
		}else if(ACTION_MYO_LOCK.equals(action)){
			Myo myo = mListener.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.lock();
				cbc.success();
			}
		}else if(ACTION_MYO_UNLOCK.equals(action)){
			Myo myo = mListener.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.unlock(Myo.UnlockType.valueOf(args.getString(1)));
				cbc.success();
			}
		}else if(ACTION_MYO_REQUEST_RSSI.equals(action)){
			Myo myo = mListener.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.requestRssi();
				cbc.success();
			}
		}else if(ACTION_MYO_VIBRATE.equals(action)){
			Myo myo = mListener.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.vibrate(Myo.VibrationType.valueOf(args.getString(1)));
				cbc.success();
			}
		}else if(ACTION_MYO_NOTIFY_USER.equals(action)){
			Myo myo = mListener.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				myo.notifyUserAction();
				cbc.success();
			}
		}else if(ACTION_MYO_GET_CONNECT_STATE.equals(action)){
			Myo myo = mListener.getMyoOrErr(args.getString(0), cbc);
			if(myo != null){
				cbc.success(myo.getConnectionState().name());
			}
		}else if(ACTION_MYO_IS_CONNECTED.equals(action)){
			Myo myo = mListener.getMyoOrErr(args.getString(0), cbc);
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
					mHub.addListener(mListener);
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
					mHub.attachToAdjacentMyos((int)reqVal);
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