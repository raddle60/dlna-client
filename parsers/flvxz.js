/**
* 解析器信息
**/
var parserInfo = {
    name : "FLVXZ飞驴",
    sort : 10,
    qualitys : [{key:"4K极清",value:"4K极清"},
    {key:"4K极致",value:"4K极致"},
    {key:"1080P",value:"1080P"},
    {key:"720P",value:"720P"},
    {key:"原画",value:"原画"},
    {key:"超清",value:"超清"},
    {key:"高清",value:"高清"},
    {key:"普清",value:"普清"},
    {key:"标清",value:"标清"},
    {key:"低清",value:"低清"},
    {key:"流畅",value:"流畅"},
    {key:"极速",value:"极速"}
    ]
};

/**
* url转换
**/
function fetchVideoUrls(url,videoQuality){
    var encodedUrl = url;
    if(url.indexOf("#") != -1){
        encodedUrl = url.substring(0,url.indexOf("#"));
    }
    //if(encodedUrl.indexOf("?") != -1){
    //    encodedUrl = encodedUrl.substring(0,encodedUrl.indexOf("?"));
    //}
    encodedUrl = encodedUrl.replace("://",":##");
    encodedUrl = videoUrlParser.toBase64String(encodedUrl,"gbk") + "";
    encodedUrl = encodedUrl.replace("+","-");
    encodedUrl = encodedUrl.replace("/","_");
    var parseUrl = "http://flv.cn/getFlv.php?url=" + encodedUrl;
    // 解析地址
    var content = httpclient.getRemotePage(parseUrl,"gbk", {"Referer":"http://flv.cn/"});
    var videoName = "名称未知";
    var matchedVideoName = content.match(new RegExp("<h4 class=\"media-heading\">([^<>]+)</h4>"));
    if(matchedVideoName != null && matchedVideoName.length > 1){
        videoName = matchedVideoName[1];
    } else {
        // 从视频列表中取
        matchedVideoName = content.match(new RegExp(">([^<>]+)_01\\."));
        if(matchedVideoName != null && matchedVideoName.length > 1){
            videoName = matchedVideoName[1];
        } else {
			// 没有编号
			matchedVideoName = content.match(new RegExp(">([^<>]+)\\.[^<>]+</a>"));
			if(matchedVideoName != null && matchedVideoName.length > 1){
				videoName = matchedVideoName[1];
			}
		}
    }
    // 视频地址
    var videoUrlRegex =  new RegExp("(>\\[[^\\[\\]]+\\]<)|(rel=\"noreferrer\" href=\"[^\"]+\">)","g");
    var videoUrls = content.match(videoUrlRegex);
    var qualityName = "";
    var qualityInfo = {};
    // 按清晰度分类
    for(var i=0; i < videoUrls.length ; i++){
        if(videoUrls[i].charAt(0) == '>'){
            qualityName = videoUrls[i].substring(2,videoUrls[i].length-2);
            qualityInfo[qualityName] = [];
            continue;
        }
        if(videoUrls[i].charAt(0) == 'r'){
            var videoUrl = videoUrls[i].match(new RegExp("href=\"([^\"]+)\">"))[1];
            if(videoUrl.indexOf("flvxz.com") != -1 || videoUrl.indexOf("flv.cn") !=-1){
                // 预览和下载
                continue;
            }
            if(videoUrl.indexOf("he.yinyuetai.com/uploads/videos/common/")!=-1){
                // 音乐台的有重定向，一些播放器不支持
               var headerInfo = httpclient.getHttpHeader(videoUrl,null);
               if(headerInfo.headers.get("Location") != null){
                   videoUrl = headerInfo.headers.get("Location");
               }
            }
            qualityInfo[qualityName].push(videoUrl);
            continue;
        }
    }
    for(var qname in qualityInfo){
            logger.info(qname+" - "+qualityInfo[qname]);
    }
    var start = false;
    for(var i=0; i < parserInfo.qualitys.length ; i++){
        if(parserInfo.qualitys[i].key == videoQuality){
            start = true;
        }
        if(start){
            var ret = findVideo(qualityInfo,parserInfo.qualitys[i].key,"单段");
            if(ret != null){
                ret.name = videoName;
                return ret;            
            }
            ret = findVideo(qualityInfo,parserInfo.qualitys[i].key,"FLV");
            if(ret != null){
                ret.name = videoName;
                return ret;            
            }
            ret = findVideo(qualityInfo,parserInfo.qualitys[i].key,null);
            if(ret != null){
                ret.name = videoName;
                return ret;            
            }
        }
    }
    return null;
}
function findVideo(qualityInfo,videoQua,keyword){
    for(var qname in qualityInfo){
        if(qname.indexOf(videoQua) != -1 && qualityInfo[qname] != null && qualityInfo[qname].length > 0){
            if(keyword != null){
                if(qname.indexOf(keyword) != -1){
                    return {urls:qualityInfo[qname],qualityName:qname};
                }
            } else {
                return {urls:qualityInfo[qname],qualityName:qname};
            }
        }
    }
    return null;
}


