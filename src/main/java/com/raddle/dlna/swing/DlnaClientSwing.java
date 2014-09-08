package com.raddle.dlna.swing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raddle.dlna.ctrl.ActionHelper;
import com.raddle.dlna.http.LocalFileHttpHandler;
import com.raddle.dlna.renderer.AVTransport;
import com.raddle.dlna.renderer.MediaRenderer;
import com.raddle.dlna.url.parser.VideoInfo;
import com.raddle.dlna.url.parser.VideoUrlParser;
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
	private List<VideoUrlParser> videoUrlParsers;
	private List<PlayListItem> playList;
	private int curVideoIndex = 0;
	private boolean paused = false;
	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
	private int curMousePos = 0;
	private int quickSyncCount = 0;
	private boolean hasPlaying = false;
	private Date urlParseTime = null;
	private LocalFileHttpHandler localFileHttpHandler = new LocalFileHttpHandler();;
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

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
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
			public void actionPerformed(ActionEvent e) {
				playBtnAction();
			}
		});
		playBtn.setBounds(72, 181, 61, 23);
		frame.getContentPane().add(playBtn);

		deviceRefreshBtn = new JButton("启动中");
		deviceRefreshBtn.setEnabled(false);
		deviceRefreshBtn.addActionListener(new ActionListener() {
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
			public void actionPerformed(ActionEvent e) {
				stopBtnAction();
			}
		});
		stopBtn.setBounds(316, 181, 61, 23);
		frame.getContentPane().add(stopBtn);

		previousBtn = new JButton("上一个");
		previousBtn.addActionListener(new ActionListener() {
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
			public void actionPerformed(ActionEvent e) {
				pasteUrlText();
			}
		});
		pasteBtn.setBounds(470, 62, 61, 23);
		frame.getContentPane().add(pasteBtn);

		parserRefreshBtn = new JButton("刷新");
		parserRefreshBtn.addActionListener(new ActionListener() {
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
			public void actionPerformed(ActionEvent e) {
				playBtnAction();
			}
		});
		playBtn2.setBounds(534, 62, 61, 23);
		frame.getContentPane().add(playBtn2);

		stopBtn2 = new JButton("停止");
		stopBtn2.setEnabled(false);
		stopBtn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopBtnAction();
			}
		});
		stopBtn2.setBounds(598, 62, 61, 23);
		frame.getContentPane().add(stopBtn2);

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
						syncPositionInfo();
					}
				}, 5, 5, TimeUnit.SECONDS);
				// 启动高频率进度同步
				scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

					@Override
					public void run() {
						if (stopBtn.isEnabled() && progressSlid.isEnabled() && !isDragSplit && !paused) {
							if (quickSyncCount > 0) {
								quickSyncCount--;
								syncPositionInfo();
								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										if (progressSlid.getValue() > 0) {
											hasPlaying = true;
										}
										showCurrentPos();
										// 自动播放下一个
										if ((progressSlid.getValue() == 0 || progressSlid.getValue() >= progressSlid
												.getMaximum())
												&& playList != null
												&& curVideoIndex < playList.size() - 1 && hasPlaying) {
											if (progressSlid.getValue() >= progressSlid.getMaximum()) {
												try {
													// 可能刚刚到最后一秒的开始
													Thread.sleep(500);
												} catch (InterruptedException e) {
													return;
												}
											}
											play(curVideoIndex + 1);
										}
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
									if (progressSlid.getValue() > progressSlid.getMaximum() - 30
											&& curVideoIndex < playList.size() - 1) {
										// 快接近尾部了，需要自动播放下一个
										// 为了防止服务端拖动，增加同步频率
										quickSyncCount = 1;
									}
									showCurrentPos();
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
								} catch (Exception e1) {
									logger.error(e1.getMessage(), e1);
									return;
								}
							}
						}
					}
				}, 5, 60, TimeUnit.SECONDS);
				// 启动httpserver本地视频推送
				Server server = new Server(HTTP_SERVER_PORT);
				server.setHandler(localFileHttpHandler);
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
			Device selectedDevice = getSelectedDevice();
			if (selectedDevice != null) {
				curVideoIndex = i;
				previousBtn.setEnabled(curVideoIndex > 0);
				nextBtn.setEnabled(curVideoIndex < playList.size() - 1);
				frame.setTitle(playList.get(curVideoIndex).getVideoInfo().getName() + " - " + (curVideoIndex + 1) + "/"
						+ playList.size());
				ActionHelper actionHelper = new ActionHelper(selectedDevice);
				// 先暂停，如果在播放过程中，直接切换，播放器会出问题
				actionHelper.pause();
				actionHelper.play(playList.get(curVideoIndex).getVideoUrl());
				paused = false;
				hasPlaying = false;
				stopBtn.setEnabled(true);
				stopBtn2.setEnabled(true);
				progressSlid.setEnabled(false);
				if (curVideoIndex < playList.size() - 1) {
					actionHelper.setNextVideoUrl(playList.get(curVideoIndex + 1).getVideoUrl());
				}
				quickSyncCount = 5;
			}
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
				if (itemName == null)
					continue;
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
		if (devComboName == null)
			return null;

		DeviceList devList = ctrlPoint.getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			if (devComboName.compareTo(getDeviceComboName(dev)) == 0)
				return dev;
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
								Argument absTimeDuration = positionInfo.getArgument(AVTransport.ABSTIME);
								if (absTimeDuration != null && StringUtils.isNotEmpty(absTimeDuration.getValue())) {
									int seconds = DurationUtils.parseTrackNRFormat(absTimeDuration.getValue());
									// 1秒以上的误差才同步
									if (Math.abs(progressSlid.getValue() - (seconds + delay)) > 1) {
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
			if (urlText.toLowerCase().endsWith(".htm") || urlText.toLowerCase().endsWith(".html")) {
				try {
					videoInfo = selectedParser.fetchVideoUrls(urlText,
							selectedParser.getVideoQualityByValue(qualityComb.getSelectedItem() + "").getKey());
					urlParseTime = new Date();
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
					JOptionPane.showMessageDialog(frame, "转换视频地址异常，" + e1.getMessage());
					return;
				}
			} else {
				videoInfo = new VideoInfo();
				videoInfo.setName(FilenameUtils.getBaseName(urlText));
				videoInfo.setUrls(new ArrayList<String>());
				videoInfo.getUrls().add(urlText);
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
			play(0);
		} else {
			logger.error("videoInfo is null");
			JOptionPane.showMessageDialog(frame, "转换视频地址失败");
			return;
		}
	}

	private void stopBtnAction() {
		Device selectedDevice = getSelectedDevice();
		if (selectedDevice != null) {
			ActionHelper actionHelper = new ActionHelper(selectedDevice);
			// 先暂停，再退出。可以增加退出速度
			actionHelper.pause();
			actionHelper.stop();
			curVideoIndex = 0;
			playList = null;
			stopBtn.setEnabled(false);
			stopBtn2.setEnabled(false);
			progressSlid.setEnabled(false);
			progressSlid.setValue(0);
			progressSlid.setMaximum(0);
			durationLeb.setText("");
			curDurationLeb.setText("");
		}
	}
}
