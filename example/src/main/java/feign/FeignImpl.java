package feign;

import feign.Feign;
import feign.gson.GsonDecoder;

public class FeignImpl {

	public static <T> T get(Class<T> clz) {
		return Feign.builder()//
				.decoder(new GsonDecoder())//
				.target(clz, clz.getAnnotation(FeignClient.class).baseUrl());
	}
}
