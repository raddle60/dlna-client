package com.raddle.dlna.video.flv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.eclipse.jetty.http.HttpHeader;

import com.raddle.dlna.http.HttpCallback;
import com.raddle.dlna.http.HttpHelper;
import com.raddle.dlna.video.flv.tag.TagHeader;
import com.raddle.dlna.video.flv.tag.script.ScriptDataDouble;
import com.raddle.dlna.video.flv.tag.script.ScriptTagBody;

/**
 * description: 
 * @author raddle
 * time : 2014年9月22日 下午10:35:27
 */
public class FlvMetaInfo {
	private long fileLength;
	private long preFileLength;
	private long joinIncrLength;
	private double preDurationSeconds;
	private int preLastTagTimestamp;
	private int lastTagTimestamp;
	private FlvHeader flvHeader;
	private TagHeader scriptTagHeader;
	private ScriptTagBody scriptTagBody;

	public FlvMetaInfo(long fileLength, FlvHeader flvHeader, TagHeader scriptTagHeader, ScriptTagBody scriptTagBody) {
		this.fileLength = fileLength;
		this.flvHeader = flvHeader;
		this.scriptTagHeader = scriptTagHeader;
		this.scriptTagBody = scriptTagBody;
	}

	public Double getDurationSeconds() {
		return scriptTagBody.getDuration().getValue();
	}

	public int getMetaInfoLength() {
		return flvHeader.getLength() + 4 + scriptTagBody.getTagLength() + 4;
	}

	public int getDataLength() {
		return scriptTagHeader.getDataLength();
	}

	public long getFileLengthNoMeta() {
		return fileLength - getMetaInfoLength();
	}

	public void writeFlvMetaInfo(OutputStream outputStream) throws IOException {
		flvHeader.writeFlvHeader(outputStream);
		scriptTagHeader.writeTagHeader(outputStream);
		scriptTagBody.writeScriptTagBody(outputStream);
	}

	public void skipFlvMetaInfo(InputStream inputStream) throws IOException {
		IOUtils.skip(inputStream, getMetaInfoLength());
	}

	public FlvMetaInfo deepClone() {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			writeFlvMetaInfo(os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			FlvMetaInfo readFlvMetaInfo = readFlvMetaInfo(fileLength, is);
			readFlvMetaInfo.setLastTagTimestamp(lastTagTimestamp);
			return readFlvMetaInfo;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static FlvMetaInfo readFlvMetaInfo(long fileLength, InputStream inputStream) throws IOException {
		FlvHeader readFlvHeader = FlvHeader.readFlvHeader(inputStream);
		TagHeader readTagHeaderForMeta = TagHeader.readTagHeader(inputStream);
		ScriptTagBody readScriptTagBody = ScriptTagBody.readScriptTagBody(readTagHeaderForMeta, inputStream);
		return new FlvMetaInfo(fileLength, readFlvHeader, readTagHeaderForMeta, readScriptTagBody);
	}

	public static void putLastTagTimestamp(FlvMetaInfo flvMetaInfo, File file) throws IOException {
		List<ScriptDataDouble> filepositions = flvMetaInfo.getScriptTagBody().getFilepositions();
		double lastPos = filepositions.get(filepositions.size() - 1).getValue();
		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.skip((long) lastPos);
		TagHeader readTagHeader = TagHeader.readTagHeader(fileInputStream);
		TagHeader lastTagHeader = null;
		while (readTagHeader != null) {
			if (readTagHeader.getTagType() != 8 && readTagHeader.getTagType() != 9) {
				throw new RuntimeException(readTagHeader.getTagType() + " is not video or audio type");
			}
			lastTagHeader = readTagHeader;
			fileInputStream.skip(readTagHeader.getDataLength() + 4);
			readTagHeader = TagHeader.readTagHeader(fileInputStream);
		}
		fileInputStream.close();
		flvMetaInfo.setLastTagTimestamp(lastTagHeader.getTimestamp());
	}

	public static void putLastTagTimestamp(final FlvMetaInfo flvMetaInfo, String httpUrl, Map<Object, Object> headers)
			throws IOException {
		List<ScriptDataDouble> filepositions = flvMetaInfo.getScriptTagBody().getFilepositions();
		double lastPos = filepositions.get(filepositions.size() - 1).getValue();
		if (headers == null) {
			headers = new HashMap<Object, Object>();
		}
		headers.put(HttpHeader.RANGE.asString(), "bytes=" + new DecimalFormat("#").format(lastPos) + "-");
		HttpHelper.getRemotePageWithCallback(httpUrl, headers, new HttpCallback() {

			@Override
			public Object httpResponse(CloseableHttpResponse response) {
				try {
					TagHeader readTagHeader = TagHeader.readTagHeader(response.getEntity().getContent());
					TagHeader lastTagHeader = null;
					while (readTagHeader != null) {
						if (readTagHeader.getTagType() != 8 && readTagHeader.getTagType() != 9) {
							throw new RuntimeException(readTagHeader.getTagType() + " is not video or audio type");
						}
						lastTagHeader = readTagHeader;
						IOUtils.skip(response.getEntity().getContent(), readTagHeader.getDataLength() + 4);
						readTagHeader = TagHeader.readTagHeader(response.getEntity().getContent());
					}
					flvMetaInfo.setLastTagTimestamp(lastTagHeader.getTimestamp());
					response.close();
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				return null;
			}
		});
	}

	/**
	 * 
	 * @param orgMetaInfo 读取未修改的信息
	 * @param joinMetaInfo 被修改的信息
	 * @return 返回合并信息
	 */
	public static List<FlvMetaInfo> joinMetaInfo(List<FlvMetaInfo> orgMetaInfo) {
		List<FlvMetaInfo> joinMetaInfo = new ArrayList<FlvMetaInfo>();
		for (FlvMetaInfo flvMetaInfo : orgMetaInfo) {
			joinMetaInfo.add(flvMetaInfo.deepClone());
		}
		FlvMetaInfo firstJoinedMetaInfo = joinMetaInfo.get(0);
		firstJoinedMetaInfo.getScriptTagBody().getDuration()
				.setValue(getCurrentDurationSeconds(orgMetaInfo, orgMetaInfo.size()));
		for (int i = 1; i < orgMetaInfo.size(); i++) {
			addFilepositionAndTime(orgMetaInfo, i, firstJoinedMetaInfo, joinMetaInfo.get(i));
		}
		// 重算body长度
		ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
		try {
			firstJoinedMetaInfo.getScriptTagBody().writeScriptTagBody(bodyOut);
		} catch (IOException e) {
		}
		// 设置body长度
		firstJoinedMetaInfo.getScriptTagHeader().setDataLength(bodyOut.size() - 4);
		// 计算增加的长度
		int incr = firstJoinedMetaInfo.getDataLength() - orgMetaInfo.get(0).getDataLength();
		for (int i = 0; i < firstJoinedMetaInfo.getScriptTagBody().getFilepositions().size(); i++) {
			ScriptDataDouble fileposition = firstJoinedMetaInfo.getScriptTagBody().getFilepositions().get(i);
			double pos = fileposition.getValue();
			// 由于增加了文件2，体积变大了
			fileposition.setValue(pos + incr);
		}
		// 文件长度也要增加
		for (int i = 0; i < joinMetaInfo.size(); i++) {
			FlvMetaInfo flvMetaInfo = joinMetaInfo.get(i);
			if (i > 0) {
				flvMetaInfo.setPreFileLength(flvMetaInfo.getPreFileLength() + incr);
			}
			flvMetaInfo.setJoinIncrLength(incr);
		}
		return joinMetaInfo;
	}

	private static void addFilepositionAndTime(List<FlvMetaInfo> orgMetaInfo, int index, FlvMetaInfo joinedMetaInfo,
			FlvMetaInfo toJoinMetaInfo) {
		long currentTagPos = getCurrentTagPos(orgMetaInfo, index);
		for (ScriptDataDouble fileposition : toJoinMetaInfo.getScriptTagBody().getFilepositions()) {
			double pos = fileposition.getValue();
			double newPos = pos - toJoinMetaInfo.getMetaInfoLength() + currentTagPos;
			toJoinMetaInfo.setPreFileLength(currentTagPos);
			joinedMetaInfo.getScriptTagBody().getFilepositions().add(new ScriptDataDouble(newPos));
		}
		double startTime = 0;
		for (ScriptDataDouble timeObj : toJoinMetaInfo.getScriptTagBody().getTimes()) {
			if (timeObj.getValue() > 1) {
				startTime = timeObj.getValue();
				break;
			}
		}
		double currentDurationSeconds = getCurrentDurationSeconds(orgMetaInfo, index);
		for (ScriptDataDouble timeObj : toJoinMetaInfo.getScriptTagBody().getTimes()) {
			double time = timeObj.getValue();
			double newTime = 0;
			if (startTime >= currentDurationSeconds) {
				// 说明分段视频，已经加过时间
				if (time == 0) {
					// 第一帧可能是特殊帧
					newTime = currentDurationSeconds;
				} else {
					newTime = time;
				}
			} else {
				// 加上增量
				newTime = time + currentDurationSeconds;
			}
			toJoinMetaInfo.setPreDurationSeconds(currentDurationSeconds);
			joinedMetaInfo.getScriptTagBody().getTimes().add(new ScriptDataDouble(newTime));
		}
		int currentLastTimestamp = getCurrentLastTimestamp(orgMetaInfo, startTime >= currentDurationSeconds, index);
		toJoinMetaInfo.setPreLastTagTimestamp(currentLastTimestamp);
	}

	private static long getCurrentTagPos(List<FlvMetaInfo> orgMetaInfo, int index) {
		long fileLength = 0;
		for (int i = 0; i < index; i++) {
			if (i == 0) {
				fileLength += orgMetaInfo.get(i).getFileLength();
			} else {
				fileLength += orgMetaInfo.get(i).getFileLengthNoMeta();
			}
		}
		return fileLength;
	}

	private static double getCurrentDurationSeconds(List<FlvMetaInfo> orgMetaInfo, int index) {
		double durationSeconds = 0;
		for (int i = 0; i < index; i++) {
			durationSeconds += orgMetaInfo.get(i).getDurationSeconds();
		}
		return durationSeconds;
	}

	private static int getCurrentLastTimestamp(List<FlvMetaInfo> orgMetaInfo, boolean isAdded, int index) {
		if (isAdded) {
			// 已经加过，不需要累加
			return 0;
		}
		int timestamp = 0;
		for (int i = 0; i < index; i++) {
			timestamp += orgMetaInfo.get(i).getLastTagTimestamp();
		}
		return timestamp;
	}

	public FlvHeader getFlvHeader() {
		return flvHeader;
	}

	public TagHeader getScriptTagHeader() {
		return scriptTagHeader;
	}

	public ScriptTagBody getScriptTagBody() {
		return scriptTagBody;
	}

	public long getFileLength() {
		return fileLength;
	}

	public long getPreFileLength() {
		return preFileLength;
	}

	public void setPreFileLength(long preFileLength) {
		this.preFileLength = preFileLength;
	}

	public double getPreDurationSeconds() {
		return preDurationSeconds;
	}

	public void setPreDurationSeconds(double preDurationSeconds) {
		this.preDurationSeconds = preDurationSeconds;
	}

	public long getJoinIncrLength() {
		return joinIncrLength;
	}

	public void setJoinIncrLength(long joinIncrLength) {
		this.joinIncrLength = joinIncrLength;
	}

	public int getLastTagTimestamp() {
		return lastTagTimestamp;
	}

	public void setLastTagTimestamp(int lastTagTimestamp) {
		this.lastTagTimestamp = lastTagTimestamp;
	}

	public int getPreLastTagTimestamp() {
		return preLastTagTimestamp;
	}

	public void setPreLastTagTimestamp(int preLastTagTimestamp) {
		this.preLastTagTimestamp = preLastTagTimestamp;
	}

}
