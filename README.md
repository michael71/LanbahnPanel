# LanbahnPanel

Android Program um Selectrix Modellbahnen über ein Tablet zu stellen. Es könne Weichen und Signale, aber auch Loks gesteuert werden.

Hierzu verbindet sich das Tablet per TCP/IP (SXnet Protokoll) mit dem [SX4 Programm](https://opensx.net/sx4) . 

## allgemeine Info 

unter [opensx.net/sx4](https://opensx.net/sx4)

## Programmdokumentation

 unter 
[michael71.github.io/LanbahnPanel](https://michael71.github.io/LanbahnPanel)

<img src="https://michael71.github.io/LanbahnPanel/lbpanel1.png" />


___



## EN

Android Program for Model Railroad Turnout and Signal control using the "SXnet" Protocol.

For more info, have a look at http://www.lanbahn.net or send a mail to michael2 (AT) oscale.net

<img src="https://michael71.github.io/LanbahnPanel/lbpanel1.png" />

Currently only Selectrix based Command Stations are supported.

Layout structure and position of tracks are defined in an xml file. Basically tracks and signals
must be defined and turnouts (=crossing tracks) are then calculated by the program. You find an
example under

         /app/src/main/assets/demo-panel.xml

## License: GPL v3

(c) Michael Blank - 2014-2020
