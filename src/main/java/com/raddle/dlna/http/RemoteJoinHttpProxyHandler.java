package com.raddle.dlna.http;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.LongHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raddle.dlna.http.join.JoinItem;
import com.raddle.dlna.util.MatchedLine;
import com.raddle.dlna.util.RegexUtils;
import com.raddle.dlna.video.flv.FlvMetaInfo;
import com.raddle.dlna.video.flv.tag.TagHeader;
import com.raddle.dlna.video.flv.tag.script.ScriptDataDouble;

public class RemoteJoinHttpProxyHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(RemoteJoinHttpProxyHandler.class);
	private List<JoinItem> joinItems;

	@Override
	public void handle(final String target, Request baseRequest, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ServletException {
		if (target.startsWith("/remote/join")) {
			logger.info("received request : " + target);
			Map<Object, Object> headers = new HashMap<Object, Object>();
			String range = request.getHeader(HttpHeader.RANGE.asString());
			int videoIndex = 0;
			List<MatchedLine> search = RegexUtils.search("(\\d+)-", range);
			final LongHolder requestStart = new LongHolder();
			MatchedLine matchedLine = search.get(0);
			long parseLong = Long.parseLong(matchedLine.getGroup(1));
			boolean hasKeyFrame = false;
			// 查找最接近的关键帧
			List<ScriptDataDouble> filepositions = joinItems.get(0).getFlvMetaInfo().getScriptTagBody()
					.getFilepositions();
			for (int j = filepositions.size() - 1; j > 0; j--) {
				double keyFramePos = filepositions.get(j).getValue();
				if (keyFramePos <= parseLong) {
					parseLong = (long) keyFramePos;
					hasKeyFrame = true;
					break;
				}
			}
			if (!hasKeyFrame) {
				parseLong = 0;
			}
			videoIndex = getIndexOfLength(parseLong);
			requestStart.value = parseLong;
			// 跳过信息头
			long subVideoStart = parseLong - joinItems.get(videoIndex).getFlvMetaInfo().getPreFileLength();
			if (videoIndex == 0) {
				// 0的时候，不用跳过，需要重写flv头
				if (parseLong > 0) {
					// 第一段由于增加了后面几段的关键帧索引，长度变长了，所以要减掉偏移量
					subVideoStart -= joinItems.get(videoIndex).getFlvMetaInfo().getJoinIncrLength();
				}
			} else if (videoIndex > 0) {
				// 除了第一段，都要加上头信息长度
				// 关键帧在拼接时，长度都被跳过了
				subVideoStart += joinItems.get(videoIndex).getFlvMetaInfo().getMetaInfoLength();
			}
			String requestRange = subVideoStart + "-";
			headers.put(HttpHeader.RANGE.asString(),
					StringUtils.replace(range, matchedLine.getMatchedString(), requestRange));
			logger.info("request : " + target + " " + videoIndex + " , received range : "
					+ matchedLine.getMatchedString() + ", request range : " + requestRange);
			logger.info("request : " + target + " " + videoIndex + " , remote server : "
					+ joinItems.get(videoIndex).getUrl());
			final JoinItem curJoinItem = joinItems.get(videoIndex);
			String reponseRange = "bytes " + requestStart.value + "-";
			response.setHeader(HttpHeader.CONTENT_RANGE.asString(), reponseRange);
			long returnLength = getTotalLength() - parseLong;
			response.setHeader(HttpHeader.CONTENT_LENGTH.asString(), returnLength + "");
			logger.info("return response , range : " + reponseRange + ", length : " + returnLength);
			response.setStatus(206);
			final BooleanHolder hasError = new BooleanHolder(false);
			HttpHelper.getRemotePageWithCallback(joinItems.get(videoIndex).getUrl(), headers, new HttpCallback() {

				@Override
				public Object httpResponse(CloseableHttpResponse remoteResponse) {
					copyHeader(HttpHeader.ACCEPT_RANGES, remoteResponse, response);
					copyHeader(HttpHeader.CONTENT_TYPE, remoteResponse, response);
					long length = Long.parseLong(remoteResponse.getFirstHeader(HttpHeader.CONTENT_LENGTH.asString())
							.getValue());
					logger.info("received response ,status : " + remoteResponse.getStatusLine().getStatusCode()
							+ ", length : " + length);
					long receivedLength = 0;
					try {
						logger.info("request : " + target + " start copy");
						if (requestStart.value == 0) {
							// 第一个，跳过原来的头信息
							FlvMetaInfo readFlvMetaInfo = FlvMetaInfo.readFlvMetaInfo(0, remoteResponse.getEntity()
									.getContent());
							receivedLength += readFlvMetaInfo.getMetaInfoLength();
							// 写入合并好的头信息
							joinItems.get(0).getFlvMetaInfo().writeFlvMetaInfo(response.getOutputStream());
						}
						// 修改时间戳
						TagHeader readTagHeader = TagHeader.readTagHeader(remoteResponse.getEntity().getContent());
						receivedLength += 11;
						while (readTagHeader != null) {
							if (readTagHeader.getTagType() == 18) {
								// 跳过
								IOUtils.skip(remoteResponse.getEntity().getContent(), readTagHeader.getDataLength() + 4);
								receivedLength += (readTagHeader.getDataLength() + 4);
								readTagHeader = TagHeader.readTagHeader(remoteResponse.getEntity().getContent());
								continue;
							}
							if (readTagHeader.getTagType() != 8 && readTagHeader.getTagType() != 9) {
								logger.error(readTagHeader.getTagType() + " is not video or audio type");
								throw new RuntimeException(readTagHeader.getTagType() + " is not video or audio type");
							}
							readTagHeader
									.setTimestamp((int) (curJoinItem.getFlvMetaInfo().getPreDurationSeconds() * 1000)
											+ readTagHeader.getTimestamp());
							readTagHeader.writeTagHeader(response.getOutputStream());
							IOUtils.copyLarge(remoteResponse.getEntity().getContent(), response.getOutputStream(), 0,
									readTagHeader.getDataLength() + 4);
							receivedLength += (readTagHeader.getDataLength() + 4);
							readTagHeader = TagHeader.readTagHeader(remoteResponse.getEntity().getContent());
						}
						logger.info("request : " + target + " copy finished , byte : " + receivedLength);
					} catch (IOException e) {
						hasError.value = true;
						logger.info("request : " + target + " force closed");
					} catch (BufferOverflowException e) {
						hasError.value = true;
						logger.info("request : " + target + " force closed");
					} catch (Exception e) {
						hasError.value = true;
						logger.info("request : " + target + " error , " + e.getMessage(), e);
					}
					return null;
				}

				private void copyHeader(HttpHeader httpHeader, CloseableHttpResponse remoteResponse,
						HttpServletResponse response) {
					if (remoteResponse.getFirstHeader(httpHeader.asString()) != null) {
						response.setHeader(httpHeader.asString(), remoteResponse.getFirstHeader(httpHeader.asString())
								.getValue());
					}
				}
			});
			// 写入后面的
			while (videoIndex < joinItems.size() - 1 && !hasError.value) {
				videoIndex++;
				Map<Object, Object> nextHeaders = new HashMap<Object, Object>();
				// 跳过信息头
				long nextVideoStart = joinItems.get(videoIndex).getFlvMetaInfo().getMetaInfoLength();
				String nextRequestRange = nextVideoStart + "-";
				nextHeaders.put(HttpHeader.RANGE.asString(),
						StringUtils.replace(range, matchedLine.getMatchedString(), nextRequestRange));
				logger.info("request : " + target + " " + videoIndex + ", request range : " + nextRequestRange);
				logger.info("request : " + target + " " + videoIndex + " , remote server : "
						+ joinItems.get(videoIndex).getUrl());
				final JoinItem nextJoinItem = joinItems.get(videoIndex);
				HttpHelper.getRemotePageWithCallback(joinItems.get(videoIndex).getUrl(), nextHeaders,
						new HttpCallback() {

							@Override
							public Object httpResponse(CloseableHttpResponse remoteResponse) {
								long length = Long.parseLong(remoteResponse.getFirstHeader(
										HttpHeader.CONTENT_LENGTH.asString()).getValue());
								logger.info("received response ,status : "
										+ remoteResponse.getStatusLine().getStatusCode() + ", length : " + length);
								long receivedLength = 0;
								try {
									logger.info("request : " + target + " start copy");
									// 修改时间戳
									TagHeader readTagHeader = TagHeader.readTagHeader(remoteResponse.getEntity()
											.getContent());
									receivedLength += 11;
									while (readTagHeader != null) {
										if (readTagHeader.getTagType() == 18) {
											// 跳过
											IOUtils.skip(remoteResponse.getEntity().getContent(),
													readTagHeader.getDataLength() + 4);
											receivedLength += (readTagHeader.getDataLength() + 4);
											readTagHeader = TagHeader.readTagHeader(remoteResponse.getEntity()
													.getContent());
											continue;
										}
										if (readTagHeader.getTagType() != 8 && readTagHeader.getTagType() != 9) {
											logger.error(readTagHeader.getTagType() + " is not video or audio type");
											throw new RuntimeException(readTagHeader.getTagType()
													+ " is not video or audio type");
										}
										readTagHeader.setTimestamp((int) (nextJoinItem.getFlvMetaInfo()
												.getPreDurationSeconds() * 1000) + readTagHeader.getTimestamp());
										readTagHeader.writeTagHeader(response.getOutputStream());
										IOUtils.copyLarge(remoteResponse.getEntity().getContent(),
												response.getOutputStream(), 0, readTagHeader.getDataLength() + 4);
										receivedLength += (readTagHeader.getDataLength() + 4);
										readTagHeader = TagHeader
												.readTagHeader(remoteResponse.getEntity().getContent());
									}
									logger.info("request : " + target + " copy finished , byte : " + receivedLength);
								} catch (IOException e) {
									hasError.value = true;
									logger.info("request : " + target + " force closed");
								} catch (BufferOverflowException e) {
									hasError.value = true;
									logger.info("request : " + target + " force closed");
								} catch (Exception e) {
									hasError.value = true;
									logger.info("request : " + target + " error , " + e.getMessage(), e);
								}
								return null;
							}
						});
			}
			baseRequest.setHandled(true);
		}
	}

	private long getTotalLength() {
		JoinItem lastItem = joinItems.get(joinItems.size() - 1);
		return lastItem.getFlvMetaInfo().getPreFileLength() + lastItem.getFlvMetaInfo().getFileLengthNoMeta();
	}

	private int getIndexOfLength(long length) {
		for (int j = joinItems.size() - 1; j > 0; j--) {
			if (joinItems.get(j).getFlvMetaInfo().getPreFileLength() <= length) {
				return j;
			}
		}
		return 0;
	}

	public List<JoinItem> getJoinItems() {
		return joinItems;
	}

	public void setJoinItems(List<JoinItem> joinItems) {
		this.joinItems = joinItems;
	}

}
