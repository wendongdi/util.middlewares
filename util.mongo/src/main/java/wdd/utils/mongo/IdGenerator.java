package wdd.utils.mongo;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.logging.LogFactory;

public class IdGenerator {
	
	private final long workerId;
	//2014-06-28
	private final static long twepoch = 1403884800000L;
	//private final static long twepoch = 1303895660503L;
	private long sequence = 0L;
	private final static long workerIdBits = 16L;
	private final static long maxWorkerId = -1L ^ -1L << workerIdBits;
	private final static long sequenceBits = 6L;

	private final static long workerIdShift = sequenceBits;
	private final static long timestampLeftShift = sequenceBits + workerIdBits;
	private final static long sequenceMask = -1L ^ -1L << sequenceBits;
	private static String localIp = "";
	

	private long lastTimestamp = -1L;

	public static IdGenerator idGenerator = null;

	public static IdGenerator getInstatnce() {
		if (idGenerator == null) {
			try {
				idGenerator = new IdGenerator(IdGenerator.getIdLong());
			} catch (Exception e) {
				LogFactory.getLog(IdGenerator.class).error(e);
			}
		}
		return idGenerator;
	}
	
	private IdGenerator(final long workerId) {
		super();
		if (workerId > IdGenerator.maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(String.format(
					"worker Id can't be greater than %d or less than 0,local Ip is %s",
						IdGenerator.maxWorkerId,localIp));
		}
		this.workerId = workerId;
	}
	
	public static long getIdLong() throws Exception{
		localIp = IdGenerator.getLocalAddress();
		String[] ips = localIp.split("\\.");
		String cip = ips[2];
		String dip = ips[3];
		String cNum = Integer.toHexString(Integer.parseInt(cip));
		String dNum = Integer.toHexString(Integer.parseInt(dip));
		return Long.valueOf(cNum+dNum,16);
	}

	public static String getLocalAddress() throws Exception {
		final Enumeration<NetworkInterface> enumeration = NetworkInterface
				.getNetworkInterfaces();
		InetAddress ipv6Address = null;
		while (enumeration.hasMoreElements()) {
			final NetworkInterface networkInterface = enumeration.nextElement();
			final Enumeration<InetAddress> en = networkInterface
					.getInetAddresses();
			while (en.hasMoreElements()) {
				final InetAddress address = en.nextElement();
				if (!address.isLoopbackAddress()) {
					if (address instanceof Inet6Address) {
						ipv6Address = address;
					} else {
						return normalizeHostAddress(address);
					}
				}
			}
		}
		if (ipv6Address != null) {
			return normalizeHostAddress(ipv6Address);
		}
		final InetAddress localHost = InetAddress.getLocalHost();
		return normalizeHostAddress(localHost);
	}

	public static String normalizeHostAddress(final InetAddress localHost) {
		if (localHost instanceof Inet6Address) {
			return "[" + localHost.getHostAddress() + "]";
		} else {
			return localHost.getHostAddress();
		}
	}

	public synchronized Long nextId() throws Exception {
		long timestamp = this.timeGen();
		if (this.lastTimestamp == timestamp) {
			this.sequence = (this.sequence + 1) & IdGenerator.sequenceMask;
			if (this.sequence == 0) {
				timestamp = this.tilNextMillis(this.lastTimestamp);
			}
		} else {
			this.sequence = 0;
		}
		if (timestamp < this.lastTimestamp) {
			throw new Exception(
					String.format(
							"Clock moved backwards.  Refusing to generate id for %d milliseconds",
							this.lastTimestamp - timestamp));
		}

		this.lastTimestamp = timestamp;
		return timestamp - twepoch << IdGenerator.timestampLeftShift
				| this.workerId << IdGenerator.workerIdShift | this.sequence;
	}

	private long tilNextMillis(final long lastTimestamp) {
		long timestamp = this.timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = this.timeGen();
		}
		return timestamp;
	}

	private long timeGen() {
		return System.currentTimeMillis();
	}
	
	public static void main(String[] args) throws Exception{
		for(int i = 0 ; i<100;i++){
			System.out.println(EncryptUtil.newId());
		}
	}
}
