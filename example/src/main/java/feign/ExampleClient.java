package feign;

import feign.RequestLine;

@FeignClient(baseUrl = "https://example.com")
public interface ExampleClient {

	@RequestLine("GET /get")
    ExampleBean get();
}
