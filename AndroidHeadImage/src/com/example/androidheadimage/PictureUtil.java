package com.example.androidheadimage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

public class PictureUtil {

	private static BitmapFactory.Options newOpts;

	public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;

    return BitmapFactory.decodeFile(filePath, options);
    }
 
	//计算图片的缩放�?
	public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {
	             final int heightRatio = Math.round((float) height/ (float) reqHeight);
	             final int widthRatio = Math.round((float) width / (float) reqWidth);
	             inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	        return inSampleSize;
	}
	 
	public static Bitmap ResizeBitmap(String strPath,Bitmap bitmap) {
		 int degree = PictureUtil.readPictureDegree(strPath);
		 if(bitmap==null){
			 return null;
		 }
	     int width = bitmap.getWidth();
	     int height = bitmap.getHeight();
	     Matrix matrix = new Matrix();
	     // resize the bit map
	     matrix.postRotate(degree);
	     Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	     return resizedBitmap;
	}
	public static Bitmap getimage(String srcPath) {
		newOpts = new BitmapFactory.Options();
		//�?��读入图片，此时把options.inJustDecodeBounds 设回true�?
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空
		
		newOpts.inJustDecodeBounds = false;
		
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，�?��高和宽我们设置为
		float hh = 800f;//这里设置高度�?00f
		float ww = 480f;//这里设置宽度�?80f
		//缩放比�?由于是固定比例缩放，只用高或者宽其中�?��数据进行计算即可
		int be = 1;//be=1表示不缩�?
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩�?
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩�?
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false�?
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
//		bitmap=ResizeBitmap(srcPath,bitmap);
		return compressImage(bitmap);//压缩好比例大小后再进行质量压�?
	}
	public static Bitmap compressImage(Bitmap image) {
		if(image==null){
			return null;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这�?00表示不压缩，把压缩后的数据存放到baos�?
		int options = 100;
		while ((baos.toByteArray().length / 1000)>100) {	//循环判断如果压缩后图片是否大�?00kb,大于继续压缩		
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos�?
			options -= 10;//每次都减�?0
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream�?
		Bitmap resizedBitmap = BitmapFactory.decodeStream(isBm, null, newOpts);//把ByteArrayInputStream数据生成图片
		image.recycle();
		return resizedBitmap;
	}
	
	/**
	 * 读取图片属�?：旋转的角度
	 * @param path 图片绝对路径
	 * @return degree旋转的角�?
	 */
    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
                ExifInterface exifInterface = new ExifInterface(path);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return degree;
    }
}
