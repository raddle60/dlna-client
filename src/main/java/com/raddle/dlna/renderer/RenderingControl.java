/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : RenderingControl.java
*
*	Revision:
*
*	02/22/08
*		- first revision.
*
******************************************************************/

package com.raddle.dlna.renderer;

public class RenderingControl {
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////

	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:RenderingControl:1";

	// Browse Action	

	public final static String PRESETNAMELIST = "PresetNameList";
	public final static String LASTCHANGE = "LastChange";
	public final static String BRIGHTNESS = "Brightness";
	public final static String CONTRAST = "Contrast";
	public final static String SHARPNESS = "Sharpness";
	public final static String REDVIDEOGAIN = "RedVideoGain";
	public final static String GREENVIDEOGAIN = "GreenVideoGain";
	public final static String BLUEVIDEOGAIN = "BlueVideoGain";
	public final static String REDVIDEOBLACKLEVEL = "RedVideoBlackLevel";
	public final static String GREENVIDEOBLACKLEVEL = "GreenVideoBlackLevel";
	public final static String BLUEVIDEOBLACKLEVEL = "BlueVideoBlackLevel";
	public final static String COLORTEMPERATURE = "ColorTemperature";
	public final static String HORIZONTALKEYSTONE = "HorizontalKeystone";
	public final static String VERTICALKEYSTONE = "VerticalKeystone";
	public final static String MUTE = "Mute";
	public final static String VOLUME = "Volume";
	public final static String VOLUMEDB = "VolumeDB";
	public final static String LOUDNESS = "Loudness";
	public final static String LISTPRESETS = "ListPresets";
	public final static String INSTANCEID = "InstanceID";
	public final static String CURRENTPRESETNAMELIST = "CurrentPresetNameList";
	public final static String SELECTPRESET = "SelectPreset";
	public final static String PRESETNAME = "PresetName";
	public final static String GETBRIGHTNESS = "GetBrightness";
	public final static String CURRENTBRIGHTNESS = "CurrentBrightness";
	public final static String SETBRIGHTNESS = "SetBrightness";
	public final static String DESIREDBRIGHTNESS = "DesiredBrightness";
	public final static String GETCONTRAST = "GetContrast";
	public final static String CURRENTCONTRAST = "CurrentContrast";
	public final static String SETCONTRAST = "SetContrast";
	public final static String DESIREDCONTRAST = "DesiredContrast";
	public final static String GETSHARPNESS = "GetSharpness";
	public final static String CURRENTSHARPNESS = "CurrentSharpness";
	public final static String SETSHARPNESS = "SetSharpness";
	public final static String DESIREDSHARPNESS = "DesiredSharpness";
	public final static String GETREDVIDEOGAIN = "GetRedVideoGain";
	public final static String CURRENTREDVIDEOGAIN = "CurrentRedVideoGain";
	public final static String SETREDVIDEOGAIN = "SetRedVideoGain";
	public final static String DESIREDREDVIDEOGAIN = "DesiredRedVideoGain";
	public final static String GETGREENVIDEOGAIN = "GetGreenVideoGain";
	public final static String CURRENTGREENVIDEOGAIN = "CurrentGreenVideoGain";
	public final static String SETGREENVIDEOGAIN = "SetGreenVideoGain";
	public final static String DESIREDGREENVIDEOGAIN = "DesiredGreenVideoGain";
	public final static String GETBLUEVIDEOGAIN = "GetBlueVideoGain";
	public final static String CURRENTBLUEVIDEOGAIN = "CurrentBlueVideoGain";
	public final static String SETBLUEVIDEOGAIN = "SetBlueVideoGain";
	public final static String DESIREDBLUEVIDEOGAIN = "DesiredBlueVideoGain";
	public final static String GETREDVIDEOBLACKLEVEL = "GetRedVideoBlackLevel";
	public final static String CURRENTREDVIDEOBLACKLEVEL = "CurrentRedVideoBlackLevel";
	public final static String SETREDVIDEOBLACKLEVEL = "SetRedVideoBlackLevel";
	public final static String DESIREDREDVIDEOBLACKLEVEL = "DesiredRedVideoBlackLevel";
	public final static String GETGREENVIDEOBLACKLEVEL = "GetGreenVideoBlackLevel";
	public final static String CURRENTGREENVIDEOBLACKLEVEL = "CurrentGreenVideoBlackLevel";
	public final static String SETGREENVIDEOBLACKLEVEL = "SetGreenVideoBlackLevel";
	public final static String DESIREDGREENVIDEOBLACKLEVEL = "DesiredGreenVideoBlackLevel";
	public final static String GETBLUEVIDEOBLACKLEVEL = "GetBlueVideoBlackLevel";
	public final static String CURRENTBLUEVIDEOBLACKLEVEL = "CurrentBlueVideoBlackLevel";
	public final static String SETBLUEVIDEOBLACKLEVEL = "SetBlueVideoBlackLevel";
	public final static String DESIREDBLUEVIDEOBLACKLEVEL = "DesiredBlueVideoBlackLevel";
	public final static String GETCOLORTEMPERATURE = "GetColorTemperature";
	public final static String CURRENTCOLORTEMPERATURE = "CurrentColorTemperature";
	public final static String SETCOLORTEMPERATURE = "SetColorTemperature";
	public final static String DESIREDCOLORTEMPERATURE = "DesiredColorTemperature";
	public final static String GETHORIZONTALKEYSTONE = "GetHorizontalKeystone";
	public final static String CURRENTHORIZONTALKEYSTONE = "CurrentHorizontalKeystone";
	public final static String SETHORIZONTALKEYSTONE = "SetHorizontalKeystone";
	public final static String DESIREDHORIZONTALKEYSTONE = "DesiredHorizontalKeystone";
	public final static String GETVERTICALKEYSTONE = "GetVerticalKeystone";
	public final static String CURRENTVERTICALKEYSTONE = "CurrentVerticalKeystone";
	public final static String SETVERTICALKEYSTONE = "SetVerticalKeystone";
	public final static String DESIREDVERTICALKEYSTONE = "DesiredVerticalKeystone";
	public final static String GETMUTE = "GetMute";
	public final static String CHANNEL = "Channel";
	public final static String CURRENTMUTE = "CurrentMute";
	public final static String SETMUTE = "SetMute";
	public final static String DESIREDMUTE = "DesiredMute";
	public final static String GETVOLUME = "GetVolume";
	public final static String CURRENTVOLUME = "CurrentVolume";
	public final static String SETVOLUME = "SetVolume";
	public final static String DESIREDVOLUME = "DesiredVolume";
	public final static String GETVOLUMEDB = "GetVolumeDB";
	public final static String SETVOLUMEDB = "SetVolumeDB";
	public final static String GETVOLUMEDBRANGE = "GetVolumeDBRange";
	public final static String MINVALUE = "MinValue";
	public final static String MAXVALUE = "MaxValue";
	public final static String GETLOUDNESS = "GetLoudness";
	public final static String CURRENTLOUDNESS = "CurrentLoudness";
	public final static String SETLOUDNESS = "SetLoudness";
	public final static String DESIREDLOUDNESS = "DesiredLoudness";

	public final static String MASTER = "Master";
	public final static String FACTORYDEFAULTS = "FactoryDefaults";
}
