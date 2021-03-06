package ru.r2cloud.jradio.lrpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;

import ru.r2cloud.jradio.BufferedByteInput;
import ru.r2cloud.jradio.Context;
import ru.r2cloud.jradio.blocks.CorrelateAccessCodeTag;
import ru.r2cloud.jradio.blocks.FixedLengthTagger;
import ru.r2cloud.jradio.blocks.TaggedStreamToPdu;
import ru.r2cloud.jradio.source.InputStreamSource;

public class LRPTTest {

	private LRPT lrpt;

	@Test
	public void success() throws Exception {
		Set<String> accessCodes = new HashSet<>(LRPT.SYNCHRONIZATION_MARKERS.length);
		for (long cur : LRPT.SYNCHRONIZATION_MARKERS) {
			accessCodes.add(StringUtils.leftPad(Long.toBinaryString(cur), 64, '0'));
		}
		Context context = new Context();
		InputStreamSource float2char = new InputStreamSource(LRPTTest.class.getClassLoader().getResourceAsStream("8bitsoft.s"));
		BufferedByteInput buffer = new BufferedByteInput(float2char, 8160 * 2, 8 * 2);
		CorrelateAccessCodeTag correlate = new CorrelateAccessCodeTag(context, buffer, 9, accessCodes, true);
		TaggedStreamToPdu tag = new TaggedStreamToPdu(context, new FixedLengthTagger(context, correlate, 8160 * 2 + 8 * 2));
		lrpt = new LRPT(context, tag, buffer);
		assertTrue(lrpt.hasNext());
		VCDU vcdu = lrpt.next();
		assertNotNull(vcdu);
		assertEquals(4649488, vcdu.getCounter());
		assertEquals(0, vcdu.getId().getSpacecraftId());
		assertEquals(5, vcdu.getId().getVirtualChannelId());
		assertFalse(vcdu.getInsertZone().isEncryption());
		assertEquals(0, vcdu.getInsertZone().getKeyNumber());
		assertEquals(0, vcdu.getmPdu().getSpareBits());
		assertEquals(54, vcdu.getmPdu().getHeaderFirstPointer());
		assertEquals(0, vcdu.getSignalling());
		assertEquals(1, vcdu.getVersion());
		assertEquals(1, vcdu.getPackets().size());
		Packet packet = vcdu.getPackets().get(0);
		assertEquals(65, packet.getApid());
		assertEquals(489, packet.getLength());
		assertEquals(0, packet.getMicrosecondOfMillisecond());
		assertEquals(47839268, packet.getMillisecondOfDay());
		assertEquals(0, packet.getNumberOfDays());
		assertTrue(packet.isSecondaryHeader());
		assertEquals(2, packet.getSequence());
		assertEquals(4858, packet.getSequenceCount());
		assertEquals(482, packet.getUserData().length);
		assertEquals(0, packet.getVersion());
		Packet partial = vcdu.getPartial();
		assertNotNull(partial);

		assertEquals(65, partial.getApid());
		assertEquals(511, partial.getLength());
		assertEquals(0, partial.getMicrosecondOfMillisecond());
		assertEquals(47839268, partial.getMillisecondOfDay());
		assertEquals(0, partial.getNumberOfDays());
		assertTrue(partial.isSecondaryHeader());
		assertEquals(2, partial.getSequence());
		assertEquals(4859, partial.getSequenceCount());
		assertEquals(318, partial.getUserData().length);
		assertEquals(0, partial.getVersion());
	}

	@After
	public void stop() throws IOException {
		if (lrpt != null) {
			lrpt.close();
		}
	}

}
