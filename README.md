# cordova-plugin-myo

The [Thalmic's Myo gesture armband](https://www.thalmic.com/en/myo/) API available as a [Cordova / PhoneGap](http://cordova.apache.org/) plugin.

Currently only the Android platform is implemented, based on the [Myo Android SDK](https://developer.thalmic.com/docs/api_reference/android/index.html) version beta4 - 0.9.0.

Check the sample application at https://github.com/TribalyteTechnologies/cordova-plugin-myo-demo
<br>And the documentation at http://tribalytetechnologies.github.io/cordova-plugin-myo/doc

## Installation
    cordova plugin add https://github.com/TribalyteTechnologies/cordova-plugin-myo.git

## Usage
The plugin exposes a `cordova.plugins.MyoApi` object with the following methods:
* [init](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#init)
* [shutdown](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#shutdown)
* [openScanDialog](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#openScanDialog)
* [attachToAdjacentMyo](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#attachToAdjacentMyo)
* [attachToAdjacentMyos](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#attachToAdjacentMyos)
* [attachByMacAddress](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#attachByMacAddress)
* [detach](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#detach)
* [setLockingPolicy](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#setLockingPolicy)
* [getLockingPolicy](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#getLockingPolicy)
* [setSendUsageData](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#setSendUsageData)
* [isSendingUsageData](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#isSendingUsageData)
* [getMyoAttachAllowance](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#getMyoAttachAllowance)
* [setMyoAttachAllowance](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#setMyoAttachAllowance)
* [getConnectedDevices](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#getConnectedDevices)
* [now](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#now)
* [on](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#on)
* [off](http://tribalytetechnologies.github.io/cordova-plugin-myo/doc/module-MyoApi.html#off)

An example of initialization code would be:
```
var MyoApi = cordova.plugins.MyoApi;
MyoApi.init(function(){
	console.log("Myo Hub initialized successfully");
}, function(err){
	console.log("Error initializing Myo Hub: " + err);
});
//...
var myMyo = null;
MyoApi
.on("connect", function(ev){
	myMyo = ev.myo;
	window.alert(myMyo.name + " is  connected");
	localStorage["lastUsedMyoMac"] = myMyo.macAddress;
	console.log("Myo MAC address stored for easier future connection: " + localStorage["lastUsedMyoMac"]);
	myMyo.vibrate(MyoApi.VibrationType.MEDIUM); //Make the Myo vibrate
})
.on("disconnect", function(ev){
	window.alert(myMyo.name + " has disconnected");
	myMyo = null;
})
.on("pose", function(ev){
	window.alert("Pose detected: " + ev.pose);
});
//...
var lastMac = localStorage["lastUsedMyoMac"];
if(lastMac){
	MyoApi.attachByMacAddress(lastMac);
}else{
	window.alert("Place the Myo very close to the mobile device");
	MyoApi.attachToAdjacentMyo();
}
//Alternatively, for testing purposes, we could use MyoApi.openScanDialog()
//to connect to a device manually
```
For a working example, check the [plugin sample application](https://github.com/TribalyteTechnologies/cordova-plugin-myo-demo).

## Supported platforms
* Android 4.3 (Jelly Bean) and up (device must have Bluetooth radio that supports Bluetooth 4.0)

## Contributing
Contributions are welcome. To do so:

1. Fork this repo ( https://github.com/TribalyteTechnologies/cordova-plugin-myo/fork )
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create a new Pull Request


# License

The code developed by Tribalyte Technologies is distributed under the terms of the license below, while other pieces of software are protected by their corresponding licenses.

The MIT License (http://www.opensource.org/licenses/mit-license.html)

Copyright (c) 2014 Tribalyte Technologies S.L. (http://www.tribalyte.com/)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
