package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2022 Trusted Third Party of the University Medicine Greifswald -
 * 							kontakt-ths@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel, c.hampf
 * 							web client
 * 							a.blumentritt, m.bialke, f.m.moser
 * 							fhir-api
 * 							m.bialke
 * 							docker
 * 							r. schuldt
 * 
 * 							The gICS was developed by the University Medicine Greifswald and published
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *  
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
 * 							https://doi.org/10.1186/s12967-020-02457-y
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.emau.icmvc.ganimed.ttp.cm2.GICSService;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentParseResultDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DetectedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DetectedModuleDTO.PARSING_RESULT_CONFIDENCE;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.SessionMapKeys;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.LoggerFactory;

/**
 * parser for pdf documents to detect template key, potential signerids as well as marked consent
 * modules
 *
 * uses pdfpox for pdf to image conversion pagewise
 * uses zxing for qrcode detection and parsing
 * uses opencv for circle detection
 *
 * singleton
 *
 * @author Martin Bialke
 *
 */
public class ConsentContentParser extends AbstractGICSBean
{
	public static final String TEMPLATE_MARKER = "template";
	private ConsentParseResultDTO parseResult;

	private final String outputFolder = "";
	private final List<String> toBeDeletedFiles = new ArrayList<>();

	private int pages = 0;
	private int currentPage = 0;
	private int progress = 0;

	private final float templateQrCodeSize = 120; // px, defined in consents.css
	private final float minimalQrCodeSizeInclOffset = 111;

	// um auch leere einwilligung korrekt verarbeiten zu können, muss ein minimalschwellwert
	// vergeben werden
	// sind alle markierungen unterhalb dieses schwellwerts, ist es eine leere einwilligung.
	private final int minThreshold = 50;

	// die seitenzerlegung per pdfbox erfolgt auf basis von 200dpi
	private final int dpi = 200;
	// die extrahierung der markierungskreise enthält sehr viel rand, da entfernen wir noch ein
	// wenig von
	private final double defaultCircleExtractMargin = 0.20;

	private static ConsentContentParser parserInstance;

	private GICSService gicsHelper;

	private ConsentContentParser()
	{
		initOpenCV();
	}

	public static ConsentContentParser getInstance()
	{
		if (ConsentContentParser.parserInstance == null)
		{
			try
			{
				ConsentContentParser.parserInstance = new ConsentContentParser();
			}
			catch (ExceptionInInitializerError | NoClassDefFoundError e)
			{
				LoggerFactory.getLogger(ConsentContentParser.class).warn("OpenCV could be initialized. Consent parsing will be disabled");
			}
		}
		return ConsentContentParser.parserInstance;
	}

	private void initOpenCV()
	{
		nu.pattern.OpenCV.loadLocally();
	}

	public String getOutputFolder()
	{
		return outputFolder;
	}

	/**
	 * remove all files
	 */
	private void cleanUp()
	{

		for (String file : toBeDeletedFiles)
		{
			try
			{
				Files.delete(Paths.get(file));
			}
			catch (IOException e)
			{
				logger.error("Cannot delete PDF", e);
			}
		}
		toBeDeletedFiles.clear();
	}

	/**
	 * set value for progressbar
	 *
	 * @param value
	 * 		progressvalue
	 */
	private void updateProgess(double value)
	{
		logger.debug("process in percent: {}", value);
		progress = (int) value;
	}

	/**
	 * Hauptfunktion zur Initialisierung der PDF Verarbeitung
	 *
	 * @param pdfFileName
	 *            absolute filename of pdf file
	 * @param cleanup
	 * 			 flag to perform clean up
	 * @param service
	 * 	 		link to gics service
	 * @return result of pdf parsing
	 */
	public ConsentParseResultDTO decodePDF(String pdfFileName, boolean cleanup, GICSService service)
	{
		gicsHelper = service;

		parseResult = new ConsentParseResultDTO();
		// mark uploaded PDF file for delete
		toBeDeletedFiles.add(pdfFileName);

		// step1: extract pages with pdfbox
		List<BufferedImage> singlePageFiles = extractPages(pdfFileName);

		// step2: detect qrcode s with name to file
		HashMap<Result, BufferedImage> qrcodes = findQrCodesInFiles(singlePageFiles);

		// split templateqr code and module qr codes
		String templateQrCodeContent = "";
		HashMap<Result, BufferedImage> moduleQrCodes = new HashMap<>();

		for (Entry<Result, BufferedImage> qrcode : qrcodes.entrySet())
		{

			String qrCodeContent = qrcode.getKey().getText();
			if (qrCodeContent != null && !qrCodeContent.isEmpty())
			{
				if (qrCodeContent.startsWith(TEMPLATE_MARKER))
				{
					templateQrCodeContent = qrCodeContent;
					parseResult.setScalingError(checkForInvalidScaling(qrcode.getKey().getResultPoints()[0].getY(), qrcode.getKey().getResultPoints()[1].getY()));
				}
				else
				{
					moduleQrCodes.put(qrcode.getKey(), qrcode.getValue());
				}
			}
		}

		// step 3; process template qr code
		if (!templateQrCodeContent.isEmpty())
		{
			processTemplateQrCodeContent(templateQrCodeContent);
		}

		// step 4:
		extractAndProcessModuleStates(moduleQrCodes);

		// step 5:
		if (cleanup)
		{
			cleanUp();
		}

		return parseResult;
	}

	/**
	 * check if template qr code is to small (printed with invalid scaling)
	 *
	 * @param y1
	 *            qrcode top
	 * @param y2
	 *            qrcode bottom
	 * @return true if qr code is too smalll
	 */
	private Boolean checkForInvalidScaling(float y1, float y2)
	{
		float qrCodeHeight = y1 - y2;

		logger.debug("--- template qr code height={}, defined height={}px, percentage={}", qrCodeHeight, templateQrCodeSize, qrCodeHeight * 100 / templateQrCodeSize);
		if (qrCodeHeight < minimalQrCodeSizeInclOffset)
		{
			logger.info("Template QR code size too small: true");
			return true;
		}
		else
		{
			logger.info("Template QR code size too small: false");
			return false;
		}
	}

	/**
	 * detect, extract and retrieve modulestates from the partial modul images
	 *
	 * @param moduleQrCodes
	 *            mapping list of pagefile and contained modulename
	 */
	private void extractAndProcessModuleStates(HashMap<Result, BufferedImage> moduleQrCodes)
	{
		ConsentTemplateDTO detectedTemplateDTO = getTemplateDTO(parseResult.getDetectedTemplateKey());

		if (detectedTemplateDTO != null)
		{
			Set<AssignedModuleDTO> modulesFromTemplate = detectedTemplateDTO.getAssignedModules();
			List<DetectedModuleDTO> detectedModules = new ArrayList<>();
			List<AssignedModuleDTO> missingModules = new ArrayList<>(modulesFromTemplate);

			int loadIndex = 0;
			// process moduleqrcodes
			for (Entry<Result, BufferedImage> moduleQrCode : moduleQrCodes.entrySet())
			{
				loadIndex++;
				BufferedImage image = moduleQrCode.getValue();
				String moduleName = moduleQrCode.getKey().getText();
				if (moduleName != null && !moduleName.isEmpty() && image != null)
				{
					// process
					// save module checkbox snippets as separate buffered images/mats and proof
					// "checked state"
					// , n results of type boolean expected
					List<Boolean> checkedStateList = storeAndInterpretPartialImage(image, 0,
							(int) moduleQrCode.getKey().getResultPoints()[0].getY(),
							image.getWidth(),
							(int) moduleQrCode.getKey().getResultPoints()[1].getY());

					// count marked
					int marked = 0;

					for (boolean answer : checkedStateList)
					{
						if (Boolean.TRUE.equals(answer))
						{
							marked++;
						}
					}

					ModuleKeyDTO detectedModuleKey = getKeyFromName(moduleName, detectedTemplateDTO);

					for (AssignedModuleDTO templateModule : modulesFromTemplate)
					{
						if (templateModule.getModule().getKey().equals(detectedModuleKey))
						{
							missingModules.remove(templateModule);

							List<ConsentStatus> states = templateModule.getDisplayCheckboxes();

							PARSING_RESULT_CONFIDENCE confidence = PARSING_RESULT_CONFIDENCE.OK;

							if (marked > 1)
							{
								confidence = PARSING_RESULT_CONFIDENCE.REVISE_TOO_MANY;
							}
							if (marked < 1)
							{
								confidence = PARSING_RESULT_CONFIDENCE.REVISE_EMPTY;
							}

							// generate list of marked consent states
							List<ConsentStatus> markedConsentStates = new ArrayList<>();

							for (int i = 0; i < checkedStateList.size(); i++)
							{

								if (Boolean.TRUE.equals(checkedStateList.get(i)))
								{
									markedConsentStates.add(states.get(i));
								}
							}

							detectedModules.add(new DetectedModuleDTO(detectedModuleKey, markedConsentStates, confidence));
						}
						else
						{
							if (templateModule.getDisplayCheckboxes().isEmpty())
							{
								// dont forget to filter modules without display checkboxes from
								// list (intro
								// and textonly-modules)
								missingModules.remove(templateModule);
							}
						}
					}




				}
				updateProgess(70 + 100 * ((float) loadIndex / (float) moduleQrCodes.entrySet().size() * 0.30));
			}
			// save parseResult
			parseResult.setDetectedModuleStates(detectedModules);
			parseResult.setMissingModules(missingModules);
		}
	}

	/**
	 * convert mat to buffered image
	 *
	 * @param matrix
	 *            mat to be converted
	 * @return converted buffered image
	 * @throws IOException in case of error
	 */
	private BufferedImage mat2BufferedImage(Mat matrix) throws IOException
	{
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".png", matrix, mob);

		return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}

	/**
	 * convert buffered image to mat
	 *
	 * @param bi
	 *            image as buffered image
	 * @return opencv mat
	 */
	private Mat bufferedImageToMat(BufferedImage bi)
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(bi, "png", byteArrayOutputStream);
			byteArrayOutputStream.flush();
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}
		return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
	}

	/**
	 * extract single pages from pdf (specified by filename) with given dpi
	 *
	 * @param filename
	 *            pdf file
	 * @return list of imagefilenames for each page
	 */
	private List<BufferedImage> extractPages(String filename)
	{
		PDDocument document = null;
		List<BufferedImage> extractedPages = new ArrayList<>();

		try
		{
			File sourceFile = new File(filename);
			File destinationFile = new File(outputFolder);
			if (!destinationFile.exists())
			{
				destinationFile.mkdir();
			}
			if (sourceFile.exists())
			{
				document = PDDocument.load(sourceFile);
				pages = document.getNumberOfPages();
				PDFRenderer pdfRenderer = new PDFRenderer(document);

				for (currentPage = 0; currentPage < pages; currentPage++)
				{
					BufferedImage image = pdfRenderer.renderImageWithDPI(currentPage, dpi);
					// optimize image and add it to list of extractedimages
					extractedPages.add(image);
					logger.debug("page {} of  {} pages extracted.", currentPage + 1, pages);
					updateProgess(((float) currentPage + 1) / pages * 0.40 * 100);
				}
			}
			else
			{
				logger.error(sourceFile.getName() + " File does not exist");
			}
		}
		catch (Exception e)
		{
			logger.error("Error reading PDF", e);
		}
		finally
		{
			try
			{
				if (document != null)
				{
					document.close();
				}
			}
			catch (IOException e)
			{
				logger.error("Cannot close PDF", e);
			}
		}

		return extractedPages;
	}

	/**
	 * perform image optimization for better parsing results (bluring, more brightness, more
	 * contrast)
	 *
	 * @param image
	 *            mat to be optimized
	 */
	private Mat optimizePageForQrDetection(Mat image)
	{
		if (!image.empty())
		{
			Mat dst = image.clone();
			Mat output = new Mat(image.rows(), image.cols(), image.type());

			// sharpen image with gaussian blur and weight
			Imgproc.GaussianBlur(image, dst, new Size(0, 0), 10);
			Core.addWeighted(image, 1.5, dst, -0.5, 0, output);

			// RETURN OPTIMIZED IMAGE
			logger.debug("page optimized");
			return output;
		}

		// worst case: return original image
		return image;
	}

	/**
	 * helper function to write mat to png file specified by filename
	 *
	 * @param data
	 *            image data
	 * @param filename
	 *            to be used image file name
	 */
	void writeMatToPngFile(Mat data, String filename)
	{
		File outputfile = new File(filename + ".png");
		BufferedImage image;
		try
		{
			image = mat2BufferedImage(data);
			ImageIO.write(image, "png", outputfile);
		}
		catch (IOException e1)
		{
			logger.error("Unable to write Mat to File. {}", e1.getMessage());
		}
	}

	/**
	 * process qr code content and split contained string to templatekey and additional data
	 *
	 * @param qrCodeContent
	 * 		detected content to be parsed
	 */
	private void processTemplateQrCodeContent(String qrCodeContent)
	{
		/*
		 * grundsaetzlicher Aufbau des Codes, separator #
		 *
		 *  template=TEMPLATEKEY
		 * # separator
		 *  patientDate=PATIENTSIGNINGDATE
		 * # separator
		 *  patientPlace=PATIENTSIGNINGPLACE
		 * # separator
		 *  physicianDate=PHYSICIANSIGNINGDATE
		 * # separator
		 * 	physicianPlace=PHYSICIANSIGNINGPLACE
		 * # separator
		 * gefolgt von 0-n Wiederholungen von
		 * # separator
		 * 	SIGNERIDTYPE=SIGNERIDVALUE
		 */

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		List<SignerIdDTO> toBeUsedSids = new ArrayList<>();
		Map<String, String> parsedQrContent = parseQrContentToMap(qrCodeContent);

		// always contained: template key, template=domain;name;version
		parseResult.setDetectedTemplateKey(convertToTemplateKey(parsedQrContent.get(TEMPLATE_MARKER).split(";")));
		parsedQrContent.remove(TEMPLATE_MARKER);

		// additional data contained?
		if (qrCodeContent.length() > 0)
		{
			try
			{

				if (parsedQrContent.containsKey(SessionMapKeys.PRINT_SIGNER_DATE))
				{
					parseResult.setDetectedPatientSigningDate(df.parse(parsedQrContent.get(SessionMapKeys.PRINT_SIGNER_DATE)));
					parsedQrContent.remove(SessionMapKeys.PRINT_SIGNER_DATE);
				}
				if (parsedQrContent.containsKey(SessionMapKeys.PRINT_PHYSICIAN_DATE))
				{
					parseResult.setDetectedPhysicianSigningDate(df.parse(parsedQrContent.get(SessionMapKeys.PRINT_PHYSICIAN_DATE)));
					parsedQrContent.remove(SessionMapKeys.PRINT_PHYSICIAN_DATE);
				}
			}
			catch (ParseException e)
			{
				logger.debug("Unable to parse date from template qr code");
			}

			if (parsedQrContent.containsKey(SessionMapKeys.PRINT_SIGNER_PLACE))
			{
				parseResult.setDetectedPatientSigningPlace(parsedQrContent.get(SessionMapKeys.PRINT_SIGNER_PLACE));
				parsedQrContent.remove(SessionMapKeys.PRINT_SIGNER_PLACE);
			}

			if (parsedQrContent.containsKey(SessionMapKeys.PRINT_PHYSICIAN_PLACE))
			{
				parseResult.setDetectedPhysicianSigningPlace(parsedQrContent.get(SessionMapKeys.PRINT_PHYSICIAN_PLACE));
				parsedQrContent.remove(SessionMapKeys.PRINT_PHYSICIAN_PLACE);
			}

			// rest: signerids and values, type=value
			for (String signerIdType : parsedQrContent.keySet())
			{
				toBeUsedSids.add(new SignerIdDTO(signerIdType, parsedQrContent.get(signerIdType)));
			}

			parseResult.setDetectedSignerIds(toBeUsedSids);
		}
	}

	private ConsentTemplateKeyDTO convertToTemplateKey(String[] templateKeyElements)
	{
		if (templateKeyElements.length != 3)
		{
			throw new IllegalArgumentException("Qr code contains unexpected format of template key");
		}

		return new ConsentTemplateKeyDTO(templateKeyElements[0], templateKeyElements[1], templateKeyElements[2]);
	}

	private Map<String, String> parseQrContentToMap(String qrCodeContent)
	{
		String[] splitContent = qrCodeContent.split("#");

		//convert splitstring to simplified map
		Map<String, String> parsedQrContent = new HashMap<>();

		for (String keyValueString : splitContent)
		{
			if (keyValueString != null && !keyValueString.isEmpty() && keyValueString.contains("="))
			{
				String[] tmp = keyValueString.split("=");
				if (tmp.length == 2)
				{
					parsedQrContent.put(tmp[0], tmp[1]);
				}
				else
				{
					logger.debug("Processing template qr code detected invalid element count in keyvaluepair. dropping partial content element.");
				}
			}
		}
		return parsedQrContent;
	}

	/**
	 * optimize contrast and brightness, try to detect qr codes
	 *
	 * @param imageMat imageMat to be optimized
	 * @param contrast
	 *            in percent (+/-)
	 * @param brightness
	 *            in percent (+/-)
	 * @return detected codes
	 */
	private Result[] detectCodes(Mat imageMat, int contrast, int brightness)
	{
		Mat optimized = optimizeContrastBrightness(imageMat, contrast, brightness);
		BufferedImage bufferedImage;

		try
		{
			bufferedImage = mat2BufferedImage(optimized);
			LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			HashMap<DecodeHintType, Object> hints = new HashMap<>();
			hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

			QRCodeMultiReader reader = new QRCodeMultiReader();
			return reader.decodeMultiple(bitmap, hints);
		}
		catch (NotFoundException | IOException e)
		{
			return null;
		}
	}

	/**
	 * extract module specific partial image from pdf page using modulqr code as reference point
	 *
	 * @param bitmap
	 *            bitmapversion of image
	 * @param x1
	 *            x upperleft corner of qr code
	 * @param y1
	 *            y upperleft corner of qrcode
	 * @param x2
	 *            image width
	 * @param y2
	 *            qr code height
	 * @return list of the patients consent markers from left side of the image to right side of the
	 *         image for the specific modul ; true=marked, false=not marked
	 */
	private List<Boolean> storeAndInterpretPartialImage(BufferedImage bitmap, int x1, int y1, int x2, int y2)
	{
		int padding = 50;
		Mat partial = bufferedImageToMat(bitmap.getSubimage(x1, y2 - padding, x2 - x1, y1 - y2 + 2 * padding));
		return interpretMarkersFromPartialImage(partial);
	}

	/**
	 * inspect partial image and interpret contained circles as marked or not marked
	 *
	 * @param partialImage
	 *            the image containing lots of horisontal cirles
	 * @return list of interpreted markers based on threshold with regards to working resolution
	 */
	private List<Boolean> interpretMarkersFromPartialImage(Mat partialImage)
	{
		Mat optimisedImage = optimizeContrastBrightness(partialImage, 75, -100);
		HashMap<Mat, Integer> identifiedCircles = extractSingleCircles(optimisedImage);
		HashMap<Mat, Integer> sortedList = sortByValue(identifiedCircles);
		List<Double> detectedRatios = new ArrayList<>();
		List<Boolean> result = new ArrayList<>();
		for (Entry<Mat, Integer> circleEntry : sortedList.entrySet())
		{
			detectedRatios.add(calcBlackWhiteRatio(circleEntry.getKey()));
		}

		// only one marker per module detected?
		if (detectedRatios.size() == 1)
		{
			// might be a withdrawal, nothing for comparison available, except minTreshold
			if (detectedRatios.get(0) > minThreshold)
			{
				result.add(true);
			}
			else
			{
				result.add(false);
			}
			return result;
		}

		// check if all ratios are below a certain Threshold
		boolean atLeastOneMarked = false;
		for (Double ratio : detectedRatios)
		{
			if (ratio >= minThreshold)
			{
				atLeastOneMarked = true;
				break;
			}
		}

		if (!atLeastOneMarked)
		{
			for (@SuppressWarnings("unused")
			Double ratio : detectedRatios)
			{
				// seems to be empty consent. set all markers for this module to false
				result.add(false);
			}
		}
		else
		{
			// which is the darkest?
			Double darkest = 0.0;

			for (Double ratio : detectedRatios)
			{
				if (ratio >= darkest)
				{
					darkest = ratio;
				}
			}

			// one last run, create result set
			for (Double ratio : detectedRatios)
			{
				if (ratio.equals(darkest))
				{
					result.add(true);
				}
				else
				{
					result.add(false);
				}
			}
		}

		return result;
	}

	/**
	 * sort generic hashmap based on value containing integer
	 *
	 * @param hm
	 *            to be sorted list
	 * @return sorted list
	 */
	private <T> HashMap<T, Integer> sortByValue(HashMap<T, Integer> hm)
	{
		// Create a list from elements of HashMap
		List<Map.Entry<T, Integer>> list = new LinkedList<>(hm.entrySet());
		list.sort(Entry.comparingByValue());

		// put data from sorted list to hashmap
		HashMap<T, Integer> temp = new LinkedHashMap<>();
		for (Entry<T, Integer> aa : list)
		{
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}

	/**
	 * find all qr codes in every given page
	 *
	 * @param singlePages
	 *            list of bufferedpages to search for qrcodes
	 * @return searchresults per page
	 */
	private HashMap<Result, BufferedImage> findQrCodesInFiles(List<BufferedImage> singlePages)
	{
		HashMap<Result, BufferedImage> detectedQrCodes = new HashMap<>();

		Result[] qrcodes;

		int step = 10;
		int minBright = -50;
		int maxBright = 30;

		int minContrast = -30;
		int maxContrast = 50;
		int pageCount = 1;

		for (BufferedImage p : singlePages)
		{
			int maxNrOfCodes = 0;
			int markerBrightness = 0;
			int markerContrast = 0;
			Result[] maxQrcodes = null;

			Mat pageMat = bufferedImageToMat(p);
			Mat blurredMat = optimizePageForQrDetection(pageMat);

			for (int b = maxBright; b >= minBright; b -= step)
			{
				for (int c = minContrast; c <= maxContrast; c += step)
				{
					qrcodes = detectCodes(blurredMat, c, b);
					if (qrcodes != null && qrcodes.length > maxNrOfCodes)
					{
						maxNrOfCodes = qrcodes.length;
						maxQrcodes = qrcodes.clone();
						System.arraycopy(qrcodes, 0, maxQrcodes, 0, qrcodes.length);
						markerContrast = c;
						markerBrightness = b;
					}
				}
			}

			if (maxNrOfCodes > 0)
			{
				logger.debug("best results: contrast={}, brightness={}", markerContrast, markerBrightness);
				for (Result r : maxQrcodes)
				{
					detectedQrCodes.put(r, p);
				}
			}

			updateProgess(40 + 100 * ((float) pageCount / (float) singlePages.size() * 0.25));
		}

		return detectedQrCodes;
	}

	/**
	 * extract SingleCircles from File
	 *
	 * @param src source mat
	 * @return List of CircleMats
	 */
	private HashMap<Mat, Integer> extractSingleCircles(Mat src)
	{
		HashMap<Mat, Integer> extractedCircles = new HashMap<>();

		// Check if src is loaded fine
		if (!src.empty())
		{
			Mat circles = new Mat();
			Imgproc.HoughCircles(src, circles, Imgproc.HOUGH_GRADIENT, 1.0,
					(double) src.rows() / 16, // change this value to detect circles with different
												// distances to each other
					100.0, 30.0,
					dpi / 16, // min_radius relates to workingdpi
					dpi / 8); // max_radius relates to workingdpi

			for (int x = 0; x < circles.cols(); x++)
			{
				double[] c = circles.get(0, x);
				Point center = new Point(Math.round(c[0]), Math.round(c[1]));
				int radius = (int) Math.round(c[2]);

				try
				{
					// cut out circles as rectangles
					double rectX = center.x - radius;
					double rectY = center.y - radius;

					int margin = (int) (radius * defaultCircleExtractMargin);

					Mat singleCircle = src.submat(
							new Rect(
									new Point(rectX - margin, rectY - margin),
									new Point(rectX + margin + 2 * radius, rectY + margin + 2 * radius)));
					extractedCircles.put(singleCircle, (int) rectX);
				}
				catch (CvException e)
				{
					logger.debug("An error occured during circle extraction. " + e.getMessage());
				}
			}
		}

		return extractedCircles;
	}

	/**
	 * calculate ration of black and white pixels
	 *
	 * @param extractedCircleMat to be processed mat
	 *
	 * @return marked status
	 */
	private double calcBlackWhiteRatio(Mat extractedCircleMat)
	{
		// pixel color counter
		int white = 0;
		int black = 0;

		for (int matx = 0; matx < extractedCircleMat.cols(); matx++)
		{
			for (int maty = 0; maty < extractedCircleMat.rows(); maty++)
			{
				double pixelColor = extractedCircleMat.get(matx, maty)[0];
				if (pixelColor > 128)
				{
					white++;
				}
				else
				{
					black++;
				}
			}
		}

		// calc ratio of black and white
		return (float) black / (float) white * 100;
	}

	private Mat optimizeContrastBrightness(Mat toBeOptimizedMat, int contrastPercent, int brightnessPercent)
	{
		return optimizeContrastBrightness(toBeOptimizedMat, contrastPercent, brightnessPercent, false, "");
	}

	/**
	 * optimize mat
	 * @param toBeOptimizedMat mat
	 * @param contrastPercent contrast value
	 * @param brightnessPercent brightness value
	 * @param writeSampleFilesToFolder flag write samples to folder
	 * @param folder output folder
	 * @return optimized mat
	 */
	private Mat optimizeContrastBrightness(Mat toBeOptimizedMat, int contrastPercent, int brightnessPercent, boolean writeSampleFilesToFolder, String folder)
	{
		int rnd = (int) (Math.random() * 1000);

		if (writeSampleFilesToFolder)
		{
			writeMatToPngFile(toBeOptimizedMat, folder + rnd + "_basis.png");
		}

		Mat dst = toBeOptimizedMat.clone();
		Mat resultMat = new Mat(dst.rows(), dst.cols(), dst.type());
		Mat gray = new Mat(dst.rows(), dst.cols(), dst.type());
		Mat brighter = new Mat(dst.rows(), dst.cols(), dst.type());

		Imgproc.cvtColor(dst, gray, Imgproc.COLOR_BGR2GRAY);

		// value between 0..2. no change =1, e.g. +75percent = 1.75
		double contrast;
		if (contrastPercent > 0)
		{
			contrast = 1 + (double) contrastPercent / 100;
		}
		else
		{
			contrast = 1 - (double) contrastPercent / 100;
		}
		// no change = 0. brighter = positive value, darker = negative value e.g.decrease to -100% =

		gray.convertTo(brighter, -1, 1, brightnessPercent);
		if (writeSampleFilesToFolder)
		{
			writeMatToPngFile(brighter, folder + rnd + "_bright.png");
		}

		brighter.convertTo(resultMat, -1, contrast, 0);
		if (writeSampleFilesToFolder)
		{
			writeMatToPngFile(resultMat, folder + rnd + "_contrast.png");
		}
		return resultMat;
	}

	private ConsentTemplateDTO getTemplateDTO(ConsentTemplateKeyDTO referenceTemplate)
	{
		ConsentTemplateDTO template = null;
		if (referenceTemplate != null)
		{
			try
			{
				template = gicsHelper.getConsentTemplate(referenceTemplate);
			}
			catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException e)
			{
				logger.error("Detected unknown Template Key in QR Code. " + e.getMessage());
			}
		}
		else
		{
			throw new IllegalArgumentException("Templatekey is necessary to find exact modulekey.");
		}
		return template;
	}

	private ModuleKeyDTO getKeyFromName(String moduleName, ConsentTemplateDTO detectedTemplateDTO)
	{
		if (detectedTemplateDTO != null
				&& detectedTemplateDTO.getAssignedModules() != null
				&& !detectedTemplateDTO.getAssignedModules().isEmpty())
		{
			for (AssignedModuleDTO m : detectedTemplateDTO.getAssignedModules())
			{
				if (m.getModule().getKey().getName().equals(moduleName))
				{
					return m.getModule().getKey();
				}
			}
		}
		return null;
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
		if (progress == 0)
		{
			return 0;
		}
		else
		{
			return progress;
		}
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}
}
