package com.raddle.dlna.swing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.event.EventListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raddle.dlna.ctrl.ActionHelper;
import com.raddle.dlna.event.DlnaEventParser;
import com.raddle.dlna.http.HttpHelper;
import com.raddle.dlna.http.LocalFileHttpHandler;
import com.raddle.dlna.http.ReceiveSpeedCallback;
import com.raddle.dlna.http.RemoteHttpProxyHandler;
import com.raddle.dlna.http.RemoteJoinHttpProxyHandler;
import com.raddle.dlna.http.join.JoinItem;
import com.raddle.dlna.renderer.AVTransport;
import com.raddle.dlna.renderer.MediaRenderer;
import com.raddle.dlna.url.parser.VideoInfo;
import com.raddle.dlna.url.parser.VideoUrlParser;
import com.raddle.dlna.util.ByteUtils;
import com.raddle.dlna.util.DurationUtils;
import com.raddle.dlna.util.KeyValue;
import com.raddle.dlna.util.LocalIpUtils;

public class DlnaClientSwing {
	private static final int HTTP_SERVER_PORT = 12173;

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
	}
	private static Logger logger = LoggerFactory.getLogger(DlnaClientSwing.class);
	///
	private ControlPoint ctrlPoint;
	private Server server;
	private DlnaEventParser dlnaEventParser;
	private List<VideoUrlParser> videoUrlParsers;
	private List<PlayListItem> playList;
	private int curVideoIndex = 0;
	private boolean paused = false;
	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
	private int curMousePos = 0;
	private int quickSyncCount = 0;
	private boolean hasPlaying = false;
	private Date urlParseTime = null;
	private LocalFileHttpHandler localFileHttpHandler = new LocalFileHttpHandler();
	private RemoteHttpProxyHandler httpBufferProxyHandler = new RemoteHttpProxyHandler();
	private RemoteJoinHttpProxyHandler httpJoinProxyHandler = new RemoteJoinHttpProxyHandler();
	private String curUrl = null;
	private Date lastStoppedEventTime = new Date();
	/**
	 * 是否在拖动进度条
	 */
	private boolean isDragSplit = false;
	///
	private JFrame frame;
	private JTextField urlTxt;
	private JComboBox deviceComb;
	private JComboBox addrParseComb;
	private JComboBox qualityComb;
	private JButton deviceRefreshBtn;
	private JButton previousBtn;
	private JButton nextBtn;
	private JButton stopBtn;
	private JSlider progressSlid;
	private JLabel durationLeb;
	private JLabel curDurationLeb;
	private JLabel mouseDurationLeb;
	private JLabel label_5;
	private JButton pauseBtn;
	private JSpinner spinner;
	private JButton pasteBtn;
	private JButton parserRefreshBtn;
	private JLabel lblip;
	private JComboBox localIpComb;
	private JButton playBtn2;
	private JButton stopBtn2;
	private JCheckBox localBufChk;
	private JCheckBox localJoinChk;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					DlnaClientSwing window = new DlnaClientSwing();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DlnaClientSwing() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				close();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}

			private void close() {
				if (ctrlPoint != null) {
					ctrlPoint.unsubscribe();
					ctrlPoint.stop();
				}
				if (server != null) {
					try {
						server.stop();
					} catch (Exception e) {
					}
				}
				HttpHelper.close();
				scheduledExecutorService.shutdown();
				logger.info("DlnaClient closed");
			}
		});
		frame.setBounds(100, 100, 793, 286);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblDlna = new JLabel("DLNA设备");
		lblDlna.setBounds(10, 10, 61, 15);
		frame.getContentPane().add(lblDlna);

		deviceComb = new JComboBox();
		deviceComb.setBounds(72, 7, 200, 21);
		frame.getContentPane().add(deviceComb);

		JLabel label = new JLabel("视频地址");
		label.setBounds(10, 63, 54, 15);
		frame.getContentPane().add(label);

		urlTxt = new JTextField();
		urlTxt.setToolTipText("右键粘帖");
		urlTxt.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					pasteUrlText();
				}
			}
		});
		urlTxt.setBounds(72, 63, 388, 21);
		frame.getContentPane().add(urlTxt);
		urlTxt.setColumns(10);

		JButton playBtn = new JButton("播放");
		playBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playBtnAction();
			}
		});
		playBtn.setBounds(72, 181, 61, 23);
		frame.getContentPane().add(playBtn);

		deviceRefreshBtn = new JButton("启动中");
		deviceRefreshBtn.setEnabled(false);
		deviceRefreshBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ctrlPoint.search();
			}
		});
		deviceRefreshBtn.setBounds(282, 6, 93, 23);
		frame.getContentPane().add(deviceRefreshBtn);

		progressSlid = new JSlider();
		progressSlid.setMaximum(0);
		progressSlid.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!isDragSplit) {
					updateMouseSpiltValue(false, e);
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				updateMouseSpiltValue(true, e);
			}
		});
		progressSlid.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				isDragSplit = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// seek
				Device selectedDevice = getSelectedDevice();
				if (progressSlid.isEnabled() && selectedDevice != null && playList != null && playList.size() > 0) {
					ActionHelper actionHelper = new ActionHelper(selectedDevice);
					actionHelper.seak(curMousePos);
					if (progressSlid.getValue() != curMousePos) {
						progressSlid.setValue(curMousePos);
					}
					quickSyncCount = 5;
				}
				isDragSplit = false;
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				updateMouseSpiltValue(false, e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouseDurationLeb.setText("");
				mouseDurationLeb.setVisible(false);
			}
		});
		progressSlid.setValue(0);
		progressSlid.setEnabled(false);
		progressSlid.setBounds(72, 88, 599, 23);
		frame.getContentPane().add(progressSlid);

		JLabel label_1 = new JLabel("播放进度");
		label_1.setBounds(10, 88, 54, 15);
		frame.getContentPane().add(label_1);

		JLabel label_2 = new JLabel("地址转换");
		label_2.setBounds(10, 35, 54, 15);
		frame.getContentPane().add(label_2);

		addrParseComb = new JComboBox();
		addrParseComb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					VideoUrlParser selectedParser = getSelectedParser();
					qualityComb.removeAllItems();
					for (KeyValue<String, String> keyValue : selectedParser.getVideoQualitys()) {
						qualityComb.addItem(keyValue.getValue());
					}
				}
			}
		});
		addrParseComb.setBounds(72, 35, 200, 21);
		frame.getContentPane().add(addrParseComb);

		JLabel label_3 = new JLabel("清晰度");
		label_3.setBounds(282, 39, 54, 15);
		frame.getContentPane().add(label_3);

		qualityComb = new JComboBox();
		qualityComb.setBounds(334, 35, 126, 21);
		frame.getContentPane().add(qualityComb);

		pauseBtn = new JButton("暂停");
		pauseBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Device selectedDevice = getSelectedDevice();
				if (selectedDevice != null) {
					ActionHelper actionHelper = new ActionHelper(selectedDevice);
					if (paused) {
						actionHelper.resume();
						pauseBtn.setText("暂停");
						paused = false;
					} else {
						actionHelper.pause();
						pauseBtn.setText("继续");
						paused = true;
					}
				}
			}
		});
		pauseBtn.setBounds(233, 150, 61, 23);
		frame.getContentPane().add(pauseBtn);

		stopBtn = new JButton("停止");
		stopBtn.setEnabled(false);
		stopBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopBtnAction();
			}
		});
		stopBtn.setBounds(316, 181, 61, 23);
		frame.getContentPane().add(stopBtn);

		previousBtn = new JButton("上一个");
		previousBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (playList != null && playList.size() > 0 && curVideoIndex > 0) {
					play(curVideoIndex - 1);
				}
			}
		});
		previousBtn.setBounds(143, 181, 78, 23);
		frame.getContentPane().add(previousBtn);

		nextBtn = new JButton("下一个");
		nextBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (playList != null && playList.size() > 0 && curVideoIndex < playList.size() - 1) {
					play(curVideoIndex + 1);
				}
			}
		});
		nextBtn.setBounds(233, 181, 73, 23);
		frame.getContentPane().add(nextBtn);

		durationLeb = new JLabel("");
		durationLeb.setBounds(674, 88, 93, 21);
		frame.getContentPane().add(durationLeb);

		JLabel label_4 = new JLabel("当前进度");
		label_4.setBounds(72, 129, 55, 15);
		frame.getContentPane().add(label_4);

		curDurationLeb = new JLabel("");
		curDurationLeb.setBounds(137, 129, 240, 15);
		frame.getContentPane().add(curDurationLeb);

		mouseDurationLeb = new JLabel("");
		mouseDurationLeb.setBounds(392, 114, 118, 15);
		frame.getContentPane().add(mouseDurationLeb);

		label_5 = new JLabel("步长");
		label_5.setBounds(72, 154, 36, 15);
		frame.getContentPane().add(label_5);

		spinner = new JSpinner();
		spinner.setBounds(104, 149, 36, 22);
		spinner.setValue(5);
		frame.getContentPane().add(spinner);

		JLabel label_6 = new JLabel("秒");
		label_6.setBounds(143, 154, 23, 15);
		frame.getContentPane().add(label_6);

		JButton quickForwardBtn = new JButton("快进");
		quickForwardBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Device selectedDevice = getSelectedDevice();
				if (progressSlid.isEnabled() && selectedDevice != null && playList != null && playList.size() > 0) {
					ActionHelper actionHelper = new ActionHelper(selectedDevice);
					int seekPos = progressSlid.getValue() + (Integer) spinner.getValue();
					actionHelper.seak(seekPos);
					progressSlid.setValue(seekPos);
					quickSyncCount = 5;
				}
			}
		});
		quickForwardBtn.setBounds(304, 150, 61, 23);
		frame.getContentPane().add(quickForwardBtn);

		JButton quickBackBtn = new JButton("快退");
		quickBackBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Device selectedDevice = getSelectedDevice();
				if (progressSlid.isEnabled() && selectedDevice != null && playList != null && playList.size() > 0) {
					ActionHelper actionHelper = new ActionHelper(selectedDevice);
					int seekPos = progressSlid.getValue() - (Integer) spinner.getValue();
					actionHelper.seak(seekPos);
					progressSlid.setValue(seekPos);
					quickSyncCount = 5;
				}
			}
		});
		quickBackBtn.setBounds(165, 150, 61, 23);
		frame.getContentPane().add(quickBackBtn);

		pasteBtn = new JButton("粘帖");
		pasteBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteUrlText();
			}
		});
		pasteBtn.setBounds(470, 62, 61, 23);
		frame.getContentPane().add(pasteBtn);

		parserRefreshBtn = new JButton("刷新");
		parserRefreshBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateUrlParsers();
			}
		});
		parserRefreshBtn.setBounds(470, 34, 93, 23);
		frame.getContentPane().add(parserRefreshBtn);

		lblip = new JLabel("本机IP");
		lblip.setBounds(72, 214, 47, 15);
		frame.getContentPane().add(lblip);

		localIpComb = new JComboBox();
		localIpComb.setBounds(134, 211, 118, 21);
		frame.getContentPane().add(localIpComb);

		JLabel label_7 = new JLabel("用于本地文件推送");
		label_7.setBounds(262, 214, 113, 15);
		frame.getContentPane().add(label_7);

		playBtn2 = new JButton("播放");
		playBtn2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playBtnAction();
			}
		});
		playBtn2.setBounds(534, 62, 61, 23);
		frame.getContentPane().add(playBtn2);

		stopBtn2 = new JButton("停止");
		stopBtn2.setEnabled(false);
		stopBtn2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopBtnAction();
			}
		});
		stopBtn2.setBounds(598, 62, 61, 23);
		frame.getContentPane().add(stopBtn2);

		localBufChk = new JCheckBox("本地缓冲");
		localBufChk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!localBufChk.isSelected()) {
					localJoinChk.setSelected(false);
				}
			}
		});
		localBufChk.setBounds(392, 6, 93, 23);
		frame.getContentPane().add(localBufChk);

		localJoinChk = new JCheckBox("本地拼接");
		localJoinChk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				localBufChk.setSelected(localJoinChk.isSelected());
			}
		});
		localJoinChk.setBounds(492, 6, 132, 23);
		frame.getContentPane().add(localJoinChk);
		///
		dlnaEventParser = new DlnaEventParser();
		dlnaEventParser.init(new File("dlna/event.js"));
		///
		updateUrlParsers();
		///
		localIpComb.removeAllItems();
		for (String ip : LocalIpUtils.getLocalIpv4()) {
			localIpComb.addItem(ip);
		}
		//
		ctrlPoint = new ControlPoint();
		ctrlPoint.addNotifyListener(new NotifyListener() {

			@Override
			public void deviceNotifyReceived(SSDPPacket ssdpPacket) {
				updateDeviceComboList();
			}
		});
		ctrlPoint.addSearchResponseListener(new SearchResponseListener() {

			@Override
			public void deviceSearchResponseReceived(SSDPPacket ssdpPacket) {
				updateDeviceComboList();
			}
		});
		ctrlPoint.addEventListener(new EventListener() {

			@Override
			public void eventNotifyReceived(String uuid, long seq, String varName, String value) {
				if (value != null
						&& (value.indexOf("RelativeTimePosition") != -1 || value.indexOf("AbsoluteTimePosition") != -1)) {
				} else {
					logger.info("eventNotifyReceived , uuid : " + uuid + ",seq : " + seq + ",varName : " + varName
							+ ",value : " + value);
				}

				if (dlnaEventParser.isSupportedEvent(getSelectedDevice().getFriendlyName())) {
					if (AVTransport.PLAYING.equals(dlnaEventParser.parseEvent(getSelectedDevice().getFriendlyName(),
							varName, value))) {
						pauseBtn.setText("暂停");
						paused = false;
						hasPlaying = true;
						progressSlid.setEnabled(true);
					} else if (AVTransport.PAUSED_PLAYBACK.equals(dlnaEventParser.parseEvent(getSelectedDevice()
							.getFriendlyName(), varName, value))) {
						pauseBtn.setText("继续");
						paused = true;
					} else if (AVTransport.STOPPED.equals(dlnaEventParser.parseEvent(getSelectedDevice()
							.getFriendlyName(), varName, value))) {
						// 不是手动触发的，服务端回调的
						if (stopBtn.isEnabled()) {
							if (playList != null && curVideoIndex < playList.size() - 1) {
								// 有时候会重复通知，一旦翻页，5秒内不再翻页
								if (DateUtils.addSeconds(lastStoppedEventTime, 30).before(new Date())) {
									lastStoppedEventTime = new Date();
									logger.info("auto play next video by stopped event , curVideoIndex : "
											+ curVideoIndex);
									play(curVideoIndex + 1);
								}
							}
						}
					}
				}
			}
		});
		// 启动
		new Thread() {

			@Override
			public void run() {
				// 启动dlna远程控制
				ctrlPoint.start();
				ctrlPoint.search();
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						deviceRefreshBtn.setText("刷新");
						deviceRefreshBtn.setEnabled(true);
					}
				});
				// 启动播放进度同步
				scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

					@Override
					public void run() {
						if (progressSlid.getMaximum() > 0 && progressSlid.getValue() > progressSlid.getMaximum() - 20) {
							// 减少服务器端负担
							// 接近尾部，事件驱动不需要同步时间
							// 非事件驱动，靠高频率进度同步
							return;
						}
						syncPositionInfo();
					}
				}, 5, 10, TimeUnit.SECONDS);
				// 启动高频率进度同步
				scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

					@Override
					public void run() {
						if (stopBtn.isEnabled() && progressSlid.isEnabled() && !isDragSplit && !paused
								&& !dlnaEventParser.isSupportedEvent(getSelectedDevice().getFriendlyName())) {
							// 减少服务器端负担,接近尾部不同步进度，防止影响切换
							if (quickSyncCount > 0 && progressSlid.getValue() < progressSlid.getMaximum() - 10) {
								quickSyncCount--;
								syncPositionInfo();
								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										showCurrentPos();
									}
								});
							}
						}
					}
				}, 1, 1, TimeUnit.SECONDS);
				// 启动进度自动增加
				scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

					@Override
					public void run() {
						if (stopBtn.isEnabled() && progressSlid.isEnabled() && !isDragSplit && !paused && hasPlaying) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									progressSlid.setValue(progressSlid.getValue() + 1);
									if (progressSlid.getValue() > progressSlid.getMaximum() - 30 && playList != null
											&& curVideoIndex < playList.size() - 1) {
										// 快接近尾部了，需要自动播放下一个
										// 为了防止服务端拖动，增加同步频率
										quickSyncCount = 10;
									}
									showCurrentPos();
									// 自动播放下一个
									// 有事件通知的，在事件那播放下一个
									if ((progressSlid.getValue() == 0 || progressSlid.getValue() >= progressSlid
											.getMaximum())
											&& playList != null
											&& curVideoIndex < playList.size() - 1
											&& hasPlaying
											&& !dlnaEventParser.isSupportedEvent(getSelectedDevice().getFriendlyName())) {
										if (progressSlid.getValue() >= progressSlid.getMaximum()) {
											try {
												// 可能刚刚到最后一秒的开始
												Thread.sleep(500);
											} catch (InterruptedException e) {
												return;
											}
										}
										logger.info("auto play next video by scheduled task , curVideoIndex : "
												+ curVideoIndex);
										play(curVideoIndex + 1);
									}
								}
							});
						}
					}
				}, 5, 1, TimeUnit.SECONDS);
				// 启动刷新url，视频网站url都有有效期的
				scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

					@Override
					public void run() {
						if (stopBtn.isEnabled() && progressSlid.isEnabled() && hasPlaying && urlParseTime != null
								&& playList != null && playList.size() > 0) {
							// 已过15分钟，重新获取url
							if (DateUtils.addMinutes(urlParseTime, 15).before(new Date())) {
								try {
									VideoUrlParser selectedParser = getSelectedParser();
									String urlText = urlTxt.getText().trim();
									VideoInfo videoInfo = selectedParser.fetchVideoUrls(urlText, selectedParser
											.getVideoQualityByValue(qualityComb.getSelectedItem() + "").getKey());
									urlParseTime = new Date();
									List<PlayListItem> newPlayList = new ArrayList<PlayListItem>();
									for (String url : videoInfo.getUrls()) {
										newPlayList.add(new PlayListItem(videoInfo, url));
									}
									playList = newPlayList;
									if (localBufChk.isSelected()) {
										httpBufferProxyHandler.setUrls(new ArrayList<String>(videoInfo.getUrls()));
										if (httpJoinProxyHandler.getJoinItems() != null && localJoinChk.isSelected()
												&& videoInfo.getUrls().size() > 1) {
											for (int i = 0; i < videoInfo.getUrls().size(); i++) {
												httpJoinProxyHandler.getJoinItems().get(i)
														.setUrl(videoInfo.getUrls().get(i));
											}
										}
									}
								} catch (Exception e1) {
									logger.error(e1.getMessage(), e1);
									return;
								}
							}
						}
					}
				}, 5, 60, TimeUnit.SECONDS);
				server = new Server(HTTP_SERVER_PORT);
				HandlerCollection handlerCollection = new HandlerCollection();
				handlerCollection.addHandler(localFileHttpHandler);
				handlerCollection.addHandler(httpBufferProxyHandler);
				handlerCollection.addHandler(httpJoinProxyHandler);
				server.setHandler(handlerCollection);
				try {
					server.start();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}.start();
	}

	private void play(int i) {
		if (playList != null && playList.size() > i) {
			final Device selectedDevice = getSelectedDevice();
			if (selectedDevice != null) {
				curVideoIndex = i;
				previousBtn.setEnabled(curVideoIndex > 0);
				nextBtn.setEnabled(curVideoIndex < playList.size() - 1);
				updateTitle(null);
				ActionHelper actionHelper = new ActionHelper(selectedDevice);
				// 先暂停，如果在播放过程中，直接切换，播放器会出问题
				try {
					actionHelper.pause();
				} catch (Exception e) {
				}
				actionHelper.play(playList.get(curVideoIndex).getVideoUrl());
				paused = false;
				hasPlaying = false;
				stopBtn.setEnabled(true);
				stopBtn2.setEnabled(true);
				progressSlid.setEnabled(false);
				progressSlid.setMaximum(0);
				progressSlid.setMinimum(0);
				progressSlid.setValue(0);
				quickSyncCount = 5;
				syncPositionInfo();
				// 确保播放，有时候调用了，播放器没启动
				new Thread() {
					@Override
					public void run() {
						Service service = selectedDevice.getService(AVTransport.SERVICE_TYPE);
						if (service != null) {
							logger.info("subscribe :" + selectedDevice.getFriendlyName());
							try {
								ctrlPoint.unsubscribe(service);
								service.clearSID();
								ctrlPoint.subscribe(service);
							} catch (Exception e) {
								logger.error("subscribe failed:" + selectedDevice.getFriendlyName(), e);
							}
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							return;
						}
						for (int j = 0; j < 20; j++) {
							if (playList != null && playList.size() > curVideoIndex && j % 3 == 0
									&& (!progressSlid.isEnabled() || progressSlid.getValue() == 0)) {
								Device selectedDevice = getSelectedDevice();
								if (selectedDevice != null) {
									ActionHelper actionHelper = new ActionHelper(selectedDevice);
									try {
										actionHelper.resume();
									} catch (Exception e) {
									}
									quickSyncCount = 5;
									syncPositionInfo();
								} else {
									return;
								}
							} else {
								return;
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
				}.start();
			}
		}
	}

	private void updateTitle(String extStr) {
		if (playList != null && curVideoIndex >= 0 && curVideoIndex < playList.size()) {
			frame.setTitle(playList.get(curVideoIndex).getVideoInfo().getName() + " - " + (curVideoIndex + 1) + "/"
					+ playList.size() + " - " + playList.get(curVideoIndex).getVideoInfo().getQualityName()
					+ StringUtils.defaultString(extStr));
		}
	}

	private void updateMouseSpiltValue(boolean isDrag, MouseEvent e) {
		if (isDrag) {
			mouseDurationLeb.setText(DurationUtils.getTrackNRFormat(progressSlid.getValue()));
			mouseDurationLeb.setLocation(e.getX() + progressSlid.getX(), e.getY() + progressSlid.getY() + 20);
			curMousePos = progressSlid.getValue();
		} else {
			int width = progressSlid.getWidth();
			int mouseRelPos = e.getX();
			int seconds = (int) Math.ceil(((double) mouseRelPos / width) * progressSlid.getMaximum());
			mouseDurationLeb.setText(DurationUtils.getTrackNRFormat(seconds));
			mouseDurationLeb.setLocation(e.getX() + progressSlid.getX(), e.getY() + progressSlid.getY() + 20);
			curMousePos = seconds;
		}
		mouseDurationLeb.setForeground(Color.BLUE);
		mouseDurationLeb.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	private void updateUrlParsers() {
		videoUrlParsers = VideoUrlParser.getVideoUrlParser(new File("parsers"));
		addrParseComb.removeAllItems();
		for (VideoUrlParser videoUrlParser : videoUrlParsers) {
			addrParseComb.addItem(videoUrlParser.getName());
		}
		if (videoUrlParsers.size() > 0) {
			qualityComb.removeAllItems();
			for (KeyValue<String, String> keyValue : videoUrlParsers.get(0).getVideoQualitys()) {
				qualityComb.addItem(keyValue.getValue());
			}
		}
		dlnaEventParser = new DlnaEventParser();
		dlnaEventParser.init(new File("dlna/event.js"));
	}

	private VideoUrlParser getSelectedParser() {
		if (videoUrlParsers != null && addrParseComb.getSelectedItem() != null) {
			for (VideoUrlParser videoUrlParser : videoUrlParsers) {
				if (videoUrlParser.getName().equals(addrParseComb.getSelectedItem())) {
					return videoUrlParser;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private synchronized void updateDeviceComboList() {
		DeviceList devList = ctrlPoint.getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			if (!dev.isDeviceType(MediaRenderer.DEVICE_TYPE)) {
				continue;
			}
			String devComboName = getDeviceComboName(dev);
			int itemCnt = deviceComb.getItemCount();
			boolean hasSameDeviceName = false;
			for (int i = 0; i < itemCnt; i++) {
				String itemName = (String) deviceComb.getItemAt(i);
				if (itemName == null) {
					continue;
				}
				if (itemName.compareTo(devComboName) == 0) {
					hasSameDeviceName = true;
					break;
				}
			}
			if (hasSameDeviceName == false) {
				deviceComb.addItem(devComboName);
			}
		}
	}

	private synchronized Device getSelectedDevice() {
		return getDevice((String) deviceComb.getSelectedItem());
	}

	private String getDeviceComboName(Device dev) {
		return dev.getFriendlyName();
	}

	private Device getDevice(String devComboName) {
		if (devComboName == null) {
			return null;
		}

		DeviceList devList = ctrlPoint.getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			if (devComboName.compareTo(getDeviceComboName(dev)) == 0) {
				return dev;
			}
		}
		return null;
	}

	private void syncPositionInfo() {
		if (playList != null && playList.size() > 0) {
			Device selectedDevice = getSelectedDevice();
			if (selectedDevice != null && !isDragSplit) {
				ActionHelper actionHelper = new ActionHelper(selectedDevice);
				try {
					long start = System.currentTimeMillis();
					final ArgumentList positionInfo = actionHelper.getPositionInfo();
					final int delay = (int) ((System.currentTimeMillis() - start) / 1000);
					if (positionInfo != null) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								Argument trackDuration = positionInfo.getArgument(AVTransport.TRACKDURATION);
								if (trackDuration != null && StringUtils.isNotEmpty(trackDuration.getValue())) {
									int seconds = DurationUtils.parseTrackNRFormat(trackDuration.getValue());
									progressSlid.setMaximum(seconds);
									progressSlid.setMinimum(0);
									durationLeb.setText(DurationUtils.getTrackNRFormat(seconds));
								}
								String posTime = null;
								Argument relTimeDuration = positionInfo.getArgument(AVTransport.RELTIME);
								Argument absTimeDuration = positionInfo.getArgument(AVTransport.ABSTIME);
								if (relTimeDuration != null && StringUtils.isNotEmpty(relTimeDuration.getValue())) {
									posTime = relTimeDuration.getValue();
								} else if (absTimeDuration != null
										&& StringUtils.isNotEmpty(absTimeDuration.getValue())) {
									posTime = absTimeDuration.getValue();
								}
								if (StringUtils.isNotEmpty(posTime)) {
									int seconds = DurationUtils.parseTrackNRFormat(posTime);
									if (seconds > 0) {
										hasPlaying = true;
									}
									// 1秒以上的误差才同步
									if (seconds == 0) {
										progressSlid.setValue(seconds);
									} else if (Math.abs(progressSlid.getValue() - (seconds + delay)) > 1) {
										progressSlid.setValue(seconds + delay);
									}
								}
								progressSlid.setEnabled(true);
							}
						});
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void pasteUrlText() {
		Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = sysc.getContents(null);
		if (contents != null) {
			// 检查内容是否是文本类型
			if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				try {
					String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
					urlTxt.setText(text);
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
					JOptionPane.showMessageDialog(frame, "粘帖失败，" + e1.getMessage());
					return;
				}
			} else if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				try {
					List<File> filelist = (List<File>) contents.getTransferData(DataFlavor.javaFileListFlavor);
					urlTxt.setText(filelist.get(0).getPath());
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
					JOptionPane.showMessageDialog(frame, "粘帖失败，" + e1.getMessage());
					return;
				}
			}
		}
	}

	private void showCurrentPos() {
		curDurationLeb.setText(DurationUtils.getTrackNRFormat(Math.min(progressSlid.getValue(),
				progressSlid.getMaximum()))
				+ " / " + DurationUtils.getTrackNRFormat(progressSlid.getMaximum()));
	}

	private void playBtnAction() {
		Device selectedDevice = getSelectedDevice();
		if (selectedDevice == null) {
			JOptionPane.showMessageDialog(frame, "请选择dlna设备");
			return;
		}
		VideoUrlParser selectedParser = getSelectedParser();
		if (selectedParser == null) {
			JOptionPane.showMessageDialog(frame, "请选择地址转换器");
			return;
		}
		if (qualityComb.getSelectedItem() == null) {
			JOptionPane.showMessageDialog(frame, "请选择清晰度");
			return;
		}
		String urlText = urlTxt.getText().trim();
		if (StringUtils.isBlank(urlText)) {
			JOptionPane.showMessageDialog(frame, "请填写视频地址");
			return;
		}
		urlParseTime = null;
		VideoInfo videoInfo = null;
		// 网络地址
		if (urlText.indexOf("://") != -1) {
			try {
				URLConnection openConnection = new URL(urlText).openConnection();
				openConnection.connect();
				openConnection.getInputStream().close();
			} catch (MalformedURLException e1) {
				logger.error(e1.getMessage(), e1);
				JOptionPane.showMessageDialog(frame, "视频地址格式不正确");
				return;
			} catch (IOException e1) {
				logger.error(e1.getMessage(), e1);
				JOptionPane.showMessageDialog(frame, "视频地址无法访问");
				return;
			}
			String baseUrlText = urlText;
			if (baseUrlText.indexOf("#") != -1) {
				baseUrlText = baseUrlText.substring(0, baseUrlText.indexOf("#"));
			}
			if (baseUrlText.indexOf("?") != -1) {
				baseUrlText = baseUrlText.substring(0, baseUrlText.indexOf("?"));
			}
			if (baseUrlText.toLowerCase().substring(baseUrlText.lastIndexOf('.')).indexOf("htm") != -1
					|| StringUtils.isBlank(FilenameUtils.getExtension(baseUrlText))) {
				try {
					videoInfo = selectedParser.fetchVideoUrls(urlText,
							selectedParser.getVideoQualityByValue(qualityComb.getSelectedItem() + "").getKey());
					urlParseTime = new Date();
					if (localBufChk.isSelected() && videoInfo != null && videoInfo.getUrls() != null) {
						httpBufferProxyHandler.setUrls(new ArrayList<String>());
						httpBufferProxyHandler.setSpeedCallback(new SpeedCallback());
						ArrayList<String> list = new ArrayList<String>();
						ArrayList<JoinItem> orgJoinItems = new ArrayList<JoinItem>();
						for (int i = 0; i < videoInfo.getUrls().size(); i++) {
							String videoUrl = videoInfo.getUrls().get(i);
							httpBufferProxyHandler.getUrls().add(videoUrl);
							if (localJoinChk.isSelected() && videoInfo.getUrls().size() > 1) {
								// 获取头信息
								try {
									JoinItem loadJoinItem = JoinItem.loadJoinItem(videoUrl, null);
									if (loadJoinItem.getFlvMetaInfo().getFlvHeader() == null) {
										JOptionPane.showMessageDialog(frame, "获得视频头信息失败, 不是flv视频");
										return;
									}
									orgJoinItems.add(loadJoinItem);
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
									JOptionPane.showMessageDialog(frame, "获得视频头信息失败, " + e.getMessage());
									return;
								}
							} else {
								list.add("http://" + localIpComb.getSelectedItem() + ":" + HTTP_SERVER_PORT
										+ "/remote/" + i);
							}
						}
						if (localJoinChk.isSelected() && videoInfo.getUrls().size() > 1) {
							list.clear();
							list.add("http://" + localIpComb.getSelectedItem() + ":" + HTTP_SERVER_PORT
									+ "/remote/join");
							httpJoinProxyHandler.setJoinItems(JoinItem.joinVideo(orgJoinItems));
							httpJoinProxyHandler.setSpeedCallback(new SpeedCallback());
						}
						videoInfo.setUrls(list);
					}
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
					JOptionPane.showMessageDialog(frame, "转换视频地址异常，" + e1.getMessage());
					return;
				}
			} else {
				videoInfo = new VideoInfo();
				videoInfo.setName(FilenameUtils.getBaseName(urlText));
				videoInfo.setQualityName("视频链接");
				videoInfo.setUrls(new ArrayList<String>());
				if (localBufChk.isSelected()) {
					httpBufferProxyHandler.setSpeedCallback(new SpeedCallback());
					httpBufferProxyHandler.setUrls(new ArrayList<String>());
					httpBufferProxyHandler.getUrls().add(urlText);
					videoInfo.getUrls().add(
							"http://" + localIpComb.getSelectedItem() + ":" + HTTP_SERVER_PORT + "/remote/" + 0);
				} else {
					videoInfo.getUrls().add(urlText);
				}
			}
		} else {
			// 本地路径
			File file = new File(urlText);
			if (!file.exists()) {
				JOptionPane.showMessageDialog(frame, "文件不存在，" + file.getAbsolutePath());
				return;
			}
			videoInfo = new VideoInfo();
			videoInfo.setName(FilenameUtils.getBaseName(urlText));
			videoInfo.setQualityName("本地视频");
			videoInfo.setUrls(new ArrayList<String>());
			String videoName = DigestUtils.shaHex(file.getAbsolutePath()) + "." + FilenameUtils.getExtension(urlText);
			videoInfo.getUrls().add(
					"http://" + localIpComb.getSelectedItem() + ":" + HTTP_SERVER_PORT + "/file/" + videoName);
			localFileHttpHandler.setLocalFile(file);
		}
		if (videoInfo != null) {
			playList = new ArrayList<PlayListItem>();
			for (String url : videoInfo.getUrls()) {
				playList.add(new PlayListItem(videoInfo, url));
			}
			if (StringUtils.equals(curUrl, urlText)) {
				play(curVideoIndex);
			} else {
				play(0);
			}
			curUrl = urlText;
		} else {
			logger.error("videoInfo is null");
			JOptionPane.showMessageDialog(frame, "转换视频地址失败");
			return;
		}
	}

	private void stopBtnAction() {
		Device selectedDevice = getSelectedDevice();
		if (selectedDevice != null) {
			// 先设置状态，防止和事件处理冲突
			stopBtn.setEnabled(false);
			stopBtn2.setEnabled(false);
			ActionHelper actionHelper = new ActionHelper(selectedDevice);
			// 先暂停，再退出。可以增加退出速度
			try {
				actionHelper.pause();
			} catch (Exception e) {
			}
			actionHelper.stop();
			curVideoIndex = 0;
			playList = null;
			progressSlid.setEnabled(false);
			progressSlid.setValue(0);
			progressSlid.setMaximum(0);
			durationLeb.setText("");
			curDurationLeb.setText("");
			showCurrentPos();
		}
	}

	private class SpeedCallback implements ReceiveSpeedCallback {
		private long preTime = -1;
		private long received = 0;

		@Override
		public void receivedComplete(final int videIndex, final int totalSegments) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					updateTitle(" - " + (videIndex + 1) + "/" + totalSegments + " - 缓冲完成");
				}
			});
		}

		@Override
		public void receivedBytes(final int videIndex, final int totalSegments, final long receivedBytes) {
			received += receivedBytes;
			if (preTime == -1) {
				preTime = System.currentTimeMillis();
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						updateTitle(" - " + (videIndex + 1) + "/" + totalSegments + " - 开始缓冲");
					}
				});
				return;
			}
			final long spanTime = System.currentTimeMillis() - preTime;
			if (spanTime > 500) {
				final long sumReceived = received;
				received = 0;
				preTime = System.currentTimeMillis();
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						double speed = sumReceived * 1000.0 / spanTime;
						updateTitle(" - " + (videIndex + 1) + "/" + totalSegments + " - "
								+ ByteUtils.readable((long) speed));
					}
				});
			}
		}
	};
}
