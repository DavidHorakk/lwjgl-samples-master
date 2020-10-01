package lvl2advanced.p03texture.p03volume;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import lwjglutils.OGLTexImageByte;
import lwjglutils.OGLTexImageFloat;
import lwjglutils.OGLTextureVolume;

/**
 * Volume data sets reading and processing
 *  
 * @author PGRF FIM UHK 
 * @version 2016
 */

public class VolumeData {
	public enum DATA_SET {
		CT_head_byte, MRI_head_byte, CT_head2_byte, PET_mouse_short, Beetle_short, Adam_color_RGB, Func_byte
	}

	private static byte[] readVolumeData(String fileName, int x, int y, int z,
			int c, int headerSize) {
		byte[] header = null;
		byte[] buffer = null;
		System.out.print("Reading volume data file: " + fileName);

		InputStream is = null; 
		BufferedInputStream bis = null;
		
		try {
			is = VolumeData.class.getResourceAsStream(fileName);

			if (is == null) {
				System.out.println(" ...File not found ");
				return null;
			}

			bis = new BufferedInputStream(is);

			header = new byte[headerSize];
			buffer = new byte[x * y * z * c];
			System.out.print(" header " + bis.read(header) + " length " + bis.read(buffer));
			 is.close();
			 bis.close();
			System.out.println(" ... OK");
		} catch (IOException e) {
			System.err.println(" failed");
			System.err.println(e.getMessage());
		}  
		return buffer;
	}

	
	/**
	 * Read volume data sets and convert it to the OGLTexture 
	 * 
	 * @param dataSet
	 *            volume data set selection
	 * @return
	 *            new instance of OGLTexture
	 */
	public static OGLTextureVolume readData(DATA_SET dataSet) {
		switch (dataSet) {
		case CT_head_byte: // CT head, byte per voxel
		{
			byte[] data = readVolumeData("/vol/hlava_reduced.raw", 56, 68, 85, 1, 0);
			if (data == null)
				return null;
			OGLTexImageByte volumeData = new OGLTexImageByte(56, 68, 85,
					new OGLTexImageByte.FormatIntensity(), data);
			return new OGLTextureVolume(volumeData);
		}
		case MRI_head_byte: // MRI head, byte per voxel
		{
			byte[] data = readVolumeData("/vol/mri_head_reduced.raw", 44, 59, 46, 1, 0);
			if (data == null)
				return null;
			OGLTexImageByte volumeData = new OGLTexImageByte( 44, 59, 46,
					new OGLTexImageByte.FormatIntensity(), data);
			return new OGLTextureVolume(volumeData);
		}
		case CT_head2_byte: // CT head, byte per voxel
		{
			byte[] data = readVolumeData("/vol/head256_reduced.raw", 64, 64, 56, 1, 0);
			if (data == null)
				return null;
			OGLTexImageByte volumeData = new OGLTexImageByte(64, 64, 56,
					new OGLTexImageByte.FormatIntensity(), data);
			return new OGLTextureVolume(volumeData);
		}
			case PET_mouse_short: // PET short per voxel
			{
				byte[] data = readVolumeData("/vol/mouse0_reduced.raw", 37, 37, 69, 2, 0);
				if (data == null)
					return null;
				//float[] floatData = new float[data.length / 2];
				float[] floatData = new float[data.length / 2] ;
				float max = -10000;
				float min = 10000;
				for (int i = 0; i < floatData.length; i++) {
					floatData[i] = (((int) (0xff & data[2 * i])) | ((int) data[2 * i + 1] << 12));
					if (max < floatData[i])
						max = floatData[i];
					if (min > floatData[i])
						min = floatData[i];
				}
				//System.out.println(max + " " + min);
				max = 1000;
				for (int i = 0; i < floatData.length; i++) {
					if (floatData[i] > max)
						floatData[i] = floatData[i] / max;
					else
						floatData[i] = floatData[i] / max;
				}
				OGLTexImageFloat volumeData = new OGLTexImageFloat(37, 37, 69,
						new OGLTexImageFloat.FormatIntensity(), floatData);
				return new OGLTextureVolume(volumeData);
			}

			case Beetle_short: // BEETLE short per voxel
			{
				byte[] data = readVolumeData("/vol/beetle.raw", 832, 832, 494, 2, 6);
				if (data == null)
					return null;
				//float[] floatData = new float[data.length / 2];
				float[] floatData = new float[data.length / 2] ;
				float max = -10000;
				float min = 10000;
				for (int i = 0; i < floatData.length; i++) {
					floatData[i] = (((int) (0xff & data[2 * i])) | ((int) data[2 * i + 1] << 12));
					if (max < floatData[i])
						max = floatData[i];
					if (min > floatData[i])
						min = floatData[i];
				}
				max = 2<<12;
				//System.out.println(max + " " + min);
				for (int i = 0; i < floatData.length; i++) {
					if (floatData[i] > max)
						floatData[i] = floatData[i] / max;
					else
						floatData[i] = floatData[i] / max;
				}
				OGLTexImageFloat volumeData = new OGLTexImageFloat(832, 832, 494,
						new OGLTexImageFloat.FormatIntensity(), floatData);
				System.out.println(volumeData);
				return new OGLTextureVolume(volumeData);
			}
			case Adam_color_RGB: // Adam head color data, 3 bytes per voxel
		{
			byte[] data = readVolumeData("/vol/AheadC_reduced.raw", 47, 61, 57, 3, 0);
			if (data == null)
				return null;
			byte[] colorData = new byte[data.length];
			for (int i = 0; i < data.length / 3; i++) {
				colorData[3 * i] = data[i];
				colorData[3 * i + 1] = data[data.length / 3 + i];
				colorData[3 * i + 2] = data[2 * data.length / 3 + i];
			}
			OGLTexImageByte volumeData = new OGLTexImageByte(47, 61, 57,
					new OGLTexImageByte.Format(3), colorData);
			return new OGLTextureVolume(volumeData);
		}

		case Func_byte: // modeled by function
		{
			int m = 64;
			byte[] data = new byte[m * m * m * 4];
			OGLTexImageByte volumeData = new OGLTexImageByte(m, m, m,
					new OGLTexImageByte.Format(4), data);
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < m; j++) {
					for (int k = 0; k < m; k++) {
						double distance = Math.sqrt(Math.pow(i - m / 2, 2)
								+ Math.pow(j - m / 2, 2)
								+ Math.pow(k - m / 2, 2));
						distance = Math.abs(distance);
						volumeData.setVoxel(i, j, k, 0,
								(byte) (4 * m - distance * 4));
						volumeData.setVoxel(i, j, k, 1, (byte) (distance * 4));
						volumeData.setVoxel(i, j, k, 2, (byte) (distance * 4));
					}
				}
			}
			for (int i = 0; i < m; i++) {
				volumeData.setVoxel(m - 1, i, m - 1, 1, (byte) (i * 4));
				volumeData.setVoxel(i, m - 1, m - 1, 1, (byte) (i * 4));
				volumeData.setVoxel(m - 1, m - 1, i, 1, (byte) (i * 4));
				volumeData.setVoxel(0, i, 0, 0, (byte) (i * 4));
				volumeData.setVoxel(i, 0, 0, 0, (byte) (i * 4));
				volumeData.setVoxel(0, 0, i, 0, (byte) (i * 4));

				volumeData.setVoxel(i, i, i, 0, (byte) 0xff);
				volumeData.setVoxel(i, i, i, 1, (byte) 0xff);
				volumeData.setVoxel(i, i, i, 2, (byte) 0xff);
			}

			return new OGLTextureVolume(volumeData);
		}
		default:
			return null;
		}
	}
}
