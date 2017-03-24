import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class summary {

	BufferedImage img;
	int width = 480;
	int height = 270;
	static int threshold = 15;
	static int sumlen = 150;

	public ArrayList<Integer> getSummaryPointer(String[] args) {
		// int width = 480;
		// int height = 270;
		ArrayList<Integer> firstf = new ArrayList<Integer>();

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);

			// int length = 5 * 60 * 15;
			int length = (int) (file.length() / (width * height * 3));
			int nums = 0;

			// FileWriter file2 = new FileWriter("output.txt");
			// file2.write("");
			// file2.close();

			int previousY = 0;
			int Y = 0;
			int dif = 0;

			/*
			 * record the first frame of each section
			 */
			while (nums < length) {
				long len = width * height * 3;
				byte[] bytes = new byte[(int) len];
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length
						&& (numRead = is.read(bytes, offset, bytes.length
								- offset)) >= 0) {
					offset += numRead;
				}

				int ind = 0;
				float totalY = 0;
				for (int y = 0; y < height; y++) {

					for (int x = 0; x < width; x++) {

						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind + height * width];
						byte b = bytes[ind + height * width * 2];

						int pix = 0xff000000 | ((r & 0xff) << 16)
								| ((g & 0xff) << 8) | (b & 0xff);
						// int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						img.setRGB(x, y, pix);
						ind++;

						totalY += 0.299 * (r & 0xff) + 0.587 * (g & 0xff)
								+ 0.144 * (b & 0xff);

					}
				}

				Y = (int) totalY / (480 * 270);
				dif = Math.abs(Y - previousY);

				if (dif > threshold)
					firstf.add(nums);

				previousY = Y;
				nums++;
				if (nums > length) {
					break;
				}

			}

			if (firstf.size() > 1) {
				for (int i = 1; i < firstf.size(); i++) {
					if (firstf.get(i) - firstf.get(i - 1) < 75) {
						firstf.remove(i);
						i--;
					}
				}
			}

			System.out.print(firstf + "\n");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return firstf;
	}

	public void showSum(String[] args, ArrayList<Integer> firstf) {
		// int width = 480;
		// int height = 270;

		// img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);

			File outputfile = new File("Summary.rgb");
			OutputStream out = new FileOutputStream(outputfile);

			File list1 = new File("list1.txt");
			OutputStream out_list1 = new FileOutputStream(list1);

//			File list2 = new File("list2.txt");
//			OutputStream out_list2 = new FileOutputStream(list2);

			// AudioClip ac = new AudioClip("shortcut.wav");
			File outputwav = new File("Summary.wav");
			OutputStream out_wav = new FileOutputStream(outputwav);

			int length = 5 * 60 * 15;
			int nums = 0;

			// FileWriter file2 = new FileWriter("output.txt");
			// file2.write("");
			// file2.close();
			//
			int previousY = 0;
			int Y = 0;
			int dif = 0;

			for (int i = 0; i < firstf.size(); i++) {
				int obj = (int) firstf.get(i);
				String str_obj = String.valueOf(obj) + "\n";
				byte[] obj_bytes = str_obj.getBytes();
				out_list1.write(obj_bytes);
			}
			out_list1.close();

			nums = 0;
			int index = 0;
			int maxindex = firstf.size();
			int temp = (int) firstf.get(index);
			int limit = 0;

			while (nums < length) {
				if (nums == temp) {
					if (index != maxindex - 1) {
						int diff = (int) firstf.get(index + 1) - temp;
						if (diff <= sumlen)
							limit = diff;
						else
							limit = sumlen;
					} else {
						if (4500 - temp <= sumlen)
							limit = 4500 - temp;
						else
							limit = sumlen;
					}
					index++;
					if (index < maxindex) {
						temp = (int) firstf.get(index);
					}

					for (int i = 0; i < limit; i++) {
						long len = width * height * 3;
						byte[] bytes = new byte[(int) len];
						int offset = 0;
						int numRead = 0;
						while (offset < bytes.length
								&& (numRead = is.read(bytes, offset,
										bytes.length - offset)) >= 0) {
							offset += numRead;
						}

						out.write(bytes);
						AudioClip ac = new AudioClip(args[1]);
						ac.write_audio(nums, limit, out_wav);

						nums++;
						if (nums > length) {
							break;
						}

					}
					System.out.println(index);
				} else {
					long len = width * height * 3;
					byte[] bytes = new byte[(int) len];
					int offset = 0;
					int numRead = 0;
					while (offset < bytes.length
							&& (numRead = is.read(bytes, offset, bytes.length
									- offset)) >= 0) {
						offset += numRead;
					}

					nums++;
					if (nums > length) {
						break;
					}

				}

			}
			out.close();
			out_wav.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err
					.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
			return;
		}
		summary ren = new summary();

		final String audioPath = args[1];

		ArrayList arr = ren.getSummaryPointer(args);
		ren.showSum(args, arr);
	}
}