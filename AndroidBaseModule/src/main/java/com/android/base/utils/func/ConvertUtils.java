package com.android.base.utils.func;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.android.base.utils.comp.ProviderUtils;
import com.android.base.utils.str.ConstantUtils;
import com.android.base.utils.str.StringUtils;
import com.android.base.utils.sys.ContextUtils;
import com.android.base.utils.view.ToastUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import static com.android.base.utils.str.ConstantUtils.BYTE;
import static com.android.base.utils.str.ConstantUtils.GB;
import static com.android.base.utils.str.ConstantUtils.KB;
import static com.android.base.utils.str.ConstantUtils.MB;

/**
 * Created by JiangZhiGuo on 2016/10/12.
 * describe 转换工具类
 */
public class ConvertUtils {

    /**
     * file转uri（拍照时调用）
     */
    public static Uri File2URI(File file) {
        if (file == null) return null;
        return Uri.fromFile(file);
    }

    /**
     * uri转file（相册返回调用）
     */
    public static File URI2File(Uri uri) {
        String[] project = new String[]{MediaStore.Images.ImageColumns.DATA}; // 字段名
        if (uri == null) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) data = uri.getPath();
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                DocumentsContract.isDocumentUri(ContextUtils.get(), uri)) { // KITKAT
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                // ExternalStorageProvider
                if ("primary".equalsIgnoreCase(type)) {
                    data = Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    ToastUtils.get().show("non-primary");
                }
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                data = ProviderUtils.getProviderColumnTop(contentUri, project, null, null, null);
            } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                // MediaProvider
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                data = ProviderUtils.getProviderColumnTop(contentUri, project, selection, selectionArgs, null);
            }
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) { // File
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) { // MediaStore (and general)
            if ("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
                data = uri.getLastPathSegment();
            } else {
                data = ProviderUtils.getProviderColumnTop(uri, project, null, null, null);
            }
        }
        if (data != null) return new File(data);
        return null;
    }

    /**
     * setStatusColor(int)的参数不是资源文件的索引，所以要转换
     */
    private static int get0xColor(int colorID) {
        return ContextCompat.getColor(ContextUtils.get().getBaseContext(), colorID);
    }

    private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * byteArr转hexString
     * <p>例如：</p>
     * bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns 00A8
     *
     * @param bytes byte数组
     * @return 16进制大写字符串
     */
    public static String bytes2HexString(byte[] bytes) {
        char[] ret = new char[bytes.length << 1];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

    /**
     * hexString转byteArr
     * <p>例如：</p>
     * hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
     *
     * @param hexString 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexString2Bytes(String hexString) {
        int len = hexString.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        char[] hexBytes = hexString.toUpperCase().toCharArray();
        byte[] ret = new byte[len >>> 1];
        for (int i = 0; i < len; i += 2) {
            ret[i >> 1] = (byte) (hex2Dec(hexBytes[i]) << 4 | hex2Dec(hexBytes[i + 1]));
        }
        return ret;
    }

    /**
     * hexChar转int
     *
     * @param hexChar hex单个字节
     * @return 0..15
     */
    private static int hex2Dec(char hexChar) {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * charArr转byteArr
     *
     * @param chars 字符数组
     * @return 字节数组
     */
    public static byte[] chars2Bytes(char[] chars) {
        int len = chars.length;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) (chars[i]);
        }
        return bytes;
    }

    /**
     * byteArr转charArr
     *
     * @param bytes 字节数组
     * @return 字符数组
     */
    public static char[] bytes2Chars(byte[] bytes) {
        int len = bytes.length;
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) (bytes[i] & 0xff);
        }
        return chars;
    }

    /**
     * 字节数转以unit为单位的size
     */
    public static double byte2Size(long byteNum, ConstantUtils.MemoryUnit unit) {
        if (byteNum < 0) return -1;
        switch (unit) {
            default:
            case BYTE:
                return (double) byteNum / BYTE;
            case KB:
                return (double) byteNum / KB;
            case MB:
                return (double) byteNum / MB;
            case GB:
                return (double) byteNum / GB;
        }
    }

    /**
     * 以unit为单位的size转字节数
     *
     * @param size 大小
     */
    public static long size2Byte(long size, ConstantUtils.MemoryUnit unit) {
        if (size < 0) return -1;
        switch (unit) {
            default:
            case BYTE:
                return size * BYTE;
            case KB:
                return size * KB;
            case MB:
                return size * MB;
            case GB:
                return size * GB;
        }
    }

    /**
     * 字节数转合适大小
     * <p>保留3位小数</p>
     *
     * @param byteNum 字节数
     * @return 1...1024 unit
     */
    public static String byte2FitSize(long byteNum) {
        if (byteNum < 0) {
            return "shouldn't be less than zero!";
        } else if (byteNum < KB) {
            return String.format(Locale.getDefault(), "%.3fB", (double) byteNum);
        } else if (byteNum < MB) {
            return String.format(Locale.getDefault(), "%.3fKB", (double) byteNum / KB);
        } else if (byteNum < GB) {
            return String.format(Locale.getDefault(), "%.3fMB", (double) byteNum / MB);
        } else {
            return String.format(Locale.getDefault(), "%.3fGB", (double) byteNum / GB);
        }
    }

    /**
     * inputStream转outputStream
     *
     * @param is 输入流
     * @return outputStream子类
     */
    public static ByteArrayOutputStream input2OutputStream(InputStream is) {
        if (is == null) return null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] b = new byte[KB];
            int len;
            while ((len = is.read(b, 0, KB)) != -1) {
                os.write(b, 0, len);
            }
            return os;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            FileUtils.closeIO(is);
        }
    }

    /**
     * outputStream转inputStream
     *
     * @param out 输出流
     * @return inputStream子类
     */
    public ByteArrayInputStream output2InputStream(OutputStream out) {
        if (out == null) return null;
        return new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
    }

    /**
     * inputStream转byteArr
     *
     * @param is 输入流
     * @return 字节数组
     */
    public static byte[] inputStream2Bytes(InputStream is) {
        return input2OutputStream(is).toByteArray();
    }

    /**
     * byteArr转inputStream
     *
     * @param bytes 字节数组
     * @return 输入流
     */
    public static InputStream bytes2InputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * outputStream转byteArr
     *
     * @param out 输出流
     * @return 字节数组
     */
    public static byte[] outputStream2Bytes(OutputStream out) {
        if (out == null) return null;
        return ((ByteArrayOutputStream) out).toByteArray();
    }

    /**
     * outputStream转byteArr
     *
     * @param bytes 字节数组
     * @return 字节数组
     */
    public static OutputStream bytes2OutputStream(byte[] bytes) {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            os.write(bytes);
            return os;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            FileUtils.closeIO(os);
        }
    }

    /**
     * inputStream转string按编码
     *
     * @param is          输入流
     * @param charsetName 编码格式
     * @return 字符串
     */
    public static String inputStream2String(InputStream is, String charsetName) {
        if (TextUtils.isEmpty(charsetName)) return null;
        try {
            return new String(inputStream2Bytes(is), charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * string转inputStream按编码
     *
     * @param string      字符串
     * @param charsetName 编码格式
     * @return 输入流
     */
    public static InputStream string2InputStream(String string, String charsetName) {
        if (TextUtils.isEmpty(charsetName))
            return null;
        try {
            return new ByteArrayInputStream(string.getBytes(charsetName));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * outputStream转string按编码
     *
     * @param out         输出流
     * @param charsetName 编码格式
     * @return 字符串
     */
    public static String outputStream2String(OutputStream out, String charsetName) {
        if (out == null) return null;
        try {
            return new String(outputStream2Bytes(out), charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * string转outputStream按编码
     *
     * @param string      字符串
     * @param charsetName 编码格式
     * @return 输入流
     */
    public static OutputStream string2OutputStream(String string, String charsetName) {
        if (StringUtils.isEmpty(charsetName)) return null;
        try {
            return bytes2OutputStream(string.getBytes(charsetName));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * bitmap转byteArr
     *
     * @param bitmap bitmap对象
     * @param format 格式
     * @return 字节数组
     */
    public static byte[] bitmap2Bytes(Bitmap bitmap, Bitmap.CompressFormat format) {
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();
    }

    /**
     * byteArr转bitmap
     *
     * @param bytes 字节数组
     * @return bitmap对象
     */
    public static Bitmap bytes2Bitmap(byte[] bytes) {
        return (bytes == null || bytes.length == 0) ? null : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * drawable转bitmap
     *
     * @param drawable drawable对象
     * @return bitmap对象
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * bitmap转drawable
     *
     * @param res    resources对象
     * @param bitmap bitmap对象
     * @return drawable对象
     */
    public static Drawable bitmap2Drawable(Resources res, Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(res, bitmap);
    }

    /**
     * drawable转byteArr
     *
     * @param drawable drawable对象
     * @param format   格式
     * @return 字节数组
     */
    public static byte[] drawable2Bytes(Drawable drawable, Bitmap.CompressFormat format) {
        return bitmap2Bytes(drawable2Bitmap(drawable), format);
    }

    /**
     * byteArr转drawable
     *
     * @param res   resources对象
     * @param bytes 字节数组
     * @return drawable对象
     */
    public static Drawable bytes2Drawable(Resources res, byte[] bytes) {
        return bitmap2Drawable(res, bytes2Bitmap(bytes));
    }

    /**
     * 将value转化成px(没算四舍五入)
     *
     * @param type eg: TypedValue.COMPLEX_UNIT_DIP
     */
    public static int getpx(int type, float value) {
        DisplayMetrics metrics = ContextUtils.get().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(type, value, metrics);
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public static int dp2px(float dpValue) {
        final float scale = ContextUtils.get().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param pxValue px值
     * @return dp值
     */
    public static int px2dp(float pxValue) {
        final float scale = ContextUtils.get().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp转px
     *
     * @param spValue sp值
     * @return px值
     */
    public static int sp2px(float spValue) {
        final float fontScale = ContextUtils.get().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * px转sp
     *
     * @param pxValue px值
     * @return sp值
     */
    public static int px2sp(float pxValue) {
        final float fontScale = ContextUtils.get().getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

}