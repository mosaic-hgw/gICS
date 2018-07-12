package org.emau.icmvc.ttp.gics.emergency;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class DatabaseDecode {

	private static final Logger logger = Logger.getLogger(DatabaseDecode.class);
	private final Properties properties = new Properties();
	private static final DateFormat dfmt = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
	private static final DateFormat dfmt2 = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	public DatabaseDecode(String fileName) throws MissingPropertyException, IOException {
		logger.info("Properties-Datei (" + fileName + ") wird eingelesen");
		BufferedInputStream stream = new BufferedInputStream(new FileInputStream(fileName));
		try {
			properties.load(stream);

			for (GICSEProperties prop : GICSEProperties.values()) {
				if (properties.getProperty(prop.getName()) == null || properties.getProperty(prop.getName()).isEmpty()) {
					throw new MissingPropertyException("Wert zur Property: " + prop.getName() + " nicht gesetzt");
				}
			}
			logger.info("Properties-Datei wurde erfolgreich eingelesen");
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	/*
	 * *** read and split IDs from 'text' table, decode base64 format and save as PDF ***
	 */

	public void base64ToPDF() throws ClassNotFoundException, SQLException {
		// Verbindung zur JDBC-Datenbank herstellen.
		Connection con = getConnection();

		if (con != null) {

			try {
				logger.info("SQL Anfrage wird aufgebaut.");

				PreparedStatement pstmt = con.prepareStatement("SELECT id, text FROM text WHERE id LIKE ?");
				pstmt.setString(1, "%" + properties.getProperty(GICSEProperties.Study.getName()) + "%CONSENTSCAN");
				ResultSet result = pstmt.executeQuery();

				logger.info("SQL Anfrage wurde erfolgreich ausgeführt");
				logger.info("Die folgenden IDs wurden gefunden und ggf. das zugehörige PDF gespeichert: ");

				int nrEntries = 0;
				int nrNoScan = 0;
				int nrProcessed = 0;
				int nrError = 0;

				while (result.next()) {
					nrEntries++;
					/* **generate IDs** */
					String id = result.getString("id");
					String[] idParts = id.split("_###_");

					if (idParts.length >= 5) {

						try {
							String outputId = getFileName(idParts);
							logger.info("Folgender Eintrag wird bearbeitet: " + outputId);
							/* **read and decode Base64 Strings** */
							String text = result.getString("text");

							if (text != null && !text.isEmpty()) {
								String fileName = properties.getProperty(GICSEProperties.OutputPath.getName()) + outputId + ".pdf";
								try {
									export(text, fileName);
									logger.info("Scan als PDF gespeichert");
									nrProcessed++;
								} catch (DecodingException e) {
									logger.error("Dekodierung konnte nicht durchgeführt werden", e);
									nrError++;
								} catch (IOException e) {
									logger.error("Datei " + fileName + " konnte nicht geschrieben werden", e);
								}
							} else {
								logger.warn("Es ist kein Scan zu folgender ID vorhanden: " + id);
								nrNoScan++;
							}
						} catch (ParseException e) {
							logger.error("unbekanntes Datumsformat", e);
							nrError++;
						}
					} else {
						logger.warn("invalid Id: " + id);
						nrError++;
					}
				}
				logger.info(nrEntries + " Einträge wurden in der Datenbank gefunden");
				logger.info(nrProcessed + " Einträge wurden verarbeitet und als PDF gespeichert");
				logger.info(nrNoScan + " Einträge enthielten keine Scans");
				logger.info(nrError + " Einträge konnten aufgrund von fehlerhaften Daten nicht verarbeitet werden");

			} catch (SQLException e) {
				logger.error("Verbindung zur Datenbank nicht möglich", e);
			}
			con.close();
		}
	}

	private String getFileName(String[] input_id) throws ParseException {
		Date date = dfmt.parse(input_id[3]);
		StringBuffer sb = new StringBuffer(dfmt2.format(date));
		sb.append("_");
		sb.append(input_id[1]);
		sb.append("_version_");
		sb.append(input_id[4]);
		sb.append("_signer-id_");
		sb.append(input_id[2]);
		String output_id = sb.toString();
		output_id = output_id.replace(" ", "_");
		output_id = output_id.replace(":", "-");
		return output_id;
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException {
		Connection result = null;
		logger.info("Verbindung zur Datenbank wird hergestellt");
		// Datenbanktreiber für JDBC Schnittstellen laden.
		Class.forName("com.mysql.jdbc.Driver");
		StringBuffer sb = new StringBuffer("jdbc:mysql://");
		sb.append(properties.getProperty(GICSEProperties.Host.getName()));
		sb.append(":");
		sb.append(properties.getProperty(GICSEProperties.Port.getName()));
		sb.append("/");
		sb.append(properties.getProperty(GICSEProperties.Name.getName()));
		sb.append("?user=");
		sb.append(properties.getProperty(GICSEProperties.User.getName()));
		sb.append("&password=");
		sb.append(properties.getProperty(GICSEProperties.Pass.getName()));

		result = DriverManager.getConnection(sb.toString());
		logger.info("Verbindung zur Datenbank erfolgreich aufgebaut");
		return result;
	}

	private static void export(String sourceFile, String targetFile) throws DecodingException, IOException {

		byte[] decodedBytes = Base64.decodeBase64(sourceFile);
		logger.info("Die Dekodierung wurde erfolgreich durchgeführt");
		writeByteArraysToFile(targetFile, decodedBytes);
	}

	private static void writeByteArraysToFile(String fileName, byte[] content) throws IOException {

		File file = new File(fileName);
		BufferedOutputStream writer = null;
		try {
			writer = new BufferedOutputStream(new FileOutputStream(file));
			writer.write(content);
		} catch (FileNotFoundException e) {
			logger.error("Konnte Datei " + fileName + " nicht erstellen", e);
			throw new IOException(e);
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException ignore) {
				}
			}
		}
	}
}
