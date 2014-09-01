package com.example.androidheadimage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class MainActivity extends Activity {
	/***
	 * 使用照相机拍照获取图片
	 */
	public static final int SELECT_PIC_BY_TACK_PHOTO = 1;

	/***
	 * 使用相册中的图片
	 */
	public static final int SELECT_PIC_BY_PICK_PHOTO = 2;
	@ViewInject(R.id.iv)
	private ImageView iv;
	@ViewInject(R.id.bt_camera)
	private Button bt_camera;
	@ViewInject(R.id.bt_photo)
	private Button bt_photo;

	private Uri photoUri;
	private String picPath = null;
	private Bitmap bm;
	private String decordPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewUtils.inject(this);

		bt_photo.setOnClickListener(new OnClickListener() {
			// 拍照
			@Override
			public void onClick(View v) {
				doPickPhotoAction();
			}
		});
		bt_camera.setOnClickListener(new OnClickListener() {
			// 拍照
			@Override
			public void onClick(View v) {
				doCameraPhotoAction();
			}
		});
	}

	/**
	 * 相册选择
	 */
	protected void doCameraPhotoAction() {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);

	}

	/**
	 * 拍照
	 */
	protected void doPickPhotoAction() {

		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
			getfromePhoto();// 用户点击了从照相机获取
		} else {
			Toast.makeText(getApplicationContext(), "没有SD卡", 0).show();
		}

	}

	// 拍照获取图片
	protected void getfromePhoto() {
		// 执行拍照前，应该先判断SD卡是否存在
		String SDState = Environment.getExternalStorageState();

		if (SDState.equals(Environment.MEDIA_MOUNTED)) {

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// "android.media.action.IMAGE_CAPTURE"
			/***
			 * 需要说明一下，以下操作使用照相机拍照，拍照后的图片会存放在相册中的 这里使用的这种方式有一个好处就是获取的图片是拍照后的原图
			 * 如果不实用ContentValues存放照片路径的话，拍照后获取的图片为缩略图不清晰
			 */
			ContentValues values = new ContentValues();
			photoUri = this.getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
			/** ----------------- */
			startActivityForResult(intent, SELECT_PIC_BY_TACK_PHOTO);
		} else {
			Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		String path = doPhoto(requestCode, data);
		if (path == null)
			return;

		File file = new File(path);
		if (file == null) {// 如果没有选择.退出程序
			System.exit(0);
		}
		// Bitmap bitmap = EditImage(file);
		Bitmap decodeFile = BitmapFactory.decodeFile(path).copy(
				Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(decodeFile);
		int width = decodeFile.getWidth();
		int height = decodeFile.getHeight();
		Paint paint = new Paint();
		paint.setAntiAlias(true);//抗锯齿
		paint.setColor(Color.RED);
		canvas.drawCircle(width - (int) Math.rint(width * 0.2),
				(int) Math.rint(width * 0.2), (int) Math.rint(width * 0.2),
				paint);
		Paint paint1 = new Paint();
		paint1.setColor(Color.WHITE);
		int textSize = (int) Math.rint(width * 0.2);// 根据图片宽度计算数字的size
		paint1.setTextSize(textSize);
		canvas.drawText("1", width - (int) Math.rint(width * 0.25),
				(int) Math.rint(width * 0.25), paint1);

		iv.setImageBitmap(decodeFile);

		SaveImageToSDcardUtil util = new SaveImageToSDcardUtil();
		String uuidRaw = UUID.randomUUID().toString();
		String toSDCard = util.bitMapToSDCard(decodeFile, uuidRaw + ".jpg",
				"headimage");
		Toast.makeText(getApplicationContext(), "保存路径：" + toSDCard, 1).show();
		super.onActivityResult(requestCode, resultCode, data);

	}

	/**
	 * 处理图片
	 * 
	 * @param file
	 * @throws IOException
	 */
	// private Bitmap EditImage(File file) throws IOException {
	//
	// BufferedImage img = ImageIO.read(file);
	// Integer imgW = img.getWidth();
	// // ///////////////////////
	// Graphics2D graphics2d1 = (Graphics2D) img.createGraphics();
	// graphics2d1.setColor(Color.RED);
	// graphics2d1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);// 抗锯齿
	// Ellipse2D.Double shape = new Ellipse2D.Double(imgW
	// - (int) Math.rint(imgW * 0.3), 0, (int) Math.rint(imgW * 0.3),
	// (int) Math.rint(imgW * 0.3));// 生成圆形,参数为左,上,宽,高(宽高一样为圆形)
	// graphics2d1.fill(shape);// 红色填充圆形
	//
	// graphics2d1.setColor(Color.WHITE);
	// String s = "1";
	// graphics2d1.setFont(new Font("宋体", Font.BOLD, (int) Math
	// .rint(imgW * 0.2)));
	//
	// graphics2d1.drawString(s, imgW - (int) Math.rint(imgW * 0.43) / 2,
	// (int) Math.rint(imgW * 0.22));// 画出"1"的位置,参数:字符串,左,上
	// ByteArrayOutputStream stream = new ByteArrayOutputStream(1024 * 1024);
	// ImageIO.write(img, "jpg", stream);// 生成图片文件到桌面
	// byte[] byteArray = stream.toByteArray();
	// Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
	// byteArray.length);
	// return bitmap;
	// }

	private String doPhoto(int requestCode, Intent data) {
		if (requestCode == SELECT_PIC_BY_PICK_PHOTO) // 从相册取图片，有些手机有异常情况，请注意
		{
			if (data == null) {
				Toast.makeText(this, "您还没有选择图片", Toast.LENGTH_LONG).show();
				return null;
			}
			photoUri = data.getData();
			if (photoUri == null) {
				Toast.makeText(this, "您还没有选择图片", Toast.LENGTH_LONG).show();
				return null;
			}
		}
		String[] pojo = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(photoUri, pojo, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
			picPath = cursor.getString(columnIndex);
			// 4.0以上的版本会自动关闭 (4.0--14;; 4.0.3--15)
			if (Integer.parseInt(Build.VERSION.SDK) < 14) {
				cursor.close();
			}
		}
		if (picPath != null && !"".equals(picPath)) {
			bm = PictureUtil.getimage(picPath);
			// bm = PictureUtil.getimage(photoUri.toString());
		} else {

			// sony （SONY）Xperia T2（XM50t）适配问题
			try {
				ContentResolver resolver = getContentResolver();
				bm = MediaStore.Images.Media.getBitmap(resolver, photoUri);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// bm = getBitmapFromFile(picPath, 720, 960);
		if (bm == null) {
			Toast.makeText(this, "您还没有选择图片", Toast.LENGTH_LONG).show();
			return null;
		}
		SaveImageToSDcardUtil util = new SaveImageToSDcardUtil();
		String uuidRaw = UUID.randomUUID().toString();
		if (bm != null) {
			decordPath = util.bitMapToSDCard(bm, uuidRaw + ".jpg", "rz");
		} else {
			Toast.makeText(this, "您还没有选择图片", Toast.LENGTH_LONG).show();
			return null;
		}
		return decordPath;

	}
}
