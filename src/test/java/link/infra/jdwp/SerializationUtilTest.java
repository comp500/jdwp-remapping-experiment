package link.infra.jdwp;

import link.infra.jdwp.packets.SerializationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializationUtilTest {
	private static Stream<Arguments> serdeIntRange() {
		return Stream.generate(new Supplier<Arguments>() {
			long i = 0;
			int numBytes = 1;
			final Random rand = new Random(10000);

			@Override
			public Arguments get() {
				Arguments ret = Arguments.arguments(i, numBytes);
				numBytes++;
				if (numBytes > 8) {
					i = rand.nextLong();
					numBytes = (Long.SIZE - Long.numberOfLeadingZeros(i));
					numBytes = (int) Math.ceil((float)numBytes / 8);
				}
				return ret;
			}
		}).limit(1000);
	}

	@ParameterizedTest
	@MethodSource({"serdeIntRange"})
	void serdeRandomInts(long value, int size) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		SerializationUtil.writeVarInt(value, size, dos);

		ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());

		assertEquals(value, SerializationUtil.readVarInt(buf, size));
	}

	@Test
	void serdeBigNumber() throws IOException {
		long value = -7836348117933378118L;
		int size = 8;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		SerializationUtil.writeVarInt(value, size, dos);

		ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());

		assertEquals(value, SerializationUtil.readVarInt(buf, size));
	}
}
