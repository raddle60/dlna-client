package com.raddle.dlna.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.InclusiveByteRange;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileHttpHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(LocalFileHttpHandler.class);
	private File localFile;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private ReceiveSpeedCallback speedCallback;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (target.startsWith("/file/") && localFile.exists()) {
			@SuppressWarnings("resource")
			FileResource resource = new FileResource(localFile.toURI());
			final long content_length = resource.length();
			Enumeration<String> reqRanges = request.getHeaders(HttpHeader.RANGE.asString());
			if (reqRanges != null && reqRanges.hasMoreElements()) {
				// Parse the satisfiable ranges
				List<InclusiveByteRange> ranges = InclusiveByteRange.satisfiableRanges(reqRanges, content_length);
				InclusiveByteRange singleSatisfiableRange = ranges.get(0);
				long singleLength = singleSatisfiableRange.getSize(content_length);
				response.setHeader(HttpHeader.CONTENT_LENGTH.asString(), Long.toString(singleLength));
				response.setHeader(HttpHeader.CONTENT_RANGE.asString(),
						singleSatisfiableRange.toHeaderRangeString(content_length));
				startReceive(0);
				int n = 0;
				InputStream input = resource.getInputStream();
				input.skip(singleSatisfiableRange.getFirst(content_length));
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				while (-1 != (n = input.read(buffer))) {
					response.getOutputStream().write(buffer, 0, n);
					received(0, n);
				}
				input.close();
				receivedComplete(0);
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			} else {
				int n = 0;
				InputStream input = resource.getInputStream();
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				while (-1 != (n = input.read(buffer))) {
					response.getOutputStream().write(buffer, 0, n);
					received(1, n);
				}
				input.close();
				receivedComplete(0);
				response.setHeader("Content-Length", "" + content_length);
				response.setStatus(HttpServletResponse.SC_OK);
			}
			response.setHeader("Content-Disposition", "attachment;filename=" + FilenameUtils.getName(target));
			response.setHeader(HttpHeader.ACCEPT_RANGES.asString(), "bytes");
			response.setContentType("application/octet-stream");
			baseRequest.setHandled(true);
		}
	}

	public File getLocalFile() {
		return localFile;
	}

	public void setLocalFile(File localFile) {
		this.localFile = localFile;
	}

	private void startReceive(int videoIndex) {
		if (speedCallback != null) {
			try {
				speedCallback.startReceive(videoIndex, 1);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void received(int videoIndex, long length) {
		if (speedCallback != null) {
			try {
				speedCallback.receivedBytes(videoIndex, 1, length);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void receivedComplete(int videoIndex) {
		if (speedCallback != null) {
			try {
				speedCallback.receivedComplete(videoIndex, 1);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public ReceiveSpeedCallback getSpeedCallback() {
		return speedCallback;
	}

	public void setSpeedCallback(ReceiveSpeedCallback speedCallback) {
		this.speedCallback = speedCallback;
	}

}
