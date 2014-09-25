package com.raddle.dlna.video.flv;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.raddle.dlna.http.join.JoinItem;
import com.raddle.dlna.video.flv.tag.TagHeader;
import com.raddle.dlna.video.flv.tag.script.ScriptDataDouble;

public class FlvMetaInfoTest {
	
	@Test
	public void testReadFlvMetaInfo() throws Exception {
		File file1 = new File("01.flv");
		File rewrite1 = new File("01-rewrite.flv");
		FileInputStream fileInputStream = new FileInputStream(file1);
		FlvMetaInfo readFlvMetaInfo = FlvMetaInfo.readFlvMetaInfo(0, fileInputStream);
		OutputStream rewriteos1 = new FileOutputStream(rewrite1);
		readFlvMetaInfo.writeFlvMetaInfo(rewriteos1);
		TagHeader readTagHeader = TagHeader.readTagHeader(fileInputStream);
		while (readTagHeader != null) {
			if (readTagHeader.getTagType() != 8 && readTagHeader.getTagType() != 9) {
				System.out.println(readTagHeader.getTagType() + " is not video or audio type");
				throw new RuntimeException(readTagHeader.getTagType() + " is not video or audio type");
			}
			readTagHeader.writeTagHeader(rewriteos1);
			IOUtils.copyLarge(fileInputStream, rewriteos1, 0, readTagHeader.getDataLength() + 4);
			readTagHeader = TagHeader.readTagHeader(fileInputStream);
		}
		rewriteos1.close();
		Assert.assertEquals(file1.length(), rewrite1.length());
	}

	@Test
	public void testJoinMetaInfo() throws Exception {
		File file1 = new File("01.flv");
		RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
		File file2 = new File("02.flv");
		RandomAccessFile raf2 = new RandomAccessFile(file2, "r");
		List<FlvMetaInfo> orgMetaInfos = new ArrayList<FlvMetaInfo>();
		orgMetaInfos.add(FlvMetaInfo.readFlvMetaInfo(file1.length(), new FileInputStream(file1)));
		orgMetaInfos.add(FlvMetaInfo.readFlvMetaInfo(file2.length(), new FileInputStream(file2)));
		List<FlvMetaInfo> joinMetaInfo = FlvMetaInfo.joinMetaInfo(orgMetaInfos);
		JoinItem firstItem = new JoinItem();
		firstItem.setFlvMetaInfo(joinMetaInfo.get(0));
		JoinItem secondItem = new JoinItem();
		secondItem.setFlvMetaInfo(joinMetaInfo.get(1));
		System.out.println(firstItem.getFlvMetaInfo().getFileLength());
		System.out.println(secondItem.getFlvMetaInfo().getPreFileLength());
		int count = 0;
		for (ScriptDataDouble fileposition : firstItem.getFlvMetaInfo().getScriptTagBody().getFilepositions()) {
			if (fileposition.getValue().longValue() < firstItem.getFlvMetaInfo().getFileLength()) {
				count++;
				raf1.seek(fileposition.getValue().longValue() - firstItem.getFlvMetaInfo().getJoinIncrLength());
				byte[] tagHeadBytes = new byte[11];
				raf1.read(tagHeadBytes);
				TagHeader tagHeader = TagHeader.readTagHeader(new ByteArrayInputStream(tagHeadBytes));
				if (tagHeader.getTagType() != 8 && tagHeader.getTagType() != 9) {
					System.out.println(fileposition.getValue() + " : " + tagHeader.getTagType());
					break;
				}
			} else {
				count++;
				long pos = fileposition.getValue().longValue() - secondItem.getFlvMetaInfo().getPreFileLength()
						+ secondItem.getFlvMetaInfo().getMetaInfoLength();
				raf2.seek(pos);
				byte[] tagHeadBytes = new byte[11];
				raf2.read(tagHeadBytes);
				TagHeader tagHeader = TagHeader.readTagHeader(new ByteArrayInputStream(tagHeadBytes));
				if (tagHeader.getTagType() != 8 && tagHeader.getTagType() != 9) {
					System.out.println(fileposition.getValue().longValue() + " : " + tagHeader.getTagType());
					break;
				}
			}
		}
		System.out.println(firstItem.getFlvMetaInfo().getScriptTagBody().getFilepositions().size() + " : " + count);
	}
	
}
