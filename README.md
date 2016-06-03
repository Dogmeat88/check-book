# Check, Book

This Android application allows the user to check Google Apps Resource (i.e. meeting rooms) availability as well as quickly book any free slots.

A Google Apps project, making available the Admin SDK and Calendar API will have to provisioned by the Google Apps Administrator.
OAuth 2.0 credentials will also have to be created and supplied the appropriate "Signing-certificate fingerprint" from your Android build key.
https://console.developers.google.com/apis


##Beacons:
Beacons are used to automatically identify rooms and display it's events. Beacons need to support Eddystone URL standard and are programmed with the resource ID for the room.
The resource ID can be found in the Google Apps admin console.
Some example programmed URLs are as follows:

```http://R34```

```http://-186627```


Screenshots: 

![screenshot01](https://cloud.githubusercontent.com/assets/6622730/15463385/38fead90-211d-11e6-99d3-869fff356ac5.png)
![screenshot02](https://cloud.githubusercontent.com/assets/6622730/15463387/390419f6-211d-11e6-85e9-c6826f821f98.png)
![screenshot03](https://cloud.githubusercontent.com/assets/6622730/15463386/390168aa-211d-11e6-8428-0544afb764c8.png)


