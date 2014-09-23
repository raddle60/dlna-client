package com.raddle.dlna.http.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.raddle.dlna.http.HttpCallback;
import com.raddle.dlna.http.HttpHelper;
import com.raddle.dlna.video.flv.FlvMetaInfo;

/**
 * description: 
 * @author raddle
 * time : 2014年9月23日 下午7:50:10
 */
public class JoinItem {
	private String url;
	private FlvMetaInfo flvMetaInfo;

	public static JoinItem loadJoinItem(final String url, Map<Object, Object> headers) throws IOException {
		return (JoinItem) HttpHelper.getRemotePageWithCallback(url, headers, new HttpCallback() {

			@Override
			public Object httpResponse(CloseableHttpResponse remoteResponse) {
				JoinItem item = new JoinItem();
				try {
					item.flvMetaInfo = FlvMetaInfo.readFlvMetaInfo(remoteResponse.getEntity().getContentLength(),
							remoteResponse.getEntity().getContent());
					item.url = url;
					remoteResponse.close();
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				return item;
			}
		});
	}

	public static List<JoinItem> joinVideo(List<JoinItem> orgJoinItems) {
		List<FlvMetaInfo> orgMetaInfos = new ArrayList<FlvMetaInfo>();
		for (JoinItem joinItem : orgJoinItems) {
			orgMetaInfos.add(joinItem.getFlvMetaInfo());
		}
		List<JoinItem> items = new ArrayList<JoinItem>();
		List<FlvMetaInfo> joinMetaInfos = FlvMetaInfo.joinMetaInfo(orgMetaInfos);
		for (int i = 0; i < joinMetaInfos.size(); i++) {
			JoinItem item = new JoinItem();
			item.setUrl(orgJoinItems.get(i).getUrl());
			item.setFlvMetaInfo(joinMetaInfos.get(i));
			items.add(item);
		}
		return items;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public FlvMetaInfo getFlvMetaInfo() {
		return flvMetaInfo;
	}

	public void setFlvMetaInfo(FlvMetaInfo flvMetaInfo) {
		this.flvMetaInfo = flvMetaInfo;
	}
}
