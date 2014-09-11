/**
* 解析器信息
**/
var parserInfo = {
	name : "FLVCD硕鼠",
	qualitys : [{key:"super",value:"超清"},{key:"high",value:"高清"}]
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
    logger.info(url);
    if(url.indexOf("#") != -1){
        url = url.substring(0,url.indexOf("#"));
    }
    logger.info(url);
    if(url.indexOf("?") != -1){
        url = url.substring(0,url.indexOf("?"));
    }
    logger.info(url);
    var parseUrl = "http://www.flvcd.com/parse.php?kw=" + httpclient.encodeUrl(url,"GBK") + "&flag=one&format=" + videoQuality;
    var content = httpclient.getRemotePage(parseUrl,"GBK",null);
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
        videoInfo.urls = urls;
    }
    // 视频名称
    var videoNameRegex =  new RegExp("<input type=\"hidden\" name=\"filename\" value=\"([^\"]+)\"/>","g");
    var videoNameHidden = content.match(videoNameRegex);
    if(videoNameHidden != null && videoNameHidden.length > 0){
        var videoName = videoNameHidden[0].match(new RegExp("value=\"([^\"]+)\"/>"))[1];
        videoInfo.name = videoName;
    } else {
        videoInfo.name = "名称未知";
    }
    if(videoInfo.urls){
	    for(var i=0; i < parserInfo.qualitys.length ; i++){
	        if(parserInfo.qualitys[i].key == videoQuality){
	            videoInfo.qualityName = parserInfo.qualitys[i].value;
	        }
	    }
        return videoInfo;
    }
    return null;
}