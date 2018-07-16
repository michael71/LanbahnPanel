package de.blankedv.lanbahnpanel.railroad.loconet

class Loco {

}


/** from digitrax loconet personal edition manual:
 *
 * Standard Address Selection:
To request a MOBILE or LOCOMOTIVE decoder task in the refresh stack, a Throttle device requests a
LOCOMOTIVE address for use,( opcode <BF>,<loco adr hi>,<loco adr lo>, <chk> ). The Master ( or PC
in a Limited Master environment) responds with a SLOT DATA READ for the SLOT ,( opcode <E7>,,)
,that contains this Locomotive address and all of its state information. If the address is currently not in
any SLOT, the master will load this NEW locomotive address into a new SLOT ,[speed=0, FWD,
Lite/Functions OFF and 128 step mode]and return this as a SLOT DATA READ. If no inactive slots are
free to load the NEW locomotive address, the response will be the Long Acknowledgment ,(opcode
<B4>,) , with a "fail" code, 0.
Note that regular "SHORT" 7 bit NMRA addresses are denoted by <loco-adr hi>=0. The Analog , Zero
stretched, loco is selected when both <loco adr hi>=<loco adr lo>=0. <Loco adr lo> is always a 7 bit
value. If <loco adr hi> is non-zero then the Master will generate NMRA type 14 bit or "LONG" address
packets using all 14 bits from <loco adr hi> and <loco adr lo> with Loco adr Hi being the MOST
significant address bits. Note that a DT200 Master does NOT process 14 bit adr requests and will consider
the <loco adr hi> to always zero. You can check the <TRK> return bits to see if the Master is a DT200.
The throttle must then examine the SLOT READ DATA bytes to work out how to process the
Master response. If the STATUS1 byte shows the SLOT to be COMMON, IDLE or NEW the throttle
may change the SLOT to IN_USE by performing a NULL MOVE instruction ,
(opcode <BA>,<slotX>,<slotX>,<chk> ) on this SLOT. This activation mechanism is used to guarantee proper
SLOT usage interlocking in a multi-user asynchronous environment.
If the SLOT return information shows the Locomotive requested is IN_USE or UP-CONSISTED (i.e. the
SL_CONUP, bit 6 of STATUS1 =1 ) the user should NOT use the SLOT. Any UP_CONSISTED locos
must be UNLINKED before usage! Always process the result from the LINK and UNLINK commands,
since the Master reserves the right to change the reply slot number and can reject the linking tasks under
several circumstances. Verify the reply slot # and the Link UP/DN bits in STAT1 are as you expected.
The throttle will then be able to update Speed./Direction and Function information . Whenever SLOT
information is changed in an active slot , the SLOT is flagged to be updated as the next DCC packet sent
to the track. If the SLOT is part of linked CONSIST SLOTS the whole CONSIST chain is updated
consecutively.
If a throttle is disconnected from the LocoNet, upon reconnection (if the throttle retains the SLOT state
from before disconnection) it will request the full status of the SLOT it was previously using. If the
reported STATUS and Speed/Function data etc., from the master exactly matches the remembered SLOT
state the throttle will continue using the SLOT. If the SLOT data does not match, the throttle will assume
the SLOT was purged free by the system and will go through the setup "log on" procedure again.
With this procedure the throttle does not need to have a unique "ID number". SLOT addresses DO NOT
imply they contain any particular LOCOMOTIVE address. The system can be mapped such that the
SLOT address matches the LOCOMOTIVE address within, if the user directly Reads and Writes to
SLOTs without using the Master to allocate Locomotive addresses
 */
