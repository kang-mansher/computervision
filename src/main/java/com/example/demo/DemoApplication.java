package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@RestController
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RequestMapping("/")
	String sayHello() {
		return "Hello World!";
	}

	@PostMapping(value = "/semantic-segmentation", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] semanticSegmentation(@RequestParam("image") MultipartFile image) throws IOException {
		image.transferTo(new File("/tmp/vision/input.jpg"));
		run("/tmp/vision/semantic_segmentation.py", null);
		Files.deleteIfExists(Paths.get("/tmp/vision/input.jpg"));
		String result = readResult();
		return Files.readAllBytes(Paths.get(result));
	}

	@PostMapping(value = "/instance-segmentation", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] instanceSegmentation(@RequestParam("image") MultipartFile image,
													 @RequestParam("input") String input) throws IOException {
		image.transferTo(new File("/tmp/vision/input.jpg"));
		run("/tmp/vision/instance_segmentation.py", input);
		Files.deleteIfExists(Paths.get("/tmp/vision/input.jpg"));
		String result = readResult();
		return Files.readAllBytes(Paths.get(result));
	}

	@PostMapping(value = "/object-detection", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] objectDetection(@RequestParam("image") MultipartFile image) throws IOException {
		image.transferTo(new File("/tmp/vision/input.jpg"));
		run("/tmp/vision/object_detection.py", null);
		Files.deleteIfExists(Paths.get("/tmp/vision/input.jpg"));
		String result = readResult();
		return Files.readAllBytes(Paths.get(result));
	}

	@PostMapping("/caption-image")
	public ResponseEntity<String> captionImage(@RequestParam("image") MultipartFile image) throws IOException {
		image.transferTo(new File("/tmp/vision/input.jpg"));
		run("/tmp/vision/caption_image.py", null);
		Files.deleteIfExists(Paths.get("/tmp/vision/input.jpg"));
		String result = readResult();
		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/caption-input")
	public ResponseEntity<String> captionInput(@RequestParam("image") MultipartFile image,
											   @RequestParam("input") String input) throws IOException {
		image.transferTo(new File("/tmp/vision/input.jpg"));
		run("/tmp/vision/caption_input.py", input);
		Files.deleteIfExists(Paths.get("/tmp/vision/input.jpg"));
		String result = readResult();
		if (result.startsWith(input))
			result =  result.substring(input.length());
		return ResponseEntity.ok().body(result);
	}

	private void run(String pythonScript, String arg1) {
		try {
			String pythonInterpreter = "python3";

			ProcessBuilder pb;
			if (arg1 == null) {
				pb = new ProcessBuilder(pythonInterpreter, pythonScript).inheritIO();
			} else {
				pb = new ProcessBuilder(pythonInterpreter, pythonScript, arg1).inheritIO();
			}

			Process process = pb.start();
			System.out.println("Python script started: " + pythonScript);
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				System.out.println("Python script executed successfully");
			} else {
				System.out.println("Error executing Python script. Exit code: " + exitCode);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String readResult() throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get("/tmp/vision/result.txt"));
		return new String(encoded, StandardCharsets.UTF_8);
	}

}
