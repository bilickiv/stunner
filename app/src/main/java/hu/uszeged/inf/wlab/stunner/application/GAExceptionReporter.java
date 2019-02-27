package hu.uszeged.inf.wlab.stunner.application;

import com.google.android.gms.analytics.ExceptionParser;

/**
 * @author rakz
 */
public class GAExceptionReporter implements ExceptionParser {

	@Override
	public String getDescription(final String threadName, final Throwable throwable) {

		final StringBuilder recordBuilder = new StringBuilder();

		recordBuilder.append("On thread ");
		recordBuilder.append(threadName);
		recordBuilder.append("\n[Stacktrace]\n");
		recordBuilder.append(traceToString(throwable));
		return recordBuilder.toString();

	}

	/**
	 * @param throwable exception thrown.
	 * @return stacktrace with cause
	 */
	private String traceToString(final Throwable throwable) {
		final StringBuilder builder = new StringBuilder();
		builder.append(throwable.getClass());
		builder.append(": ");
		builder.append(throwable.getMessage());

		builder.append("\n");
		for (final StackTraceElement element : throwable.getStackTrace()) {
			if (null == element.getFileName()) {
				continue;
			}
			builder.append("\t");
			builder.append(element.getClassName());
			builder.append(".");
			builder.append(element.getMethodName());
			builder.append(":");
			builder.append(element.getFileName());
			builder.append(":");
			builder.append(element.getLineNumber());
			builder.append("\n");
		}

		if (null != throwable.getCause()) {
			builder.append(traceToString(throwable.getCause()));
		}

		return builder.toString();
	}

}