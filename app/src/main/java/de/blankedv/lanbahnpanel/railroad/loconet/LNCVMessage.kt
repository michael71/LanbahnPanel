package de.blankedv.lanbahnpanel.railroad.loconet

/* from arduino loconet library

#define OPC_PEER_XFER     0xe5
#define OPC_IMM_PACKET    0xed
#define OPC_IMM_PACKET_2  0xee

typedef union {
	uint8_t D[7];
	struct {
		uint16_t deviceClass;
		uint16_t lncvNumber;
		uint16_t lncvValue;
		uint8_t flags;
	} data;
} UhlenbrockMsgPayload;

typedef struct
{
	uint8_t command;   // OPC_PEER_XFER for replies, OPC_IMM_PACKET for commands
	uint8_t mesg_size; // 15 bytes
	uint8_t SRC;       // source
	uint8_t DSTL;      // destination, low byte
	uint8_t DSTH;      // destination, high byte
	uint8_t ReqId;     // Request ID, distinguishes commands
	uint8_t PXCT1;     // MSBs of following data
	UhlenbrockMsgPayload payload; // Data Bytes
} UhlenbrockMsg;

/* peer-peer transfer message */
typedef struct peerxfer_t {
    uint8_t command;
    uint8_t mesg_size;     /* ummmmm, size of the message in bytes?                */
    uint8_t src;           /* source of transfer                                   */
    uint8_t dst_l;         /* ls 7 bits of destination                             */
    uint8_t dst_h;         /* ms 7 bits of destination                             */
    uint8_t pxct1;
    uint8_t d1;            /* data byte 1                                          */
    uint8_t d2;            /* data byte 2                                          */
    uint8_t d3;            /* data byte 3                                          */
    uint8_t d4;            /* data byte 4                                          */
    uint8_t pxct2;
    uint8_t d5;            /* data byte 5                                          */
    uint8_t d6;            /* data byte 6                                          */
    uint8_t d7;            /* data byte 7                                          */
    uint8_t d8;            /* data byte 8                                          */
    uint8_t chksum;        /* exclusive-or checksum for the message                */
} peerXferMsg;

/* send packet immediate message */
typedef struct sendpkt_t {
    uint8_t command;
    uint8_t mesg_size;     /* ummmmm, size of the message in bytes?                */
    uint8_t val7f;         /* fixed value of 0x7f                                  */
    uint8_t reps;          /* repeat count                                         */
    uint8_t dhi;           /* high bits of data bytes                              */
    uint8_t im1;
    uint8_t im2;
    uint8_t im3;
    uint8_t im4;
    uint8_t im5;
    uint8_t chksum;        /* exclusive-or checksum for the message                */
} sendPktMsg;


example : read LNCV#0 from device 63120, adr=1

        ED 0F 01 05 00 21 41 28 18 00 00 01 00 00 48

        ED 0F => 15byte PEER_XFER message, src = 01, dest 0x0005, reqID 0x21, pxct1 0x41
              => payload: 28 18 00 00 01 00 00
                    deviceClass 28 18 (=dec muesste eigentlich A8 18 = 6312 sein...)
                    lncvNumber;  00 00
	                lncvValue;  01 00  (dec 1)
	                flags 00

RECEIVE E5 0F 05 49 4B 1F 01 28 18 00 00 01 00 00 3D

Beispiel LNCV 2: (mit resultat =3)

	     ED 0F 01 05 00 21 01 28 18 02 00 01 00 00 0A
RECEIVE  E5 0F 05 49 4B 1F 01 28 18 02 00 03 00 00 3D



 */
