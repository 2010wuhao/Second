/**
 * =====================================================================
 *
 * @file  HttpHelper.java
 * @Module Name   com.nj.utils
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013-12-4
 * @brief  This file is the http **** implementation.
 * @This file is responsible by ANDROID TEAM.
 * @Comments: 
 * =====================================================================
 * Revision History:
 *
 *                   Modification  Tracking
 *
 * Author            Date            OS version        Reason 
 * ----------      ------------     -------------     -----------
 * benz          2013-12-4           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.common.data;

import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import android.content.Context;

public class JHttpHelper {

    private static JAsyncHttpClient mAsyncHttpClient;
    private static JImageProvider mImageProvider;
    private static JCacheParams mJCacheParams;

    private static JAsyncHttpClient getHttpClient() {
        if (mAsyncHttpClient == null) {
            mAsyncHttpClient = JAsyncHttpClient.getInstance();
        }
        return mAsyncHttpClient;
    }

    private static JImageProvider getImageProvider(Context ctx) {
        if (mImageProvider == null) {
            mImageProvider = JImageProvider.getInstance(ctx, mJCacheParams);
        }
        return mImageProvider;
    }

    /**
     * @category Perform a HTTP GET request, without any parameters
     * @param url
     * @param cb
     */
    public static JRequestReceipt getJson(String url, JResponseInterface cb) {
        return getHttpClient().get(null, url, null, cb);
    }

    /**
     * @category Perform a HTTP GET request, with parameters
     * @param url
     * @param params
     * @param cb
     */
    public static JRequestReceipt getJson(String url, JRequestParams params, JResponseInterface cb) {
        return getHttpClient().get(null, url, params, cb);
    }

    /**
     * Use context to record the request, then to destroy all requests when
     * activity onDestory()
     * 
     * @category Perform a HTTP GET request, with parameters
     * @param ctx
     * @param url
     * @param cb
     */
    public static JRequestReceipt getJson(Context ctx, String url, JResponseInterface cb) {
        return getHttpClient().get(ctx, url, null, cb);
    }

    /**
     * Use context to record the request, then to destroy all requests when
     * activity onDestory()
     * 
     * @category Perform a HTTP GET request, with parameters
     * @param ctx
     * @param url
     * @param params
     * @param cb
     */
    public static JRequestReceipt getJson(Context ctx, String url, JRequestParams params, JResponseInterface cb) {
        return getHttpClient().get(ctx, url, params, cb);
    }

    /**
     * use context to record the request, then to destroy all requests when
     * activity onDestory()
     * 
     * @category Perform a HTTP GET request, with parameters and headers
     * @param ctx
     * @param url
     * @param headers
     * @param params
     * @param cb
     */
    public static JRequestReceipt getJson(Context ctx, String url, Header[] headers, JRequestParams params, //
            JResponseInterface cb) {
        return getHttpClient().get(ctx, url, headers, params, cb);
    }

    /**
     * @category Perform a HTTP POST request, without any parameters
     * @param url
     * @param cb
     */
    public static JRequestReceipt postJson(String url, JResponseInterface cb) {
        return getHttpClient().post(null, url, null, null, cb);
    }

    /**
     * @category Perform a HTTP POST request, with parameters
     * @param url
     * @param params
     * @param cb
     */
    public static JRequestReceipt postJson(String url, JRequestParams params, JResponseInterface cb) {
        return getHttpClient().post(null, url, null, params, null, cb);
    }

    /**
     * use context to record the request, then to destroy all requests when
     * activity onDestory()
     * 
     * @category Perform a HTTP POST request, with parameters
     * @param ctx
     * @param url
     * @param params
     * @param cb
     */
    public static JRequestReceipt postJson(Context ctx, String url, JRequestParams params, JResponseInterface cb) {
        return getHttpClient().post(ctx, url, null, params, null, cb);
    }

    /**
     * use context to record the request, then to destroy all requests when
     * activity onDestory()
     * 
     * @category Perform a HTTP POST request, with parameters, HttpEntity,
     *           contentType
     * @param ctx
     * @param url
     * @param entity
     * @param contentType
     * @param cb
     */
    public static JRequestReceipt postJson(Context ctx, String url, HttpEntity entity, String contentType, JResponseInterface cb) {
        return getHttpClient().post(ctx, url, null, entity, contentType, cb);
    }

    /**
     * @category Perform a HTTP POST request, with parameters, headers,
     *           content-type
     * @param ctx
     * @param url
     * @param headers
     * @param params
     * @param contentType
     * @param cb
     */
    public static JRequestReceipt postJson(Context ctx, String url, Header[] headers, JRequestParams params, String contentType, //
            JResponseInterface cb) {
        return getHttpClient().post(ctx, url, headers, params, contentType, cb);
    }

    /**
     * @category Perform a HTTP POST request, with parameters, headers,
     *           content-type, httpEntity
     * @param ctx
     * @param url
     * @param headers
     * @param entity
     * @param params
     * @param contentType
     * @param cb
     */
    public static JRequestReceipt postJson(Context ctx, String url, Header[] headers, HttpEntity entity, JRequestParams params, //
            String contentType, JResponseInterface cb) {
        return getHttpClient().post(ctx, url, headers, entity, contentType, cb);
    }

    /**
     * category Perform a HTTP PUT request, without any parameters
     * 
     * @param url
     * @param cb
     */
    public static JRequestReceipt putJson(String url, JResponseInterface cb) {
        return getHttpClient().put(url, cb);
    }

    /**
     * category Perform a HTTP PUT request, with parameters
     * 
     * @param url
     * @param params
     * @param cb
     */
    public static JRequestReceipt putJson(String url, JRequestParams params, JResponseInterface cb) {
        return getHttpClient().put(url, params, cb);
    }

    /**
     * @param ctx
     * @param mayInterruptIfRunning : 是否中断正在运行的线程
     */
    public static void cancelJsonRequests(Context ctx, boolean mayInterruptIfRunning) {
        getHttpClient().cancelRequests(ctx, mayInterruptIfRunning);
    }

    public static void cancelJsonRequests(String url, boolean mayInterruptIfRunning) {
        getHttpClient().cancelRequest(url, mayInterruptIfRunning);
    }

    public static void cancelJsonRequests(ArrayList<String> urls, boolean mayInterruptIfRunning) {
        getHttpClient().cancelRequest(urls, mayInterruptIfRunning);
    }

    public static void cancelAllJsonRequests(boolean mayInterruptIfRunning) {
        getHttpClient().cancelAllRequest(mayInterruptIfRunning);
    }

    /**
     * @category set JSON request timeout
     * @param time
     */
    public static void setJsonTimeout(int time) {
        getHttpClient().setTimeout(time);
    }

    public static void setRequestRetryCount(int count) {

    }

    /**
     * initialize image cache
     */
    public static void initCache(Context ctx, JCacheParams params) {
        mJCacheParams = params;
        getImageProvider(ctx);
    }

    /**
     * Request HTTP Image
     * 
     * @param ctx
     * @param key
     * @param fb
     */
    public static void getImage(Context ctx, String key, JFetchBackListener fb) {
        getImageProvider(ctx).getBitmap(key, fb);
    }

    /**
     * Request HTTP Image by w h
     * 
     * @param ctx
     * @param key
     * @param size
     * @param fb
     */
    public static void getImage(Context ctx, String key, int[] size, JFetchBackListener fb) {
        getImageProvider(ctx).getBitmap(key, size, fb);
    }

    /**
     * flush cache when change context
     */
    public static void flushImageCache(Context ctx) {
        getImageProvider(ctx).flush();
    }

    /**
     * close cache when exit APP
     */
    public static void closeImageCache(Context ctx) {
        getImageProvider(ctx).close();
    }

    public static void cancelTask(Context ctx, String url, boolean mayInterruptIfRunning) {
        getImageProvider(ctx).cancelTask(url, mayInterruptIfRunning);
    }

    public static void cancelTask(Context ctx, ArrayList<String> urls, boolean mayInterruptIfRunning) {
        getImageProvider(ctx).cancelTask(urls, mayInterruptIfRunning);
    }
}
