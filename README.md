LanbahnPanel
============

Android Program for Model Railroad Turnout and Signal control using the "lanbahn.net" Protocol.

For more info, have a look at http://www.lanbahn.net or send a mail to michael2 (AT) oscale.net


<img src="http://www.lanbahn.net/wp-content/uploads/2014/08/stellpult-lonstoke2-604x270.png" />

Layout structure and position of tracks is defined in an xml file, basically tracks and signals must be defined and turnouts are then calculated by the program. Here a basic example:
<pre>
<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
<layout-config>
<panel name="demo1">
<track x="0" y="60" x2="300" y2="60" />
<track x="20" y="60" x2="60" y2="100" />
<track x="20" y="100" x2="240" y2="100" />
<track x="40" y="60" x2="80" y2="20" />
<track x="60" y="40" x2="240" y2="40" />
<track x="180" y="120" x2="240" y2="60" />
<track x="80" y="20" x2="260" y2="20" />
<track x="240" y="40" x2="260" y2="60" />
<track x="100" y="120" x2="180" y2="120" />
<turnout x="20" y="60" x2="34" y2="60" xt="30" yt="70" adr="804" />
<turnout x="40" y="60" x2="54" y2="60" xt="50" yt="50" adr="810" />
<turnout x="240" y="60" x2="226" y2="60" xt="230" yt="70" adr="820" />
<turnout x="60" y="100" x2="46" y2="100" xt="50" yt="90" adr="804" />
<turnout x="60" y="40" x2="70" y2="30" xt="74" yt="40" adr="814" />
<turnout x="260" y="60" x2="246" y2="60" xt="250" yt="50" adr="874" />
<turnout x="200" y="100" x2="214" y2="100" xt="210" yt="90" adr="820" />
<turnout x="200" y="100" x2="186" y2="100" xt="190" yt="110" adr="822" />
<signal x="10" y="74" x2="2" y2="74" adr="174" />
<signal x="70" y="90" x2="78" y2="90" adr="176" />
<signal x="70" y="50" x2="78" y2="50" adr="172" />
<sensor name="CS24" x="140" y="60" adr="925" />
<sensor name="CS26" x="140" y="100" adr="976" />
<sensor name="CS22" x="100" y="40" x2="180" y2="40" adr="940"/>
</panel>
</layout-config>
</pre>
