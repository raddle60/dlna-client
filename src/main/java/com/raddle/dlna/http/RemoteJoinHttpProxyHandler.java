package com.raddle.dlna.http;

import java.io.IOException;
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
import org.omg.CORBA.LongHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raddle.dlna.util.MatchedLine;
import com.raddle.dlna.util.RegexUtils;

public class RemoteJoinHttpProxyHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(RemoteJoinHttpProxyHandler.class);
	private List<String> urls;
	private List<Long> contentLengths;

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
			if (search.size() > 0) {
				MatchedLine matchedLine = search.get(0);
				long parseLong = Long.parseLong(matchedLine.getGroup(1));
				requestStart.value = parseLong;
				videoIndex = getIndexOfLength(Long.parseLong(matchedLine.getGroup(1)));
				String requestRange = (parseLong - getSubLengthTo(videoIndex - 1)) + "-";
				headers.put(HttpHeader.RANGE.asString(),
						StringUtils.replace(range, matchedLine.getMatchedString(), requestRange));
				logger.info("request : " + target + " " + videoIndex + " , received range : "
						+ matchedLine.getMatchedString() + ", request range : " + requestRange);
			}
			logger.info("request : " + target + " " + videoIndex + " remote server : " + urls.get(videoIndex));
			HttpHelper.getRemotePageWithCallback(urls.get(videoIndex), headers, new HttpCallback() {

				@Override
				public Object httpResponse(CloseableHttpResponse remoteResponse) {
					copyHeader(HttpHeader.CONTENT_LENGTH, remoteResponse, response);
					copyHeader(HttpHeader.ACCEPT_RANGES, remoteResponse, response);
					copyHeader(HttpHeader.CONTENT_TYPE, remoteResponse, response);
					long length = Long.parseLong(remoteResponse.getFirstHeader(HttpHeader.CONTENT_LENGTH.asString())
							.getValue());
					String reponseRange = "bytes " + requestStart.value + "-" + (requestStart.value + length) + "/"
							+ getTotalLength();
					logger.info("received response ,status : " + remoteResponse.getStatusLine().getStatusCode()
							+ ", length : " + length + ", range : " + reponseRange);
					response.setHeader(HttpHeader.CONTENT_RANGE.asString(), reponseRange);
					response.setStatus(206);
					try {
						logger.info("request : " + target + " start copy");
						IOUtils.copy(remoteResponse.getEntity().getContent(), response.getOutputStream());
						logger.info("request : " + target + " copy finished , byte : " + length + ", range : "
								+ reponseRange);
					} catch (IOException e) {
						logger.info("request : " + target + " force closed");
					} catch (Exception e) {
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
			baseRequest.setHandled(true);
		}
	}

	private long getTotalLength() {
		long length = 0;
		for (Long long1 : contentLengths) {
			length += long1;
		}
		return length;
	}

	private long getSubLengthTo(int i) {
		long length = 0;
		for (int j = 0; j <= i; j++) {
			length += contentLengths.get(j);
		}
		return length;
	}

	private int getIndexOfLength(long length) {
		long subLength = 0;
		for (int j = 0; j < contentLengths.size(); j++) {
			subLength += contentLengths.get(j);
			if (subLength > length) {
				return j;
			}
		}
		return 0;
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public List<Long> getContentLengths() {
		return contentLengths;
	}

	public void setContentLengths(List<Long> contentLengths) {
		this.contentLengths = contentLengths;
	}

}
