package com.markupartist.musicmachine.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

/**
 * ImageLoaded, modified from excellent class posted at wu-media.com 
 * http://wu-media.com/2009/06/android-imageloader-load-images-sequencially-in-the-background/
 */
public class ImageLoader {
    static private ImageLoader sInstance;
    static final String TAG = "ImageLoader";
    static final int IO_BUFFER_SIZE = 4096;

    static public ImageLoader getInstance() {
        if (sInstance == null) {
            sInstance = new ImageLoader();
        }
        return sInstance;
    }

	static public boolean hasInstance() {
		return sInstance != null;
	}
    
    private HashMap<String, Bitmap> mUrlToBitmap;
    private Queue<Group> mQueue;
    private DownloadThread mThread;
    private Bitmap mMissing;
    private boolean mBusy;

    /**
     * Constructor
     */
    private ImageLoader() {
        mUrlToBitmap = new HashMap<String, Bitmap>();
        mQueue = new LinkedList<Group>();
        mBusy = false;
    }

    public Bitmap get(String url) {
        return mUrlToBitmap.get(url);
    }

    public void load(ImageView image, String url) {
        load(image, url, false, null);
    }

    public void load(ImageView image, String url, boolean cache, EventHandler eventHandler) {
        load(image, url, cache, 0, eventHandler);
    }

    public void load(ImageView image, String url, boolean cache, 
            int defaultImageResource, EventHandler eventHandler) {
        if (mUrlToBitmap.get(url) != null) {
            if (image != null) {                
                if(eventHandler != null) {
                	eventHandler.onFinished(mUrlToBitmap.get(url));
                }
                
                image.setImageBitmap(mUrlToBitmap.get(url));
            }
        } else {
            if (defaultImageResource != 0) {
                image.setImageResource(defaultImageResource);
            } else {
                image.setImageBitmap(null);
            }
            queue(image, url, cache, eventHandler);
            
            if(eventHandler != null) {
            	eventHandler.onDownloadStarted();
            }
        }        
    }
 
    public void queue(ImageView image, String url, boolean cache, EventHandler eventHandler) {
        Iterator<Group> it = mQueue.iterator();
        if (image != null) {
            while (it.hasNext()) {
                if (it.next().image.equals(image)) {
                    it.remove();
                    break;
                }
            }
        } else if (url != null) {
            while (it.hasNext()) {
                if (it.next().url.equals(url)) {
                    it.remove();
                    break;
                }
            }
        }
        mQueue.add(new Group(image, url, null, cache, eventHandler));
        loadNext();
    }

    public void clearQueue() {
        mQueue.clear();
    }

    public void clearCache() {
    	// Only clear if it contains something. This reduces the log entries and make the
    	// ones printed to the log actually mean something when debugging.
    	if(mUrlToBitmap.size() > 0) {
    		Log.d(TAG, "Flushing image cache");
    		mUrlToBitmap.clear();
    	}
    }

    public void cancel() {
        clearQueue();
        if (mThread != null) {
            mThread.disconnect();
            mThread = null;
        }
    }

    public void setMissingBitmap(Bitmap bitmap) {
        mMissing = bitmap;
    }

    private void loadNext() {
        Iterator<Group> it = mQueue.iterator();
        if (!mBusy && it.hasNext()) {
            mBusy = true;
            Group group = it.next();
            it.remove();
            // double check image availability
            if (mUrlToBitmap.get(group.url) != null) {
                if (group.image != null) {                    
                    if(null != group.eventHandler) {
                    	group.eventHandler.onFinished(mUrlToBitmap.get(group.url));
                    }
                    
                    group.image.setImageBitmap(mUrlToBitmap.get(group.url));
                }
                mBusy = false;
                loadNext();
            } else {
                mThread = new DownloadThread(group);
                mThread.start();
            }
        }
    }

    private void onLoad() {
        if (mThread != null) {
        	Group group = mThread.group;
        	switch(mThread.status) {
        	case DownloadThread.STATUS_OK:
	            if (group.bitmap != null) {
	                if (group.cache) {
	                    mUrlToBitmap.put(group.url, group.bitmap);
	                }
	                if (group.image != null) {
	                    group.image.setImageBitmap(group.bitmap);
	                }
	            } else if (mMissing != null && group.image != null) {
	            	group.image.setImageBitmap(mMissing);
	            }
	            
                if(null != group.eventHandler) {
                	group.eventHandler.onFinished(group.bitmap);
                }
	            break;
        	case DownloadThread.STATUS_DECODE_FAILED:
        		if (mMissing != null && group.image != null) {
        			group.image.setImageBitmap(mMissing);
	            }

                if(null != group.eventHandler) {
                	group.eventHandler.onDecodeFailed();
                }
        		break;
        	case DownloadThread.STATUS_ERROR:
	            if(null != group.eventHandler) {
	            	group.eventHandler.onDownloadError();
	            }
	            break;
        	}
        }
        mThread = null;
        mBusy = false;
        loadNext();
    }
    
    /**
     * @author marco
     * Event handler used to notify the caller about the image download status.
     */
    public interface EventHandler {
    	/**
    	 * Called before starting downloading an image.
    	 * @note This will not be called if the image is cached.
    	 */
    	public void onDownloadStarted();
    	
    	/**
    	 * Called when a download error occurs.
    	 */
    	public void onDownloadError();
    	
    	/**
    	 * Called when download finished successfully.
    	 */
    	public void onFinished(Bitmap bitmap);
    	
    	/**
    	 * Called when an image was downloaded but the decoding failed.
    	 */
    	public void onDecodeFailed();
    }

    private static class Group {
        public Group(ImageView image, String url, Bitmap bitmap, boolean cache, EventHandler eventHandler) {
            this.image = image;
            this.url = url;
            this.bitmap = bitmap;
            this.cache = cache;
            this.eventHandler = eventHandler;
        }

        public ImageView image;
        public String url;
        public Bitmap bitmap;
        public boolean cache;
        public EventHandler eventHandler;

    }

    private class DownloadThread extends Thread {
        final Handler threadHandler = new Handler();
        final Runnable threadCallback = new Runnable() {
            public void run() {
                onLoad();
            }
        };
        private HttpURLConnection mConn;
        public Group group;
        
        /**
         * Image downloading status code.
         */
        public int status;
        
        /**
         * Status ok. Image downloaded and decoded successfully.
         */
        public static final int STATUS_OK = 0;
        
        /**
         * Download error. Usually a network problem.
         */
        public static final int STATUS_ERROR = 1;
        
        /**
         * Image downloaded but decoding failed.
         */
        public static final int STATUS_DECODE_FAILED = 2;

        public DownloadThread(Group group) {
            this.group = group;
        }

        @Override
        public void run() {
            InputStream inStream = null;
            BufferedOutputStream out = null;
            mConn = null;
            try {            	
            	Log.d(TAG, "Downloading " + group.url);
                mConn = (HttpURLConnection) new URL(group.url).openConnection();
                mConn.setDoInput(true);
                mConn.connect();
                inStream = mConn.getInputStream();
                final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
                copy(inStream, out);
                out.flush();
                final byte[] data = dataStream.toByteArray();
                group.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if(null == group.bitmap) {
                	status = STATUS_DECODE_FAILED;
                } else {
                	status = STATUS_OK;
                }

                inStream.close();
                mConn.disconnect();
                inStream = null;
                mConn = null;
            } catch (Exception ex) {
            	status = STATUS_ERROR;
            	Log.d(TAG, "Download failed: " + ex.getMessage());
                // nothing
            }
            
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception ex) {
                }
            }
            
            disconnect();
            inStream = null;
            mConn = null;
            threadHandler.post(threadCallback);
        }

        private void copy(InputStream in, OutputStream out) throws IOException {
            byte[] b = new byte[IO_BUFFER_SIZE];
            int read;
            while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
            }
        }
        
        public void disconnect() {
            if (mConn != null) {
                mConn.disconnect();
            }
        }
    }
}
