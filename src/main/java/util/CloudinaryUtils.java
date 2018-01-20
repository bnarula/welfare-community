package util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryUtils {
	private static Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
			  "cloud_name", "welfare-cdn",
			  "api_key", "921954663419325",
			  "api_secret", "BaKypTYEm8ZWh_Bzf-t1qRNaCBg"));
	private static Api api = cloudinary.api();
	public static Map deleteImage(String public_id, Map options) throws IOException{
		if (options == null){
			options = ObjectUtils.emptyMap();
		}
		Map result = cloudinary.uploader().destroy(public_id, options);
		return result;
	}
	public static Map uploadImage(File file, Map options) throws IOException{
		if (options == null){
			options = ObjectUtils.emptyMap();
		}
		Map uploadResult = cloudinary.uploader().upload(file, options);
		return uploadResult;
	}
	public static void deleteImages(String[] public_ids, Map options) throws Exception{
		if (options == null){
			options = ObjectUtils.emptyMap();
		}
		final Map lOptions  = options;
		final String[] lPublicIds = public_ids;
		System.out.println("started to delete");
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					api.deleteResources(Arrays.asList(lPublicIds), lOptions);
					System.out.println("deletion complete");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();
		System.out.println(api);
		api = null;
		System.out.println("deletion submitted");
		
		//return result;
	}
}
