package hu.uszeged.inf.wlab.stunner.sync.communication;

import hu.uszeged.inf.wlab.stunner.sync.communication.request.UploadRequest;
import hu.uszeged.inf.wlab.stunner.sync.communication.response.ErrorCode;
import hu.uszeged.inf.wlab.stunner.sync.communication.response.Result;
import hu.uszeged.inf.wlab.stunner.sync.communication.response.UploadResponse;
import hu.uszeged.inf.wlab.stunner.utils.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * Sends the request.
 */
public class Connector {

	private static final String TAG = "Connector";
	/**
	 * Indication that default timeout needs to be used.
	 */
	private static final int DEFAULT_TIMEOUT = 20000;

	/**
	 * Connection timeout.
	 */
	private final int timeout;

	/** The m context. */
	private final WeakReference<Context> mContext;

	/**
	 * Instantiates a new connector.
	 * 
	 * @param context the context
	 */
	public Connector(final Context context) {
		this.timeout = DEFAULT_TIMEOUT;
		this.mContext = new WeakReference<Context>(context);
	}

	/**
	 * Instantiates a new connector.
	 * 
	 * @param context the context
	 * @param timeout connection timeout.
	 */
	public Connector(final Context context, final int timeout) {
		this.timeout = timeout;
		this.mContext = new WeakReference<Context>(context);
	}

	/**
	 * Performs the HTTP request for a given servlet and optional POST parameters (for POST requests).
	 * 
	 * @param request the <code>Request</code> object.
	 * @return the string response
	 */
	public UploadResponse post(final UploadRequest request) {

		if (this.mContext == null || this.mContext.get() == null) {
			throw new IllegalArgumentException("Context is null");
		}

		Result errorOccurred = null;
		try {

			final ObjectMapper mapper = new ObjectMapper();

			Log.d(TAG, "UploadResponse "+mapper.writeValueAsString(request));

			/*
			 * Try to perform the HTTP operation.
			 */
			final String httpResponse = HTTP.post(this.mContext.get(), Constants.SERVLET_URL, mapper.writeValueAsString(request), timeout);

			Log.d(TAG, "RESPONSE: " + httpResponse);

			/*
			 * Here we got a proper response.
			 */
			return mapper.readValue(httpResponse, UploadResponse.class);

		} catch (final JsonParseException e) {
			Log.e(TAG,"(" + Constants.SERVLET_URL + "):"+ e.toString());
			errorOccurred = new Result(false, ErrorCode.CLIENT_ERROR);
		} catch (final JsonMappingException e) {
			Log.e(TAG,"(" + Constants.SERVLET_URL + "):"+ e.toString());
			errorOccurred = new Result(false, ErrorCode.CLIENT_ERROR);
		} catch (final HttpResponseException e) {
			Log.e(TAG,"(" + Constants.SERVLET_URL + "):"+ e.toString());
			errorOccurred = new Result(false, ErrorCode.SERVER_ERROR);
		} catch (final ConnectTimeoutException e) {
			Log.e(TAG,"(" + Constants.SERVLET_URL + "):"+ e.toString());
			errorOccurred = new Result(false, ErrorCode.SERVER_ERROR);
		} catch (final FileNotFoundException e) {
			Log.e(TAG,"(" + Constants.SERVLET_URL + "):"+ e.toString());
			errorOccurred = new Result(false, ErrorCode.CLIENT_ERROR);
		} catch (final IOException e) {
			Log.e(TAG,"(" + Constants.SERVLET_URL + "):"+ e.toString());
			errorOccurred = new Result(false, ErrorCode.SERVER_ERROR);
		} catch (final Resources.NotFoundException e) {
			Log.e(TAG,"(" + Constants.SERVLET_URL + "):"+ e.toString());
			errorOccurred = new Result(false, ErrorCode.CLIENT_ERROR);
		}

		return createDefaultResponse(errorOccurred);
	}

	/**
	 * Create default
	 * 
	 * @param errorOccurred
	 * @return {@link UploadResponse} instance set with the result.
	 */
	private UploadResponse createDefaultResponse(final Result errorOccurred) {
		final UploadResponse response = new UploadResponse();
		response.setResult(errorOccurred);
		return response;
	}
}