package com.example.demo;

import org.apache.commons.io.FileUtils;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@SpringBootApplication
@RestController
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RequestMapping("/")
	String sayHello() {
		saveAllScripts();
		return "Hello World!";
	}

	private void saveAllScripts() {
		String[] scripts = {
				"caption_image.py",
				"caption_input.py",
				"instance_segmentation.py",
				"object_detection.py",
				"semantic_segmentation.py"
		};

		Arrays.stream(scripts).forEach(script -> {
			try {
				FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream(script), new File("/app/vision/" + script));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@PostMapping(value = "/semantic-segmentation", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] semanticSegmentation(@RequestParam("image") MultipartFile image) throws IOException {
		File file = new File("/app/vision/result.txt");
		if (file.exists()) {
			FileUtils.delete(file);
		}
		FileUtils.deleteDirectory(new File("/app/result"));
		saveResizedImage(ImageIO.read(image.getInputStream()));
		run("/app/vision/semantic_segmentation.py", null);
		FileUtils.delete(new File("/app/vision/input.jpg"));
		String result = readResult();
		return Files.readAllBytes(Paths.get(result));
	}

	@PostMapping(value = "/instance-segmentation", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] instanceSegmentation(@RequestParam("image") MultipartFile image,
													 @RequestParam("input") String input) throws IOException {
		File file = new File("/app/vision/result.txt");
		if (file.exists()) {
			FileUtils.delete(file);
		}
		FileUtils.deleteDirectory(new File("/app/result"));
		saveResizedImage(ImageIO.read(image.getInputStream()));
		run("/app/vision/instance_segmentation.py", input);
		FileUtils.delete(new File("/app/vision/input.jpg"));
		String result = readResult();
		return Files.readAllBytes(Paths.get(result));
	}

	@PostMapping(value = "/object-detection", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] objectDetection(@RequestParam("image") MultipartFile image) throws IOException {
		File file = new File("/app/vision/result.txt");
		if (file.exists()) {
			FileUtils.delete(file);
		}
		FileUtils.deleteDirectory(new File("/app/result"));
		saveResizedImage(ImageIO.read(image.getInputStream()));
		run("/app/vision/object_detection.py", null);
		FileUtils.delete(new File("/app/vision/input.jpg"));
		String result = readResult();
		return Files.readAllBytes(Paths.get(result));
	}

	@PostMapping("/caption-image")
	public ResponseEntity<String> captionImage(@RequestParam("image") MultipartFile image) throws IOException {
		File file = new File("/app/vision/result.txt");
		if (file.exists()) {
			FileUtils.delete(file);
		}
		FileUtils.deleteDirectory(new File("/app/result"));
		saveResizedImage(ImageIO.read(image.getInputStream()));
		run("/app/vision/caption_image.py", null);
		FileUtils.delete(new File("/app/vision/input.jpg"));
		String result = readResult();
		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/caption-input")
	public ResponseEntity<String> captionInput(@RequestParam("image") MultipartFile image,
											   @RequestParam("input") String input) throws IOException {
		File file = new File("/app/vision/result.txt");
		if (file.exists()) {
			FileUtils.delete(file);
		}
		FileUtils.deleteDirectory(new File("/app/result"));
		saveResizedImage(ImageIO.read(image.getInputStream()));
		run("/app/vision/caption_input.py", input);
		FileUtils.delete(new File("/app/vision/input.jpg"));
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
		byte[] encoded = Files.readAllBytes(Paths.get("/app/vision/result.txt"));
		return new String(encoded, StandardCharsets.UTF_8);
	}

	private void saveResizedImage(BufferedImage originalImage) throws IOException {
		File file = new File("/app/vision/input.jpg");
		int targetSize = 1024;
		if (originalImage.getWidth() <= targetSize && originalImage.getHeight() <= targetSize) {
			ImageIO.write(originalImage, "jpg", file);
			return;
		}

		int originalWidth = originalImage.getWidth();
		int originalHeight = originalImage.getHeight();

		// Calculate the aspect ratio
		double aspectRatio = (double) originalWidth / originalHeight;

		int newWidth, newHeight;
		if (originalWidth > originalHeight) {
			// Width is greater than height
			newWidth = targetSize;
			newHeight = (int) (newWidth / aspectRatio);
		} else {
			// Height is greater than or equal to width
			newHeight = targetSize;
			newWidth = (int) (newHeight * aspectRatio);
		}

		Image resultingImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
		BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

		ImageIO.write(outputImage, "jpg", file);
	}

}
