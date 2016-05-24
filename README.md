# Check, Book

This Android application allows the user to check Google Apps Resource (i.e. meeting rooms) availibility as well as quickly book any free slots.

A Google Apps project, making available the Admin SDK and Calendar API will have to provisioned by the Google Apps Administrator.
OAuth 2.0 credentials will also have to be created and supplied the appropriate "Signing-certificate fingerprint" from your Android build key.
https://console.developers.google.com/apis

For beacon support you will need to add an XML Map, mapping the a beacon id2 (major) to the email address of the resource.
The file should be called beacon_resource_map.xml and put in the /app/src/main/res/xml directory
Here is an example of what it's contents should contain:

```<?xml version="1.0" encoding="utf-8"?>
<map linked="false">
<entry key="0xb5c1517668d8">domain.com_343336383331352d393557@resource.calendar.google.com</entry>
<entry key="0xb5c1517888d9">domain.com_2d34373836333139353999@resource.calendar.google.com</entry>
</map>```


Screenshots: 
![screenshot01](https://cloud.githubusercontent.com/assets/6622730/15463385/38fead90-211d-11e6-99d3-869fff356ac5.png)
![screenshot02](https://cloud.githubusercontent.com/assets/6622730/15463387/390419f6-211d-11e6-85e9-c6826f821f98.png)
![screenshot03](https://cloud.githubusercontent.com/assets/6622730/15463386/390168aa-211d-11e6-8428-0544afb764c8.png)


