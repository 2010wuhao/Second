package com.joysee.common.data;

import android.content.Context;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

/**
 * Processes http requests in synchronous mode, so your caller thread will be blocked on each
 * request
 *
 * @see com.JAsyncHttpClient.android.http.AsyncHttpClient
 */
class JSyncHttpClient extends JAsyncHttpClient {

    /**
     * Creates a new SyncHttpClient with default constructor arguments values
     */
    public JSyncHttpClient() {
        super();
    }

    /**
     * Creates a new SyncHttpClient.
     *
     * @param schemeRegistry SchemeRegistry to be used
     */
    public JSyncHttpClient(SchemeRegistry schemeRegistry) {
        super(schemeRegistry);
    }

    @Override
    protected JRequestReceipt sendRequest(DefaultHttpClient client,
                                        HttpContext httpContext, HttpUriRequest uriRequest,
                                        String contentType, JResponseInterface responseHandler,
                                        Context context) {
        if (contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }

        responseHandler.setUseSynchronousMode(true);

		/*
         * will execute the request directly
		*/
        new JAsyncHttpRequest(client, httpContext, DEFAULT_SOCKET_TIMEOUT, uriRequest, responseHandler).run();

        // Return a Request Handle that cannot be used to cancel the request
        // because it is already complete by the time this returns
        return new JRequestReceipt(null);
    }
}
