package neoxzim.com.neoxznativeimage;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Carlos Fonseca on 10/9/2017.
 */


@TargetApi(Build.VERSION_CODES.KITKAT)
public class NativeImageListener implements ImageReader.OnImageAvailableListener {

    private final String TAG = "ImageAvailableListener";

    private OutputStream outStream;
    private Image latestImage;
    private ByteArrayOutputStream imageStream;
    private ByteBuffer imageByteBuffer;
    private Image.Plane[] imagePlanes;
    private int rowStride, pixelStride, rowPadding, height, width, compressionLevel;
    private Bitmap bitmap, croppedBitmap;

    /**
     * Default ctor
     */
    public NativeImageListener() {
        Log.d(TAG, "NativeImageListener: Class created - default ctor");
    }


    /** Initializes image reader listener
     * @param height Height of the device screen
     * @param width Width of the device screen
     * @param outStream OutputStream to write image data
     * @param compressionLevel Compression level of the acquired images before write to outputStream (Bitmap.compress)
     */
    public NativeImageListener(int height, int width, OutputStream outStream, int compressionLevel) {
        this.height = height;
        this.width = width;
        this.compressionLevel = compressionLevel;
        this.outStream = outStream;
        Log.d(TAG, "NativeImageListener: Class created - prop ctor");
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        try {
            while ((latestImage = imageReader.acquireLatestImage()) != null) {
                imageStream = new ByteArrayOutputStream();
                imagePlanes = latestImage.getPlanes();
                imageByteBuffer = imagePlanes[0].getBuffer();
                pixelStride = imagePlanes[0].getPixelStride();
                rowStride = imagePlanes[0].getRowStride();
                rowPadding = rowStride - pixelStride * width;
                bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(imageByteBuffer);
                latestImage.close();
                croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height); //removes black border of the images
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, compressionLevel, imageStream); //delay of 70-80ms
                outStream.write(ByteBuffer.allocate(4).putInt(imageStream.size()).array());
                outStream.flush();
                outStream.write(imageStream.toByteArray());
                outStream.flush();
                bitmap.recycle();
                imageStream.close();
                croppedBitmap.recycle();
            }
        } catch (Exception e) {
            Log.d(TAG, "onImageAvailable: " + e.toString());
            e.printStackTrace();
        }
    }
}