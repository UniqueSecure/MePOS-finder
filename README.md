# MePOS Finder

This is a sample code to  integrate the MePOS SDK to your application with MePOS Pro finder over USB and WiFi. On this example we color the MePOS over Wifi so you can determinate which one is detected, if you need to color over USB need to ask for permissions.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites


* Go to [Mepos.io](http://mepos.io/developers) and register as developer. There you will find the latest SDK file for your project.

## Installing

### Add the SDK to your project

There are two options:

***1 Manual***

- Download the .aar
- On your project create a new module (Import AAR Package)
- Go to project structure
- Add a app module dependencies on the library
    - On scope select compile

***2 Gradle Integration***

  You can integrate the MePOS connect library using gradle, adding the following configuration to your build.gradle file:

```
repositories {
 maven { url "http://connect.mepos.io/artifactory/libs-release-local" }
}
```

```
dependencies {
 compile 'com.uniquesecure:meposconnect:1.23:@aar'
}
```

* Prepare you manifest.xml file (necessary for wifi) and include the following lines:
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```

## Running the tests

1. To find a device over WiFi implement a client socket.
```
    Socket mePOS_Socket;
    mePOS_Socket.connect((new InetSocketAddress(192.168.1.64, port)), 60);
    
    Note.- In order to identify your MePOS Pro we suggest that you create a WiFi instance and impleent the setCosmeticLedCol(Integer colour) method.
    
    MePOS mePOS = new MePOS(context, MePOSConnectionType.WIFI);
    mePOS.getConnectionManager().setConnectionIPAddress("192.168.1.64");
```


2. To find a device over USB implement the class UsbManager.
```
Once you connect to a device over USB implement UsbDevice class so you obtain the getVendorId() and the getProductId();
The MePOS Pro vendor id is: 11406
the MePOS Pro product id is: 9220
```

```
Sample
protected boolean isAMePOS(UsbDevice device) 
{
        return device.getVendorId() == MEPOS_VENDOR_ID &&
                device.getProductId() == MEPOS_PRODUCT_ID;
    }
    
Note.- To change the decorative LED's you must create a USB MePOS instance.
MePOS mePOS = new MePOS(this, MePOSConnectionType.USB);
mePOS.setCosmeticLedCol(MePOSColorCodes.COSMETIC_YELLOW);
```

###### Note.- You can select from seven diferent colors.
* MePOSColorCodes.COSMETIC_GREEN
* MePOSColorCodes.COSMETIC_BLUE
* MePOSColorCodes.COSMETIC_CYAN
* MePOSColorCodes.COSMETIC_RED
* MePOSColorCodes.COSMETIC_YELLOW
* MePOSColorCodes.COSMETIC_MAGENTA
* MePOSColorCodes.COSMETIC_WHITE
* MePOSColorCodes.COSMETIC_OFF

```
4. Finally display the results on a listview
```

### About the test

The code on this repository is an example from the SDK and for MePOS developers willing to implement it.

## Reference

* [Android SDK guide](http://mepos.io/) - Developers section.


## Contact

Please rise a ticket [here](https://mepos.zendesk.com/hc/en-us/requests/new) MePOS support.
