package com.raddle.dlna.video.flv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

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
	private double preDurationSeconds;
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
		return (Double) scriptTagBody.getDuration().getValue();
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

	public static FlvMetaInfo readFlvMetaInfo(long fileLength, InputStream inputStream) throws IOException {
		FlvHeader readFlvHeader = FlvHeader.readFlvHeader(inputStream);
		TagHeader readTagHeaderForMeta = TagHeader.readTagHeader(inputStream);
		ScriptTagBody readScriptTagBody = ScriptTagBody.readScriptTagBody(readTagHeaderForMeta, inputStream);
		return new FlvMetaInfo(fileLength, readFlvHeader, readTagHeaderForMeta, readScriptTagBody);
	}

	/**
	 * 
	 * @param orgMetaInfo 读取未修改的信息
	 * @param joinMetaInfo 被修改的信息
	 * @return 返回合并信息
	 */
	public static FlvMetaInfo joinMetaInfo(List<FlvMetaInfo> orgMetaInfo, List<FlvMetaInfo> joinMetaInfo) {
		if (joinMetaInfo.size() == 1) {
			return joinMetaInfo.get(0);
		}
		FlvMetaInfo joinedMetaInfo = joinMetaInfo.get(0);
		joinedMetaInfo.getScriptTagBody().getDuration()
				.setValue(getCurrentDurationSeconds(orgMetaInfo, orgMetaInfo.size()));
		for (int i = 1; i < orgMetaInfo.size(); i++) {
			addFilepositionAndTime(orgMetaInfo, i, joinedMetaInfo, joinMetaInfo.get(i));
		}
		// 重算body长度
		ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
		try {
			joinedMetaInfo.getScriptTagBody().writeScriptTagBody(bodyOut);
		} catch (IOException e) {
		}
		// 设置body长度
		joinedMetaInfo.getScriptTagHeader().setDataLength(bodyOut.size() - 4);
		// 计算增加的长度
		int incr = joinedMetaInfo.getDataLength() - orgMetaInfo.get(0).getDataLength();
		for (int i = orgMetaInfo.get(0).getScriptTagBody().getFilepositions().size(); i < joinedMetaInfo
				.getScriptTagBody().getFilepositions().size(); i++) {
			ScriptDataDouble fileposition = joinedMetaInfo.getScriptTagBody().getFilepositions().get(i);
			double pos = (Double) fileposition.getValue();
			// 由于增加了文件2，体积变大了
			fileposition.setValue(pos + incr);
		}
		return joinedMetaInfo;
	}

	private static void addFilepositionAndTime(List<FlvMetaInfo> orgMetaInfo, int index, FlvMetaInfo joinedMetaInfo,
			FlvMetaInfo toJoinMetaInfo) {
		for (ScriptDataDouble fileposition : toJoinMetaInfo.getScriptTagBody().getFilepositions()) {
			double pos = (Double) fileposition.getValue();
			double newPos = pos - toJoinMetaInfo.getMetaInfoLength() + getCurrentTagPos(orgMetaInfo, index);
			toJoinMetaInfo.setPreFileLength(getCurrentTagPos(orgMetaInfo, index));
			joinedMetaInfo.getScriptTagBody().getFilepositions().add(new ScriptDataDouble(newPos));
		}
		for (ScriptDataDouble timeObj : toJoinMetaInfo.getScriptTagBody().getTimes()) {
			double time = (Double) timeObj.getValue();
			double newTime = time + getCurrentDurationSeconds(orgMetaInfo, index);
			toJoinMetaInfo.setPreDurationSeconds(getCurrentDurationSeconds(orgMetaInfo, index));
			joinedMetaInfo.getScriptTagBody().getTimes().add(new ScriptDataDouble(newTime));
		}
	}

	public static long getCurrentTagPos(List<FlvMetaInfo> orgMetaInfo, int index) {
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

	public static double getCurrentDurationSeconds(List<FlvMetaInfo> orgMetaInfo, int index) {
		double durationSeconds = 0;
		for (int i = 0; i < index; i++) {
			durationSeconds += orgMetaInfo.get(i).getDurationSeconds();
		}
		return durationSeconds;
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

}
