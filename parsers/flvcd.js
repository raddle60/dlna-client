/**
* 解析器信息
**/
var parserInfo = {
	name : "FLVCD硕鼠",
	qualitys : [{key:"real",value:"原画或1080P或全高清"},
	{key:"super2",value:"720P"},
	{key:"super",value:"超清"},
	{key:"high",value:"高清"},
	{key:"normal",value:"流畅"},
	{key:"fluent",value:"极速"}
	]
};

/**
* url转换
**/
function fetchVideoUrls(url,videoQuality){
    var videoInfo = innerFetchVideoUrls(url,videoQuality);
    // 为空再试一次，经常地再一次请求会失败
    if(videoInfo == null){
        videoInfo = innerFetchVideoUrls(url,videoQuality);
    }
    return videoInfo;
}

function innerFetchVideoUrls(url,videoQuality){
    if(url.indexOf("#") != -1){
        url = url.substring(0,url.indexOf("#"));
    }
    if(url.indexOf("?") != -1){
        url = url.substring(0,url.indexOf("?"));
    }
    var parseUrl = "http://www.flvcd.com/parse.php?kw=" + httpclient.encodeUrl(url,"GBK") + "&flag=one&format=" + videoQuality;
    var content = httpclient.getRemotePage(parseUrl,"GBK",null);
    logger.info(content);
    var videoUrlRegex =  new RegExp("<input type=\"hidden\" name=\"inf\" value=\"([^\"]+)\"/>","g");
    var matchedUrlHidden = content.match(videoUrlRegex);
    var videoInfo = {};
    // url地址
    if(matchedUrlHidden != null && matchedUrlHidden.length > 0){
        var urlstr = matchedUrlHidden[0].match(new RegExp("value=\"([^\"]+)\"/>"))[1];
        if(urlstr.charAt(urlstr.length - 1) == '|'){
            urlstr=urlstr.substring(0,urlstr.length - 1);
        }
        var urls = urlstr.split("|");
        for(var i=0; i < urls.length ; i++){
            var videoUrl = urls[i];
            if(videoUrl.indexOf("he.yinyuetai.com/uploads/videos/common/")!=-1){
                // 音乐台的有重定向，一些播放器不支持
               var headerInfo = httpclient.getHttpHeader(videoUrl,null);
               if(headerInfo.headers.get("Location") != null){
                   urls[i] = headerInfo.headers.get("Location");
               }
            }
        }
        videoInfo.urls = urls;
    }
    // 视频名称
    var videoNameRegex =  new RegExp("<input type=\"hidden\" name=\"filename\" value=\"([^\"]+)\"/>","g");
    var videoNameHidden = content.match(videoNameRegex);
    if(videoNameHidden != null && videoNameHidden.length > 0){
        var videoName = videoNameHidden[0].match(new RegExp("value=\"([^\"]+)\"/>"))[1];
        videoInfo.name = videoName;
        var qualityName = videoName.match(new RegExp("(\\[[^\\[\\]]+\\])"));
        if(qualityName != null && qualityName.length > 1){
            videoInfo.qualityName = qualityName[1];
        }
    } else {
        videoInfo.name = "名称未知";
    }
    if(videoInfo.urls){
	    if(!videoInfo.qualityName){
	       videoInfo.qualityName = "未知清晰度";
	    }
        return videoInfo;
    }
    return null;
}