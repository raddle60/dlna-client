/**
* 解析器信息
**/
var parserInfo = {
    name : "FLVXZ飞驴",
    qualitys : [{key:"4K极清",value:"4K极清"},
    {key:"4K极致",value:"4K极致"},
    {key:"1080P",value:"1080P"},
    {key:"720P",value:"720P"},
    {key:"原画",value:"原画"},
    {key:"超清",value:"超清"},
    {key:"高清MP4",value:"高清MP4"},
    {key:"高清FLV",value:"高清FLV"},
    {key:"高清",value:"高清"},
    {key:"普清",value:"普清"},
    {key:"FLV标清",value:"FLV标清"},
    {key:"标清",value:"标清"},
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
    encodedUrl = encodedUrl.replace("://",":##");
    encodedUrl = videoUrlParser.toBase64String(encodedUrl,"gbk") + "";
    encodedUrl = encodedUrl.replace("+","-");
    encodedUrl = encodedUrl.replace("/","_");
    var parseUrl = "https://www.flvxz.com/getFlv.php?url=" + encodedUrl;
    // 解析地址
    var content = httpclient.getRemotePage(parseUrl,"gbk", {"Referer":"https://www.flvxz.com/"});
    var videoName = "名称未知";
    var matchedVideoName = content.match(new RegExp("<h4 class=\"media-heading\">([^<>]+)</h4>"));
    if(matchedVideoName != null && matchedVideoName.length > 0){
        videoName = matchedVideoName[1];
    }
    // 视频地址
    var videoUrlRegex =  new RegExp("(\\[[^\\[\\]]+\\])|(<a rel=\"noreferrer\" href=\"[^\"]+\">)","g");
    var videoUrls = content.match(videoUrlRegex);
    var qualityName = "";
    var qualityInfo = {};
    // 按清晰度分类
    for(var i=0; i < videoUrls.length ; i++){
        if(videoUrls[i].charAt(0) == '['){
            qualityName = videoUrls[i].substring(1,videoUrls[i].length-1);
            qualityInfo[qualityName] = [];
            continue;
        }
        if(videoUrls[i].charAt(0) == '<'){
            var videoUrl = videoUrls[i].match(new RegExp("href=\"([^\"]+)\">"))[1];
            qualityInfo[qualityName].push(videoUrl);
            continue;
        }
    }
    var start = false;
    for(var i=0; i < parserInfo.qualitys.length ; i++){
        if(parserInfo.qualitys[i].key == videoQuality){
            start = true;
        }
        if(start && qualityInfo[parserInfo.qualitys[i].key] != null && qualityInfo[parserInfo.qualitys[i].key].length > 0){
            return {name:videoName,urls:qualityInfo[parserInfo.qualitys[i].key],qualityName:parserInfo.qualitys[i].value};
        }
    }
    return null;
}