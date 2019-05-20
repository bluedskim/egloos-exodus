package org.dskim.egloosExodus;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * pandoc사용하지 않기로 함. html을 md 로 변환이 잘 안됨. -.-
 */
public class PandocDemo {
	private static final Logger logger = LogManager.getLogger(PandocDemo.class);

	public static void main(String[] args) throws Exception {
		test3();
	}

	public static void test1() throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("docker exec keen_wing pandoc --help");
		logger.debug(IOUtils.toString(pr.getInputStream()));
	}

	public static void test2() throws IOException {
		Runtime rt = Runtime.getRuntime();
		//Process pr = rt.exec("docker exec keen_wing pandoc --help");
		Process pr = rt.exec("docker exec keen_wing pandoc");
		OutputStream stdin = pr.getOutputStream ();
		stdin.write("Hello *pandoc*!\n".getBytes());
		stdin.flush();
		stdin.close();

		logger.debug(IOUtils.toString(pr.getInputStream()));
	}

	/**
	 * 잘됨
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void test3() throws IOException, InterruptedException {
		//ProcessBuilder builder = new ProcessBuilder("/home/bluedskim/Utils/shell/echoFromStdIn.sh");
		ProcessBuilder builder = new ProcessBuilder("pandoc");
		builder.redirectErrorStream(true);
		Process pr = builder.start();

		//Process pr = rt.exec("docker exec keen_wing pandoc");
		OutputStream stdin = pr.getOutputStream ();
		InputStream stderr = pr.getErrorStream ();
		InputStream stdout = pr.getInputStream ();
		stdin.write("Hello *pandoc*!\n\n- one\n- two\n".getBytes());
		stdin.flush();
		stdin.close();

		pr.waitFor();
		logger.debug("exitValue={}, out={}", pr.exitValue(), IOUtils.toString(stdout));
		//logger.debug("stderr={}", IOUtils.toString(stderr));

		stderr.close();
		stdout.close();
	}
}
