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

/** @overview
 * JavaScript API of the Myo plugin for Cordova
 * @license MIT
 * @copyright [Tribalyte Technologies S.L.]{@link http://www.tribalyte.com/}
 * @author rbarriuso
 */

"use strict";

//TODO: minify

var DEBUG_LOG = false; //TODO: make API log level configurable (e.g. none / all / only errors)

var cordova = require("cordova");
var utils = require("cordova/utils");

//Internal functions
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

/** Enums namespace
 * @namespace Enum
 */
var Enum = {
	/** Locking policy as defined in the
	 * [Myo Android SDK]{@link https://developer.thalmic.com/docs/api_reference/android/enumcom_1_1thalmic_1_1myo_1_1_hub_1_1_locking_policy.html}
	 * @enum {string}
	 */
	LockingPolicy: {
		/** Pose events are always sent. */
		NONE: "NONE",
		/** Pose events are not sent while a Myo is locked. */
		STANDARD: "STANDARD"
	},
	/** Arm identification as defined in the
	 * [Myo Android SDK]{@link https://developer.thalmic.com/docs/api_reference/android/enumcom_1_1thalmic_1_1myo_1_1_arm.html}
	 * @enum {string}
	 */
	Arm: {
		LEFT: "LEFT",
		RIGHT: "RIGHT",
		UNKNOWN: "UNKNOWN"
	},
	/** Possible directions for Myo's +x axis relative to a user's arm as defined in the
	 * [Myo Android SDK]{@link https://developer.thalmic.com/docs/api_reference/android/enumcom_1_1thalmic_1_1myo_1_1_x_direction.html}
	 * @enum {string}
	 */
	XDirection: {
		TOWARD_WRIST: "TOWARD_WRIST",
		TOWARD_ELBOW: "TOWARD_ELBOW",
		UNKNOWN: "UNKNOWN"
	},
	/** Supported hand poses as defined in the
	 * [Myo Android SDK]{@link https://developer.thalmic.com/docs/api_reference/android/enumcom_1_1thalmic_1_1myo_1_1_pose.html}
	 * @enum {string}
	 */
	Pose: {
		REST: "REST",
		FIST: "FIST",
		WAVE_IN: "WAVE_IN",
		WAVE_OUT: "WAVE_OUT",
		FINGERS_SPREAD: "FINGERS_SPREAD",
		DOUBLE_TAP: "DOUBLE_TAP",
		UNKNOWN: "UNKNOWN"
	},
	/** Unlock types supported by Myo, as defined in the
	 * [Myo Android SDK]{@link https://developer.thalmic.com/docs/api_reference/android/enumcom_1_1thalmic_1_1myo_1_1_hub_1_1_locking_policy.html}
	 * @enum {string}
	 */
	UnlockType: {
		/** Unlock for a fixed period of time. */
		TIMED: "TIMED",
		/** Unlock until explicitly told to re-lock. */
		HOLD: "HOLD"
	},
	/** Types of vibration supported by the Myo, as defined in the
	 * [Myo Android SDK]{@link https://developer.thalmic.com/docs/api_reference/android/enumcom_1_1thalmic_1_1myo_1_1_hub_1_1_locking_policy.html}
	 * @enum {string}
	 */
	VibrationType: {
		SHORT: "SHORT",
		MEDIUM: "MEDIUM",
		LONG: "LONG"
	},
	/** Connetion state
	 * @enum {string}
	 */
	ConnectionState: {
		CONNECTED: "CONNECTED",
		CONNECTING: "CONNECTING",
		DISCONNECTED: "DISCONNECTED"
	}
};

var execOperation = getExecOperationFn("MyoApi");

/** Represents a Myo device
 * @constructor
 * @param {Array} dataArray Myo information fields as an array
 */
var Myo = function(dataArray){
	//dataArray format: [name, macAddress, fwVersion]

	/** Given name of the Myo device
	 * @type {string} */
	this.name = dataArray[0];
	/** MAC address of the Myo device
	 * @type {string} */
	this.macAddress = dataArray[1];
	/** Firmware version of the Myo device (e.g. "1.1.4")
	 * @type {string} */
	this.fwVersion = dataArray[2];
};
Myo.prototype = {

	constructor: Myo,

	/** Comparison function
	 * @param {Myo} myo2 Device to compare to
	 * @returns {Boolean} true if it's the same Myo device, false otherwise.
	 */
	equals: function(myo2){
		var res = false;
		if(myo2){
			res = (this.macAddress === myo2.macAddress);
		}
		return res;
	},

	/** Request to get the current locking state of the device.
	 * @param {SuccessCallback} sCb Success callback, which will receive a boolean as result
	 * @param {ErrorCallback} eCb Error callback
	 */
	isUnlocked: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_isUnlocked", [this.macAddress]);
	},

	/** Request to lock the device.
	 * @fires module:MyoApi#lock
	 * @param {SuccessCallback} sCb Success callback
	 * @param {ErrorCallback} eCb Error callback
	 */
	lock: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_lock", [this.macAddress]);
	},

	/** Request to unlock the device.
	 * @fires module:MyoApi#unlock
	 * @param {SuccessCallback} sCb Success callback
	 * @param {ErrorCallback} eCb Error callback
	 */
	unlock: function(unlockType, sCb, eCb){
		execOperation(sCb, eCb, "myo_unlock", [this.macAddress, unlockType]);
	},

	/** Request RSSI (received signal strength indication). The data is received via
	 * the callback registered with {@link MyoApi.on} for the "rssi" event.
	 * @fires module:MyoApi#rssi
	 * @param {SuccessCallback} sCb Success callback
	 * @param {ErrorCallback} eCb Error callback
	 */
	requestRssi: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_requestRssi", [this.macAddress]);
	},

	/** Send command to vibrate this device.
	 * @param vibrationType {MyoApi.VibrationType} Vibration type to be requested
	 * @param {SuccessCallback} sCb Success callback
	 * @param {ErrorCallback} eCb Error callback
	 */
	vibrate: function(vibrationType, sCb, eCb){
		execOperation(sCb, eCb, "myo_vibrate", [this.macAddress, vibrationType]);
	},

	/** Request the device to notify the user (short vibration)
	 * @param {SuccessCallback} sCb Success callback
	 * @param {ErrorCallback} eCb Error callback
	 */
	notifyUserAction: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_notifyUserAction", [this.macAddress]);
	},

	/**
	 * @callback ConnStateCallback
	 * @param {MyoApi.ConnectionState} connectionState Current connection state
	 */
	/** Request the current connection state of the device
	 * @param {ConnStateCallback} sCb Success callback, which receives a
	 * {@link MyoApi.ConnectionState} representing the connection state
	 * of the device
	 * @param {ErrorCallback} eCb Error callback
	 */
	getConnectionState: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_getConnectionState", [this.macAddress]);
	},

	/** Check if the device is currently connected
	 * @param {BooleanCallback} sCb Success callback, which receives a boolean
	 * representing if the device is connected
	 * @param {ErrorCallback} eCb Error callback
	 */
	isConnected: function(sCb, eCb){
		execOperation(sCb, eCb, "myo_isConnected", [this.macAddress]);
	}
};

/** @module MyoApi */
var MyoApi = function(){
	plgLog("MyoApi constructor called");
	return {
		/** @see {@link Enum.LockingPolicy} */
		LockingPolicy: Enum.LockingPolicy,
		/** @see {@link Enum.Arm} */
		Arm: Enum.Arm,
		/** @see {@link Enum.XDirection} */
		XDirection: Enum.XDirection,
		/** @see {@link Enum.Pose} */
		Pose: Enum.Pose,
		/** @see {@link Enum.UnlockType} */
		UnlockType: Enum.UnlockType,
		/** @see {@link Enum.VibrationType} */
		VibrationType: Enum.VibrationType,
		/** @see {@link Enum.ConnectionState} */
		ConnectionState: Enum.ConnectionState,

		/** Request to initialize the API. Will fail if the system doesn't support Bluetooth Low Energy
		* @param {SuccessCallback} sCb Success callback
		* @param {ErrorCallback} eCb Error callback
		*/
		init: function(sCb, eCb){
			execOperation(sCb, eCb, "init");
		},

		/** Request to free all resources of the API
		* @param {SuccessCallback} sCb Success callback
		* @param {ErrorCallback} eCb Error callback
		*/
		shutdown: function(sCb, eCb){
			execOperation(sCb, eCb, "shutdown");
		},

		/** Opens a native dialog which displays the Myo devices in range and
		 * gives the option to connect to them. No result is returned.
		 * @param {SuccessCallback} sCb Success callback
		 * @param {ErrorCallback} eCb Error callback
		 * */
		openScanDialog: function(sCb, eCb){
			execOperation(sCb, eCb, "openScanDialog");
		},

		/** Opens the Bluetooth adapter configuration screen.
		 * @param {BooleanCallback} sCb Success callback. Receives 1 if the user
		 * turned on the Bluetooth adapter, 0 otherwise.
		 * @param {ErrorCallback} eCb Error callback
		 * */
		openBluetoothConfig: function(sCb, eCb){
			execOperation(sCb, eCb, "openBluetoothConfig");
		},

		/** Checks if the Bluetooth adapter is enabled
		 * @param {BooleanCallback} sCb Success callback. Receives 1 if the adapter is
		 * enabled, 0 otherwise.
		 * @param {ErrorCallback} eCb Error callback
		 * */
		isBluetoothEnabled: function(sCb, eCb){
			execOperation(sCb, eCb, "isBluetoothEnabled");
		},

		/** Initiate attaching to a Myo that is physically very near to (almost touching) the Bluetooth radio.
		 * When the device is connected, events will be fired, and can be processed
		 * registering a callback for the "attach" and / or "connect" event.
		 * @fires module:MyoApi#attach
		 * @fires module:MyoApi#connect
		 * @param {SuccessCallback} sCb Success callback
		 * @param {ErrorCallback} eCb Error callback
		 */
		attachToAdjacentMyo: function(sCb, eCb){
			execOperation(sCb, eCb, "attachToAdjacentMyo");
		},

		/** Initiate attaching to several Myos that are physically very near to (almost touching) the Bluetooth radio.
		 * When a device is connected, events will be fired, and can be processed
		 * registering a callback for the "attach" and / or "connect" event.
		 * @fires module:MyoApi#attach
		 * @fires module:MyoApi#connect
		 * @param {number} count The number of devices which want to be attached
		 * @param {SuccessCallback} sCb Success callback
		 * @param {ErrorCallback} eCb Error callback
		 */
		attachToAdjacentMyos: function(count, sCb, eCb){
			execOperation(sCb, eCb, "attachToAdjacentMyos", count);
		},

		/** Attach / connect to a Myo device identified by its MAC address.
		 * When the device is connected, events will be fired, and can be processed
		 * registering a callback for the "attach" and / or "connect" event.
		 * @fires module:MyoApi#attach
		 * @fires module:MyoApi#connect
		 * @param {string} macAddress The MAC address of the device to be attached
		 * @param {SuccessCallback} sCb Success callback
		 * @param {ErrorCallback} eCb Error callback
		 */
		attachByMacAddress: function(macAddress, sCb, eCb){
			execOperation(sCb, eCb, "attachByMacAddress", macAddress);
		},

		/** Detach from the Myo device identified by its MAC address
		 * @fires module:MyoApi#detach
		 * @fires module:MyoApi#disconnect
		 * @param {string} macAddress The MAC address of the device to be detached
		 * @param {SuccessCallback} sCb Success callback
		 * @param {ErrorCallback} eCb Error callback
		 */
		detach: function(macAddress, sCb, eCb){
			execOperation(sCb, eCb, "detach", macAddress);
		},

		/** Set the locking policy for the connected Myos
		 * @param {MyoApi.LockingPolicy} policy Locking policy to be set
		 * @param {SuccessCallback} sCb Success callback
		 * @param {ErrorCallback} eCb Error callback
		 */
		setLockingPolicy: function(policy, sCb, eCb){
			execOperation(sCb, eCb, "setLockingPolicy", policy);
		},

		/** @callback GetLockPolicyCallback
		* @param {MyoApi.LockingPolicy} policy Currently configured locking policy.
		*/
		/** Get the current local policy of the Hub
		 * @param {GetLockPolicyCallback} sCb Success callback
		 * @param {ErrorCallback} eCb Error callback
		 * @todo TODO: return info synchronously?
		 */
		getLockingPolicy: function(sCb, eCb){
			execOperation(sCb, eCb, "getLockingPolicy");
		},

		/** Enable or disable the option to send device usage data to Thalmic Labs
		 * over the network.
		 * @param {boolean} isSend Whether to send or not usage data to Thalmic
		 * @param {SuccessCallback} sCb
		 * @param {ErrorCallback} eCb
		 */
		setSendUsageData: function(isSend, sCb, eCb){
			execOperation(sCb, eCb, "setSendUsageData", isSend);
		},

		/** Retrieve the current state of the usage data sending option.
		 * @param {BooleanCallback} sCb
		 * @param {ErrorCallback} eCb
		 * @todo TODO: return bool synchronously?
		 */
		isSendingUsageData: function(sCb, eCb){
			execOperation(sCb, eCb, "isSendingUsageData");
		},

		/** @callback GetAllowanceCallback
		* @param {number} devNum Number of devices allowed
		*/
		/** Retrieve the current maximum number of devices allowed to connect at once.
		 * @param {SuccessCallback} sCb Callback which receives the requested number of devices
		 * @param {ErrorCallback} eCb
		 * @todo TODO: return number synchronously?
		 */
		getMyoAttachAllowance: function(sCb, eCb){
			execOperation(sCb, eCb, "getMyoAttachAllowance");
		},

		/** Set the current maximum number of devices allowed to connect at once.
		 * @param {number} allowance Number of devices to allow to connect at the same time
		 * @param {SuccessCallback} sCb
		 * @param {ErrorCallback} eCb
		 */
		setMyoAttachAllowance: function(allowance, sCb, eCb){
			execOperation(sCb, eCb, "setMyoAttachAllowance", allowance);
		},

		/**@callback DeviceListCallback
		 * @param {Array} devList An array of Myo objects which represents the
		 * list of connected devices.
		 */
		/** Requests the list of currently connected Myo devices.
		 * @param {DeviceListCallback} sCb Callback to receive the result
		 * @param {ErrorCallback} eCb
		 * @todo TODO: return list synchronously?
		 */
		getConnectedDevices: function(sCb, eCb){
			execOperation(function(res){
				var devList = [];
				for(var i = 0; i < res.length; ++i){
					devList.push(new Myo(res[i]));
				}
				sCb(devList);
			}, eCb, "getConnectedDevices");
		},

		/** Requests the current timestamp to the API
		 * @param {SuccessCallback} sCb Success callback which receives the current timestamp
		 * as a Number representing the milliseconds since the Unix Epoch
		 * @param {ErrorCallback} eCb
		 */
		now: function(sCb, eCb){
			execOperation(function(res){
				if(sCb){
					sCb(new Number(res)); //Convert from String to Number
				}
			}, eCb, "now");
		},

		/** @typedef {Object} EventDataType
		 * @property {string} eventName Name (type) of the event
		 * @property {Myo} myo Myo device which originated the event
		 * @property {number} timestamp Timestamp when the event occurred
		 * @property ... Other extra properties depending on the specific type of event
		 */
		/** @callback EventCallback
		 * @param {EventDataType} eventData
		 */
		/** Registers an event listener. Use {@link MyoApi.off} to unregister
		 * @param {string} eventName One of the supported events: "connect", "disconnect", "pose", "attach", "detach", "armSync", "armUnsync",
		 * "unlock", "lock", "orientationData", "accelerometerData", "gyroscopeData", "rssi".
		 * @param {EventCallback} onEventCb Callback to be called when an event is received. The signature depends on event,
		 * for example, for "pose": function(Myo myo, number timestamp, Pose pose)
		 * @param {ErrorCallback} onErrCb Error callback.
		 * @todo TODO: calculate edge and add parameter to event callback
		 * @todo TODO: timer function to reduce false positives?
		 */
		on: function(eventName, onEventCb, onErrCb){
			execOperation(function(res){
				res.myo = new Myo(res.myo); //Wrap it with the myo object API
				onEventCb(res);
			}, onErrCb, "on", eventName);
			return this;
		},

		/** Unregisters an event listener registered with {@link MyoApi.on}
		 * @param {string} eventName Name / type of event to unregister
		 * @param {SuccessCallback} sCb
		 * @param {ErrorCallback} eCb
		 */
		off: function(eventName, sCb, eCb){
			execOperation(sCb, eCb, "off", eventName);
			return this;
		}
	};
};

module.exports = MyoApi();


/**Function which is called when the requested operation could not be completed
 * @callback ErrorCallback
 * @param {string} err Error reason
 *
 */

/**Function that is called when the requested operation succeeded
 * @callback SuccessCallback
 * @param {Object|number|boolean|string|null} res Result (if any) of the request.
 *
 */

/**Success callback returning a boolean result
* @callback BooleanCallback
* @param {boolean} Boolean res response to the requested operation.
*/
