package io.github.libkodi.security.utils;

import java.util.concurrent.locks.Lock;

import io.github.libkodi.security.interfaces.ThreadLockException;
import io.github.libkodi.security.interfaces.ThreadLockHandle;

public class ThreadUtils {
	public static <T> T sync(Lock lock, ThreadLockHandle<T> func, ThreadLockException exceptionFunc) {
		T ret = null;
		
		if (lock != null) {
			lock.lock();
			
			try {
				if (func != null) {
					ret = func.call();
				}
			} catch (Exception e) {
				if (exceptionFunc != null) {
					try {
						exceptionFunc.call(e);
					} catch (Exception e2) {}
				}
			} finally {
				lock.unlock();
			}
		}
		
		return ret;
	}
	
	public static <T> T sync(Lock lock, ThreadLockHandle<T> func) {
		return sync(lock, func, null);
	}
}
