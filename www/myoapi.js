//TODO: add header
"use strict";

//TODO: add JSDoc

var DEBUG_LOG = true;

var cordova = require("cordova");
var utils = require("cordova/utils");

function plgLog(arg){
	if(DEBUG_LOG){
		console.log(arg);
	}
}
function dbgSuccess(opName){
	return function(res){
		plgLog("Success! Operation: " + opName + ". Res: " + JSON.stringify(res));
	};
}
function dbgError(opName){
	return function(err){
		plgLog("ERROR. Operation: " + opName + ". Err: " + JSON.stringify(err));
	};
}

function getExecOperationFn(apiName){
	return function(sCb, eCb, opName, args){
		var argsToSend = [];
		if(args){
			argsToSend = (utils.isArray(args) ? args : [args]);
		}
		plgLog("Executing \"" + opName + "\"" + (args ? ". Args: " + args : ""));
		cordova.exec(sCb || dbgSuccess, eCb || dbgError, apiName, opName, argsToSend);
	};
}

var Enum = {
	LockingPolicy: {
		NONE: "NONE",
		STANDARD: "STANDARD"
	},
	Arm: {
		LEFT: "LEFT",
		RIGHT: "RIGHT",
		UNKNOWN: "UNKNOWN"
	},
	XDirection: {
		TOWARD_WRIST: "TOWARD_WRIST",
		TOWARD_ELBOW: "TOWARD_ELBOW",
		UNKNOWN: "UNKNOWN"
	},
	Pose: {
		REST: "REST",
		FIST: "FIST",
		WAVE_IN: "WAVE_IN",
		WAVE_OUT: "WAVE_OUT",
		FINGERS_SPREAD: "FINGERS_SPREAD",
		DOUBLE_TAP: "DOUBLE_TAP",
		UNKNOWN: "UNKNOWN"
	},
	UnlockType: {
		TIMED: "TIMED",
		HOLD: "HOLD"
	},
	Vibration: {
		SHORT: "SHORT",
		MEDIUM: "MEDIUM",
		LONG: "LONG"
	},
	ConnectionState: {
		CONNECTED: "CONNECTED",
		CONNECTING: "CONNECTING",
		DISCONNECTED: "DISCONNECTED"
	}
};

var execOperation = getExecOperationFn("MyoApi");

var Myo = function(name, macAddress, fwVersion){
	this.name = name;
	this.macAddress = macAddress;
	this.fwVersion = fwVersion;
};
Myo.prototype = {
	constructor: Myo,
	equals: function(myo2){
		return this.macAddress === myo2.macAddress;
	},
	isUnlocked: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_isUnlocked", [this.macAddress]);
	},
	lock: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_lock", [this.macAddress]);
	},
	unlock: function(unlockType, sCb, eCb){
		execOperation(sCb, eCb, "myo_unlock", [this.macAddress, unlockType]);
	},
	requestRssi: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_requestRssi", [this.macAddress]);
	},
	vibrate: function(vibrationType, sCb, eCb){
		execOperation(sCb, eCb, "myo_vibrate", [this.macAddress, vibrationType]);
	},
	notifyUserAction: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_notifyUserAction", [this.macAddress]);
	},
	getConnectionState: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_getConnectionState", [this.macAddress]);
	},
	isConnected: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_isConnected", [this.macAddress]);
	}
};

//TODO: add Vector3, Quaternion?

var myoApi = function(){
	var myoHubApi = hubApi();

	plgLog("MyoApi constructor called");
	return { //TODO: this is actually the Hub API
		LockingPolicy: Enum.LockingPolicy,
		Arm: Enum.Arm,
		XDirection: Enum.XDirection,
		Pose: Enum.Pose,
		UnlockType: Enum.UnlockType,
		Vibration: Enum.Vibration,
		ConnectionState: Enum.ConnectionState,
		Myo: Myo,
		init: function(sCb, eCb){
			execOperation(sCb, eCb, "init");
		},
		shutdown: function(sCb, eCb){
			execOperation(sCb, eCb, "shutdown");
		},
		/** This function is only intended for testing the connectivity with your Myos */
		openScanDialog: function(sCb, eCb){
			execOperation(sCb, eCb, "openScanDialog");
		},
		attachToAdjacentMyo: function(sCb, eCb){
			execOperation(sCb, eCb, "attachToAdjacentMyo");
		},
		attachToAdjacentMyos: function(count, sCb, eCb){
			execOperation(sCb, eCb, "attachToAdjacentMyos", count);
		},
		attachByMacAddress: function(macAddress, sCb, eCb){
			execOperation(sCb, eCb, "attachByMacAddress", macAddress);
		},
		detach: function(macAddress, sCb, eCb){
			execOperation(sCb, eCb, "detach", macAddress);
		},
		setLockingPolicy: function(policy, sCb, eCb){
			execOperation(sCb, eCb, "setLockingPolicy", policy);
		},
		getLockingPolicy: function(sCb, eCb){
			execOperation(sCb, eCb, "getLockingPolicy");
			//TODO: return info synchronously?
		},
		setSendUsageData: function(isSend, sCb, eCb){
			execOperation(sCb, eCb, "setSendUsageData", isSend);
		},
		isSendingUsageData: function(sCb, eCb){
			execOperation(sCb, eCb, "isSendingUsageData");
			//TODO: return bool synchronously?
		},
		getMyoAttachAllowance: function(sCb, eCb){
			execOperation(sCb, eCb, "getMyoAttachAllowance");
			//TODO: return number synchronously?
		},
		setMyoAttachAllowance: function(allowance, sCb, eCb){
			execOperation(sCb, eCb, "setMyoAttachAllowance", allowance);
		},
		getConnectedDevices: function(sCb, eCb){
			execOperation(sCb, eCb, "getConnectedDevices");
			//TODO: return list synchronously?
		},
		now: function(sCb, eCb){
			execOperation(function(res){
				if(sCb){
					sCb(new Number(res)); //Convert from String to Number
				}
			}, eCb, "now");
		},

		/** Registers a Hub event listener. Use @off to unregister
		 * @param {string} eventName Supported events: "connect", "disconnect", "pose", "attach", "detach", "armSync", "armUnsync",
		 * "unlock", "lock", "orientationData", "accelerometerData", "gyroscopeData", "rssi".
		 * @param {function} onFnCallback Callback to be called when an event is received. Signature depends on event,
		 * for example, for "pose": function(Myo myo, number timestamp, Pose pose)
		 * @param {function} onErrCb Error callback.
		 */
		on: function(eventName, onFnCallback, onErrCb){
			execOperation(onFnCallback, onErrCb, "on", eventName);
		},
		/** Unregisters a Hub event listenr
		 * See @on
		 */
		off: function(eventName, sCb, eCb){
			execOperation(sCb, eCb, "off", eventName);
		}
	};
};

module.exports = myoApi();
