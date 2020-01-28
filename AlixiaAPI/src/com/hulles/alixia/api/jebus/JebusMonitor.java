/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.alixia.api.jebus;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.shared.SharedUtils;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 * The JebusMonitor exists to report on the Alixia pub/sub traffic.
 * 
 * @author hulles
 *
 */
final class JebusMonitor implements Closeable {
	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JebusMonitor.class);
	final JebusPool jebusPool;
	JebusListener listener = null;
	private ExecutorService executor;

	JebusMonitor(JebusPool pool) {
		
		SharedUtils.checkNotNull(pool);
		executor = Executors.newFixedThreadPool(1);
		jebusPool = pool;
		listener = new JebusListener();
		startMonitor();
	}

	/**
	 * Log traffic on a Jebus channel.
	 * 
	 * @param channel
	 * @param msgBytes
	 */
	void logJebusChannel(byte[] channel, byte[] msgBytes) {
		String doohickey;
		
		SharedUtils.checkNotNull(channel);
		SharedUtils.checkNotNull(msgBytes);
		if (channel.equals(JebusBible.getBytesKey(JebusKey.FROMCHANNEL, jebusPool))) {
			doohickey = " => ";
		} else {
			doohickey = " <= ";
		}
		// TODO actually log the DialogHeader and Dialog after deserializing them
		LOGGER.info(getChannelString(channel, doohickey, "byte[]"));
	}
	/**
	 * Log traffic on a Jebus channel.
	 * 
	 * @param channel
	 * @param text
	 */
	void logJebusChannel(byte[] channel, String text) {
		String doohickey;
		
		SharedUtils.checkNotNull(channel);
		SharedUtils.checkNotNull(text);
		if (channel.equals(JebusBible.getBytesKey(JebusKey.FROMCHANNEL, jebusPool))) {
			doohickey = " => ";
		} else {
			doohickey = " <= ";
		}
		LOGGER.info(getChannelString(channel, doohickey, text));
	}

	/**
	 * Format a string for logging the channel traffic.
	 * 
	 * @param channel
	 * @param doohickey
	 * @param text
	 * @return
	 */
	private String getChannelString(byte[] channel, String doohickey, String text) {
		StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("JEBUS ");
		sb.append(jebusPool.getPoolType().toString());
		sb.append(" Channel: ");
		sb.append(channel);
		sb.append(doohickey);
		sb.append(text);
		return sb.toString();
	}
	
	/**
	 * Start the JebusMonitor as an executor thread.
	 * 
	 */
	private void startMonitor() {
		
		executor.submit(new Runnable() {
			@Override
			public void run() {
				try (Jedis jebus = jebusPool.getResource()) {
					listener = new JebusListener();
					// the following line blocks while waiting for responses...
					jebus.subscribe(listener,
							JebusBible.getBytesKey(JebusKey.FROMCHANNEL, jebusPool),
							JebusBible.getBytesKey(JebusKey.TOCHANNEL, jebusPool));
				}
			}
		});
	}
	
	/**
	 * Stop the JebusMonitor
	 */
	private void stopMonitor() {
				
		if (listener == null) {
			return;
		}
		listener.unsubscribe();
		listener = null;
	}

	/**
	 * Listen to pub/sub channel traffic.
	 * 
	 * @author hulles
	 *
	 */
	private class JebusListener extends BinaryJedisPubSub {
		
		JebusListener() {
		}
		
        @Override
		public void onMessage(byte[] channel, byte[] msgBytes) {
        	logJebusChannel(channel, msgBytes);
        }

        @Override
		public void onSubscribe(byte[] channel, int subscribedChannels) {
        	logJebusChannel(channel, "Jebus Monitor subscribed");
        }

        @Override
		public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        	logJebusChannel(channel, "Jebus Monitor unsubscribed");
        }
	}

	@Override
	public void close() {
			
		stopMonitor();
		if (executor != null) {
			try {
			    LOGGER.info("JebusMonitor: attempting to shutdown executor");
			    executor.shutdown();
			    executor.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
			    LOGGER.error("JebusMonitor: tasks interrupted");
			}
			finally {
			    if (!executor.isTerminated()) {
			        LOGGER.error("JebusMonitor: cancelling non-finished tasks");
			    }
			    executor.shutdownNow();
			    LOGGER.info("JebusMonitor: shutdown finished");
			}
		}
		executor = null;
	}

}
