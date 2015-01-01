package com.raddle.dlna.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.omg.CORBA.BooleanHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteHttpProxyHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(RemoteHttpProxyHandler.class);
	private List<String> urls;
	private BufferThread bufferThread;
	private static final long MAX_BUFFERED_SIZE = 1024 * 1024 * 10;//10M
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private ReceiveSpeedCallback speedCallback;

	@Override
	public void handle(final String target, Request baseRequest, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ServletException {
		if (target.startsWith("/remote/") && target.indexOf("join") == -1) {
			logger.info("received request : " + target);
			startBufferThread();
			Map<Object, Object> headers = new HashMap<Object, Object>();
			String range = request.getHeader(HttpHeader.RANGE.asString());
			if (range != null) {
				headers.put(HttpHeader.RANGE.asString(), range);
			}
			final int videoIndex = Integer.parseInt(target.substring("/remote/".length()));
			final byte[] bufferedBytes = bufferThread.out.toByteArray();
			final int bufferedVideoIndex = bufferThread.bufferedVideoIndex;
			final BooleanHolder isSetHeader = new BooleanHolder(false);
			startReceive(0);
			if (bufferedVideoIndex == videoIndex) {
				if (range == null || (range != null && range.indexOf("0-") != -1)) {
					for (Map.Entry<String, String> entry : bufferThread.headers.entrySet()) {
						if (StringUtils.isNotEmpty(entry.getValue())) {
							response.setHeader(entry.getKey(), entry.getValue());
						}
					}
					response.setStatus(bufferThread.statusCode);
					isSetHeader.value = true;
					logger.info("read buffer data " + videoIndex);
					int n = 0;
					InputStream input = new ByteArrayInputStream(bufferedBytes);
					byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
					while (-1 != (n = input.read(buffer))) {
						response.getOutputStream().write(buffer, 0, n);
						received(videoIndex, n);
					}
					input.close();
					logger.info("write buffer data complete " + videoIndex);
					// 跳过缓冲区中的数据
					headers.put(HttpHeader.RANGE.asString(),
							StringUtils.replace(range, "0-", bufferedBytes.length + "-"));
				}
			}
			if (videoIndex < urls.size() - 1) {
				bufferThread.bufferVideo(videoIndex + 1);
			}
			logger.info("request : " + target + " remote server " + urls.get(videoIndex));
			HttpHelper.getRemotePageWithCallback(urls.get(videoIndex), headers, new HttpCallback() {

				@Override
				public Object httpResponse(CloseableHttpResponse remoteResponse) {
					if (!isSetHeader.value) {
						copyHeader(HttpHeader.CONTENT_LENGTH, remoteResponse, response);
						copyHeader(HttpHeader.CONTENT_RANGE, remoteResponse, response);
						copyHeader(HttpHeader.ACCEPT_RANGES, remoteResponse, response);
						copyHeader(HttpHeader.CONTENT_TYPE, remoteResponse, response);
						response.setStatus(remoteResponse.getStatusLine().getStatusCode());
					}
					try {
						logger.info("request : " + target + " start copy");
						int n = 0;
						InputStream input = remoteResponse.getEntity().getContent();
						byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
						while (-1 != (n = input.read(buffer))) {
							response.getOutputStream().write(buffer, 0, n);
							received(videoIndex, n);
						}
						input.close();
						logger.info("request : " + target + " copy finished");
					} catch (Exception e) {
						logger.info("request : " + target + " force closed");
					}
					receivedComplete(videoIndex);
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

	private synchronized void startBufferThread() {
		if (bufferThread == null) {
			bufferThread = new BufferThread();
			bufferThread.setName("buffer-next-video-thread");
			bufferThread.setDaemon(true);
			bufferThread.start();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
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
				speedCallback.receivedBytes(videoIndex, urls.size(), length);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void receivedComplete(int videoIndex) {
		if (speedCallback != null) {
			try {
				speedCallback.receivedComplete(videoIndex, urls.size());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private class BufferThread extends Thread {
		private volatile int bufferedVideoIndex = -1;
		private volatile boolean isBuffering = false;
		private volatile boolean watingStop = false;
		private ByteArrayOutputStream out = new ByteArrayOutputStream();
		private Map<String, String> headers = new HashMap<String, String>();
		private int statusCode = -1;

		private void bufferVideo(int videoIndex) {
			if (bufferedVideoIndex == videoIndex) {
				return;
			}
			while (isBuffering) {
				watingStop = true;
				try {
					sleep(10);
				} catch (InterruptedException e) {
				}
			}
			try {
				sleep(10);// 等待调用wait
			} catch (InterruptedException e) {
			}
			bufferedVideoIndex = videoIndex;
			watingStop = false;
			synchronized (bufferThread) {
				bufferThread.notify();
			}
		}

		@Override
		public void run() {
			logger.info("buffer thread started");
			while (true) {
				synchronized (bufferThread) {
					try {
						logger.info("wating to buffer");
						bufferThread.wait();
					} catch (InterruptedException e) {
						return;
					}
				}
				isBuffering = true;
				out.reset();
				headers.clear();
				logger.info("start buffer " + bufferedVideoIndex);
				try {
					HttpHelper.getRemotePageWithCallback(urls.get(bufferedVideoIndex), null, new HttpCallback() {

						@Override
						public Object httpResponse(CloseableHttpResponse remoteResponse) {
							if (watingStop) {
								logger.info("buffer stoppd " + bufferedVideoIndex);
								isBuffering = false;
								return null;
							}
							copyHeader(HttpHeader.CONTENT_LENGTH, remoteResponse);
							copyHeader(HttpHeader.CONTENT_RANGE, remoteResponse);
							copyHeader(HttpHeader.ACCEPT_RANGES, remoteResponse);
							copyHeader(HttpHeader.CONTENT_TYPE, remoteResponse);
							statusCode = remoteResponse.getStatusLine().getStatusCode();
							try {
								int n = 0;
								InputStream input = remoteResponse.getEntity().getContent();
								byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
								while (-1 != (n = input.read(buffer))) {
									out.write(buffer, 0, n);
									if (out.size() >= MAX_BUFFERED_SIZE || watingStop) {
										break;
									}
								}
								input.close();
							} catch (Exception e) {
								logger.info("buffer exception " + bufferedVideoIndex);
								logger.error(e.getMessage(), e);
								try {
									Thread.sleep(1000); // 防止IO错误，频繁的异常
								} catch (InterruptedException e1) {
									return null;
								}
							}
							logger.info("buffer complete " + bufferedVideoIndex);
							return null;
						}

						private void copyHeader(HttpHeader httpHeader, CloseableHttpResponse remoteResponse) {
							if (remoteResponse.getFirstHeader(httpHeader.asString()) != null) {
								headers.put(httpHeader.asString(), remoteResponse.getFirstHeader(httpHeader.asString())
										.getValue());
							}
						}
					});
					isBuffering = false;
				} catch (Throwable e) {
					isBuffering = false;
					logger.info("buffer exception " + bufferedVideoIndex);
					logger.error(e.getMessage(), e);
					try {
						Thread.sleep(1000); // 防止url错误，频繁的异常
					} catch (InterruptedException e1) {
						return;
					}
				}
			}
		}
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public ReceiveSpeedCallback getSpeedCallback() {
		return speedCallback;
	}

	public void setSpeedCallback(ReceiveSpeedCallback speedCallback) {
		this.speedCallback = speedCallback;
	}
}
