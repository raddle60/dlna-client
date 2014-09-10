/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : AVTransport.java
*
*	Revision:
*
*	02/22/08
*		- first revision.
*
******************************************************************/

package com.raddle.dlna.renderer;


public class AVTransport {
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////

	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:AVTransport:1";

	// Browse Action	

	public final static String TRANSPORTSTATE = "TransportState";
	public final static String TRANSPORTSTATUS = "TransportStatus";
	public final static String PLAYBACKSTORAGEMEDIUM = "PlaybackStorageMedium";
	public final static String RECORDSTORAGEMEDIUM = "RecordStorageMedium";
	public final static String POSSIBLEPLAYBACKSTORAGEMEDIA = "PossiblePlaybackStorageMedia";
	public final static String POSSIBLERECORDSTORAGEMEDIA = "PossibleRecordStorageMedia";
	public final static String CURRENTPLAYMODE = "CurrentPlayMode";
	public final static String TRANSPORTPLAYSPEED = "TransportPlaySpeed";
	public final static String RECORDMEDIUMWRITESTATUS = "RecordMediumWriteStatus";
	public final static String CURRENTRECORDQUALITYMODE = "CurrentRecordQualityMode";
	public final static String POSSIBLERECORDQUALITYMODES = "PossibleRecordQualityModes";
	public final static String NUMBEROFTRACKS = "NumberOfTracks";
	public final static String CURRENTTRACK = "CurrentTrack";
	public final static String CURRENTTRACKDURATION = "CurrentTrackDuration";
	public final static String CURRENTMEDIADURATION = "CurrentMediaDuration";
	public final static String CURRENTTRACKMETADATA = "CurrentTrackMetaData";
	public final static String CURRENTTRACKURI = "CurrentTrackURI";
	public final static String AVTRANSPORTURI = "AVTransportURI";
	public final static String AVTRANSPORTURIMETADATA = "AVTransportURIMetaData";
	public final static String NEXTAVTRANSPORTURI = "NextAVTransportURI";
	public final static String NEXTAVTRANSPORTURIMETADATA = "NextAVTransportURIMetaData";
	public final static String RELATIVETIMEPOSITION = "RelativeTimePosition";
	public final static String ABSOLUTETIMEPOSITION = "AbsoluteTimePosition";
	public final static String RELATIVECOUNTERPOSITION = "RelativeCounterPosition";
	public final static String ABSOLUTECOUNTERPOSITION = "AbsoluteCounterPosition";
	public final static String CURRENTTRANSPORTACTIONS = "CurrentTransportActions";
	public final static String LASTCHANGE = "LastChange";
	public final static String SETAVTRANSPORTURI = "SetAVTransportURI";
	public final static String INSTANCEID = "InstanceID";
	public final static String CURRENTURI = "CurrentURI";
	public final static String CURRENTURIMETADATA = "CurrentURIMetaData";
	public final static String SETNEXTAVTRANSPORTURI = "SetNextAVTransportURI";
	public final static String NEXTURI = "NextURI";
	public final static String NEXTURIMETADATA = "NextURIMetaData";
	public final static String GETMEDIAINFO = "GetMediaInfo";
	public final static String NRTRACKS = "NrTracks";
	public final static String MEDIADURATION = "MediaDuration";
	public final static String PLAYMEDIUM = "PlayMedium";
	public final static String RECORDMEDIUM = "RecordMedium";
	public final static String WRITESTATUS = "WriteStatus";
	public final static String GETTRANSPORTINFO = "GetTransportInfo";
	public final static String CURRENTTRANSPORTSTATE = "CurrentTransportState";
	public final static String CURRENTTRANSPORTSTATUS = "CurrentTransportStatus";
	public final static String CURRENTSPEED = "CurrentSpeed";
	public final static String GETPOSITIONINFO = "GetPositionInfo";
	public final static String TRACK = "Track";
	public final static String TRACKDURATION = "TrackDuration";
	public final static String TRACKMETADATA = "TrackMetaData";
	public final static String TRACKURI = "TrackURI";
	public final static String RELTIME = "RelTime";
	public final static String ABSTIME = "AbsTime";
	public final static String RELCOUNT = "RelCount";
	public final static String ABSCOUNT = "AbsCount";
	public final static String GETDEVICECAPABILITIES = "GetDeviceCapabilities";
	public final static String PLAYMEDIA = "PlayMedia";
	public final static String RECMEDIA = "RecMedia";
	public final static String RECQUALITYMODES = "RecQualityModes";
	public final static String GETTRANSPORTSETTINGS = "GetTransportSettings";
	public final static String PLAYMODE = "PlayMode";
	public final static String RECQUALITYMODE = "RecQualityMode";
	public final static String STOP = "Stop";
	public final static String PLAY = "Play";
	public final static String SPEED = "Speed";
	public final static String PAUSE = "Pause";
	public final static String RECORD = "Record";
	public final static String SEEK = "Seek";
	public final static String UNIT = "Unit";
	public final static String TARGET = "Target";
	public final static String NEXT = "Next";
	public final static String PREVIOUS = "Previous";
	public final static String SETPLAYMODE = "SetPlayMode";
	public final static String NEWPLAYMODE = "NewPlayMode";
	public final static String SETRECORDQUALITYMODE = "SetRecordQualityMode";
	public final static String NEWRECORDQUALITYMODE = "NewRecordQualityMode";
	public final static String GETCURRENTTRANSPORTACTIONS = "GetCurrentTransportActions";
	public final static String ACTIONS = "Actions";

	public final static String STOPPED = "STOPPED";
	public final static String PLAYING = "PLAYING";
	public final static String TRANSITIONING = "TRANSITIONING";
	public final static String PAUSED_PLAYBACK = "PAUSED_PLAYBACK";
	public final static String OK = "OK";
	public final static String ERROR_OCCURRED = "ERROR_OCCURRED";
	public final static String NORMAL = "NORMAL";
	public final static String TRACK_NR = "TRACK_NR";
}
