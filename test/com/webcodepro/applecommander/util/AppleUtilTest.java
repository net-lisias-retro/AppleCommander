/*
 * AppleCommander - An Apple ][ image utility.
 * Copyright (C) 2003 by Robert Greene
 * robgreene at users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 2 of the License, or (at your 
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.webcodepro.applecommander.util;

import junit.framework.TestCase;

import com.webcodepro.applecommander.storage.Disk;
import com.webcodepro.applecommander.storage.DiskFullException;
import com.webcodepro.applecommander.storage.DosFormatDisk;
import com.webcodepro.applecommander.storage.FileEntry;
import com.webcodepro.applecommander.storage.ProdosFormatDisk;
import com.webcodepro.applecommander.storage.physical.ByteArrayImageLayout;
import com.webcodepro.applecommander.storage.physical.DosOrder;
import com.webcodepro.applecommander.storage.physical.NibbleOrder;
import com.webcodepro.applecommander.storage.physical.ProdosOrder;

/**
 * Test AppleUtil.
 */
public class AppleUtilTest extends TestCase {
	public AppleUtilTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AppleUtilTest.class);
	}

	/**
	 * These constants were pulled from Applesoft itself.
	 */
	public void testApplesoftFloat() {
		testApplesoftFloat(1.0, 0x8100000000L);
		testApplesoftFloat(10.0, 0x8420000000L);
		testApplesoftFloat(0.5, 0x8000000000L);
		testApplesoftFloat(-0.5, 0x8080000000L);
		testApplesoftFloat(0.25, 0x7F00000000L);
		testApplesoftFloat(-32768.0, 0x9080000000L);
		testApplesoftFloat(99999999.9, 0x9B3EBC1FFDL);
		testApplesoftFloat(999999999.0, 0x9E6E6B27FDL);
		testApplesoftFloat(1000000000.0, 0x9E6E6B2800L);
		testApplesoftFloat(Math.sqrt(0.5), 0x803504F334L);
		testApplesoftFloat(Math.sqrt(2.0), 0x813504F334L);
		testApplesoftFloat(Math.log(2.0), 0x80317217F8L);
		testApplesoftFloat(Math.PI / 2, 0x81490FDAA2L);
		testApplesoftFloat(Math.PI * 2, 0x83490FDAA2L);
	}
	
	protected void testApplesoftFloat(double question, long correctAnswer) {
		byte[] testAnswer = AppleUtil.getApplesoftFloat(question);
		long answer = testAnswer[0] & 0xff;
		answer <<= 8;
		answer |= testAnswer[1] & 0xff;
		answer <<= 8;
		answer |= testAnswer[2] & 0xff;
		answer <<= 8;
		answer |= testAnswer[3] & 0xff;
		answer <<= 8;
		answer |= testAnswer[4] & 0xff;
		// Allow some variance = +/- 1 (that would be the least-significant bit)
		if (Math.abs(answer - correctAnswer) > 1) {
			fail("Numbers should match - " + question);
		}
	}
	
	public void testChangeDosImageOrder() throws DiskFullException {
		// Straight DOS disk in standard DOS order
		DosFormatDisk dosDiskDosOrder = DosFormatDisk.create("dostemp.dsk", 
			new DosOrder(new ByteArrayImageLayout(Disk.APPLE_140KB_DISK)))[0];
		FileEntry fileEntry = dosDiskDosOrder.createFile();
		fileEntry.setFilename("TESTFILE");
		fileEntry.setFiletype("T");
		fileEntry.setFileData("This is a test file.".getBytes());
		// A duplicate - then we change it to a NIB disk image...
		DosFormatDisk dosDiskNibbleOrder = DosFormatDisk.create("dostemp2.nib",
			new NibbleOrder(new ByteArrayImageLayout(Disk.APPLE_140KB_DISK)))[0];
		AppleUtil.changeImageOrderByTrackAndSector(dosDiskDosOrder.getImageOrder(),
			dosDiskNibbleOrder.getImageOrder());
		// Confirm that these disks are identical:
		assertTrue(AppleUtil.disksEqualByTrackAndSector(dosDiskDosOrder, dosDiskNibbleOrder));
	}

	public void testChangeProdosImageOrder() throws DiskFullException {
		// Straight ProDOS disk in standard ProDOS block order
		ProdosFormatDisk prodosDiskDosOrder = ProdosFormatDisk.create("prodostemp.po", 
			"prodostemp",
			new ProdosOrder(new ByteArrayImageLayout(Disk.APPLE_140KB_DISK)))[0];
		FileEntry fileEntry = prodosDiskDosOrder.createFile();
		fileEntry.setFilename("TESTFILE");
		fileEntry.setFiletype("TXT");
		fileEntry.setFileData("This is a test file.".getBytes());
		// A duplicate - then we change it to a NIB disk image...
		ProdosFormatDisk prodosDiskNibbleOrder = ProdosFormatDisk.create("prodostemp2.nib",
			"prodostemp2",
			new NibbleOrder(new ByteArrayImageLayout(Disk.APPLE_140KB_DISK)))[0];
		AppleUtil.changeImageOrderByBlock(prodosDiskDosOrder.getImageOrder(),
			prodosDiskNibbleOrder.getImageOrder());
		// Confirm that these disks are identical:
		assertTrue(AppleUtil.disksEqualByBlock(prodosDiskDosOrder, prodosDiskNibbleOrder));
	}
}