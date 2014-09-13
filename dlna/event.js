/**
* 该设备是否支持事件通知
*/
function isSupportedEvent(deviceName){
    if(deviceName.indexOf("优酷") != -1){
        return true;
    }
    if(deviceName.indexOf("BubbleUPnP") != -1){
        return true;
    }
    if(deviceName.indexOf("ITV") != -1){
        return true;
    }
    return false;
}

/**
* 解析事件返回事件信息
* {eventType:"TRANSITIONING,PLAYING,PAUSED_PLAYBACK,STOPPED"}
*/
function parseEvent(deviceName,varName,value){
    if(value.indexOf("TRANSITIONING") != -1){
        return {eventType:"TRANSITIONING"};
    } else if(value.indexOf("PLAYING") != -1){
        return {eventType:"PLAYING"};
    } else if(value.indexOf("PAUSED_PLAYBACK") != -1){
        return {eventType:"PAUSED_PLAYBACK"};
    } else if(value.indexOf("STOPPED") != -1){
        return {eventType:"STOPPED"};
    }
    return null;
}