package com.codepath.beacon.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class Utils {
	public static final long NANOS_IN_MILLI = 1000 * 1000;

	public static boolean waitForBooleanFlag (final AtomicBoolean booleanFlag, final boolean desiredState,
			final long timeoutInMillis) throws Exception
			{
		if (booleanFlag == null) {
			throw new IllegalArgumentException("booleanFlag value is null");
		}

		return waitForCondition(booleanFlag, new ConditionChecker() {
			@Override
			public boolean conditionOk() {
				return booleanFlag.get() == desiredState;
			}
		}, timeoutInMillis, null, -1);
			}

	public static interface ConditionChecker {
		boolean conditionOk();
	}

	public static boolean waitForCondition (final Object monitor, final ConditionChecker conditionChecker,
			final long timeoutInMillis, String logMessage, int logLevel) throws Exception
			{
		if (monitor == null) {
			throw new IllegalArgumentException("Monitor is null");
		}

		if (conditionChecker == null) {
			throw new IllegalArgumentException("Condition checker is null");
		}

		if (timeoutInMillis < 0) {
			throw new IllegalArgumentException("Wait time cannot be negative");
		}

		boolean returnedValue = true;
		final long startTime = System.nanoTime();

		try {
			if (conditionChecker.conditionOk()) {
				returnedValue = true;
				return returnedValue;
			}

			long remainingTimeToWaitMillis = timeoutInMillis;
			final long timeToStopWaitingNanos = startTime + remainingTimeToWaitMillis * NANOS_IN_MILLI;

			while (remainingTimeToWaitMillis > 0) {
				synchronized (monitor) {
					if (conditionChecker.conditionOk()) {
						returnedValue = true;
						return returnedValue;
					}
					remainingTimeToWaitMillis = ((timeToStopWaitingNanos - System.nanoTime()) / NANOS_IN_MILLI);
					if (remainingTimeToWaitMillis <= 0) {
						returnedValue = false;
						return returnedValue;
					}
					monitor.wait(remainingTimeToWaitMillis);
					remainingTimeToWaitMillis = ((timeToStopWaitingNanos - System.nanoTime()) / NANOS_IN_MILLI);
					if (conditionChecker.conditionOk()) {
						returnedValue = true;
						return returnedValue;
					}
				}
			}
			returnedValue = conditionChecker.conditionOk();
			return returnedValue;
		} catch (final Throwable t) {
			throw new Exception("Failed while waiting for condition state", t);
		}
			}
}

