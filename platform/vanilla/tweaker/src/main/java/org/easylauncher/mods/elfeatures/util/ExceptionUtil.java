/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.easylauncher.mods.elfeatures.util;

import java.io.UncheckedIOException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

// original source code: https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/impl/util/ExceptionUtil.java
public final class ExceptionUtil {

	public static RuntimeException wrap(Throwable throwable) {
		if (throwable instanceof RuntimeException)
			return (RuntimeException) throwable;

		throwable = unwrap(throwable);
		if (throwable instanceof RuntimeException)
			return (RuntimeException) throwable;

		return new WrappedException(throwable);
	}

	private static Throwable unwrap(Throwable throwable) {
		if (throwable instanceof WrappedException
				|| throwable instanceof UncheckedIOException
				|| throwable instanceof ExecutionException
				|| throwable instanceof CompletionException
		) {
			Throwable cause = throwable.getCause();
			if (cause != null) {
                return unwrap(cause);
            }
		}

		return throwable;
	}

	public static final class WrappedException extends RuntimeException {

		public WrappedException(Throwable cause) {
			super(cause);
		}

	}

}