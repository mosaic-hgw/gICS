package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
 * 							http://dx.doi.org/10.3205/17gmds146
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;


/**
 * qr code based template detector for pdf documents
 * 
 * @author Martin Bialke
 *
 */
public class ConsentTemplateDetector extends AbstractGICSBean
{

	private String outputFolder = "";
	private List<String> fileList = new ArrayList<>();

	private int pages = 0;
	private int currentPage = 0;

	// TODO wird aktuell nicht verwendet?
//	public ConsentTemplateDetector(String outputFolder)
//	{
//		this.outputFolder = outputFolder;
//	}

	public ConsentTemplateDetector()
	{}

	public String getOutputFolder()
	{
		return outputFolder;
	}

	/**
	 * remove all files
	 */
	private void cleanUp()
	{

		for (String file : fileList)
		{

			try
			{
				Files.delete(Paths.get(file));
				logger.info("deleted: " + file);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// TODO remove folder

		fileList.clear();
	}

	private List<String> decode(String filename)
	{
		try
		{
			File file = new File(filename);
			List<String> decodedTexts = decodeQRCode(file);
			if (decodedTexts == null || decodedTexts.size() == 0)
			{
				logger.debug("No QR Code found in the image");
				return new ArrayList<String>();
			}
			else
			{
				for (String code : decodedTexts)
				{
					logger.debug("Decoded text = " + code);
				}
			}

			return decodedTexts;
		}
		catch (IOException e)
		{
			logger.debug("Could not decode QR Code, IOException :: " + e.getMessage());
			return new ArrayList<String>();
		}
	}

	private List<String> decodeFromPDF(String filename)
	{

		List<String> decoded = new ArrayList<String>();

		PDDocument document = null;

		try
		{
			File sourceFile = new File(filename);
			File destinationFile = new File(outputFolder);
			if (!destinationFile.exists())
			{
				destinationFile.mkdir();
				logger.debug("Folder Created -> " + destinationFile.getAbsolutePath());
			}
			if (sourceFile.exists())
			{
				document = PDDocument.load(sourceFile);
				pages = document.getNumberOfPages();

				String fileName = sourceFile.getName().replace(".pdf", "");
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				int dpi = 500;
				for (currentPage = 0; currentPage < pages; currentPage++)
				{
					BufferedImage image = pdfRenderer.renderImageWithDPI(currentPage, dpi);
					File outputfile = new File(outputFolder + fileName + "_" + currentPage + ".png");

					// write to hdd
					ImageIO.write(image, "png", outputfile);
					fileList.add(outputfile.getAbsolutePath());

					// process for qrcode
					decoded.addAll(decode(outputfile.getAbsolutePath()));
				}
				logger.debug("Image saved at -> " + destinationFile.getAbsolutePath());
			}
			else
			{
				logger.error(sourceFile.getName() + " File does not exist");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				document.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return decoded;
	}

	private List<String> decodeQRCode(File qrCodeimage) throws IOException
	{

		BufferedImage bufferedImage = ImageIO.read(qrCodeimage);
		LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		try
		{
			// Result result = new MultiFormatReader().decode(bitmap);

			Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
			hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

			QRCodeMultiReader reader = new QRCodeMultiReader();
			Result[] results = reader.decodeMultiple(bitmap, hints);

			List<String> stringResults = new ArrayList<String>();

			for (int i = 0; i < results.length; i++)
			{
				stringResults.add(results[i].getText());
			}

			return stringResults;
		}
		catch (NotFoundException e)
		{
			logger.debug("There is no QR code in the image");
			return null;
		}
	}

	/**
	 * 
	 * @param filename
	 *            absolute filename of pdf file
	 * @param cleanup
	 * @return
	 */
	public List<String> decodePDF(String filename, boolean cleanup)
	{


		List<String> result = decodeFromPDF(filename);

		fileList.add(filename);

		if (cleanup)
		{
			cleanUp();
		}

		return result;
	}

	public int getPages()
	{
		return pages;
	}

	public int getCurrentPage()
	{
		return currentPage;
	}
	
	public int getProgress()
	{
		if (pages == 0)
		{
			return 0;
		}
		else
		{
			Integer progress = currentPage * 100 / pages;
			return progress == 0 ? 1 : progress;
		}
	}
}
