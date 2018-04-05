package util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import constants.ConfigConstants;

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
		options.put("upload_preset", ConfigConstants.get("cloudinary_upload_prest"));
		
		Map uploadResult = cloudinary.uploader().upload(file, options);
		return uploadResult;
	}
	public static void deleteImages(List<String> public_ids, Map options) throws Exception{
		if (options == null){
			options = ObjectUtils.emptyMap();
		}
		final Map lOptions  = options;
		final List<String> lPublicIds = public_ids;
		System.out.println("started to delete");
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					api.deleteResources(lPublicIds, lOptions);
					System.out.println("deletion complete");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();
		System.out.println(api);
		System.out.println("deletion submitted");
		
		//return result;
	}
}
