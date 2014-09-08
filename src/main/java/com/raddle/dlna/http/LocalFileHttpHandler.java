package com.raddle.dlna.http;

import java.io.File;
import java.io.IOException;
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

public class LocalFileHttpHandler extends AbstractHandler {
	private File localFile;

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
				resource.writeTo(response.getOutputStream(), singleSatisfiableRange.getFirst(content_length),
						singleLength);
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			} else {
				resource.writeTo(response.getOutputStream(), 0, content_length);
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

}
